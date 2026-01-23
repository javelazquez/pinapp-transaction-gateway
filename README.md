# PinApp Transaction Gateway

> **Financial Transaction Processor & Notification Orchestrator**

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen?style=flat-square&logo=springboot)
![Architecture](https://img.shields.io/badge/Architecture-Hexagonal-purple?style=flat-square)
![Docker](https://img.shields.io/badge/Docker-Ready-blue?style=flat-square&logo=docker)

---

## 📋 Overview

**PinApp Transaction Gateway** es un microservicio crítico diseñado para orquestar el procesamiento de transacciones financieras. Su responsabilidad principal es interpretar el estado de cada transacción y decidir la estrategia de notificación más adecuada para el cliente final, integrándose nativamente con la librería core `pinapp-notify-sdk`.

Este proyecto sirve como una **implementación de referencia de Arquitectura Hexagonal**, demostrando cómo desacoplar completamente la lógica de negocio (Domain) de los frameworks y detalles de infraestructura.

---

## 🏛️ Arquitectura y Diseño

El servicio sigue estrictamente los principios de **Clean Architecture**:

*   **Domain (Java Puro)**: Contiene la lógica de negocio, reglas y modelos. No tiene dependencias de Spring ni de librerías externas.
*   **Infrastructure (Spring Boot)**: Implementa los adaptadores que conectan el dominio con el mundo exterior (API REST, SDKs, Bases de Datos).

### 📏 Reglas de Negocio (Business Rules)

El Gateway decide el canal de notificación basándose en el estado final de la transacción:

| Transaction Status | Notification Channel | Estrategia | Prioridad |
| :--- | :--- | :--- | :--- |
| **COMPLETED** | 📧 **Email** | Síncrono | Crítica (Alta) |
| **PUSH** | 📲 **Pending** | Asíncrono (Batch) | Baja (No Bloqueante) |
| **REJECTED** | 💬 **SMS** | Síncrono | Seguridad (Alertas) |

---

## 🚀 Getting Started

### Prerrequisitos

*   ☕ **Java 21** (Eclipse Temurin recomendado)
*   🐘 **Maven 3.9+**
*   🐳 **Docker** (para containerización)

### Instalación

Esta aplicación depende de `pinapp-notify-sdk`. Se asume que el SDK se encuentra en un directorio hermano:

```text
/workspace
  ├── pinapp-notify-sdk/         <-- Source del SDK
  └── pinapp-transaction-gateway/ <-- Este proyecto
```

### 🛠️ Uso del Makefile

Hemos simplificado el ciclo de desarrollo usando `make`. Comandos disponibles:

```bash
make install-sdk   # 📦 Compila e instala la librería SDK localmente
make build         # 🏗️ Compila el gateway (libs + source)
make run           # ▶️ Levanta la aplicación en local
make docker-build  # 🐳 Genera la imagen Docker automáticamente (incluye SDK)
make docker-run    # 🏃 Ejecuta el contenedor en puerto 8080
make docker-stop   # 🛑 Detiene el contenedor en ejecución
make docker-clean  # 🧹 Elimina la imagen y limpia archivos temporales
```

---

## 🐳 Docker Demo

¿No tienes Java instalado o quieres probar una versión limpia? Utiliza nuestra infraestructura Dockerizada.

El script de automatización se encarga de todo (compilar SDK, empaquetar y construir imagen).

```bash
# 1. Construir la imagen
make docker-build

# 2. Ejecutar la demo
make docker-run
```

La aplicación estará disponible en `http://localhost:8080`.

---

## 🔌 API Reference

Documentación interactiva disponible vía Swagger UI:
👉 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

### Endpoints Principales

| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `POST` | **/v1/transactions** | **Procesamiento Unitario**. Recibe una transacción, evalúa reglas y notifica síncronamente (si aplica). |
| `POST` | **/v1/transactions/batch** | **Procesamiento Batch**. Ingesta masiva de transacciones para procesamiento diferido/asíncrono. |
| `GET` | **/v1/transactions/status/{id}** | **Consulta de Estado**. Verifica el estatus de notificaciones asíncronas pendientes. |

---

## 📂 Estructura del Proyecto

```text
pinapp-transaction-gateway/
├── domain/                      # 🧠 NÚCLEO (Sin dependencias de framework)
│   ├── model/                   # Entidades (Transaction, Notification)
│   ├── port/                    # Interfaces (Inbound/Outbound Ports)
│   └── usecase/                 # Lógica de aplicación (ProcessTransaction)
├── infrastructure/              # 🔌 ADAPTADORES (Spring Boot)
│   ├── adapter/
│   │   ├── notification/        # Implementación usando pinapp-notify-sdk
│   │   └── input/rest/          # Controllers REST
│   └── config/                  # Configuración (Beans, Swagger)
├── Dockerfile                   # 🐳 Multistage Build Definition
├── Makefile                     # 🛠️ Task Runner
└── prepare_docker.sh            # 📜 Automation Script
```

---

<div align="center">
  <sub>Built with ❤️ by PinApp DevOps Team</sub>
</div>
