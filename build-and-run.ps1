#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Script para compilar, empaquetar e iniciar el servicio Product API
.DESCRIPTION
    Automatiza el ciclo de desarrollo: compilar, tests, empaquetar e iniciar
.EXAMPLE
    .\build-and-run.ps1
    .\build-and-run.ps1 -SkipTests
    .\build-and-run.ps1 -OnlyRun
#>

param(
    [switch]$SkipTests,
    [switch]$OnlyRun,
    [switch]$OnlyBuild,
    [int]$Port = 8080,
    [string]$DbHost = "localhost",
    [string]$DbPort = "5432",
    [string]$DbName = "product_api",
    [string]$DbUsername = "postgres",
    [string]$DbPassword = "MartinDesarrollo28"
)

$ErrorActionPreference = "Stop"
$WarningPreference = "Continue"

$MAVEN_CMD = ".mvn\wrapper\apache-maven-3.9.11\bin\mvn.cmd"
$JAR_FILE = "target\product-api-0.0.1-SNAPSHOT.jar"

function Write-Header {
    param([string]$Message)
    Write-Host "`n" -NoNewline
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host $Message -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Cyan
}

function Write-Info {
    param([string]$Message)
    Write-Host "✓ $Message" -ForegroundColor Green
}

function Write-Error-Custom {
    param([string]$Message)
    Write-Host "✗ $Message" -ForegroundColor Red
}

function Test-Maven {
    if (-not (Test-Path $MAVEN_CMD)) {
        Write-Error-Custom "Maven no encontrado en: $MAVEN_CMD"
        exit 1
    }
    Write-Info "Maven encontrado"
}

function Test-Jar {
    if (-not (Test-Path $JAR_FILE)) {
        Write-Error-Custom "JAR no encontrado. Ejecuta: .\build-and-run.ps1 -OnlyBuild"
        exit 1
    }
    Write-Info "JAR encontrado: $JAR_FILE"
}

function Build-Project {
    Write-Header "COMPILANDO PROYECTO"
    
    if ($SkipTests) {
        Write-Host "Saltando tests..." -ForegroundColor Yellow
        & $MAVEN_CMD clean package -DskipTests -q
    } else {
        Write-Host "Ejecutando tests..." -ForegroundColor Yellow
        & $MAVEN_CMD clean package -q
    }
    
    if ($LASTEXITCODE -eq 0) {
        Write-Info "Compilación exitosa"
        Test-Jar
    } else {
        Write-Error-Custom "Error en la compilación. Ejecuta: $MAVEN_CMD clean compile"
        exit 1
    }
}

function Run-Service {
    Write-Header "INICIANDO SERVICIO"
    
    # Establecer variables de entorno
    $env:SERVER_PORT = $Port
    $env:DB_HOST = $DbHost
    $env:DB_PORT = $DbPort
    $env:DB_NAME = $DbName
    $env:DB_USERNAME = $DbUsername
    $env:DB_PASSWORD = $DbPassword
    
    Write-Info "Configuración:"
    Write-Host "  Puerto: $Port" -ForegroundColor Gray
    Write-Host "  BD Host: $DbHost" -ForegroundColor Gray
    Write-Host "  BD Puerto: $DbPort" -ForegroundColor Gray
    Write-Host "  BD Nombre: $DbName" -ForegroundColor Gray
    Write-Host "  BD Usuario: $DbUsername" -ForegroundColor Gray
    Write-Host ""
    
    Write-Host "Iniciando Java..." -ForegroundColor Yellow
    & java -jar $JAR_FILE
}

function Show-Help {
    Write-Host @"
USO: .\build-and-run.ps1 [opciones]

OPCIONES:
  -SkipTests          Saltar ejecución de tests
  -OnlyRun            Solo iniciar el servicio (sin compilar)
  -OnlyBuild          Solo compilar (sin iniciar)
  -Port <número>      Puerto del servidor (default: 8080)
  -DbHost <host>      Host de la base de datos (default: localhost)
  -DbPort <puerto>    Puerto de la BD (default: 5432)
  -DbName <nombre>    Nombre de la BD (default: product_api)
  -DbUsername <user>  Usuario de BD (default: postgres)
  -DbPassword <pass>  Contraseña de BD

EJEMPLOS:
  # Compilar, tests y ejecutar
  .\build-and-run.ps1

  # Compilar sin tests y ejecutar
  .\build-and-run.ps1 -SkipTests

  # Solo iniciar (sin compilar)
  .\build-and-run.ps1 -OnlyRun

  # Solo compilar
  .\build-and-run.ps1 -OnlyBuild

  # Con BD personalizada
  .\build-and-run.ps1 -DbHost "192.168.1.100" -DbPort "5432"

  # Puerto personalizado
  .\build-and-run.ps1 -Port 9090
"@
}

# MAIN
Clear-Host
Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║         Product API - Build & Run Script                   ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan

Test-Maven

if ($OnlyRun) {
    Write-Header "MODO: SOLO EJECUTAR"
    Test-Jar
    Run-Service
} elseif ($OnlyBuild) {
    Write-Header "MODO: SOLO COMPILAR"
    Build-Project
    Write-Host "`n✓ Compilación completada. JAR disponible en: $JAR_FILE" -ForegroundColor Green
} else {
    Write-Header "MODO: COMPILAR Y EJECUTAR"
    Build-Project
    Run-Service
}
