@echo off
setlocal EnableDelayedExpansion

:: Nombre oficial del proyecto
set "PROJECT_NAME=FlashCommute"

echo ====================================================
echo   VERIFICADOR GOOGLE PLAY PROTECT - FLASHCHIP
echo   by WoRFReD - Canal: FlashChip
echo ====================================================
echo.

set "CERT_SHA256=CB:F2:50:E0:92:11:E1:8C:A8:68:44:41:56:4B:C3:3A:19:BA:B0:DE:14:1F:87:B1:E0:99:93:A5:7C:56:7B:1D"
set "APK_FILE=!PROJECT_NAME!.apk"
set "APK_URL=https://github.com/WoRFReD/!PROJECT_NAME!/releases/latest/download/!APK_FILE!"

echo [1/3] Datos de identidad del Desarrollador:
echo ----------------------------------------------------
echo Titular:        WoRFReD - Canal FlashChip
echo Proyecto:       %PROJECT_NAME%
echo Enlace APK:     %APK_URL%
echo Huella SHA-256: %CERT_SHA256%
echo ----------------------------------------------------
echo.

if not exist "!APK_FILE!" (
    echo [AVISO] !APK_FILE! no encontrada en la carpeta.
    echo Compila primero con actualizar_y_probar.bat.
    echo.
    goto :copiar_portapapeles
)

echo [2/3] Calculando Hash SHA-256 de !APK_FILE!...
for /f "skip=1 tokens=*" %%A in ('certutil -hashfile "!APK_FILE!" SHA256 ^| findstr /v "CertUtil"') do (
    if not defined APK_HASH (
        set "APK_HASH=%%A"
        set "APK_HASH=!APK_HASH: =!"
    )
)
echo Hash APK: !APK_HASH!
echo.

:copiar_portapapeles
echo [3/3] Copiando la huella digital SHA-256 al portapapeles...
<nul set /p "=%CERT_SHA256%" | clip
echo [OK] Huella SHA-256 copiada al portapapeles con exito.
echo.

echo ====================================================
echo   INSTRUCCIONES PARA GOOGLE PLAY PROTECT:
echo   * Se abrira el formulario oficial de apelaciones de Google.
echo   * Pega la huella SHA-256 con Ctrl+V (ya esta en tu portapapeles).
echo   * URL de descarga de la APK:
echo     %APK_URL%
echo   * En comentarios: Indica que es una aplicacion de utilidad
echo     de codigo abierto desarrollada por WoRFReD y sin malware.
echo ====================================================
echo.
pause

start https://support.google.com/googleplay/android-developer/contact/protectappeals
exit /b 0
