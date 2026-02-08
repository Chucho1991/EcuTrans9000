# EcuTrans9000

Aplicacion para digitalizar la bitácora de viajes de camiones para registrar información operativa y financiera, autocompletar datos del vehículo, gestionar estados de facturación/pago y calcular totales por placa

## Servicios
- Frontend (Angular): http://localhost:4200
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/index.html

## Requisitos
- Docker + Docker Compose

## Variables de entorno
Revisar `.env.example` para valores por defecto:
- `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_URL`
- `MONGO_URI`
- `JWT_SECRET`, `JWT_EXPIRATION_MINUTES`
- `AUDIT_MAX_PAYLOAD_SIZE`
- `CORS_ALLOWED_ORIGINS`

## Levantar con Docker
```bash
cp .env.example .env
docker compose up --build
```