import json
import threading
import time
from concurrent.futures import ThreadPoolExecutor, as_completed
from dataclasses import dataclass
from datetime import datetime
from queue import Queue
from typing import Any, Dict, List, Optional, Set, Tuple

import pandas as pd
import requests


@dataclass
class UpdateStats:
    """Estatísticas de atualização"""

    updated_count: int = 0
    error_count: int = 0
    not_found_count: int = 0
    invalid_data_count: int = 0
    duplicates_removed: int = 0
    total_processed: int = 0
    start_time: float = 0
    lock: threading.Lock = None
    removed_movies: List[Dict] = None

    def __post_init__(self):
        if self.lock is None:
            self.lock = threading.Lock()
        if self.removed_movies is None:
            self.removed_movies = []

    def increment_updated(self):
        with self.lock:
            self.updated_count += 1
            self.total_processed += 1

    def increment_error(self):
        with self.lock:
            self.error_count += 1
            self.total_processed += 1

    def increment_not_found(self):
        with self.lock:
            self.not_found_count += 1
            self.total_processed += 1

    def increment_invalid_data(self):
        with self.lock:
            self.invalid_data_count += 1
            self.total_processed += 1

    def add_removed_movie(self, movie_data: Dict, reason: str):
        with self.lock:
            self.removed_movies.append(
                {
                    "id": movie_data.get("id", "N/A"),
                    "title": movie_data.get("title", "N/A"),
                    "reason": reason,
                }
            )

    def get_speed(self) -> float:
        """Retorna velocidade em filmes/segundo"""
        elapsed = time.time() - self.start_time
        if elapsed == 0:
            return 0
        return self.total_processed / elapsed

    def get_eta(self, total_movies: int) -> str:
        """Estima tempo restante"""
        speed = self.get_speed()
        if speed == 0:
            return "N/A"

        remaining = total_movies - self.total_processed
        eta_seconds = remaining / speed

        if eta_seconds < 60:
            return f"{eta_seconds:.0f}s"
        elif eta_seconds < 3600:
            return f"{eta_seconds/60:.1f}min"
        else:
            return f"{eta_seconds/3600:.1f}h"


