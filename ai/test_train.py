# test_train.py
import json

import requests


def test_train():
    """Testa o endpoint de treinamento"""
    
    # Dados de exemplo para treinamento
    ratings = [
        {"user": "1", "movie": "541671", "rating": 1},  # Gostou
        {"user": "1", "movie": "11", "rating": 0},      # Não gostou
        {"user": "1", "movie": "1359977", "rating": 1}, # Gostou
        {"user": "1", "movie": "1094274", "rating": 0}, # Não gostou
        {"user": "4", "movie": "870028", "rating": 1},  # Gostou
        {"user": "4", "movie": "896536", "rating": 0},  # Não gostou
        {"user": "4", "movie": "696374", "rating": 1},  # Gostou
        {"user": "3", "movie": "330459", "rating": 0},  # Não gostou
        {"user": "3", "movie": "1151039", "rating": 1}, # Gostou
        {"user": "3", "movie": "650033", "rating": 0},  # Não gostou
    ]

    payload = {"ratings": ratings}
    
    try:
        response = requests.post("http://localhost:5005/train", json=payload, timeout=30)
        print(f"Status Code: {response.status_code}")
        print(f"Response: {json.dumps(response.json(), indent=2)}")
        
        if response.status_code == 200:
            print("✅ Treinamento iniciado com sucesso!")
        else:
            print("❌ Erro no treinamento")
            
    except requests.exceptions.RequestException as e:
        print(f"❌ Erro de conexão: {e}")

if __name__ == "__main__":
    test_train()

import json

# test_recommend.py
import requests


def test_recommend():
    """Testa o endpoint de recomendação"""
    
    # IDs de filmes candidatos para recomendação
    candidate_ids = ["9836", "870028", "896536", "1122099", "40096", "539", "713364"]
    
    payload = {
        "user": "1",
        "candidate_ids": candidate_ids,
        "top_n": 3  # Pedir top 3 recomendações
    }
    
    try:
        response = requests.post("http://localhost:5005/recommend", json=payload, timeout=10)
        print(f"Status Code: {response.status_code}")
        print(f"Response: {json.dumps(response.json(), indent=2)}")
        
        if response.status_code == 200:
            result = response.json()
            if "recommended_movie" in result:
                print(f"✅ Filme recomendado: {result['recommended_movie']} (Score: {result['score']:.4f})")
                if "all_recommendations" in result:
                    print("📋 Todas as recomendações:")
                    for movie_id, score in result["all_recommendations"]:
                        print(f"  - Filme {movie_id}: {score:.4f}")
            else:
                print(f"❌ Erro na recomendação: {result.get('error', 'Erro desconhecido')}")
        else:
            print("❌ Erro na requisição")
            
    except requests.exceptions.RequestException as e:
        print(f"❌ Erro de conexão: {e}")

def test_batch_recommend():
    """Testa o endpoint de recomendação em lote"""
    
    users = ["1", "2", "3"]
    candidate_ids = ["9836", "870028", "896536", "1122099", "40096"]
    
    payload = {
        "users": users,
        "candidate_ids": candidate_ids,
        "top_n": 2
    }
    
    try:
        response = requests.post("http://localhost:5005/batch_recommend", json=payload, timeout=15)
        print(f"Status Code: {response.status_code}")
        print(f"Response: {json.dumps(response.json(), indent=2)}")
        
        if response.status_code == 200:
            print("✅ Recomendações em lote geradas com sucesso!")
        else:
            print("❌ Erro nas recomendações em lote")
            
    except requests.exceptions.RequestException as e:
        print(f"❌ Erro de conexão: {e}")

if __name__ == "__main__":
    print("🎬 Testando recomendação individual...")
    test_recommend()
    
    print("\n" + "="*50 + "\n")
    
    print("🎬 Testando recomendação em lote...")
    test_batch_recommend()

import json

# test_health.py
import requests


def test_health():
    """Testa o endpoint de health check"""
    
    try:
        response = requests.get("http://localhost:5005/health", timeout=5)
        print(f"Status Code: {response.status_code}")
        print(f"Response: {json.dumps(response.json(), indent=2)}")
        
        if response.status_code == 200:
            result = response.json()
            if result["status"] == "healthy":
                print("✅ API está saudável!")
                print(f"📊 Modelo carregado: {result['model_loaded']}")
                if result.get("last_training"):
                    print(f"🕒 Último treinamento: {result['last_training']}")
            else:
                print("⚠️ API não está saudável")
        else:
            print("❌ Erro no health check")
            
    except requests.exceptions.RequestException as e:
        print(f"❌ Erro de conexão: {e}")

