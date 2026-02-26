package com.ecutrans9000.backend.application.usecase.vehiculo;

import com.ecutrans9000.backend.application.vehiculo.VehiculoApplicationService;
import com.ecutrans9000.backend.domain.vehiculo.Vehiculo;
import org.springframework.data.domain.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Componente publico de backend para SearchVehiculosUseCase.
 */
@Service
@RequiredArgsConstructor
public class SearchVehiculosUseCase {

  private final VehiculoApplicationService service;

  public Page<Vehiculo> execute(int page, int size, String q, String estado, Boolean includeDeleted) {
    return service.search(page, size, q, estado, includeDeleted);
  }
}
