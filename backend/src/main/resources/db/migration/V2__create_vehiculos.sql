CREATE TABLE IF NOT EXISTS vehiculos (
  id UUID PRIMARY KEY,
  placa VARCHAR(50) NOT NULL,
  placa_norm VARCHAR(50) NOT NULL UNIQUE,
  chofer_default VARCHAR(200) NOT NULL,
  licencia VARCHAR(100),
  tipo_documento VARCHAR(20) NOT NULL,
  documento_personal VARCHAR(100) NOT NULL,
  tonelaje_categoria VARCHAR(100) NOT NULL,
  m3 NUMERIC(10,2) NOT NULL CHECK (m3 >= 0),
  estado VARCHAR(20) NOT NULL,
  foto_path VARCHAR(500),
  doc_path VARCHAR(500),
  lic_path VARCHAR(500),
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  deleted_at TIMESTAMP,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_vehiculos_placa_norm ON vehiculos(placa_norm);
CREATE INDEX IF NOT EXISTS idx_vehiculos_chofer_default ON vehiculos(chofer_default);
