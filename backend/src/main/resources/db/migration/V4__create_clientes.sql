CREATE TABLE IF NOT EXISTS clientes (
  id UUID PRIMARY KEY,
  tipo_documento VARCHAR(20) NOT NULL,
  documento VARCHAR(100) NOT NULL UNIQUE,
  documento_norm VARCHAR(100) NOT NULL,
  nombre VARCHAR(200) NOT NULL,
  nombre_comercial VARCHAR(200),
  descripcion VARCHAR(1000),
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  deleted_at TIMESTAMP,
  deleted_by VARCHAR(100),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_clientes_documento_norm ON clientes(documento_norm);
CREATE INDEX IF NOT EXISTS idx_clientes_nombre ON clientes(nombre);
CREATE INDEX IF NOT EXISTS idx_clientes_deleted ON clientes(deleted);
