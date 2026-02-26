CREATE TABLE IF NOT EXISTS vehiculo_archivos (
  id UUID PRIMARY KEY,
  vehiculo_id UUID NOT NULL,
  tipo VARCHAR(20) NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  content_type VARCHAR(100) NOT NULL,
  contenido BYTEA NOT NULL,
  size_bytes BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_vehiculo_archivos_vehiculo FOREIGN KEY (vehiculo_id) REFERENCES vehiculos(id),
  CONSTRAINT uq_vehiculo_archivos_vehiculo_tipo UNIQUE (vehiculo_id, tipo)
);

CREATE INDEX IF NOT EXISTS idx_vehiculo_archivos_vehiculo_id ON vehiculo_archivos(vehiculo_id);
