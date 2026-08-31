#!/usr/bin/env python3
"""
Benchmark & Monitoramento de Recursos — Flixmate AI
Mede CPU, RAM, e tempo de cada etapa do pipeline de treinamento.
"""

import json
import os
import subprocess
import sys
import time
import threading
from datetime import datetime

# ─── Configuração ─────────────────────────────────────────────
AI_DIR = "/home/bkl/git/Flixmate/ai"
PROJECT_DIR = "/home/bkl/git/Flixmate"
REPORT_PATH = os.path.join(os.path.expanduser("~"), "benchmark_report.json")
SAMPLE_INTERVAL = 0.5  # segundos entre amostras de CPU/RAM


# ─── Monitor de recursos em background ───────────────────────
class ResourceMonitor:
    """Coleta amostras de RSS(MB) do processo em background."""

    def __init__(self, interval=SAMPLE_INTERVAL):
        self.interval = interval
        self.samples = []
        self._stop = threading.Event()
        self._thread = None

    def _collect(self, pid):
        while not self._stop.is_set():
            try:
                rss_kb = 0
                with open(f"/proc/{pid}/status") as f:
                    for line in f:
                        if line.startswith("VmRSS"):
                            rss_kb = int(line.split()[1])
                            break

                self.samples.append({
                    "ts": time.time(),
                    "rss_mb": rss_kb / 1024,
                })
            except Exception:
                pass
            self._stop.wait(self.interval)

    def start(self, pid=None):
        pid = pid or os.getpid()
        self._stop.clear()
        self.samples = []
        self._thread = threading.Thread(target=self._collect, args=(pid,), daemon=True)
        self._thread.start()

    def stop(self):
        self._stop.set()
        if self._thread:
            self._thread.join(timeout=2)

    def summary(self):
        if not self.samples:
            return {"peak_rss_mb": 0, "avg_rss_mb": 0, "samples": 0}
        rss_vals = [s["rss_mb"] for s in self.samples]
        return {
            "peak_rss_mb": round(max(rss_vals), 2),
            "avg_rss_mb": round(sum(rss_vals) / len(rss_vals), 2),
            "min_rss_mb": round(min(rss_vals), 2),
            "samples": len(rss_vals),
        }


# ─── Runner de etapa ─────────────────────────────────────────
def run_step(name, command, cwd=AI_DIR):
    """Executa um comando e retorna métricas."""
    print(f"\n{'='*60}")
    print(f"▶  {name}")
    print(f"   cmd: {command}")
    print(f"{'='*60}")

    monitor = ResourceMonitor()

    t0 = time.time()

    proc = subprocess.Popen(
        command,
        shell=True,
        cwd=cwd,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
    )

    # Monitorar o subprocesso
    monitor.start(proc.pid)

    output_lines = []
    for line in proc.stdout:
        sys.stdout.write(f"   │ {line}")
        output_lines.append(line.rstrip())

    proc.wait()
    elapsed = time.time() - t0
    monitor.stop()

    resources = monitor.summary()

    result = {
        "step": name,
        "command": command,
        "exit_code": proc.returncode,
        "elapsed_sec": round(elapsed, 3),
        "resources": resources,
        "success": proc.returncode == 0,
        "output_tail": output_lines[-10:] if output_lines else [],
    }

    status_icon = "✅" if result["success"] else "❌"
    print(f"\n   {status_icon} {name}")
    print(f"   ⏱  Tempo: {elapsed:.2f}s")
    print(f"   💾 RAM pico: {resources['peak_rss_mb']:.1f} MB  |  média: {resources['avg_rss_mb']:.1f} MB")

    return result


