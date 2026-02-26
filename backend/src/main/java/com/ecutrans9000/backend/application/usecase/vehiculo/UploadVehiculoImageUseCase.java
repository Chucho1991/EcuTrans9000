package com.ecutrans9000.backend.application.usecase.vehiculo;

import com.ecutrans9000.backend.application.vehiculo.VehiculoApplicationService;
import com.ecutrans9000.backend.domain.vehiculo.Vehiculo;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UploadVehiculoImageUseCase {

  private final VehiculoApplicationService service;

  public Vehiculo uploadFoto(UUID id, MultipartFile file, String actorUsername, String actorRole) {
    return service.uploadFoto(id, file, actorUsername, actorRole);
  }

  public Vehiculo uploadDocumento(UUID id, MultipartFile file, String actorUsername, String actorRole) {
    return service.uploadDocumento(id, file, actorUsername, actorRole);
  }

  public Vehiculo uploadLicencia(UUID id, MultipartFile file, String actorUsername, String actorRole) {
    return service.uploadLicencia(id, file, actorUsername, actorRole);
  }
}
