package com.ecutrans9000.backend.adapters.in.rest.dto.descuento;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO paginado de salida para descuentos de viajes.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DescuentoViajeListResponse {
  private List<DescuentoViajeResponse> content;
  private int page;
  private int size;
  private long totalElements;
  private int totalPages;
}
