#!/bin/bash

set -e

BACKUP_DIR="/opt/backups/docker"
DATE=$(date +%F-%H-%M)
TEMP_DIR="/tmp/docker_backup_$DATE"

POSTGRES_CONTAINER="ecutrans-postgres"
POSTGRES_USER="ecutrans"
POSTGRES_PASSWORD="ecutrans"

mkdir -p "$TEMP_DIR"
mkdir -p "$BACKUP_DIR"

echo "Backup iniciado: $DATE"

# ========================
# Backup PostgreSQL
# ========================
if docker ps --format '{{.Names}}' | grep -qx "$POSTGRES_CONTAINER"; then
    echo "Backup PostgreSQL..."

    if docker exec -e PGPASSWORD="$POSTGRES_PASSWORD" "$POSTGRES_CONTAINER" \
        pg_dumpall -U "$POSTGRES_USER" > "$TEMP_DIR/postgres.sql"; then
        gzip "$TEMP_DIR/postgres.sql"
        echo "Backup PostgreSQL completado"
    else
        echo "Error al generar backup de PostgreSQL"
    fi
else
    echo "No se encontró el contenedor PostgreSQL en ejecución"
fi

# ========================
# Backup de volúmenes Docker
# ========================
echo "Backup de volúmenes..."

for VOLUME in $(docker volume ls -q); do
  echo "Respaldando volumen: $VOLUME"
  docker run --rm \
    -v "${VOLUME}:/volume:ro" \
    -v "${TEMP_DIR}:/backup" \
    alpine \
    tar czf "/backup/${VOLUME}.tar.gz" -C /volume .
done

# ========================
# Información de Docker
# ========================
echo "Guardando información docker..."

docker ps -a --format 'table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}' > "$TEMP_DIR/containers.txt"
docker images --format 'table {{.Repository}}\t{{.Tag}}\t{{.ID}}\t{{.Size}}' > "$TEMP_DIR/images.txt"
docker volume ls > "$TEMP_DIR/volumes.txt"
docker network ls > "$TEMP_DIR/networks.txt"

# ========================
# Comprimir backup final
# ========================
tar -czf "$BACKUP_DIR/docker-backup-$DATE.tar.gz" -C "$TEMP_DIR" .

# ========================
# Limpiar temporales
# ========================
rm -rf "$TEMP_DIR"

# ========================
# Eliminar backups antiguos
# ========================
find "$BACKUP_DIR" -type f -mtime +60 -delete

echo "Backup finalizado: $BACKUP_DIR/docker-backup-$DATE.tar.gz"
