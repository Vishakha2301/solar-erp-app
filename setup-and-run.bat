@echo off
REM Solar ERP Application Setup Script
REM This script helps you quickly set up and run the Solar ERP application

setlocal enabledelayedexpansion

echo.
echo ======================================
echo Solar ERP - Quick Setup Script
echo ======================================
echo.

REM Check if Docker is installed
docker --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker is not installed. Please install Docker Desktop.
    echo Visit: https://www.docker.com/products/docker-desktop
    exit /b 1
)

echo [OK] Docker found:
docker --version

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java is not installed. Please install Java 21 or later.
    exit /b 1
)

echo [OK] Java found:
java -version

echo.
echo ======================================
echo Step 1: Starting PostgreSQL Database
echo ======================================
echo.

cd /d "%~dp0solar-erp-app"

echo Starting PostgreSQL container...
docker-compose up -d

if errorlevel 1 (
    echo [ERROR] Failed to start PostgreSQL
    exit /b 1
)

echo [OK] PostgreSQL is running
echo.

REM Wait for PostgreSQL to be ready
echo Waiting for PostgreSQL to be ready (10 seconds)...
timeout /t 10

echo.
echo ======================================
echo Step 2: Building Spring Boot Application
echo ======================================
echo.

cd /d "%~dp0"

echo Building the project with Maven...
call mvn clean install -DskipTests

if errorlevel 1 (
    echo [ERROR] Maven build failed
    exit /b 1
)

echo [OK] Build completed successfully

echo.
echo ======================================
echo Step 3: Running Application
echo ======================================
echo.

echo Starting Spring Boot application...
echo The application will be available at: http://localhost:8080
echo.

call mvn spring-boot:run -pl solar-erp-app

endlocal

