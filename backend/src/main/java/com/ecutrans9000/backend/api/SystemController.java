package com.ecutrans9000.backend.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system")
@Tag(name = "System", description = "Endpoints de estado del sistema")
public class SystemController {

  @GetMapping("/health")
  @Operation(summary = "Estado del servicio")
  public Map<String, String> health() {
    return Map.of("status", "UP", "service", "ecutrans9000-backend");
  }
}
