# Variables
APP_NAME=pinapp-transaction-gateway
MAVEN=mvn
PORT=8080

.PHONY: help build run test clean swagger install-sdk

help: ## Muestra ayuda de los comandos disponibles
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

build: ## Compila el proyecto generando el archivo JAR
	$(MAVEN) clean package -DskipTests

run: ## Ejecuta la aplicación Spring Boot
	$(MAVEN) spring-boot:run

test: ## Ejecuta todas las pruebas unitarias y de integración
	$(MAVEN) test

clean: ## Limpia los archivos generados por la compilación (target)
	$(MAVEN) clean

swagger: ## Abre la documentación de Swagger en el navegador (macOS)
	open http://localhost:$(PORT)/swagger-ui.html || echo "Abre http://localhost:$(PORT)/swagger-ui.html en tu navegador"

install-sdk: ## Instala localmente la dependencia pinapp-notify-sdk (requerido para el build)
	@echo "Asegúrate de tener el repositorio pinapp-notify-sdk clonado y ejecuta 'mvn install' en esa carpeta."

# Comandos combinados
rebuild: clean build ## Limpia y compila el proyecto

dev: build run ## Compila y ejecuta la aplicación
