ALTER TABLE clientes
  ADD COLUMN IF NOT EXISTS aplica_tabla_equivalencia BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS cliente_equivalencias (
  id UUID PRIMARY KEY,
  cliente_id UUID NOT NULL REFERENCES clientes(id) ON DELETE CASCADE,
  destino VARCHAR(200) NOT NULL,
  destino_norm VARCHAR(200) NOT NULL,
  valor_destino NUMERIC(14,2) NOT NULL,
  costo_chofer NUMERIC(14,2) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_cliente_equivalencias_cliente ON cliente_equivalencias(cliente_id);
CREATE INDEX IF NOT EXISTS idx_cliente_equivalencias_destino_norm ON cliente_equivalencias(destino_norm);
CREATE UNIQUE INDEX IF NOT EXISTS uq_cliente_equivalencias_cliente_destino
  ON cliente_equivalencias(cliente_id, destino_norm);
