@echo off
setlocal enabledelayedexpansion

title Package Chinese Chess

set "DIST_DIR=%~dp0dist"
set "JAR_NAME=openUcciChineseChess-0.0.1-SNAPSHOT-jar-with-dependencies.jar"
set "PROJECT_DIR=%~dp0"

echo ============================================
echo  Chinese Chess v0.0.1 - Package Script
echo ============================================
echo.

:: Step 1: Maven build
echo [1/5] Running Maven build...
cd /d "%PROJECT_DIR%"
call mvn clean package -DskipTests
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Maven build failed, error code: %ERRORLEVEL%
    pause
    exit /b 1
)
echo [OK] Maven build succeeded
echo.

:: Step 2: Clean and create dist directory
echo [2/5] Creating output directory...
if exist "%DIST_DIR%" (
    rmdir /s /q "%DIST_DIR%"
)
mkdir "%DIST_DIR%"
mkdir "%DIST_DIR%\engines"
echo [OK] Output directory created
echo.

:: Step 3: Copy JAR
echo [3/5] Copying JAR...
copy /y "%PROJECT_DIR%target\%JAR_NAME%" "%DIST_DIR%\" >nul
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Failed to copy JAR
    pause
    exit /b 1
)
echo [OK] JAR copied
echo.

:: Step 4: Copy external resources (engines, images, config)
echo [4/5] Copying external resources...

if exist "%PROJECT_DIR%engines\ElephantEye" (
    xcopy /e /i /q "%PROJECT_DIR%engines\ElephantEye" "%DIST_DIR%\engines\ElephantEye" >nul
    echo [OK] Engine copied
) else (
    echo [SKIP] Engine directory not found
)

if exist "%PROJECT_DIR%pic" (
    xcopy /e /i /q "%PROJECT_DIR%pic" "%DIST_DIR%\pic" >nul
    echo [OK] Images copied
) else (
    echo [SKIP] Image directory not found
)

if exist "%PROJECT_DIR%chessConfig.ini" (
    copy /y "%PROJECT_DIR%chessConfig.ini" "%DIST_DIR%\" >nul
    echo [OK] Config copied
) else (
    echo [SKIP] Config file not found
)

echo.

:: Step 5: Generate run scripts
echo [5/5] Generating run scripts...

del "%DIST_DIR%\run_single.bat" "%DIST_DIR%\run_multi.bat" 2>nul

:: Write run_single.bat
(
echo @echo off
echo chcp 65001 ^>nul
echo title Chinese Chess
echo java -jar "%%~dp0%JAR_NAME%"
echo pause
) > "%DIST_DIR%\run_single.bat"

:: Write run_multi.bat (two instances for network play)
(
echo @echo off
echo chcp 65001 ^>nul
echo title Chinese Chess - Two Players
echo echo Starting Player 1...
echo start "Player1" java -jar "%%~dp0%JAR_NAME%"
echo timeout /t 2 /nobreak ^>nul
echo echo Starting Player 2...
echo start "Player2" java -jar "%%~dp0%JAR_NAME%"
echo echo.
echo echo Both instances started. Start local server in Player 1 window,
echo echo then click Network - Join Server in Player 2 window.
echo pause
) > "%DIST_DIR%\run_multi.bat"

echo [OK] Run scripts created
echo.
echo ============================================
echo  Package complete!
echo.
echo  Output: %DIST_DIR%
echo.
echo  How to run:
echo    run_single.bat  - Single player
echo    run_multi.bat   - Two players (network)
echo ============================================
echo.

pause
