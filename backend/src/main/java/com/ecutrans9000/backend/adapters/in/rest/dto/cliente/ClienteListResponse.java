package com.ecutrans9000.backend.adapters.in.rest.dto.cliente;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * DTO paginado de salida para listados de clientes.
 */
public class ClienteListResponse {
  private List<ClienteResponse> content;
  private int page;
  private int size;
  private long totalElements;
  private int totalPages;
}
