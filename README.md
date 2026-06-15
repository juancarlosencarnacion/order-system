# order-system

`order-system` es un ecosistema robusto y de alto rendimiento diseñado para la gestión integral del ciclo de vida de órdenes de compra. El núcleo del sistema está impulsado por el microservicio **`order-api`**, desarrollado en **Java con Quarkus**, implementando una arquitectura orientada a eventos (EDA) y patrones de optimización de datos para garantizar una ejecución asíncrona, desacoplada y de baja latencia.

---

## 🚀 Propósito del Proyecto

El objetivo principal de `order-system` es procesar transacciones y órdenes de manera eficiente y escalable. Para evitar cuellos de botella tradicionales en la base de datos relacional y mejorar drásticamente el tiempo de respuesta, **`order-api`** delega las tareas pesadas (como la generación y el envío de facturas por correo electrónico) a un flujo de trabajo asíncrono automatizado, mientras optimiza las lecturas concurrentes utilizando mecanismos de acceso rápido en memoria.

---

## 🛠️ Arquitectura y Tecnologías Clave

El ecosistema está completamente contenedorizado mediante **Docker** e integra los siguientes componentes:

* **order-api (Quarkus Framework):** El microservicio central de la aplicación, seleccionado por su tiempo de arranque supersónico, perfil reactivo y bajo consumo de memoria.
* **PostgreSQL:** Base de datos relacional transaccional utilizada como el almacenamiento persistente definitivo. Se encarga de garantizar la consistencia, integridad y persistencia de las órdenes, usuarios y transacciones comerciales.
* **Redis:** Utilizado como una **capa de cacheo ultra-rápida en memoria**. Permite almacenar y consultar las órdenes frecuentes en milisegundos, reduciendo drásticamente la carga de lecturas repetitivas sobre **PostgreSQL**.
* **Apache Kafka (KRaft mode):** Motor de mensajería distribuida encargado del desacoplamiento. Al crearse y persistirse una orden, `order-api` publica un *Thin Event* (evento delgado) conteniendo únicamente el ID de la orden (`{"orderId": 17}`).
* **n8n:** Motor de automatización de flujos de trabajo (*Workflow Automation*). Escucha los eventos de Kafka en segundo plano, realiza una consulta HTTP de retorno (*callback*) a `order-api` para obtener el DTO detallado de la orden y monta la factura de manera dinámica.
* **SMTP (Gmail App Password):** Integración en el nodo de salida de n8n para el envío real y seguro de correos electrónicos informativos a los usuarios, utilizando credenciales seguras de aplicación de Google.

### Herramientas de Monitoreo e Infraestructura Visual
Para facilitar el desarrollo, depuración y la observabilidad del entorno local, el archivo `docker-compose.yml` expone:
* **Kafdrop (Puerto 9000):** Interfaz web para la gestión e inspección visual de los *topics*, mensajes, particiones y el estado general del clúster de Kafka.
* **Redis Insight (Puerto 5540):** Entorno gráfico avanzado para visualizar las estructuras llave-valor del caché, analizar el rendimiento de las consultas y depurar los datos en memoria de Redis.

---

## 📐 Flujo de Datos Asíncrono

El flujo de procesamiento sigue un ciclo de vida completamente reactivo y desacoplado:

1. El cliente genera una transacción a través del microservicio **`order-api`**.
2. La API registra la orden de forma persistente en **PostgreSQL**, actualiza la capa de caché en **Redis** para acelerar futuras lecturas y publica el evento estructurado en **Kafka** (`{"orderId": ID}`).
3. El trigger de **n8n** captura el evento desde Kafka, procesa el payload de la mensajería y ejecuta una petición `GET` hacia el endpoint `/api/v1/orders/{id}` de `order-api` para extraer los datos frescos y unificados de la orden.
4. **n8n** inyecta los datos en una plantilla estructurada en HTML y dispara el envío del correo electrónico