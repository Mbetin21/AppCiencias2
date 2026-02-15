@echo off
echo ===============================================
echo   Ciencias de la Computacion 2
echo   Compilando y ejecutando la aplicacion...
echo ===============================================
echo.

cd /d "%~dp0AppCiencias2"

echo [1/2] Compilando archivos Java...
javac -d bin src\com\appciencias\models\*.java src\com\appciencias\algorithms\*.java src\com\appciencias\views\*.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: La compilacion ha fallado.
    echo Revise los errores mostrados arriba.
    pause
    exit /b 1
)

echo [2/2] Ejecutando la aplicacion...
echo.
java -cp bin com.appciencias.views.MainWindow

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: La aplicacion ha terminado con errores.
    pause
    exit /b 1
)