# ─── Medir treinamento online via API ────────────────────────
def benchmark_online_training():
    """Simula treinamento online (POST /train) e mede latência."""
    import urllib.request
    import urllib.error

    print(f"\n{'='*60}")
    print("▶  Treinamento Online (POST /train via API)")
    print(f"{'='*60}")

    # Buscar IDs de filmes reais do banco
    try:
        out = subprocess.check_output(
            ["docker", "exec", "flixmate-postgres-1",
             "psql", "-U", "cinematch", "-d", "cinematch", "-t", "-A",
             "-c", "SELECT id FROM movies LIMIT 10;"],
            text=True,
        )
        movie_ids = [int(x.strip()) for x in out.strip().split("\n") if x.strip()]
    except Exception:
        movie_ids = [634649, 860508, 82023]

    # Gerar ratings simulados
    ratings = []
    for user_id in range(1000000, 1000010):
        for i, mid in enumerate(movie_ids[:5]):
            ratings.append({
                "user": user_id,
                "movie": mid,
                "rating": True if i % 2 == 0 else False,
            })

    payload = json.dumps({"ratings": ratings}).encode("utf-8")

    # POST /train (é assíncrono, retorna 202)
    req = urllib.request.Request(
        "http://localhost:5005/train",
        data=payload,
        headers={"Content-Type": "application/json"},
    )

    t0 = time.time()
    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            body = json.loads(resp.read())
            accept_time = time.time() - t0
            print(f"   ⚡ Resposta 202 em {accept_time*1000:.1f}ms (treinamento em background)")
            print(f"   📊 {body}")
    except urllib.error.HTTPError as e:
        accept_time = time.time() - t0
        print(f"   ❌ HTTP {e.code}: {e.read().decode()}")
        return {"step": "online_train", "error": str(e), "elapsed_sec": accept_time, "success": False}

    # Esperar o treinamento em background terminar
    print("   ⏳ Aguardando treinamento em background completar (5s)...")
    time.sleep(5)

    # POST /recommend para medir latência
    rec_payload = json.dumps({
        "user": 1000000,
        "candidate_ids": movie_ids[:5],
        "top_n": 3,
    }).encode("utf-8")

    req2 = urllib.request.Request(
        "http://localhost:5005/recommend",
        data=rec_payload,
        headers={"Content-Type": "application/json"},
    )

    t1 = time.time()
    try:
        with urllib.request.urlopen(req2, timeout=30) as resp:
            rec_body = json.loads(resp.read())
            rec_time = time.time() - t1
            cache = rec_body.get("cache_used", "?")
            print(f"   🎯 Recomendação 1ª chamada: {rec_time*1000:.1f}ms (cache={cache})")
    except Exception as e:
        rec_time = time.time() - t1
        print(f"   ❌ Erro na recomendação: {e}")

    # 2ª chamada — deve ser cache hit
    t2 = time.time()
    try:
        with urllib.request.urlopen(req2, timeout=30) as resp:
            rec_body2 = json.loads(resp.read())
            cache_time = time.time() - t2
            cache2 = rec_body2.get("cache_used", "?")
            print(f"   ⚡ Recomendação 2ª chamada: {cache_time*1000:.1f}ms (cache={cache2})")
    except Exception as e:
        cache_time = time.time() - t2

    return {
        "step": "4. Treinamento Online + Inferência",
        "accept_latency_ms": round(accept_time * 1000, 1),
        "recommend_latency_ms": round(rec_time * 1000, 1),
        "recommend_cached_latency_ms": round(cache_time * 1000, 1),
        "success": True,
    }


# ─── Snapshot de containers Docker ───────────────────────────
def docker_stats_snapshot(label=""):
    """Captura stats dos containers."""
    if label:
        print(f"\n📊 Docker Stats ({label}):")
    try:
        out = subprocess.check_output(
            ["docker", "stats", "--no-stream", "--format",
             "{{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}"],
            text=True,
        )
        containers = {}
        for line in out.strip().split("\n"):
            parts = line.split("\t")
            if len(parts) >= 4:
                containers[parts[0]] = {
                    "cpu": parts[1],
                    "mem_usage": parts[2],
                    "mem_percent": parts[3],
                }
                if label:
                    print(f"   {parts[0]:30s} CPU={parts[1]:>7s}  MEM={parts[2]} ({parts[3]})")
        return containers
    except Exception as e:
        print(f"   ⚠️  Erro ao coletar docker stats: {e}")
        return {"error": str(e)}


