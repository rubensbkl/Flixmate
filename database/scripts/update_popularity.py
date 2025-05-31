import time
import requests
import pandas as pd
import psycopg2
from psycopg2.extras import execute_values
import json
from datetime import datetime


class TMDBPopularityUpdater:
    def __init__(self, api_key, db_config):
        self.api_key = api_key
        self.db_config = db_config
        self.base_url = "https://api.themoviedb.org/3"
        self.session = requests.Session()
        self.updated_count = 0
        self.error_count = 0
        self.not_found_count = 0
        
    def conectar_banco(self):
        """Conecta ao banco PostgreSQL"""
        return psycopg2.connect(**self.db_config)
    
    def get_movie_details(self, movie_id):
        """Busca detalhes de um filme na API do TMDB"""
        url = f"{self.base_url}/movie/{movie_id}"
        params = {
            'api_key': self.api_key,
            'language': 'pt-BR'  # Para manter consistência
        }
        
        try:
            response = self.session.get(url, params=params, timeout=10)
            
            if response.status_code == 200:
                return response.json()
            elif response.status_code == 404:
                print(f"❌ Filme ID {movie_id} não encontrado no TMDB")
                return None
            else:
                print(f"⚠️ Erro {response.status_code} para filme ID {movie_id}")
                return None
                
        except requests.exceptions.RequestException as e:
            print(f"🌐 Erro de rede para filme ID {movie_id}: {e}")
            return None
    
    def get_movies_from_db(self, limit=None):
        """Busca filmes do banco de dados"""
        conn = self.conectar_banco()
        cur = conn.cursor()
        
        try:
            if limit:
                query = "SELECT id, title, popularity FROM movies ORDER BY popularity DESC LIMIT %s"
                cur.execute(query, (limit,))
            else:
                query = "SELECT id, title, popularity FROM movies ORDER BY id"
                cur.execute(query)
            
            filmes = cur.fetchall()
            return filmes
            
        finally:
            cur.close()
            conn.close()
    
    def update_movie_popularity(self, movie_id, new_popularity, new_rating=None):
        """Atualiza popularidade (e rating se fornecido) de um filme no banco"""
        conn = self.conectar_banco()
        cur = conn.cursor()
        
        try:
            if new_rating is not None:
                query = """
                    UPDATE movies 
                    SET popularity = %s, rating = %s, updated_at = CURRENT_TIMESTAMP 
                    WHERE id = %s
                """
                cur.execute(query, (new_popularity, new_rating, movie_id))
            else:
                query = """
                    UPDATE movies 
                    SET popularity = %s, updated_at = CURRENT_TIMESTAMP 
                    WHERE id = %s
                """
                cur.execute(query, (new_popularity, movie_id))
            
            conn.commit()
            return cur.rowcount > 0
            
        except Exception as e:
            print(f"❌ Erro ao atualizar filme {movie_id}: {e}")
            conn.rollback()
            return False
        finally:
            cur.close()
            conn.close()
    
    def update_batch_popularities(self, updates):
        """Atualiza múltiplos filmes em lote para melhor performance"""
        if not updates:
            return 0
            
        conn = self.conectar_banco()
        cur = conn.cursor()
        
        try:
            # Preparar dados para execute_values
            query = """
                UPDATE movies 
                SET popularity = data.new_popularity,
                    rating = data.new_rating,
                    updated_at = CURRENT_TIMESTAMP
                FROM (VALUES %s) AS data(movie_id, new_popularity, new_rating)
                WHERE movies.id = data.movie_id
            """
            
            execute_values(cur, query, updates, template=None)
            conn.commit()
            
            rows_updated = cur.rowcount
            return rows_updated
            
        except Exception as e:
            print(f"❌ Erro no update em lote: {e}")
            conn.rollback()
            return 0
        finally:
            cur.close()
            conn.close()
    
    def process_movies(self, limit=None, batch_size=50, delay=0.1):
        """Processa atualização de popularidades"""
        print("🎬 INICIANDO ATUALIZAÇÃO DE POPULARIDADES")
        print("=" * 60)
        
        # Buscar filmes do banco
        print("📊 Buscando filmes do banco de dados...")
        filmes = self.get_movies_from_db(limit)
        total_filmes = len(filmes)
        
        print(f"🎯 Total de filmes a processar: {total_filmes}")
        
        if limit:
            print(f"📌 Limitado aos {limit} filmes mais populares atuais")
        
        # Processar em lotes
        updates_batch = []
        
        for i, (movie_id, title, current_popularity) in enumerate(filmes, 1):
            print(f"\n🔄 [{i}/{total_filmes}] Processando: {title} (ID: {movie_id})")
            print(f"   Popularidade atual: {current_popularity}")
            
            # Buscar dados atualizados do TMDB
            movie_data = self.get_movie_details(movie_id)
            
            if movie_data:
                new_popularity = movie_data.get('popularity', current_popularity)
                new_rating = movie_data.get('vote_average')
                
                print(f"   Nova popularidade: {new_popularity}")
                print(f"   Nova avaliação: {new_rating}")
                
                # Adicionar ao lote de updates
                updates_batch.append((movie_id, new_popularity, new_rating))
                self.updated_count += 1
                
                # Processar lote quando atingir o tamanho definido
                if len(updates_batch) >= batch_size:
                    rows_updated = self.update_batch_popularities(updates_batch)
                    print(f"💾 Lote atualizado: {rows_updated} filmes")
                    updates_batch = []
                
            else:
                self.not_found_count += 1
                self.error_count += 1
            
            # Delay para respeitar rate limits da API
            if delay > 0:
                time.sleep(delay)
            
            # Progresso a cada 10 filmes
            if i % 10 == 0:
                print(f"\n📈 PROGRESSO: {i}/{total_filmes} ({(i/total_filmes)*100:.1f}%)")
                print(f"   ✅ Atualizados: {self.updated_count}")
                print(f"   ❌ Erros: {self.error_count}")
                print(f"   🔍 Não encontrados: {self.not_found_count}")
        
        # Processar lote restante
        if updates_batch:
            rows_updated = self.update_batch_popularities(updates_batch)
            print(f"💾 Lote final atualizado: {rows_updated} filmes")
        
        self.print_final_report()
    
    def update_top_popular_only(self, count=1000):
        """Atualiza apenas os filmes mais populares (mais eficiente)"""
        print(f"🔥 ATUALIZANDO TOP {count} FILMES MAIS POPULARES")
        print("=" * 60)
        
        # Buscar filmes populares direto da API do TMDB
        print("🌟 Buscando filmes populares atuais do TMDB...")
        
        all_popular_movies = []
        pages_to_fetch = min(count // 20 + 1, 50)  # TMDB retorna 20 por página, máximo 50 páginas
        
        for page in range(1, pages_to_fetch + 1):
            url = f"{self.base_url}/movie/popular"
            params = {
                'api_key': self.api_key,
                'language': 'pt-BR',
                'page': page
            }
            
            try:
                response = self.session.get(url, params=params, timeout=10)
                if response.status_code == 200:
                    data = response.json()
                    all_popular_movies.extend(data['results'])
                    print(f"📄 Página {page}: {len(data['results'])} filmes")
                    time.sleep(0.1)  # Rate limiting
                else:
                    print(f"❌ Erro na página {page}: {response.status_code}")
                    break
                    
            except Exception as e:
                print(f"🌐 Erro na página {page}: {e}")
                break
        
        print(f"🎯 Total de filmes populares obtidos: {len(all_popular_movies)}")
        
        # Filtrar apenas os filmes que existem no nosso banco
        conn = self.conectar_banco()
        cur = conn.cursor()
        
        try:
            # Buscar IDs que existem no banco
            movie_ids = [movie['id'] for movie in all_popular_movies]
            placeholders = ','.join(['%s'] * len(movie_ids))
            query = f"SELECT id FROM movies WHERE id IN ({placeholders})"
            cur.execute(query, movie_ids)
            existing_ids = {row[0] for row in cur.fetchall()}
            
            print(f"🎬 Filmes encontrados no banco: {len(existing_ids)}")
            
            # Atualizar filmes existentes
            updates_batch = []
            updated_count = 0
            
            for movie in all_popular_movies:
                if movie['id'] in existing_ids:
                    movie_id = movie['id']
                    new_popularity = movie.get('popularity', 0)
                    new_rating = movie.get('vote_average', 0)
                    
                    updates_batch.append((movie_id, new_popularity, new_rating))
                    updated_count += 1
                    
                    print(f"📝 {movie['title']}: popularity={new_popularity}, rating={new_rating}")
                    
                    # Processar em lotes de 100
                    if len(updates_batch) >= 100:
                        rows_updated = self.update_batch_popularities(updates_batch)
                        print(f"💾 Lote atualizado: {rows_updated} filmes")
                        updates_batch = []
            
            # Processar lote restante
            if updates_batch:
                rows_updated = self.update_batch_popularities(updates_batch)
                print(f"💾 Lote final atualizado: {rows_updated} filmes")
            
            print(f"\n✅ CONCLUÍDO: {updated_count} filmes atualizados com popularidades atuais!")
            
        finally:
            cur.close()
            conn.close()
    
    def print_final_report(self):
        """Imprime relatório final da atualização"""
        print("\n" + "=" * 60)
        print("📊 RELATÓRIO FINAL DA ATUALIZAÇÃO")
        print("=" * 60)
        print(f"✅ Filmes atualizados com sucesso: {self.updated_count}")
        print(f"❌ Filmes com erro: {self.error_count}")
        print(f"🔍 Filmes não encontrados no TMDB: {self.not_found_count}")
        print(f"📅 Data/hora da atualização: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        print("=" * 60)


def main():
    """Função principal"""
    # ⚠️ CONFIGURE SUA API KEY DO TMDB AQUI
    TMDB_API_KEY = "04586c4e9d103b384159866fedee8d46"  # Obtenha em: https://www.themoviedb.org/settings/api
    
    # 🔧 CONFIGURAÇÕES DO BANCO DE DADOS
    USUARIO = "cinematch"
    SENHA = "cinematch"
    HOST = "localhost"  # ou "postgres" se estiver em outro container
    PORTA = "5432"
    DB = "cinematch"
    
    DB_CONFIG = {
        'dbname': DB,
        'user': USUARIO,
        'password': SENHA,
        'host': HOST,
        'port': PORTA
    }
    
    # Verificar se a API key foi configurada
    if TMDB_API_KEY == "SUA_API_KEY_AQUI":
        print("❌ ERRO: Configure sua API key do TMDB!")
        print("Obtenha em: https://www.themoviedb.org/settings/api")
        return
    
    # Criar updater
    updater = TMDBPopularityUpdater(TMDB_API_KEY, DB_CONFIG)
    
    print("🎬 ATUALIZADOR DE POPULARIDADES TMDB")
    print("=" * 60)
    print("Escolha uma opção:")
    print("1. Atualizar TOP 1000 filmes mais populares (RECOMENDADO)")
    print("2. Atualizar TODOS os filmes do banco (LENTO)")
    print("3. Atualizar apenas os primeiros 100 filmes (TESTE)")
    
    try:
        opcao = input("\nDigite sua opção (1-3): ").strip()
        
        if opcao == "1":
            updater.update_top_popular_only(1000)
        elif opcao == "2":
            confirm = input("⚠️ Isso pode demorar horas. Confirma? (sim/não): ").lower()
            if confirm in ['sim', 's', 'yes', 'y']:
                updater.process_movies(delay=0.2)  # Delay maior para não sobrecarregar
            else:
                print("❌ Operação cancelada.")
        elif opcao == "3":
            updater.process_movies(limit=100, delay=0.1)
        else:
            print("❌ Opção inválida!")
            
    except KeyboardInterrupt:
        print("\n\n⏹️ Operação interrompida pelo usuário")
        updater.print_final_report()
    except Exception as e:
        print(f"\n❌ Erro durante execução: {e}")


if __name__ == "__main__":
    main()