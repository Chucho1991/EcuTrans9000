package com.ecutrans9000.backend.adapters.out.persistence.entity;

import com.ecutrans9000.backend.domain.cliente.TipoDocumentoCliente;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "clientes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteJpaEntity {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_documento", nullable = false, length = 20)
  private TipoDocumentoCliente tipoDocumento;

  @Column(name = "documento", nullable = false, unique = true, length = 100)
  private String documento;

  @Column(name = "documento_norm", nullable = false, length = 100)
  private String documentoNorm;

  @Column(name = "nombre", nullable = false, length = 200)
  private String nombre;

  @Column(name = "nombre_comercial", length = 200)
  private String nombreComercial;

  @Column(name = "direccion", length = 300)
  private String direccion;

  @Column(name = "descripcion", length = 1000)
  private String descripcion;

  @Column(name = "logo_file_name", length = 255)
  private String logoFileName;

  @Column(name = "logo_content_type", length = 100)
  private String logoContentType;

  @Column(name = "logo_contenido")
  private byte[] logoContenido;

  @Column(name = "activo", nullable = false)
  private Boolean activo;

  @Column(name = "deleted", nullable = false)
  private Boolean deleted;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Column(name = "deleted_by", length = 100)
  private String deletedBy;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
