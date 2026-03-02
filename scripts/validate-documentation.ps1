$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot

function Test-Javadocs {
  $javaRoot = Join-Path $repoRoot "backend/src/main/java/com/ecutrans9000/backend"
  $files = rg --files $javaRoot | Where-Object { $_ -like "*.java" }
  $missing = @()

  foreach ($file in $files) {
    $lines = Get-Content $file
    for ($i = 0; $i -lt $lines.Count; $i++) {
      if ($lines[$i] -match "^\s*public\s+(class|interface|enum|record)\s+") {
        $j = $i - 1
        while ($j -ge 0 -and ($lines[$j].Trim() -eq "" -or $lines[$j].Trim().StartsWith("@"))) {
          $j--
        }
        if ($j -lt 0 -or $lines[$j].Trim() -notmatch "^\*/") {
          $missing += "${file}:$($i + 1)"
        }
      }
    }
  }

  return $missing
}

function Test-Postman {
  $postmanPath = Join-Path $repoRoot "postman/EcuTrans9000.postman_collection.json"
  if (-not (Test-Path $postmanPath)) {
    return @{
      MissingFile = $true
      MissingVariables = @()
      MissingEndpoints = @()
    }
  }

  $json = Get-Content -Raw $postmanPath | ConvertFrom-Json
  $requiredVariables = @("baseUrl", "token", "targetUserId", "targetVehiculoId", "targetBitacoraId")
  $existingVariables = @($json.variable | ForEach-Object { $_.key })
  $missingVariables = $requiredVariables | Where-Object { $_ -notin $existingVariables }

  $rawContent = Get-Content -Raw $postmanPath
  $requiredEndpoints = @(
    "/auth/login",
    "/dashboard",
    "/api/system/health",
    "/users",
    "/users/me",
    "/api/vehiculos",
    "/api/vehiculos/import/template",
    "/api/vehiculos/import/preview",
    "/api/vehiculos/import",
    "/api/bitacora/viajes",
    "/api/bitacora/viajes/export",
    "/api/bitacora/viajes/import/template",
    "/api/bitacora/viajes/import/template/example",
    "/api/bitacora/viajes/import/preview",
    "/api/bitacora/viajes/import"
  )
  $missingEndpoints = $requiredEndpoints | Where-Object { $rawContent -notmatch [regex]::Escape($_) }

  return @{
    MissingFile = $false
    MissingVariables = $missingVariables
    MissingEndpoints = $missingEndpoints
  }
}

function Test-Readme {
  $readmePath = Join-Path $repoRoot "README.md"
  if (-not (Test-Path $readmePath)) {
    return @{
      MissingFile = $true
      MissingTopics = @()
    }
  }

  $content = Get-Content -Raw $readmePath
  $requiredTopics = @(
    "Swagger UI",
    "OpenAPI JSON",
    "Variables de entorno",
    "Endpoints principales",
    "Módulo Clientes",
    "Soft delete y auditoría",
    "Colección Postman"
  )
  $missingTopics = $requiredTopics | Where-Object { $content -notmatch [regex]::Escape($_) }

  return @{
    MissingFile = $false
    MissingTopics = $missingTopics
  }
}

$javadocsMissing = Test-Javadocs
$postmanStatus = Test-Postman
$readmeStatus = Test-Readme

Write-Output "=== VALIDACION DE DOCUMENTACION ==="
Write-Output "Fecha: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
Write-Output ""

Write-Output "[JAVADOCS]"
Write-Output "Tipos publicos sin Javadoc: $($javadocsMissing.Count)"
if ($javadocsMissing.Count -gt 0) {
  $javadocsMissing | Select-Object -First 20 | ForEach-Object { Write-Output " - $_" }
  if ($javadocsMissing.Count -gt 20) {
    Write-Output " - ... y $($javadocsMissing.Count - 20) mas"
  }
}
Write-Output ""

Write-Output "[POSTMAN]"
if ($postmanStatus.MissingFile) {
  Write-Output "Falta el archivo de coleccion Postman."
} else {
  Write-Output "Variables faltantes: $($postmanStatus.MissingVariables.Count)"
  $postmanStatus.MissingVariables | ForEach-Object { Write-Output " - $_" }
  Write-Output "Endpoints faltantes: $($postmanStatus.MissingEndpoints.Count)"
  $postmanStatus.MissingEndpoints | ForEach-Object { Write-Output " - $_" }
}
Write-Output ""

Write-Output "[README]"
if ($readmeStatus.MissingFile) {
  Write-Output "Falta README.md"
} else {
  Write-Output "Temas faltantes: $($readmeStatus.MissingTopics.Count)"
  $readmeStatus.MissingTopics | ForEach-Object { Write-Output " - $_" }
}

$hasErrors =
  ($javadocsMissing.Count -gt 0) -or
  $postmanStatus.MissingFile -or
  ($postmanStatus.MissingVariables.Count -gt 0) -or
  ($postmanStatus.MissingEndpoints.Count -gt 0) -or
  $readmeStatus.MissingFile -or
  ($readmeStatus.MissingTopics.Count -gt 0)

if ($hasErrors) {
  exit 1
}

exit 0