# ─── Main ─────────────────────────────────────────────────────
def main():
    print("🔬 Flixmate AI — Benchmark & Monitoramento de Recursos")
    print(f"📅 {datetime.now().isoformat()}")
    print(f"{'='*60}\n")

    results = []

    docker_before = docker_stats_snapshot("antes do pipeline")

    # ─── Etapa 0: Importar catálogo TMDB filtrado ───
    r0 = run_step(
        "0. Importação TMDB v11 (filtro 60K filmes)",
        f"{sys.executable} /home/bkl/git/Flixmate/database/scripts/import_tmdb_v11.py",
        cwd="/home/bkl/git/Flixmate/database/scripts",
    )
    results.append(r0)

    if not r0["success"]:
        print("\n❌ Importação TMDB falhou. Abortando.")
        save_report(results, docker_before, {})
        return

    docker_after_import = docker_stats_snapshot("após importação TMDB")

    # ─── Preparar dados para os próximos passos (dentro do container) ───
    print("\n📦 Preparando dados no container da IA...")
    os.system("docker exec flixmate-ai-1 mkdir -p /app/movielens /app/output")
    os.system("docker cp /home/bkl/git/Flixmate/ai/movielens/ratings.csv flixmate-ai-1:/app/movielens/ratings.csv")
    os.system("docker cp /home/bkl/git/Flixmate/ai/movielens/links.csv flixmate-ai-1:/app/movielens/links.csv")
    # Exportar lista de IDs de filmes do banco atualizado
    os.system(
        'docker exec flixmate-postgres-1 psql -U cinematch -d cinematch '
        '-c "COPY (SELECT id FROM movies) TO STDOUT WITH CSV HEADER" '
        '> /tmp/movies_export.csv'
    )
    os.system("docker cp /tmp/movies_export.csv flixmate-ai-1:/app/output/movies.csv")
    os.system("docker exec -u root flixmate-ai-1 chown -R appuser:appuser /app/movielens /app/output")
    print("   ✅ Dados preparados")

    # ─── Etapa 1: Preprocessamento (dentro do container) ───
    r1 = run_step(
        "1. Preprocessamento (MovieLens → tmdbId)",
        "docker exec flixmate-ai-1 python /app/1preprocess.py",
        cwd=PROJECT_DIR,
    )
    results.append(r1)

    if not r1["success"]:
        print("\n❌ Preprocessamento falhou. Abortando.")
        save_report(results, docker_before, {})
        return

    # ─── Etapa 2: Filtro de ratings (dentro do container) ───
    r2 = run_step(
        "2. Filtro de ratings (só filmes do catálogo)",
        "docker exec flixmate-ai-1 python /app/2preprocess.py",
        cwd=PROJECT_DIR,
    )
    results.append(r2)

    if not r2["success"]:
        print("\n❌ Filtro falhou. Abortando.")
        save_report(results, docker_before, {})
        return

    r3 = run_step(
        "3. Pré-treinamento offline (HybridRecommender)",
        "docker exec flixmate-ai-1 python /app/3pretrain.py",
        cwd="/home/bkl/git/Flixmate",
    )
    results.append(r3)

    docker_after = docker_stats_snapshot("após pré-treinamento")

    # ─── Etapa 4: Treinamento online via API ───
    if r3["success"]:
        # Recarregar o modelo no container
        print("\n🔄 Recarregando modelo na API...")
        try:
            import urllib.request
            req = urllib.request.Request("http://localhost:5005/admin/reload_model", method="POST")
            with urllib.request.urlopen(req, timeout=15) as resp:
                print(f"   ✅ {json.loads(resp.read())}")
        except Exception as e:
            print(f"   ⚠️  Reload: {e}")

        time.sleep(2)
        r4 = benchmark_online_training()
        results.append(r4)

    docker_final = docker_stats_snapshot("estado final")

    save_report(results, docker_before, docker_final)


def save_report(results, docker_before, docker_after):
    report = {
        "timestamp": datetime.now().isoformat(),
        "steps": results,
        "docker_before": docker_before,
        "docker_after": docker_after,
        "summary": {
            "total_time_sec": round(sum(r.get("elapsed_sec", 0) for r in results), 2),
            "all_passed": all(r.get("success", False) for r in results),
        },
    }

    os.makedirs(os.path.dirname(REPORT_PATH), exist_ok=True)
    with open(REPORT_PATH, "w") as f:
        json.dump(report, f, indent=2, default=str)

    print(f"\n{'='*60}")
    print("📋 RELATÓRIO FINAL")
    print(f"{'='*60}")
    for r in results:
        icon = "✅" if r.get("success") else "❌"
        time_str = f"{r.get('elapsed_sec', 0):.2f}s" if "elapsed_sec" in r else ""
        peak = r.get("resources", {}).get("peak_rss_mb")
        peak_str = f"{peak:.0f}MB" if isinstance(peak, (int, float)) else ""
        latency = r.get("accept_latency_ms")
        extra = f"  API: {latency:.0f}ms" if latency else ""
        rec = r.get("recommend_latency_ms")
        rec_str = f"  Rec: {rec:.0f}ms" if rec else ""
        cached = r.get("recommend_cached_latency_ms")
        cache_str = f"  Cache: {cached:.0f}ms" if cached else ""

        line = f"   {icon} {r['step']:50s}"
        if time_str:
            line += f" ⏱ {time_str:>8s}"
        if peak_str:
            line += f"  💾 {peak_str}"
        line += extra + rec_str + cache_str
        print(line)

    total = report["summary"]["total_time_sec"]
    print(f"\n   ⏱  Tempo total pipeline: {total:.2f}s")
    print(f"   📁 Relatório JSON: {REPORT_PATH}")


if __name__ == "__main__":
    main()
