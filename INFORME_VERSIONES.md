# 📋 INFORME DE VERSIONES - FlashCommute
> **Proyecto:** FlashCommute  
> **Autor:** WoRFReD • **Canal:** FlashChip (https://youtube.com/@flashchiped)  
> **Package:** `com.flashchip.flashcommute`

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
