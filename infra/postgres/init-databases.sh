#!/bin/bash
# Tworzy osobne bazy danych dla każdego mikroserwisu w jednym kontenerze PostgreSQL.
# W produkcji każdy serwis miałby własną instancję bazy — tutaj to kompromis dev/portfolio.
set -e

function create_db() {
    local database=$1
    echo "  → Tworzenie bazy: $database"
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
        CREATE DATABASE $database;
        GRANT ALL PRIVILEGES ON DATABASE $database TO $POSTGRES_USER;
EOSQL
}

if [ -n "$POSTGRES_MULTIPLE_DATABASES" ]; then
    echo "Inicjalizacja baz danych StreamFlix..."
    for db in $(echo "$POSTGRES_MULTIPLE_DATABASES" | tr ',' ' '); do
        create_db "$db"
    done
    echo "Gotowe."
fi
