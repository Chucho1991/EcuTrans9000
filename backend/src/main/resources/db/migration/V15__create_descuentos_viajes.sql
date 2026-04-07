CREATE TABLE IF NOT EXISTS descuentos_viajes (
  id BIGSERIAL PRIMARY KEY,
  vehiculo_id UUID NOT NULL REFERENCES vehiculos (id),
  descripcion_motivo VARCHAR(250) NOT NULL,
  descripcion_motivo_norm VARCHAR(250) NOT NULL,
  monto_motivo NUMERIC(12, 2) NOT NULL DEFAULT 0,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  deleted_at TIMESTAMP NULL,
  deleted_by VARCHAR(100) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_descuentos_viajes_vehiculo_motivo UNIQUE (vehiculo_id, descripcion_motivo_norm)
);

CREATE INDEX IF NOT EXISTS idx_descuentos_viajes_vehiculo_id
  ON descuentos_viajes (vehiculo_id);

CREATE INDEX IF NOT EXISTS idx_descuentos_viajes_activo_deleted
  ON descuentos_viajes (activo, deleted);
