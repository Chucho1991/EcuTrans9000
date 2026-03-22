package com.ecutrans9000.backend.domain.cliente;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad de dominio del módulo clientes.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {
  private UUID id;
  private TipoDocumentoCliente tipoDocumento;
  private String documento;
  private String documentoNorm;
  private String nombre;
  private String nombreComercial;
  private String direccion;
  private String descripcion;
  private Boolean aplicaTablaEquivalencia;
  @Builder.Default
  private List<ClienteEquivalencia> equivalencias = new ArrayList<>();
  private String logoFileName;
  private String logoContentType;
  private byte[] logoContenido;
  private Boolean activo;
  private Boolean deleted;
  private LocalDateTime deletedAt;
  private String deletedBy;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static String normalizeDocumento(String documento) {
    if (documento == null) {
      return "";
    }
    return documento.trim().toUpperCase(Locale.ROOT);
  }
}
