CREATE TABLE IF NOT EXISTS viajes_bitacora (
  id UUID PRIMARY KEY,
  numero_viaje INTEGER NOT NULL UNIQUE,
  fecha_viaje DATE NOT NULL,
  vehiculo_id UUID NOT NULL,
  cliente_id UUID NOT NULL,
  destino VARCHAR(250) NOT NULL,
  detalle_viaje VARCHAR(500),
  valor NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (valor >= 0),
  estiba NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (estiba >= 0),
  anticipo NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (anticipo >= 0),
  facturado_cliente BOOLEAN NOT NULL DEFAULT FALSE,
  numero_factura VARCHAR(100),
  fecha_factura DATE,
  fecha_pago_cliente DATE,
  pagado_transportista BOOLEAN NOT NULL DEFAULT FALSE,
  observaciones VARCHAR(1000),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_viajes_bitacora_vehiculo FOREIGN KEY (vehiculo_id) REFERENCES vehiculos(id),
  CONSTRAINT fk_viajes_bitacora_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id)
);

CREATE INDEX IF NOT EXISTS idx_viajes_bitacora_fecha_viaje ON viajes_bitacora(fecha_viaje);
CREATE INDEX IF NOT EXISTS idx_viajes_bitacora_vehiculo ON viajes_bitacora(vehiculo_id);
CREATE INDEX IF NOT EXISTS idx_viajes_bitacora_cliente ON viajes_bitacora(cliente_id);
