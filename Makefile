# Variables
APP_NAME=pinapp-transaction-gateway
MAVEN=mvn
PORT=8080

.PHONY: help build run test clean swagger javadoc install-sdk docker-build docker-run docker-stop docker-clean

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

javadoc: ## Genera y abre la documentación JavaDoc en el navegador (macOS)
	$(MAVEN) javadoc:javadoc
	@if [ -f target/reports/apidocs/index.html ]; then \
		open target/reports/apidocs/index.html || echo "Abre target/reports/apidocs/index.html en tu navegador"; \
	else \
		echo "Error: No se pudo generar la documentación JavaDoc"; \
		exit 1; \
	fi

install-sdk: ## Instala localmente la dependencia pinapp-notify-sdk (requerido para el build)
	@echo "Asegúrate de tener el repositorio pinapp-notify-sdk clonado y ejecuta 'mvn install' en esa carpeta."

# Comandos combinados
rebuild: clean build ## Limpia y compila el proyecto

dev: build run ## Compila y ejecuta la aplicación

# Docker
docker-build: ## Construye la imagen Docker completando dependencias locales
	chmod +x prepare_docker.sh
	./prepare_docker.sh

docker-run: ## Ejecuta el contenedor (Map puerto 8080)
	docker run -p $(PORT):8080 --rm --name pinapp-gateway pinapp-gateway

docker-stop: ## Detiene el contenedor de la aplicación
	docker stop pinapp-gateway || true

docker-clean: docker-stop ## Detiene el contenedor y elimina la imagen
	docker rmi pinapp-gateway || true
	rm -rf libs/
