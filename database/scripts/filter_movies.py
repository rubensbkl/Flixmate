import json
import os
from datetime import datetime
from typing import Dict, List, Tuple

import pandas as pd


class MovieFilter:
    """Filtro avançado para base de filmes com critérios de qualidade"""
    
    def __init__(self):
        # Principais idiomas do cinema mundial
        self.allowed_languages = {
            'en',  # Inglês
            'es',  # Espanhol
            'fr',  # Francês
            'de',  # Alemão
            'it',  # Italiano
            'pt',  # Português
        }
        
        self.removed_movies = []
        self.stats = {
            'total_input': 0,
            'total_output': 0,
            'removed_by_popularity': 0,
            'removed_by_rating': 0,
            'removed_by_invalid_data': 0,
            'removed_duplicates': 0,
            'removed_by_release_date': 0,
            'removed_by_language': 0,
            'language_distribution': {}
        }
    
    def validate_movie_data(self, row: Dict) -> Tuple[bool, str]:
        """
        Valida se todos os campos obrigatórios estão presentes e válidos
        Retorna: (is_valid, error_reason)
        """
        required_fields = {
            'id': 'ID do filme',
            'title': 'Título',
            'overview': 'Sinopse',
            'rating': 'Rating',
            'release_date': 'Data de lançamento',
            'original_language': 'Idioma original',
            'popularity': 'Popularidade',
            'poster_path': 'Poster path',
            'backdrop_path': 'Backdrop path'
        }
        
        try:
            # Primeiro aplica o filtro de idioma
            passes_language, language_reason = self.apply_language_filter(row)
            if not passes_language:
                return False, language_reason
            
            # Verificar se todos os campos existem
            for field, field_name in required_fields.items():
                if field not in row or pd.isna(row[field]):
                    return False, f"Campo obrigatório ausente: {field_name}"
            
            # Validações específicas
            try:
                # ID deve ser número inteiro positivo
                movie_id = int(row['id'])
                if movie_id <= 0:
                    return False, "ID inválido (deve ser positivo)"
                
                # Rating deve ser número entre 0 e 10
                rating = float(row['rating'])
                if rating < 0 or rating > 10:
                    return False, f"Rating inválido: {rating} (deve estar entre 0-10)"
                
                # Popularidade deve ser número positivo
                popularity = float(row['popularity'])
                if popularity < 0:
                    return False, f"Popularidade inválida: {popularity} (deve ser positiva)"
                
                # Título não pode estar vazio
                title = str(row['title']).strip()
                if len(title) == 0:
                    return False, "Título vazio"
                
                # Overview deve ter pelo menos 10 caracteres
                overview = str(row['overview']).strip()
                if len(overview) < 10:
                    return False, f"Sinopse muito curta: {len(overview)} chars (mínimo 10)"
                
                # Data de lançamento deve ter pelo menos 4 caracteres (ano)
                release_date = str(row['release_date']).strip()
                if len(release_date) < 4:
                    return False, f"Data de lançamento inválida: {release_date}"
                
                # Idioma deve ter pelo menos 2 caracteres
                language = str(row['original_language']).strip()
                if len(language) < 2:
                    return False, f"Idioma inválido: {language}"
                    
            except (ValueError, TypeError) as e:
                return False, f"Erro de conversão de dados: {str(e)}"
            
            return True, ""
        except (ValueError, TypeError) as e:
            return False, f"Erro de conversão de dados: {str(e)}"
    
    def apply_language_filter(self, row: Dict) -> Tuple[bool, str]:
        """
        Aplica filtro de idioma para manter apenas principais línguas do cinema mundial
        Retorna: (passes_filter, rejection_reason)
        """
        try:
            original_language = str(row.get('original_language', '')).lower().strip()
            
            if not original_language:
                return False, "Idioma original não informado"
            
            if original_language not in self.allowed_languages:
                return False, f"Idioma não permitido: {original_language} (permitidos: {', '.join(sorted(self.allowed_languages))})"
            
            return True, ""
            
        except Exception as e:
            return False, f"Erro no filtro de idioma: {str(e)}"
    
    def apply_quality_filters(self, row: Dict) -> Tuple[bool, str]:
        """
        Aplica filtros de qualidade
        Retorna: (passes_filter, rejection_reason)
        """
        try:
            popularity = float(row['popularity'])
            rating = float(row['rating'])
            release_date = str(row['release_date'])
            
            # Filtro 1: Popularidade > 5
            if popularity <= 2.0:
                return False, f"Popularidade baixa: {popularity} (mínimo: 2.0)"
            
            # Filtro 2: Rating > 2
            if rating <= 5.5:
                return False, f"Rating baixo: {rating} (mínimo: 5.5)"
            
            # Filtro 3: Filme não muito antigo (opcional - você pode ajustar)
            if len(release_date) >= 4:
                try:
                    year = int(release_date[:4])
                    if year < 1950:  # Filmes muito antigos podem ter dados inconsistentes
                        return False, f"Filme muito antigo: {year} (mínimo: 1950)"
                except:
                    pass  # Se não conseguir extrair o ano, continua
            
            return True, ""
            
        except (ValueError, TypeError) as e:
            return False, f"Erro nos filtros de qualidade: {str(e)}"
    
    def add_removed_movie(self, movie_data: Dict, reason: str):
        """Adiciona filme removido ao relatório"""
        self.removed_movies.append({
            'id': movie_data.get('id', 'N/A'),
            'title': movie_data.get('title', 'N/A'),
            'popularity': movie_data.get('popularity', 'N/A'),
            'rating': movie_data.get('rating', 'N/A'),
            'release_date': movie_data.get('release_date', 'N/A'),
            'reason': reason
        })
    
    def filter_movies(self, input_file: str, output_file: str = None):
        """Executa o processo completo de filtragem"""
        if output_file is None:
            output_file = input_file.replace('.csv', '_filtered.csv')
        
        print("🎬 FILTRO AVANÇADO DE QUALIDADE DE FILMES")
        print("=" * 60)
        print(f"📥 Arquivo de entrada: {input_file}")
        print(f"📤 Arquivo de saída: {output_file}")
        print(f"🎯 Critérios:")
        print(f"   • Popularidade > 2.0")
        print(f"   • Rating > 5.5") 
        print(f"   • Ano de lançamento >= 1950")
        print(f"   • Todos os campos obrigatórios válidos")
        print(f"   • Remoção de duplicatas por ID")
        print()
        
        # Carregar CSV
        try:
            df = pd.read_csv(input_file)
            self.stats['total_input'] = len(df)
            print(f"✅ Carregados {len(df)} filmes")
        except Exception as e:
            print(f"❌ Erro ao carregar arquivo: {e}")
            return
        
        # Verificar colunas necessárias
        required_columns = ['id', 'title', 'overview', 'rating', 'release_date', 
                           'original_language', 'popularity', 'poster_path', 'backdrop_path']
        missing_columns = [col for col in required_columns if col not in df.columns]
        if missing_columns:
            print(f"❌ Colunas faltantes: {missing_columns}")
            return
        
        print("\n🔍 Iniciando filtragem...")
        
        # Lista para armazenar filmes válidos
        valid_movies = []
        seen_ids = set()
        
        for idx, row in df.iterrows():
            movie_data = row.to_dict()
            
            # 1. Verificar duplicatas
            movie_id = movie_data.get('id')
            if movie_id in seen_ids:
                self.stats['removed_duplicates'] += 1
                self.add_removed_movie(movie_data, "Duplicata removida")
                continue
            seen_ids.add(movie_id)
            
            # 2. Validar dados obrigatórios
            is_valid, validation_error = self.validate_movie_data(movie_data)
            if not is_valid:
                # Categorizar por tipo de erro de validação
                if "Idioma não permitido" in validation_error or "Idioma original não informado" in validation_error:
                    self.stats['removed_by_language'] += 1
                    # Rastrear idiomas removidos
                    lang = movie_data.get('original_language', 'unknown')
                    if lang not in self.stats['language_distribution']:
                        self.stats['language_distribution'][lang] = 0
                    self.stats['language_distribution'][lang] += 1
                else:
                    self.stats['removed_by_invalid_data'] += 1
                
                self.add_removed_movie(movie_data, validation_error)
                continue
            
            # 3. Aplicar filtros de qualidade
            passes_filter, filter_reason = self.apply_quality_filters(movie_data)
            if not passes_filter:
                # Categorizar por tipo de filtro
                if "Popularidade baixa" in filter_reason:
                    self.stats['removed_by_popularity'] += 1
                elif "Rating baixo" in filter_reason:
                    self.stats['removed_by_rating'] += 1
                elif "muito antigo" in filter_reason:
                    self.stats['removed_by_release_date'] += 1
                
                self.add_removed_movie(movie_data, filter_reason)
                continue
            
            # Filme passou em todos os filtros
            valid_movies.append(movie_data)
        
        self.stats['total_output'] = len(valid_movies)
        
        # Criar DataFrame filtrado
        if valid_movies:
            df_filtered = pd.DataFrame(valid_movies)
            
            # Garantir ordem das colunas
            df_filtered = df_filtered[required_columns]
            
            # Salvar arquivo filtrado
            try:
                df_filtered.to_csv(output_file, index=False)
                print(f"\n💾 Arquivo filtrado salvo: {output_file}")
            except Exception as e:
                print(f"❌ Erro ao salvar arquivo: {e}")
                return
        else:
            print("\n❌ Nenhum filme passou nos filtros!")
            return
        
        # Relatório final
        self.print_final_report()
        self.save_filter_report(output_file)
        self.show_quality_analysis(input_file, output_file)
    
    def print_final_report(self):
        """Imprime relatório final detalhado"""
        total_removed = self.stats['total_input'] - self.stats['total_output']
        retention_rate = (self.stats['total_output'] / self.stats['total_input']) * 100
        
        print("\n" + "=" * 70)
        print("🏁 RELATÓRIO FINAL - FILTRO DE QUALIDADE")
        print("=" * 70)
        
        print(f"📊 ESTATÍSTICAS GERAIS:")
        print(f"   📥 Total de entrada: {self.stats['total_input']:,}")
        print(f"   📤 Total de saída: {self.stats['total_output']:,}")
        print(f"   🗑️ Total removido: {total_removed:,}")
        print(f"   📊 Taxa de retenção: {retention_rate:.1f}%")
        
        print(f"\n🧹 DETALHES DA FILTRAGEM:")
        print(f"   🔥 Removidos por popularidade ≤ 2.0: {self.stats['removed_by_popularity']:,}")
        print(f"   ⭐ Removidos por rating ≤ 5.5: {self.stats['removed_by_rating']:,}")
        print(f"   🌍 Removidos por idioma: {self.stats['removed_by_language']:,}")
        print(f"   📅 Removidos por data antiga: {self.stats['removed_by_release_date']:,}")
        print(f"   ⚠️ Removidos por dados inválidos: {self.stats['removed_by_invalid_data']:,}")
        print(f"   🔄 Duplicatas removidas: {self.stats['removed_duplicates']:,}")
        
        # Mostrar distribuição de idiomas removidos
        if self.stats['language_distribution']:
            print(f"\n🌐 IDIOMAS REMOVIDOS (TOP 10):")
            sorted_langs = sorted(self.stats['language_distribution'].items(), 
                                key=lambda x: x[1], reverse=True)
            for lang, count in sorted_langs[:10]:
                print(f"   🗣️ {lang}: {count:,} filmes")
        
        print(f"\n📅 Finalizado em: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        print("=" * 70)
        
        # Avisos
        if retention_rate < 50:
            print("⚠️ ATENÇÃO: Mais de 50% dos filmes foram removidos!")
            print("   Considere revisar os critérios de filtragem.")
        elif retention_rate > 95:
            print("✅ EXCELENTE: Alta taxa de retenção - base já estava muito limpa!")
    
    def save_filter_report(self, output_file: str):
        """Salva relatório detalhado dos filmes removidos"""
        if not self.removed_movies:
            return
        
        report_file = output_file.replace('.csv', '_filter_report.json')
        
        report = {
            'timestamp': datetime.now().isoformat(),
            'filter_criteria': {
                'min_popularity': 2.0,
                'min_rating': 5.5,
                'min_year': 1950,
                'required_fields_validation': True,
                'duplicate_removal': True
            },
            'statistics': self.stats,
            'removed_movies': self.removed_movies
        }
        
        try:
            with open(report_file, 'w', encoding='utf-8') as f:
                json.dump(report, f, indent=2, ensure_ascii=False)
            print(f"📋 Relatório de filtragem salvo: {report_file}")
        except Exception as e:
            print(f"⚠️ Erro ao salvar relatório: {e}")
    
    def show_quality_analysis(self, input_file: str, output_file: str):
        """Mostra análise comparativa de qualidade"""
        try:
            df_original = pd.read_csv(input_file)
            df_filtered = pd.read_csv(output_file)
            
            print(f"\n📊 ANÁLISE DE QUALIDADE:")
            print("-" * 50)
            
            # Estatísticas de popularidade
            orig_pop_mean = df_original['popularity'].mean()
            filt_pop_mean = df_filtered['popularity'].mean()
            orig_pop_median = df_original['popularity'].median()
            filt_pop_median = df_filtered['popularity'].median()
            
            print(f"🔥 POPULARIDADE:")
            print(f"   Média - Antes: {orig_pop_mean:.1f} | Depois: {filt_pop_mean:.1f}")
            print(f"   Mediana - Antes: {orig_pop_median:.1f} | Depois: {filt_pop_median:.1f}")
            
            # Estatísticas de rating
            orig_rating_mean = df_original['rating'].mean()
            filt_rating_mean = df_filtered['rating'].mean()
            orig_rating_median = df_original['rating'].median()
            filt_rating_median = df_filtered['rating'].median()
            
            print(f"\n⭐ RATING:")
            print(f"   Média - Antes: {orig_rating_mean:.2f} | Depois: {filt_rating_mean:.2f}")
            print(f"   Mediana - Antes: {orig_rating_median:.2f} | Depois: {filt_rating_median:.2f}")
            
            # Top filmes por popularidade
            top_popular = df_filtered.nlargest(5, 'popularity')[['title', 'popularity', 'rating']]
            print(f"\n🚀 TOP 5 MAIS POPULARES (APÓS FILTRO):")
            for _, row in top_popular.iterrows():
                print(f"   📈 {row['title']}: {row['popularity']:.1f} pop, {row['rating']:.1f} rating")
            
            # Distribuição por idioma aceito
            lang_dist = df_filtered['original_language'].value_counts()
            print(f"\n🌐 IDIOMAS MANTIDOS:")
            for lang, count in lang_dist.items():
                lang_name = {
                    'en': 'Inglês', 'es': 'Espanhol', 'fr': 'Francês', 
                    'de': 'Alemão', 'it': 'Italiano', 'pt': 'Português'
                }.get(lang, lang)
                print(f"   🗣️ {lang_name} ({lang}): {count:,} filmes")
            
            # Distribuição por década
            df_filtered['year'] = df_filtered['release_date'].str[:4].astype(int, errors='ignore')
            decade_dist = df_filtered['year'].apply(lambda x: f"{(x//10)*10}s" if pd.notnull(x) and x >= 1950 else "Outros").value_counts().head(5)
            print(f"\n📅 DISTRIBUIÇÃO POR DÉCADA (TOP 5):")
            for decade, count in decade_dist.items():
                print(f"   📆 {decade}: {count:,} filmes")
                
        except Exception as e:
            print(f"❌ Erro na análise comparativa: {e}")


def main():
    """Função principal"""
    print("🎬 FILTRO AVANÇADO DE QUALIDADE - FLIXMATE")
    print("=" * 60)
    
    # Arquivo de entrada
    input_file = "movies_limpo_atualizado_threaded.csv"
    
    if not os.path.exists(input_file):
        print(f"❌ Arquivo não encontrado: {input_file}")
        print("Certifique-se de que o arquivo está no diretório atual.")
        return
    
    # Criar filtro
    movie_filter = MovieFilter()
    
    # Executar filtragem
    output_file = "movies_limpo.csv"
    movie_filter.filter_movies(input_file, output_file)
    
    print(f"\n🎉 PROCESSO CONCLUÍDO!")
    print(f"📥 Entrada: {input_file}")
    print(f"📤 Saída: {output_file}")
    print(f"📋 Relatório: {output_file.replace('.csv', '_filter_report.json')}")


if __name__ == "__main__":
    main()
