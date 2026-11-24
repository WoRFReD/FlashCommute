# 📋 INFORME DE VERSIONES - FlashCommute
> **Proyecto:** FlashCommute  
> **Autor:** WoRFReD • **Canal:** FlashChip (https://youtube.com/@flashchiped)  
> **Package:** `com.flashchip.flashcommute`

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
