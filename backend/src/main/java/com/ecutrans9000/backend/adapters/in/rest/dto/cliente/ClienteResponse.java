package com.ecutrans9000.backend.adapters.in.rest.dto.cliente;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Payload REST de salida para el catálogo de clientes.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponse {
  private UUID id;
  private String tipoDocumento;
  private String documento;
  private String nombre;
  private String nombreComercial;
  private String direccion;
  private String descripcion;
  private String logoPath;
  private String logoPreviewDataUrl;
  private Boolean activo;
  private Boolean deleted;
  private LocalDateTime deletedAt;
  private String deletedBy;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
