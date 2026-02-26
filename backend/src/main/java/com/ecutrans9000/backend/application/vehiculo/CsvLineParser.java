package com.ecutrans9000.backend.application.vehiculo;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser liviano de líneas CSV con soporte de comillas escapadas.
 */
public final class CsvLineParser {

  private CsvLineParser() {
  }

  /**
   * Convierte una línea CSV en columnas usando el delimitador indicado.
   */
  public static List<String> parse(String line, char delimiter) {
    List<String> fields = new ArrayList<>();
    StringBuilder current = new StringBuilder();
    boolean inQuotes = false;

    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);

      if (c == '"') {
        if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
          current.append('"');
          i++;
        } else {
          inQuotes = !inQuotes;
        }
        continue;
      }

      if (c == delimiter && !inQuotes) {
        fields.add(current.toString().trim());
        current.setLength(0);
        continue;
      }

      current.append(c);
    }

    fields.add(current.toString().trim());
    return fields;
  }
}
