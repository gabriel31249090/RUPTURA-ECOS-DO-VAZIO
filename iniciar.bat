@echo off
cd /d "%~dp0"
if exist "lwjgl3\build\install\Ruptura\bin\Ruptura.bat" (
    call "lwjgl3\build\install\Ruptura\bin\Ruptura.bat"
) else (
    call gradlew.bat lwjgl3:run
)
