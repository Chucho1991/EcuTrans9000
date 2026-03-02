package com.ecutrans9000.backend.adapters.in.rest.dto.bitacora;

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
public class ViajeBitacoraListResponse {
  private List<ViajeBitacoraResponse> content;
  private int page;
  private int size;
  private long totalElements;
  private int totalPages;
}
