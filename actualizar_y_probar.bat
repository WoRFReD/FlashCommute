@echo off
setlocal EnableDelayedExpansion

:: ======================================================================
:: PLANTILLA MAESTRA DE COMPILACION, INSTALACION Y PUBLICACION FLASHCHIP
:: by WoRFReD - Canal: FlashChip (https://youtube.com/@flashchiped)
:: ======================================================================

:: Nombre oficial del proyecto
set "PROJECT_NAME=FlashCommute"

echo ====================================================
echo   COMPILANDO, INSTALANDO Y PUBLICANDO: !PROJECT_NAME!
echo   by WoRFReD - Canal: FlashChip
echo ====================================================

:: ------------------------------------------------------
:: 0. UBICACION DE HERRAMIENTAS COMPARTIDAS (SDK_Gradle)
:: ------------------------------------------------------
pushd "%~dp0.."
set "SHARED_TOOLS=%CD%\SDK_Gradle"
popd
if not exist "%SHARED_TOOLS%" if exist "Z:\DIGIstorage\APPs_Atigravity\SDK_Gradle" set "SHARED_TOOLS=Z:\DIGIstorage\APPs_Atigravity\SDK_Gradle"
if not exist "%SHARED_TOOLS%" mkdir "%SHARED_TOOLS%" 2>nul

:: ------------------------------------------------------
:: 1. COMPROBACION DE HERRAMIENTAS: GIT Y GITHUB CLI (gh)
:: ------------------------------------------------------
echo.
echo [INFO] Verificando herramientas de control de versiones...

:: Deteccion de Git
where git >nul 2>&1
if %errorlevel% neq 0 (
    if exist "%SHARED_TOOLS%\.git_bin\cmd\git.exe" set "PATH=%SHARED_TOOLS%\.git_bin\cmd;!PATH!"
    if exist "C:\Program Files\Git\cmd\git.exe" set "PATH=C:\Program Files\Git\cmd;!PATH!"
    if exist "C:\Program Files\Git\bin\git.exe" set "PATH=C:\Program Files\Git\bin;!PATH!"
    if exist "%LOCALAPPDATA%\Programs\Git\cmd\git.exe" set "PATH=%LOCALAPPDATA%\Programs\Git\cmd;!PATH!"
    where git >nul 2>&1
    if !errorlevel! neq 0 (
        echo [INFO] Descargando MinGit portable en SDK_Gradle...
        if not exist "%SHARED_TOOLS%\.git_bin" mkdir "%SHARED_TOOLS%\.git_bin"
        set "GIT_ZIP=%SHARED_TOOLS%\.git_bin\mingit.zip"
        set "GIT_URL=https://github.com/git-for-windows/git/releases/download/v2.44.0.windows.1/MinGit-2.44.0-64-bit.zip"
        curl -L -s -o "!GIT_ZIP!" "!GIT_URL!" || powershell -Command "$ProgressPreference = 'SilentlyContinue'; Invoke-WebRequest -Uri '!GIT_URL!' -OutFile '!GIT_ZIP!'"
        if exist "!GIT_ZIP!" (
            powershell -Command "Expand-Archive -Path '!GIT_ZIP!' -DestinationPath '%SHARED_TOOLS%\.git_bin' -Force"
            del /f /q "!GIT_ZIP!" >nul 2>&1
            if exist "%SHARED_TOOLS%\.git_bin\cmd\git.exe" set "PATH=%SHARED_TOOLS%\.git_bin\cmd;!PATH!"
        )
        where git >nul 2>&1
        if !errorlevel! neq 0 (
            echo.
            echo [ERROR] Git no esta instalado en el sistema.
            echo [INFO] Puedes instalar Git con: winget install --id Git.Git -e --source winget
            echo.
            pause
            exit /b 1
        )
    )
)
echo [OK] Git localizado en el sistema.

:: Deteccion de GitHub CLI (gh)
if exist "%SHARED_TOOLS%\.gh_bin\bin\gh.exe" set "PATH=%SHARED_TOOLS%\.gh_bin\bin;!PATH!"
if exist "C:\APPs_Atigravity\SDK_Gradle\.gh_bin\bin\gh.exe" set "PATH=C:\APPs_Atigravity\SDK_Gradle\.gh_bin\bin;!PATH!"
if exist "Z:\DIGIstorage\APPs_Atigravity\SDK_Gradle\.gh_bin\bin\gh.exe" set "PATH=Z:\DIGIstorage\APPs_Atigravity\SDK_Gradle\.gh_bin\bin;!PATH!"
if exist "C:\Program Files\GitHub CLI\gh.exe" set "PATH=C:\Program Files\GitHub CLI;!PATH!"
if exist "%LOCALAPPDATA%\Programs\GitHub CLI\bin\gh.exe" set "PATH=%LOCALAPPDATA%\Programs\GitHub CLI\bin;!PATH!"

where gh >nul 2>&1
if !errorlevel! neq 0 (
    echo [INFO] Descargando GitHub CLI portable en SDK_Gradle...
    if not exist "%SHARED_TOOLS%\.gh_bin" mkdir "%SHARED_TOOLS%\.gh_bin"
    set "GH_ZIP=%SHARED_TOOLS%\.gh_bin\gh.zip"
    set "GH_URL=https://github.com/cli/cli/releases/download/v2.58.0/gh_2.58.0_windows_amd64.zip"
    curl -L -s -o "!GH_ZIP!" "!GH_URL!" || powershell -Command "$ProgressPreference = 'SilentlyContinue'; Invoke-WebRequest -Uri '!GH_URL!' -OutFile '!GH_ZIP!'"
    if exist "!GH_ZIP!" (
        powershell -Command "Expand-Archive -Path '!GH_ZIP!' -DestinationPath '%SHARED_TOOLS%\.gh_temp' -Force"
        if exist "%SHARED_TOOLS%\.gh_temp\gh_2.58.0_windows_amd64\bin\gh.exe" (
            if not exist "%SHARED_TOOLS%\.gh_bin\bin" mkdir "%SHARED_TOOLS%\.gh_bin\bin"
            copy /y "%SHARED_TOOLS%\.gh_temp\gh_2.58.0_windows_amd64\bin\gh.exe" "%SHARED_TOOLS%\.gh_bin\bin\gh.exe" >nul
            rmdir /s /q "%SHARED_TOOLS%\.gh_temp" >nul 2>&1
        )
        del /f /q "!GH_ZIP!" >nul 2>&1
        if exist "%SHARED_TOOLS%\.gh_bin\bin\gh.exe" set "PATH=%SHARED_TOOLS%\.gh_bin\bin;!PATH!"
    )
    where gh >nul 2>&1
    if !errorlevel! neq 0 (
        echo.
        echo [AVISO] GitHub CLI [gh] no instalado. Se omitira la publicacion automatica en GitHub Releases.
        echo [INFO] Puedes instalarlo con: winget install --id GitHub.cli
    )
)

where gh >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] GitHub CLI [gh] localizado en el sistema.
    gh auth status >nul 2>&1
    if !errorlevel! neq 0 (
        echo [GITHUB] Iniciando login seguro en GitHub CLI...
        gh auth login
    ) else (
        echo [OK] Sesion activa verificada en GitHub.
    )
)