def test_stats():
    """Testa o endpoint de estatísticas"""
    
    try:
        response = requests.get("http://localhost:5005/stats", timeout=5)
        print(f"Status Code: {response.status_code}")
        print(f"Response: {json.dumps(response.json(), indent=2)}")
        
        if response.status_code == 200:
            print("✅ Estatísticas obtidas com sucesso!")
        else:
            print("❌ Erro ao obter estatísticas")
            
    except requests.exceptions.RequestException as e:
        print(f"❌ Erro de conexão: {e}")

def test_all_endpoints():
    """Testa todos os endpoints da API"""
    
    print("🏥 Testando Health Check...")
    test_health()
    
    print("\n" + "="*50 + "\n")
    
    print("📊 Testando Estatísticas...")
    test_stats()
    
    print("\n" + "="*50 + "\n")
    
    print("🎓 Testando Treinamento...")
    test_train()
    
    print("\n" + "="*50 + "\n")
    
    print("🎯 Testando Recomendação...")
    test_recommend()

if __name__ == "__main__":
    test_all_endpoints()

import json
import time

# complete_test.py
import requests


def run_complete_test():
    """Executa um teste completo do sistema"""
    
    base_url = "http://localhost:5005"
    
    print("🚀 Iniciando teste completo do sistema de recomendação")
    print("="*60)
    
    # 1. Testar health check
    print("\n1️⃣ Testando Health Check...")
    try:
        response = requests.get(f"{base_url}/health", timeout=5)
        if response.status_code == 200:
            print("✅ Health check OK")
        else:
            print("❌ Health check falhou")
            return
    except Exception as e:
        print(f"❌ Erro na conexão: {e}")
        return
    
    # 2. Treinar o modelo
    print("\n2️⃣ Treinando modelo...")
    ratings = [
        {"user": "1", "movie": "541671", "rating": 1},
        {"user": "1", "movie": "11", "rating": 0},
        {"user": "1", "movie": "1359977", "rating": 1},
        {"user": "2", "movie": "870028", "rating": 1},
        {"user": "2", "movie": "896536", "rating": 0},
        {"user": "3", "movie": "696374", "rating": 1},
    ]
    
    try:
        response = requests.post(f"{base_url}/train", json={"ratings": ratings}, timeout=30)
        if response.status_code == 200:
            print("✅ Treinamento iniciado")
            print("⏳ Aguardando treinamento... (5 segundos)")
            time.sleep(5)
        else:
            print("❌ Erro no treinamento")
    except Exception as e:
        print(f"❌ Erro no treinamento: {e}")
    
    # 3. Verificar estatísticas
    print("\n3️⃣ Verificando estatísticas...")
    try:
        response = requests.get(f"{base_url}/stats", timeout=5)
        if response.status_code == 200:
            stats = response.json()
            print(f"📊 Filmes: {stats.get('total_movies', 0)}")
            print(f"📊 Avaliações: {stats.get('total_ratings', 0)}")
            print(f"📊 Perfis de usuário: {stats.get('user_profiles', 0)}")
        else:
            print("❌ Erro ao obter estatísticas")
    except Exception as e:
        print(f"❌ Erro nas estatísticas: {e}")
    
    # 4. Testar recomendações
    print("\n4️⃣ Testando recomendações...")
    candidate_ids = ["9836", "870028", "896536", "1122099", "40096"]
    
    for user in ["1", "2", "3"]:
        try:
            payload = {
                "user": user,
                "candidate_ids": candidate_ids,
                "top_n": 2
            }
            
            response = requests.post(f"{base_url}/recommend", json=payload, timeout=10)
            if response.status_code == 200:
                result = response.json()
                if "recommended_movie" in result:
                    print(f"✅ Usuário {user}: Filme {result['recommended_movie']} (Score: {result['score']:.4f})")
                else:
                    print(f"⚠️ Usuário {user}: {result.get('error', 'Sem recomendação')}")
            else:
                print(f"❌ Erro na recomendação para usuário {user}")
        except Exception as e:
            print(f"❌ Erro na recomendação para usuário {user}: {e}")
    
    # 5. Testar recomendação em lote
    print("\n5️⃣ Testando recomendação em lote...")
    try:
        payload = {
            "users": ["1", "2", "3"],
            "candidate_ids": candidate_ids,
            "top_n": 1
        }
        
        response = requests.post(f"{base_url}/batch_recommend", json=payload, timeout=15)
        if response.status_code == 200:
            results = response.json()["results"]
            for user, result in results.items():
                if "recommended_movie" in result:
                    print(f"✅ Usuário {user}: Filme {result['recommended_movie']}")
                else:
                    print(f"⚠️ Usuário {user}: {result.get('error', 'Sem recomendação')}")
        else:
            print("❌ Erro na recomendação em lote")
    except Exception as e:
        print(f"❌ Erro na recomendação em lote: {e}")
    
    print("\n" + "="*60)
    print("🎉 Teste completo finalizado!")

if __name__ == "__main__":
    run_complete_test()