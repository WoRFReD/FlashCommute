# 🚗 FlashCommute (v1.00)

<p align="center">
  <img src="https://img.shields.io/badge/Versi%C3%B3n-1.00-00E5FF?style=for-the-badge&logo=android" alt="Version 1.00" />
  <img src="https://img.shields.io/badge/Firma-Release%20V1%2BV2-00E676?style=for-the-badge&logo=android" alt="Firma V1+V2" />
  <img src="https://img.shields.io/badge/Estilo-Cyberpunk%20Dark-FFB300?style=for-the-badge" alt="Cyberpunk Dark" />
  <img src="https://img.shields.io/badge/Autor-WoRFReD%20%E2%80%A2%20FlashChip-FF5252?style=for-the-badge&logo=youtube" alt="FlashChip" />
</p>

**FlashCommute** es una aplicación inteligente para Android diseñada para anticiparse a los atascos y accidentes en tu trayecto diario al trabajo con **cero fricción**: sin necesidad de abrir Google Maps ni tocar la pantalla del móvil cada mañana.

---

## ⚡ ¿Cómo funciona?

1. **Detección Automática por Bluetooth:**  
   Te subes al coche por la mañana y tu teléfono se conecta al Bluetooth del vehículo.
2. **Evaluación de Franja Horaria:**  
   La app verifica si te encuentras dentro de tu horario habitual de salida al trabajo (ej: Lunes a Viernes entre las 06:00 y las 09:00).
3. **Cálculo de Ruta y Tráfico en Vivo:**  
   Calcula en segundo plano la ruta desde tu posición GPS actual hasta tu centro de trabajo mediante las APIs de **TomTom** y **Google Routes**.
4. **Alertas Inteligentes según Severidad:**
   * **🟢 Tráfico Normal / Fluido:** Notificación discreta en la barra de estado con el tiempo estimado de viaje y hora de llegada.
   * **🔴 Accidente / Retención Crítica:** Alerta prioritaria con sonido en tu teléfono y notificación visible en pantalla de bloqueo avisándote antes de que te quedes atrapado, para que puedas tomar una vía alternativa.

---

## 🛠️ Características Principales

* **Doble Motor de Tráfico (TomTom & Google Routes):** Arquitectura híbrida con tolerancia a fallos.
* **Totalmente Personalizable:**
  * Franja horaria y días de activación ajustables.
  * Umbral de retraso crítico configurable (por defecto 7 minutos).
  * Selector visual del dispositivo Bluetooth del coche.
* **Guía y Tutorial de APIs Gratis Integrado:** Acceso a claves de capa gratuita de TomTom y Google con enlaces directos y verificador de conexión.
* **Interfaz Cyberpunk Cockpit:** Diseñada en Jetpack Compose + Material 3 con fondo oscuro `#0A0E17` que no deslumbra de madrugada.
* **Actualizaciones OTA Integradas:** Descarga e instalación automática de nuevas versiones desde GitHub Releases.

---

## 📋 Changelog

| Versión | Fecha | Resumen de Cambios |
| :---: | :---: | :--- |
| **1.00** | 24/11/2026 | Versión inicial oficial con detección Bluetooth, doble motor TomTom/Google, alertas críticas y actualizaciones OTA. |

---

## 👨‍💻 Desarrollador
Desarrollado por **WoRFReD** para el canal de YouTube **[FlashChip](https://youtube.com/@flashchiped)**.
