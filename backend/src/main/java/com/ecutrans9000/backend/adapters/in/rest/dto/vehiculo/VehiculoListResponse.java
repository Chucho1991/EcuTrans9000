package com.ecutrans9000.backend.adapters.in.rest.dto.vehiculo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Componente publico de backend para VehiculoListResponse.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculoListResponse {
  private List<VehiculoResponse> content;
  private int page;
  private int size;
  private long totalElements;
  private int totalPages;
}