:: ------------------------------------------------------
:: 2. GESTION DE REPOSITORIO LOCAL Y REMOTO (.git / origin)
:: ------------------------------------------------------
echo.
echo [INFO] Verificando estado del repositorio Git...

if not exist ".git" (
    echo [INFO] No se encontro la carpeta .git. Inicializando repositorio local...
    git init
    git branch -M main
    git config user.name >nul 2>&1
    if !errorlevel! neq 0 (
        for /f "tokens=*" %%U in ('gh api user --jq .login 2^>nul') do set "GH_USER=%%U"
        if not defined GH_USER set "GH_USER=WoRFReD"
        git config user.name "!GH_USER!"
        git config user.email "!GH_USER!@users.noreply.github.com"
    )
    git add .
    git commit -m "Initial commit - !PROJECT_NAME! v1.00 by WoRFReD (FlashChip)"
    echo [OK] Repositorio Git local inicializado con la rama principal 'main'.
)

git remote get-url origin >nul 2>&1
if %errorlevel% neq 0 (
    echo [INFO] No existe el repositorio remoto 'origin'.
    where gh >nul 2>&1
    if !errorlevel! equ 0 (
        echo [INFO] Creando repositorio publico en GitHub con gh...
        gh repo create !PROJECT_NAME! --public --source=. --remote=origin --push
        if !errorlevel! neq 0 (
            for /f "tokens=*" %%U in ('gh api user --jq .login 2^>nul') do set "GH_USER=%%U"
            if defined GH_USER (
                echo [INFO] Vinculando a origin: https://github.com/!GH_USER!/!PROJECT_NAME!.git
                git remote add origin "https://github.com/!GH_USER!/!PROJECT_NAME!.git"
                git push -u origin main
            )
        )
    )
) else (
    echo [OK] Repositorio remoto 'origin' verificado.
)

