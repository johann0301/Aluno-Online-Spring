#!/usr/bin/env bash
set -euo pipefail

# PostgreSQL descartavel: os testes nao se conectam ao banco da aplicacao.
for security_command in initdb pg_ctl createdb; do
  if ! command -v "$security_command" >/dev/null 2>&1; then
    echo "Instale as ferramentas do PostgreSQL: comando ausente: $security_command" >&2
    exit 1
  fi
done

security_project_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)
security_pg_dir=$(mktemp -d /tmp/aluno-security-tests.XXXXXX)
security_pg_port=${SECURITY_TEST_DB_PORT:-55433}

security_cleanup() {
  pg_ctl -D "$security_pg_dir/data" -m fast -w stop >/dev/null 2>&1 || true
  rm -rf -- "$security_pg_dir"
}
trap security_cleanup EXIT

initdb -D "$security_pg_dir/data" -U security_test -A trust --no-locale -E UTF8 > "$security_pg_dir/init.log"
pg_ctl -D "$security_pg_dir/data" -l "$security_pg_dir/postgres.log" \
  -o "-h 127.0.0.1 -p $security_pg_port -k $security_pg_dir" -w start
createdb -h 127.0.0.1 -p "$security_pg_port" -U security_test aluno_security_test

cd -- "$security_project_dir"
DB_URL="jdbc:postgresql://127.0.0.1:$security_pg_port/aluno_security_test" \
DB_USERNAME=security_test DB_PASSWORD=temporario \
  ./mvnw -Dapi.security.token.secret=segredo-exclusivo-dos-testes verify "$@"