class TMDBCSVUpdaterThreaded:
    def __init__(self, api_key: str, max_workers: int = 10):
        self.api_key = api_key
        self.base_url = "https://api.themoviedb.org/3"
        self.max_workers = max_workers
        self.stats = UpdateStats()
        self.progress_queue = Queue()
        self.valid_movies_lock = threading.Lock()
        self.valid_movies: Dict[int, Dict] = {}

        # Configurar sessão com pool de conexões
        self.session = requests.Session()
        adapter = requests.adapters.HTTPAdapter(
            pool_connections=max_workers, pool_maxsize=max_workers * 2, max_retries=3
        )
        self.session.mount("http://", adapter)
        self.session.mount("https://", adapter)

    def validate_movie_data(
        self, movie_data: Dict, original_data: Dict
    ) -> Tuple[bool, Dict, str]:
        """
        Valida se os dados do filme são válidos para uso no site
        Retorna: (is_valid, validated_data, error_reason)
        """
        if not movie_data:
            return False, {}, "API returned no data"

        # Campos obrigatórios que devem existir e não estar vazios
        required_fields = {
            "id": "ID do filme",
            "title": "Título",
            "overview": "Sinopse",
            "vote_average": "Rating",
            "release_date": "Data de lançamento",
            "original_language": "Idioma original",
            "popularity": "Popularidade",
        }

        validated_data = {}

        # Validar campos obrigatórios
        for api_field, field_name in required_fields.items():
            value = movie_data.get(api_field)

            if value is None or value == "":
                return False, {}, f"Campo obrigatório vazio: {field_name}"

            # Validações específicas
            if api_field == "id":
                try:
                    validated_data["id"] = int(value)
                except (ValueError, TypeError):
                    return False, {}, "ID inválido"

            elif api_field == "vote_average":
                try:
                    rating = float(value)
                    if rating < 0 or rating > 10:
                        return False, {}, "Rating fora do intervalo válido (0-10)"
                    validated_data["rating"] = rating
                except (ValueError, TypeError):
                    return False, {}, "Rating inválido"

            elif api_field == "popularity":
                try:
                    pop = float(value)
                    if pop < 0:
                        return False, {}, "Popularidade inválida"
                    validated_data["popularity"] = pop
                except (ValueError, TypeError):
                    return False, {}, "Popularidade inválida"

            elif api_field == "release_date":
                # Verificar se a data está em formato válido
                if not value or len(value) < 4:
                    return False, {}, "Data de lançamento inválida"
                validated_data["release_date"] = value

            elif api_field == "original_language":
                if len(str(value)) < 2:
                    return False, {}, "Idioma inválido"
                validated_data["original_language"] = value

            else:
                validated_data[
                    api_field if api_field != "vote_average" else "rating"
                ] = value

        # Campos opcionais (manter do original se não disponível na API)
        optional_fields = {
            "poster_path": original_data.get("poster_path", ""),
            "backdrop_path": original_data.get("backdrop_path", ""),
        }

        for field, default in optional_fields.items():
            api_value = movie_data.get(field)
            validated_data[field] = api_value if api_value else default

        # Validação adicional: título deve ter pelo menos 1 caractere
        if len(str(validated_data.get("title", "")).strip()) == 0:
            return False, {}, "Título vazio"

        # Validação adicional: overview deve ter pelo menos 10 caracteres
        if len(str(validated_data.get("overview", "")).strip()) < 10:
            return False, {}, "Sinopse muito curta ou vazia"

        return True, validated_data, ""

    def get_movie_details(
        self, movie_id: int, title: str = ""
    ) -> Optional[Dict[str, Any]]:
        """Busca detalhes de um filme na API do TMDB - thread-safe"""
        url = f"{self.base_url}/movie/{movie_id}"
        params = {"api_key": self.api_key, "language": "en-US"}

        try:
            response = self.session.get(url, params=params, timeout=15)

            if response.status_code == 200:
                return response.json()
            elif response.status_code == 404:
                self.progress_queue.put(
                    f"❌ {title} (ID:{movie_id}) não encontrado na TMDB"
                )
                return None
            elif response.status_code == 429:  # Rate limit
                time.sleep(1)  # Espera 1 segundo e tenta novamente
                return self.get_movie_details(movie_id, title)
            else:
                self.progress_queue.put(f"⚠️ Erro {response.status_code} para {title}")
                return None

        except requests.exceptions.RequestException as e:
            self.progress_queue.put(f"🌐 Erro de rede para {title}: {str(e)[:50]}...")
            return None

    def process_movie_batch(
        self, movie_batch: List[Tuple[int, Dict]]
    ) -> List[Tuple[int, Dict, bool]]:
        """
        Processa um lote de filmes
        Retorna: List[Tuple[int, Dict, bool]] onde bool indica se o filme é válido
        """
        results = []

        for idx, row_data in movie_batch:
            movie_id = int(row_data["id"])
            title = row_data["title"]

            # Buscar dados atualizados na API
            movie_data = self.get_movie_details(movie_id, title)

            if movie_data is None:
                # Filme não encontrado na TMDB - será removido
                self.stats.increment_not_found()
                self.stats.add_removed_movie(row_data, "Não encontrado na TMDB")
                results.append((idx, row_data, False))  # Marcar como inválido
                continue

            # Validar dados do filme
            is_valid, validated_data, error_reason = self.validate_movie_data(
                movie_data, row_data
            )

            if not is_valid:
                # Dados inválidos - será removido
                self.stats.increment_invalid_data()
                self.stats.add_removed_movie(
                    row_data, f"Dados inválidos: {error_reason}"
                )
                results.append((idx, row_data, False))  # Marcar como inválido
                continue

            # Verificar duplicatas (thread-safe)
            with self.valid_movies_lock:
                if movie_id in self.valid_movies:
                    # Duplicata encontrada - manter apenas a primeira ocorrência
                    self.stats.duplicates_removed += 1
                    self.stats.add_removed_movie(row_data, "Duplicata removida")
                    results.append((idx, row_data, False))  # Marcar como inválido
                    continue
                else:
                    # Adicionar à lista de filmes válidos
                    self.valid_movies[movie_id] = validated_data

            # Filme válido e único
            self.stats.increment_updated()
            results.append((idx, validated_data, True))  # Marcar como válido

        return results

    def progress_monitor(self, total_movies: int, stop_event: threading.Event):
        """Monitor de progresso em thread separada"""
        last_update = 0

        while not stop_event.is_set():
            # Processar mensagens da queue
            while not self.progress_queue.empty():
                try:
                    message = self.progress_queue.get_nowait()
                    print(f"   {message}")
                except:
                    break

            # Atualizar progresso a cada 5 segundos ou a cada 50 filmes
            current_processed = self.stats.total_processed
            if (time.time() - last_update > 5) or (
                current_processed - last_update > 50
            ):
                if current_processed > 0:
                    progress_percent = (current_processed / total_movies) * 100
                    speed = self.stats.get_speed()
                    eta = self.stats.get_eta(total_movies)

                    print(f"\n🚀 PROGRESSO TEMPO REAL:")
                    print(
                        f"   📊 {current_processed}/{total_movies} ({progress_percent:.1f}%)"
                    )
                    print(f"   ⚡ Velocidade: {speed:.1f} filmes/segundo")
                    print(f"   ⏰ ETA: {eta}")
                    print(f"   ✅ Sucesso: {self.stats.updated_count}")
                    print(f"   ❌ Erros: {self.stats.error_count}")
                    print(f"   🔍 Não encontrados: {self.stats.not_found_count}")
                    print("-" * 50)

                last_update = time.time()

            time.sleep(1)

    def update_csv_file(
        self,
        input_file: str,
        output_file: str = None,
        batch_size: int = 50,
        limit: int = None,
    ):
        """Atualiza CSV com processamento paralelo e remoção de filmes inválidos"""
        if output_file is None:
            output_file = input_file.replace(".csv", "_atualizado_threaded.csv")

        print("🚀 ATUALIZADOR CSV MULTITHREADED - TURBO MODE!")
        print("=" * 60)
        print(f"⚡ Threads simultâneas: {self.max_workers}")
        print(f"📦 Tamanho do lote: {batch_size}")
        print("🧹 Modo limpeza: Remove filmes inválidos e duplicatas")

        # Carregar CSV
        print(f"📊 Carregando arquivo: {input_file}")
        try:
            df = pd.read_csv(input_file)
            print(f"✅ Carregado: {len(df)} filmes")
        except Exception as e:
            print(f"❌ Erro ao carregar arquivo: {e}")
            return

        # Verificar colunas necessárias
        required_columns = [
            "id",
            "title",
            "overview",
            "rating",
            "release_date",
            "original_language",
            "popularity",
            "poster_path",
            "backdrop_path",
        ]
        missing_columns = [col for col in required_columns if col not in df.columns]
        if missing_columns:
            print(f"❌ Colunas faltantes: {missing_columns}")
            return

        # Remover duplicatas do CSV original baseado no ID
        original_count = len(df)
        df = df.drop_duplicates(subset=["id"], keep="first")
        initial_duplicates = original_count - len(df)
        if initial_duplicates > 0:
            print(f"🧹 Removidas {initial_duplicates} duplicatas do CSV original")

        # Aplicar limite se especificado
        if limit:
            df = df.head(limit)
            print(f"📌 Limitado aos primeiros {limit} filmes")

        total_movies = len(df)
        print(f"🎯 Total de filmes a processar: {total_movies}")

        # Preparar dados para processamento
        movie_data = []
        for idx, row in df.iterrows():
            movie_data.append((idx, row.to_dict()))

        # Dividir em lotes
        batches = [
            movie_data[i : i + batch_size]
            for i in range(0, len(movie_data), batch_size)
        ]
        print(f"📦 Dividido em {len(batches)} lotes")

        # Iniciar estatísticas
        self.stats.start_time = time.time()

        # Iniciar monitor de progresso
        stop_event = threading.Event()
        progress_thread = threading.Thread(
            target=self.progress_monitor, args=(total_movies, stop_event)
        )
        progress_thread.daemon = True
        progress_thread.start()

        print(f"\n🔥 Iniciando processamento paralelo...")
        print("=" * 60)

        # Processar lotes em paralelo
        valid_indices = set()
        valid_data = {}

        try:
            with ThreadPoolExecutor(max_workers=self.max_workers) as executor:
                # Submeter todos os lotes
                future_to_batch = {
                    executor.submit(self.process_movie_batch, batch): batch
                    for batch in batches
                }

                # Coletar resultados conforme completam
                for future in as_completed(future_to_batch):
                    try:
                        batch_results = future.result()
                        for idx, row_data, is_valid in batch_results:
                            if is_valid:
                                valid_indices.add(idx)
                                valid_data[idx] = row_data
                    except Exception as e:
                        print(f"❌ Erro no lote: {e}")

        except KeyboardInterrupt:
            print("\n⚠️ Interrompido pelo usuário...")
            stop_event.set()
            return

        # Parar monitor de progresso
        stop_event.set()

        # Criar DataFrame apenas com filmes válidos
        print(
            f"\n💾 Criando DataFrame limpo com {len(valid_indices)} filmes válidos..."
        )

        valid_rows = []
        for idx in sorted(valid_indices):
            valid_rows.append(valid_data[idx])

        if valid_rows:
            df_clean = pd.DataFrame(valid_rows)

            # Garantir ordem das colunas
            column_order = [
                "id",
                "title",
                "overview",
                "rating",
                "release_date",
                "original_language",
                "popularity",
                "poster_path",
                "backdrop_path",
            ]
            df_clean = df_clean[column_order]

            # Salvar arquivo limpo
            try:
                df_clean.to_csv(output_file, index=False)
                print(f"💾 Arquivo limpo salvo: {output_file}")
            except Exception as e:
                print(f"❌ Erro ao salvar arquivo: {e}")
                return
        else:
            print("❌ Nenhum filme válido encontrado!")
            return

        self.print_final_report(total_movies, len(valid_rows))
        self.save_removed_movies_report(output_file)
        self.show_comparison(input_file, output_file)

    def save_removed_movies_report(self, output_file: str):
        """Salva relatório detalhado dos filmes removidos"""
        if not self.stats.removed_movies:
            return

        report_file = output_file.replace(".csv", "_removed_movies_report.json")

        report = {
            "timestamp": datetime.now().isoformat(),
            "summary": {
                "total_removed": len(self.stats.removed_movies),
                "not_found_count": self.stats.not_found_count,
                "invalid_data_count": self.stats.invalid_data_count,
                "duplicates_removed": self.stats.duplicates_removed,
            },
            "removed_movies": self.stats.removed_movies,
        }

        try:
            with open(report_file, "w", encoding="utf-8") as f:
                json.dump(report, f, indent=2, ensure_ascii=False)
            print(f"📋 Relatório de filmes removidos salvo: {report_file}")
        except Exception as e:
            print(f"⚠️ Erro ao salvar relatório: {e}")

    def print_final_report(self, total_movies: int, final_count: int):
        """Imprime relatório final com métricas de performance e limpeza"""
        elapsed_time = time.time() - self.stats.start_time
        speed = self.stats.get_speed()

        print("\n" + "=" * 70)
        print("🏁 RELATÓRIO FINAL - MODO TURBO COM LIMPEZA")
        print("=" * 70)
        print(f"📊 ESTATÍSTICAS DE PROCESSAMENTO:")
        print(f"   📥 Total recebido: {total_movies}")
        print(f"   ✅ Filmes válidos mantidos: {self.stats.updated_count}")
        print(f"   🗑️ Total removido: {total_movies - final_count}")
        print(f"   📤 Total final: {final_count}")

        print(f"\n🧹 DETALHES DA LIMPEZA:")
        print(f"   ❌ Não encontrados na TMDB: {self.stats.not_found_count}")
        print(f"   ⚠️ Dados inválidos: {self.stats.invalid_data_count}")
        print(f"   🔄 Duplicatas removidas: {self.stats.duplicates_removed}")
        print(f"   🌐 Erros de conexão: {self.stats.error_count}")

        print(f"\n⚡ PERFORMANCE:")
        print(
            f"   ⏱️ Tempo total: {elapsed_time:.1f} segundos ({elapsed_time/60:.1f} min)"
        )
        print(f"   🚀 Velocidade média: {speed:.1f} filmes/segundo")
        print(f"   🧵 Threads utilizadas: {self.max_workers}")

        efficiency = (final_count / total_movies) * 100 if total_movies > 0 else 0
        print(f"   📊 Taxa de aproveitamento: {efficiency:.1f}%")

        print(f"\n📅 Finalizado em: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        print("=" * 70)

        if final_count < total_movies * 0.5:
            print("⚠️ ATENÇÃO: Mais de 50% dos filmes foram removidos!")
            print("   Verifique o relatório de filmes removidos para detalhes.")

    def update_top_movies_only(
        self, input_file: str, output_file: str = None, count: int = 100
    ):
        """Atualiza apenas os filmes mais populares do CSV"""
        if output_file is None:
            output_file = input_file.replace(".csv", f"_top{count}_threaded.csv")

        print(f"🔥 ATUALIZANDO TOP {count} FILMES - MODO TURBO!")
        print("=" * 60)

        # Carregar e ordenar por popularidade
        df = pd.read_csv(input_file)
        df_sorted = df.sort_values(by="popularity", ascending=False).head(count)

        print(f"📊 Selecionados os {len(df_sorted)} filmes mais populares")

        # Salvar temporariamente
        temp_file = f"temp_top{count}_movies.csv"
        df_sorted.to_csv(temp_file, index=False)

        # Processar
        self.update_csv_file(temp_file, output_file, batch_size=20)

        # Limpar arquivo temporário
        import os

        try:
            os.remove(temp_file)
        except:
            pass

    def show_comparison(self, original_file: str, updated_file: str):
        """Mostra comparação entre arquivo original e atualizado"""
        try:
            df_original = pd.read_csv(original_file)
            df_updated = pd.read_csv(updated_file)

            print("\n📊 ANÁLISE COMPARATIVA:")
            print("-" * 50)

            # Métricas básicas
            orig_rating = df_original["rating"].mean()
            new_rating = df_updated["rating"].mean()
            orig_pop = df_original["popularity"].mean()
            new_pop = df_updated["popularity"].mean()

            print(f"⭐ Rating médio:")
            print(f"   Antes: {orig_rating:.3f}")
            print(f"   Depois: {new_rating:.3f}")
            print(f"   Diferença: {(new_rating - orig_rating):+.3f}")

            print(f"\n🔥 Popularidade média:")
            print(f"   Antes: {orig_pop:.1f}")
            print(f"   Depois: {new_pop:.1f}")
            print(f"   Diferença: {(new_pop - orig_pop):+.1f}")

            # Análise de mudanças (se mesmo número de filmes)
            if len(df_original) == len(df_updated):
                df_comparison = df_updated.copy()
                df_comparison["popularity_diff"] = (
                    df_updated["popularity"] - df_original["popularity"]
                )
                df_comparison["rating_diff"] = (
                    df_updated["rating"] - df_original["rating"]
                )

                # Maiores aumentos
                top_pop = df_comparison.nlargest(3, "popularity_diff")
                print(f"\n🚀 TOP 3 AUMENTOS DE POPULARIDADE:")
                for _, row in top_pop.iterrows():
                    print(f"   📈 {row['title']}: +{row['popularity_diff']:.1f}")

                top_rating = df_comparison.nlargest(3, "rating_diff")
                print(f"\n⭐ TOP 3 AUMENTOS DE RATING:")
                for _, row in top_rating.iterrows():
                    print(f"   📈 {row['title']}: +{row['rating_diff']:.3f}")

        except Exception as e:
            print(f"❌ Erro na comparação: {e}")


def main():
    """Função principal otimizada"""
    # ⚠️ CONFIGURE SUA API KEY DO TMDB AQUI
    TMDB_API_KEY = "04586c4e9d103b384159866fedee8d46"

    if TMDB_API_KEY == "SUA_API_KEY_AQUI":
        print("❌ ERRO: Configure sua API key do TMDB!")
        print("Obtenha em: https://www.themoviedb.org/settings/api")
        return

    print("🚀 ATUALIZADOR CSV TMDB - VERSÃO TURBO MULTITHREADED COM LIMPEZA!")
    print("=" * 70)
    print("NOVA FUNCIONALIDADE: Remove automaticamente filmes inválidos e duplicatas")
    print("=" * 70)
    print("Escolha uma opção:")
    print("1. 🔥 TOP 100 filmes (RÁPIDO - ~2-3 min)")
    print("2. 🚀 TOP 500 filmes (MÉDIO - ~8-10 min)")
    print("3. 🧪 Teste com 50 filmes (TESTE - ~30s)")
    print("4. 💥 TODOS os filmes (LENTO - pode levar horas)")
    print("5. ⚙️ Configuração personalizada")

    try:
        opcao = input("\nDigite sua opção (1-5): ").strip()

        # Configurar número de threads baseado na opção
        if opcao in ["1", "3"]:
            max_workers = 15  # Mais agressivo para lotes menores
        elif opcao == "2":
            max_workers = 12  # Equilibrado
        else:
            max_workers = 8  # Mais conservador para volumes grandes

        updater = TMDBCSVUpdaterThreaded(TMDB_API_KEY, max_workers=max_workers)
        input_file = "movies_limpo.csv"

        if opcao == "1":
            print(f"🔥 Modo TURBO ativado! ({max_workers} threads)")
            updater.update_top_movies_only(input_file, count=100)

        elif opcao == "2":
            print(f"🚀 Modo SPEED ativado! ({max_workers} threads)")
            updater.update_top_movies_only(input_file, count=500)

        elif opcao == "3":
            print(f"🧪 Modo TESTE ativado! ({max_workers} threads)")
            updater.update_csv_file(input_file, limit=50, batch_size=10)

        elif opcao == "4":
            confirm = input(
                "⚠️ Isso processará TODOS os filmes e removerá inválidos. Confirma? (sim/não): "
            ).lower()
            if confirm in ["sim", "s", "yes", "y"]:
                print(f"💥 Modo COMPLETO ativado! ({max_workers} threads)")
                updater.update_csv_file(input_file, batch_size=30)
            else:
                print("❌ Operação cancelada")

        elif opcao == "5":
            try:
                threads = int(input("Número de threads (5-20): "))
                threads = max(5, min(20, threads))

                count = int(input("Quantos filmes processar: "))

                updater = TMDBCSVUpdaterThreaded(TMDB_API_KEY, max_workers=threads)
                print(
                    f"⚙️ Configuração personalizada: {threads} threads, {count} filmes"
                )

                if count <= len(pd.read_csv(input_file)):
                    updater.update_csv_file(input_file, limit=count)
                else:
                    print("❌ Número muito alto de filmes!")

            except ValueError:
                print("❌ Valores inválidos")
        else:
            print("❌ Opção inválida")

    except KeyboardInterrupt:
        print("\n\n⚠️ Programa interrompido pelo usuário")
    except Exception as e:
        print(f"\n❌ Erro durante execução: {e}")


if __name__ == "__main__":
    main()
