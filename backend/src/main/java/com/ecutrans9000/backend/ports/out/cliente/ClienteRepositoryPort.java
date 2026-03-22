package com.ecutrans9000.backend.ports.out.cliente;

import com.ecutrans9000.backend.domain.cliente.Cliente;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;

/**
 * Puerto de salida para persistencia del módulo clientes.
 */
public interface ClienteRepositoryPort {

  Cliente save(Cliente cliente);

  Optional<Cliente> findById(UUID id);

  Optional<Cliente> findByDocumentoNorm(String documentoNorm);

  boolean existsByDocumentoNorm(String documentoNorm);

  boolean existsByDocumentoNormAndIdNot(String documentoNorm, UUID id);

  Page<Cliente> search(int page, int size, String q, Boolean includeDeleted);

  void deleteById(UUID id);
}