:: ------------------------------------------------------
:: 3. AUTO-DETECCION DE JDK 17
:: ------------------------------------------------------
set "DETECTED_JDK="
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" set "DETECTED_JDK=%JAVA_HOME%"
if not defined DETECTED_JDK if exist "C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot" set "DETECTED_JDK=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"
if not defined DETECTED_JDK (
    for /d %%D in ("C:\Program Files\Eclipse Adoptium\jdk-17*") do set "DETECTED_JDK=%%D"
)
if not defined DETECTED_JDK (
    for /d %%D in ("C:\Program Files\Java\*17*") do set "DETECTED_JDK=%%D"
)
if not defined DETECTED_JDK if exist "C:\Program Files\Android\Android Studio\jbr" set "DETECTED_JDK=C:\Program Files\Android\Android Studio\jbr"

if defined DETECTED_JDK (
    set "JAVA_HOME=!DETECTED_JDK!"
    set "PATH=!JAVA_HOME!\bin;!PATH!"
    echo [OK] JDK asignado: !JAVA_HOME!
) else (
    echo [ERROR] No se encontro instalacion de JDK 17.
    pause
    exit /b 1
)

:: ------------------------------------------------------
:: 4. AUTO-DETECCION DEL SDK ANDROID
:: ------------------------------------------------------
echo.
echo [INFO] Verificando ubicacion del SDK de Android...

set "DETECTED_SDK="
if exist "C:\Android\Sdk\platforms\android-34" set "DETECTED_SDK=C:\Android\Sdk"
if not defined DETECTED_SDK if defined ANDROID_HOME if exist "%ANDROID_HOME%\platforms\android-34" set "DETECTED_SDK=%ANDROID_HOME%"
if not defined DETECTED_SDK if exist "%LOCALAPPDATA%\Android\Sdk\platforms\android-34" set "DETECTED_SDK=%LOCALAPPDATA%\Android\Sdk"
if not defined DETECTED_SDK if exist "%SHARED_TOOLS%\.sdk\platforms\android-34" set "DETECTED_SDK=%SHARED_TOOLS%\.sdk"

if defined DETECTED_SDK (
    set "ANDROID_HOME=!DETECTED_SDK!"
    set "PATH=!ANDROID_HOME!\platform-tools;!PATH!"
    echo [OK] SDK de Android localizado en: !ANDROID_HOME!
    set "ESCAPED_SDK=!DETECTED_SDK:\=/!"
    echo sdk.dir=!ESCAPED_SDK!> local.properties
    echo [OK] local.properties generado correctamente.
) else (
    echo [ERROR] No se encontro el SDK de Android [platforms\android-34].
    pause
    exit /b 1
)

