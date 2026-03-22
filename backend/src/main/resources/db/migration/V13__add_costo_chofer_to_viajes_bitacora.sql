ALTER TABLE viajes_bitacora
  ADD COLUMN IF NOT EXISTS costo_chofer NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (costo_chofer >= 0);
