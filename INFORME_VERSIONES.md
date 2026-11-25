# 📋 INFORME DE VERSIONES - FlashCommute
> **Proyecto:** FlashCommute  
> **Autor:** WoRFReD • **Canal:** FlashChip (https://youtube.com/@flashchiped)  
> **Package:** `com.flashchip.flashcommute`

---

## [1.06] - Permiso en Segundo Plano, Despertar de Pantalla y Red de Garaje
- **Fecha:** 25/11/2026
- **VersionCode:** `106`
- **VersionName:** `1.06`
- **Estado:** 🟢 Estable / Producción

### 🟢 [NUEVA_FUNCION]
- **Gestión Avanzada de Permisos en Segundo Plano:** Nuevo flujo guiado de permisos para solicitar "Permitir todo el tiempo" (`ACCESS_BACKGROUND_LOCATION`) necesario en Android 10, 11, 12, 13 y 14 para detectar el coche con la pantalla bloqueada o en el bolsillo.
- **Despertar Automático de Pantalla (WakeLock):** Al conectarse al Bluetooth del coche y completar el análisis de tráfico, la pantalla del móvil se enciende automáticamente durante 8 segundos. Permite ver la notificación de un vistazo en el soporte/trípode del coche sin tocar el móvil.
- **Notificaciones Visibles por Código de Color:**
  - 🟢 **Verde (Fluido):** Alerta en pantalla de bloqueo con duración, hora de llegada y tono suave.
  - 🟡 **Amarillo (Moderado):** Alerta en pantalla de bloqueo con demora añadida y tono informativo.
  - 🔴 **Rojo (Crítico / Alarma):** Prioridad máxima con sonido de alarma sonoro y vibración prolongada.

### 🔴 [CORRECCION]
- **Rescate de Cobertura en Garajes Subterráneos:** Eliminado el límite que descartaba ubicaciones antiguas. Implementada memoria persistente en `SettingsManager` que conserva la última posición válida registrada (por ejemplo, al caminar por la calle hacia el coche) para usarla de partida inmediata si los satélites no llegan bajo techo.
- **Desbloqueo del Botón Manual:** Corregido el bloqueo en "Consultando Tráfico en Vivo..." de la pantalla principal con `LaunchedEffect` reactivo que se auto-desbloquea en cuanto llega el reporte o tras un tiempo prudencial.

---

## [1.05] - Gestión del Botón Atrás Nativo (BackHandler)
- **Fecha:** 24/11/2026
- **VersionCode:** `105`
- **VersionName:** `1.05`
- **Estado:** 🟢 Estable / Producción

### 🟢 [NUEVA_FUNCION]
- **Navegación Fluida con Botón Físico / Gesto Atrás:** Implementado `BackHandler` nativo de Jetpack Compose en `MainActivity`. Al estar en Ajustes o en la Guía de APIs, pulsar el botón Atrás del móvil regresa suavemente a la pantalla principal sin cerrar la aplicación.

---

## [1.04] - Origen 100% Dinámico en Tiempo Real (Sin punto fijo)
- **Fecha:** 24/11/2026
- **VersionCode:** `104`
- **VersionName:** `1.04`
- **Estado:** 🟢 Estable / Producción

### 🔵 [OPTIMIZACION]
- **Eliminación del Punto de Salida Prefijado:** Eliminada la tarjeta de configuración de Casa/Salida en Ajustes. La app ahora calcula siempre y únicamente desde las coordenadas GPS en tiempo real de donde se encuentre el usuario al arrancar el coche.
- **Simplificación de Ajustes:** Pantalla de configuración más limpia, directa y enfocada exclusivamente en el destino del trabajo, Bluetooth y umbrales.

---

## [1.03] - Ubicación de Salida/Casa y Localización Instantánea en Interiores/Garajes
- **Fecha:** 24/11/2026
- **VersionCode:** `103`
- **VersionName:** `1.03`
- **Estado:** 🟢 Estable / Producción

### 🔴 [CORRECCION]
- **Eliminación del Origen por Defecto Erróneo (Madrid):** Solucionado el problema que calculaba la ruta desde Madrid (536 km y 280 min) cuando el GPS tardaba en conectar en interiores o garajes.
- **Multilocalizador GPS + Red:** Combinación de `FusedLocationProviderClient` con `LocationManager` (Network y GPS) con resolución en milisegundos mediante Wi-Fi y antenas celulares.

### 🟢 [NUEVA_FUNCION]
- **Ubicación de Salida Habitual / Casa Configurable:** Posibilidad de fijar la dirección de salida o casa en Ajustes mediante GPS o enlace de Google Maps. Si el GPS aún no tiene cobertura satelital al encender el coche, se usa esta ubicación exacta en lugar de fallar.
- **Información del Origen en Pantalla Principal:** El HUD ahora muestra claramente el punto de partida (GPS en vivo o Casa) y la distancia real en kilómetros.

---

## [1.02] - Corrección Extracción Enlaces Google Maps y Caja de Pegado Directo
- **Fecha:** 24/11/2026
- **VersionCode:** `102`
- **VersionName:** `1.02`
- **Estado:** 🟢 Estable / Producción

### 🔴 [CORRECCION]
- **Soporte Completo de Enlaces Google Maps (Rutas y Destinos):** Corrección para enlaces cortos (`maps.app.goo.gl`) con formato de ruta (`/dir/.../data=!3d...!4d...`), extrayendo las coordenadas exactas de destino y el nombre del lugar automáticamente.

### 🟡 [INTERFAZ]
- **Caja de Texto para Pegar o Ver Enlace:** Campo visual integrado en Ajustes para pegar o escribir el enlace directamente con botón "Aplicar Enlace", permitiendo ver la URL y evitando problemas con el portapapeles.

---

## [1.01] - Mejoras de UX, Google Maps y Botones de API
- **Fecha:** 24/11/2026
- **VersionCode:** `101`
- **VersionName:** `1.01`
- **Estado:** 🟢 Estable / Producción

### 🟢 [NUEVA_FUNCION]
- **Asignación de Ubicación con Google Maps:** Botones integrados para abrir Google Maps y pegar el enlace o dirección copiado, extrayendo las coordenadas GPS exactas automáticamente.
- **Aviso Interactivo Sin Claves API:** Diálogo explicativo al pulsar "Comprobar Ruta Ahora" si no hay clave configurada, con acceso directo a Ajustes.
- **Feedback Dinámico por Color del Tiempo de Ruta:** Presentación clara del tiempo restante en verde (fluido), amarillo (moderado) o rojo (retraso crítico >= umbral).

### 🟡 [INTERFAZ]
- **Botones de Prueba de API Unificados:** Botones grandes y visibles en color Cian Neón uniforme, con estado inactivo en gris cuando el campo está vacío y activo al introducir la clave.
- Mensajes visuales con tarjeta de confirmación del estado de conexión tras la prueba.

---

## [1.00] - Versión Inicial Oficial (Release Inicial)
- **Fecha:** 24/11/2026
- **VersionCode:** `100`
- **VersionName:** `1.00`
- **Estado:** 🟢 Estable / Producción

### 🟢 [NUEVA_FUNCION]
- **Detección Automática por Bluetooth del Coche:** Receptor en segundo plano que detecta la conexión con el dispositivo Bluetooth del vehículo configurado.
- **Ventana Horaria Inteligente:** Comprobación condicional personalizable por el usuario (días laborables y franja horaria configurable, ej. 06:00 a 09:00).
- **Rutas y Tráfico en Tiempo Real Dual (TomTom + Google Routes):**
  - Soporte integrado para TomTom Routing & Traffic API (incidentes, atascos, obras, accidentes).
  - Soporte para Google Routes API con cálculo de congestión en vivo.
  - Modo Híbrido inteligente con fallback automático.
- **Sistema de Alertas Dinámicas:**
  - Si el tráfico es fluido: Notificación sutil y limpia en la barra de estado indicando tiempo y hora estimada de llegada.
  - Si hay retraso crítico (umbral ajustable, por defecto >= 7 min) o accidentes/cortes: Notificación prioritaria (`PRIORITY_MAX`) con sonido de aviso y visibilidad en pantalla de bloqueo.
- **Selector de Destino y Coordenadas:** Configuración rápida del destino habitual (dirección o coordenadas GPS del trabajo).
- **Guía y Tutorial de APIs Gratis Integrado:** Pantalla de ayuda paso a paso con enlaces directos para obtener claves gratuitas de TomTom y Google Cloud Console en 2 minutos.
- **Verificador de Claves API:** Botón de prueba en vivo para verificar la validez de las claves introducidas.
- **Sistema de Actualizaciones OTA FlashChip:** Comprobación automática de nuevas versiones desde las Releases de GitHub con descarga e instalación integrada vía `FileProvider`.

### 🟡 [INTERFAZ]
- Diseño Cyberpunk / Dark Cockpit nativo en **Jetpack Compose + Material 3** (`#0A0E17`, cian `#00E5FF`, verde neón `#00E676`, ámbar `#FFB300`, rojo neón `#FF5252`).
- Tarjetas de estado con estética HUD de vehículo (estado de conexión Bluetooth, última comprobación, resumen de incidentes y estado vial).
- Pantalla de bienvenida (Onboarding) para concesión guiada de permisos (Bluetooth, Ubicación en segundo plano, Notificaciones y Batería).

### 🟣 [SEGURIDAD]
- Firma oficial con certificado maestro FlashChip (`flashchip-master.jks`) con esquema dual V1 + V2.