:: ------------------------------------------------------
:: 5. GRADLE WRAPPER
:: ------------------------------------------------------
if not exist "gradlew.bat" (
    echo [INFO] Configurando Gradle Wrapper...
    if exist "%SHARED_TOOLS%\gradle_wrapper\gradlew.bat" (
        copy /y "%SHARED_TOOLS%\gradle_wrapper\gradlew.bat" "gradlew.bat" >nul
        copy /y "%SHARED_TOOLS%\gradle_wrapper\gradlew" "gradlew" >nul
        copy /y "%SHARED_TOOLS%\gradle_wrapper\gradle-wrapper.jar" "gradle\wrapper\gradle-wrapper.jar" >nul
        copy /y "%SHARED_TOOLS%\gradle_wrapper\gradle-wrapper.properties" "gradle\wrapper\gradle-wrapper.properties" >nul
    )
)

:: ------------------------------------------------------
:: 6. EXTRACCION DINAMICA DE VERSION
:: ------------------------------------------------------
set "APP_VERSION="
for /f "tokens=2 delims==" %%V in ('findstr /i "versionName" app\build.gradle.kts') do (
    set "RAW_VER=%%~V"
    set "APP_VERSION=!RAW_VER:"=!"
    set "APP_VERSION=!APP_VERSION: =!"
)
if not defined APP_VERSION set "APP_VERSION=1.00"
set "TAG=v!APP_VERSION!"

echo.
echo ====================================================
echo [VERSION DETECTADA]: !PROJECT_NAME! !TAG!
echo ====================================================

:: ------------------------------------------------------
:: 7. COMPILACION RELEASE CON GRADLE (APK OFICIAL FIRMADA)
:: ------------------------------------------------------
echo.
echo [1/4] Compilando APK Release oficial con Gradle (!TAG!)...

:: Asegurar que existe el keystore maestro de FlashChip
if not exist "%~dp0flashchip-master.jks" (
    if exist "Z:\DIGIstorage\APPs_Atigravity\Reglas_APKs\flashchip-master.jks" (
        copy /y "Z:\DIGIstorage\APPs_Atigravity\Reglas_APKs\flashchip-master.jks" "%~dp0flashchip-master.jks" >nul
    ) else if exist "C:\APPs_Atigravity\APK_installer_Monitor\flashchip-master.jks" (
        copy /y "C:\APPs_Atigravity\APK_installer_Monitor\flashchip-master.jks" "%~dp0flashchip-master.jks" >nul
    ) else (
        echo [INFO] Creando certificado de firma Release para FlashChip...
        "%JAVA_HOME%\bin\keytool.exe" -genkeypair -v -keystore "%~dp0flashchip-master.jks" -storepass "flashchippass" -alias "flashchip" -keypass "flashchippass" -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=WoRFReD, OU=FlashChip, O=FlashChip, L=Madrid, ST=Madrid, C=ES" >nul 2>&1
    )
)

call gradlew.bat assembleRelease

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] La compilacion ha fallado. Revisa el codigo Kotlin o dependencias.
    pause
    exit /b %errorlevel%
)

set "ORIGINAL_APK=%~dp0app\build\outputs\apk\release\app-release.apk"
set "RELEASE_APK=%~dp0!PROJECT_NAME!.apk"

if not exist "%ORIGINAL_APK%" (
    echo [ERROR] No se encontro el archivo APK compilado en: %ORIGINAL_APK%
    pause
    exit /b 1
)

:: Copiar y renombrar APK oficial Release
copy /y "%ORIGINAL_APK%" "%RELEASE_APK%" >nul
echo.
echo ====================================================
echo [OK] APK RELEASE OFICIAL CREADA CON EXITO EN:
echo %RELEASE_APK%
echo ====================================================

:: ------------------------------------------------------
:: 8. DESPLIEGUE POR ADB (SI HAY DISPOSITIVO O TABLET CONECTADO)
:: ------------------------------------------------------
echo.
echo [2/4] Comprobando conexion USB por ADB...
adb devices | findstr /V "List of devices attached" | findstr "device" >nul 2>&1
if %errorlevel% neq 0 (
    echo [AVISO] No se detecto ninguna tablet o movil por ADB. Omitiendo instalacion local...
) else (
    echo [INFO] Instalando APK en la tablet/movil por ADB...
    adb install -r "%RELEASE_APK%"
    if !errorlevel! equ 0 (
        echo [OK] Aplicacion instalada correctamente en el dispositivo.
    ) else (
        echo [AVISO] Fallo la instalacion por ADB. Continuando con la publicacion...
    )
)

