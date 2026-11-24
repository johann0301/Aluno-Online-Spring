#!/usr/bin/env bash
set -euo pipefail

# Banco exclusivo da apresentação. Não usa nem reinicia o PostgreSQL original.
demo_project_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)
demo_state_dir="$demo_project_dir/.demo"
demo_db_port=${DEMO_DB_PORT:-55434}
demo_app_port=${DEMO_APP_PORT:-8081}
demo_app_pid=''
demo_started_db=false

for demo_command in initdb pg_ctl createdb psql; do
  command -v "$demo_command" >/dev/null 2>&1 || {
    echo "Comando necessário não encontrado: $demo_command (instale PostgreSQL)." >&2
    exit 1
  }
done
if [[ -z "${JAVA_HOME:-}" && -x /usr/lib/jvm/java-21-openjdk/bin/java ]]; then
  export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
fi
demo_java="${JAVA_HOME:+$JAVA_HOME/bin/}java"
command -v "$demo_java" >/dev/null 2>&1 || {
  echo 'Configure JAVA_HOME com a instalação do Java 21.' >&2
  exit 1
}

demo_cleanup() {
  if [[ -n "$demo_app_pid" ]]; then
    kill "$demo_app_pid" 2>/dev/null || true
    wait "$demo_app_pid" 2>/dev/null || true
  fi
  if [[ "$demo_started_db" == true ]]; then
    pg_ctl -D "$demo_state_dir/data" -m fast -w stop >/dev/null 2>&1 || true
  fi
}
trap demo_cleanup EXIT
trap 'exit 130' INT
trap 'exit 143' TERM

cd -- "$demo_project_dir"
./mvnw -DskipTests package
mkdir -p "$demo_state_dir"
chmod 700 "$demo_state_dir"
if [[ ! -f "$demo_state_dir/data/PG_VERSION" ]]; then
  initdb -D "$demo_state_dir/data" -U aluno_demo -A trust --no-locale -E UTF8 \
    > "$demo_state_dir/init.log"
fi
if ! pg_ctl -D "$demo_state_dir/data" status >/dev/null 2>&1; then
  pg_ctl -D "$demo_state_dir/data" -l "$demo_state_dir/postgres.log" \
    -o "-h 127.0.0.1 -p $demo_db_port -k ''" -w start
  demo_started_db=true
fi
demo_db_exists=$(psql -h 127.0.0.1 -p "$demo_db_port" -U aluno_demo -d postgres \
  -tAc "SELECT 1 FROM pg_database WHERE datname = 'aluno_online_demo'")
if [[ "$demo_db_exists" != 1 ]]; then
  createdb -h 127.0.0.1 -p "$demo_db_port" -U aluno_demo aluno_online_demo
fi

printf '\nIniciando apresentação. Aguarde a mensagem de inicialização do Spring.\n'
printf 'Swagger: http://localhost:%s/swagger-ui.html\n' "$demo_app_port"
printf 'Sistema: http://localhost:%s/\n' "$demo_app_port"
printf 'Banco da apresentação: 127.0.0.1:%s/aluno_online_demo\n' "$demo_db_port"
printf 'Ctrl+C encerra a aplicação e este banco; os dados ficam em .demo/ para a próxima execução.\n\n'

DB_URL="jdbc:postgresql://127.0.0.1:$demo_db_port/aluno_online_demo" \
DB_USERNAME=aluno_demo DB_PASSWORD=apenas-demo \
  "$demo_java" -jar target/api-0.0.1-SNAPSHOT.jar \
    --server.address=127.0.0.1 --server.port="$demo_app_port" --app.seed.reset=false &
demo_app_pid=$!
wait "$demo_app_pid"