:: ------------------------------------------------------
:: 9. SUBIDA A GIT (COMMIT Y PUSH)
:: ------------------------------------------------------
echo.
echo [3/4] Sincronizando cambios con Git...

git add .
git diff --staged --quiet >nul 2>&1
if %errorlevel% neq 0 (
    echo [INFO] Creando commit para la version !TAG!...
    git commit -m "Actualizacion version !TAG! - !PROJECT_NAME! by WoRFReD (FlashChip)"
) else (
    echo [INFO] No hay cambios de codigo pendientes de commit.
)

git push -u origin main >nul 2>&1
if %errorlevel% neq 0 (
    echo [AVISO] No se pudo hacer push directo a origin main. Revisa conexion o credenciales.
) else (
    echo [OK] Codigo fuente sincronizado en GitHub.
)

:: ------------------------------------------------------
:: 10. PUBLICACION DE RELEASE OTA EN GITHUB CLI
:: ------------------------------------------------------
echo.
echo [4/4] Publicando Release OTA oficial en GitHub (!TAG!)...

where gh >nul 2>&1
if %errorlevel% equ 0 (
    gh release view !TAG! >nul 2>&1
    if !errorlevel! equ 0 (
        echo [INFO] La release !TAG! ya existe en GitHub. Actualizando archivo !PROJECT_NAME!.apk...
        gh release upload !TAG! "!PROJECT_NAME!.apk" --clobber
    ) else (
        echo [INFO] Creando release !TAG! y adjuntando !PROJECT_NAME!.apk...
        gh release create !TAG! "!PROJECT_NAME!.apk" --title "!PROJECT_NAME! !TAG!" --notes "Release oficial de !PROJECT_NAME! !TAG! por WoRFReD - Canal: FlashChip. Instalador !PROJECT_NAME!.apk listo para descargar e instalar."
    )

    if !errorlevel! equ 0 (
        echo.
        echo ====================================================
        echo   [OK] RELEASE OTA PUBLICADA CON EXITO EN GITHUB
        echo   Tag: !TAG!
        echo   Archivo: !PROJECT_NAME!.apk
        echo ====================================================
    ) else (
        echo [AVISO] No se pudo completar la release automatica en GitHub.
    )
) else (
    echo [INFO] GitHub CLI no disponible, release omitida.
)

:: ------------------------------------------------------
:: 11. RESPALDO AUTOMATICO LIMPIO EN ALMACENAMIENTO Z: (RaiDrive / WebDAV Optimizado)
:: ------------------------------------------------------
if not exist "Z:" (
    echo.
    echo [AVISO] La unidad Z: no esta montada o RaiDrive no esta ejecutandose. Omitiendo copia a Z:...
) else (
    set "DEST_Z=Z:\DIGIstorage\APPs_Atigravity\!PROJECT_NAME!"
    if not exist "!DEST_Z!" mkdir "!DEST_Z!" 2>nul
    echo.
    echo [INFO] Sincronizando respaldo rapido en Z: (!DEST_Z!)...
    robocopy "%~dp0." "!DEST_Z!" /E /COPY:D /NODCOPY /MT:16 /FFT /XO /R:0 /W:0 /NP /XD .gradle build app\build .idea .cxx captures /XF local.properties *.iml >nul 2>&1
    echo [OK] Copia de fuentes y APK sincronizada limpiamente en DIGI Storage (Z:).
)

echo.
echo ====================================================
echo   Proceso completado - !PROJECT_NAME! !TAG!
echo   by WoRFReD - Canal: FlashChip (https://youtube.com/@flashchiped)
echo ====================================================
pause
