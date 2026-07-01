# E-Commerce Microservices Practice (AWS SQS & S3 with LocalStack)

Este repositorio contiene una arquitectura de microservicios desarrollada con **Java (Spring Boot)** que simula el flujo de compra de un e-commerce utilizando un diseño orientado a eventos (EDA - Event-Driven Architecture) con colas **Amazon SQS** y almacenamiento en **Amazon S3**, simulados localmente mediante **LocalStack** y bases de datos **MySQL**.

---

## 🏗️ Arquitectura y Funcionamiento

El proyecto está dividido en **5 microservicios** independientes que se comunican de forma asíncrona a través de colas de mensajes SQS:

```
[Cliente (HTTP POST)]
         │
         ▼
 ┌───────────────┐        pedido-creado       ┌───────────────┐
 │pedido-service │ ─────────────────────────> │ stock-service │ (Descuenta stock)
 └───────────────┘            (SQS)           └───────────────┘
                                                      │
                                                      │ stock-validado (SQS)
                                                      ▼
 ┌───────────────┐        pago-procesado      ┌───────────────┐
 │ email-service │ <───────────────────────── │ pago-service  │ (Aprobación aleatoria 70%)
 └───────────────┘            (SQS)           └───────────────┘
                                                      │
                                                      │ pago-procesado (SQS)
                                                      ▼
                                              ┌───────────────┐
                                              │factura-service│ (Genera PDF y sube a S3)
                                              └───────────────┘
                                                      │
                                                      │ factura-generada (SQS)
                                                      ▼
                                                [LocalStack S3]
```

### Detalle de Servicios

1. **`pedido-service` (Puerto 8080)**
   - Expone la API REST para la creación de pedidos (`POST /pedidos`).
   - Registra el pedido en la base de datos `pedido_service` con estado `CREADO`.
   - Publica el evento `PedidoCreadoEvent` en la cola `pedido-creado`.

2. **`stock-service` (Puerto 8081)**
   - Escucha la cola `pedido-creado`.
   - Verifica el stock del producto solicitado en la base de datos `stock_service`.
   - Si hay stock, lo descuenta y publica `StockValidadoEvent` con `stockDisponible = true` en la cola `stock-validado`.
   - Si no hay stock, publica el evento con `stockDisponible = false`.

3. **`pago-service` (Puerto 8082)**
   - Escucha la cola `stock-validado`.
   - Si `stockDisponible` es `false`, rechaza el pago automáticamente con el motivo `"Sin stock"`.
   - Si es `true`, procesa el pago simulando una pasarela (con un 70% de probabilidad de éxito).
   - Publica `PagoProcesadoEvent` en la cola `pago-procesado`.

4. **`email-service` (Puerto 8083)**
   - Escucha la cola `pago-procesado`.
   - Simula el envío de un correo de notificación al cliente imprimiendo el formato en la consola del servicio (stdout).

5. **`factura-service` (Puerto 8084)**
   - Escucha la cola `pago-procesado`.
   - Si el pago es aprobado, genera un archivo PDF localmente (`factura-<id>.pdf`) utilizando OpenPDF.
   - Sube el PDF a S3 (`facturas-bucket`) utilizando LocalStack.
   - Publica `FacturaGeneradaEvent` en la cola `factura-generada`.

---

## 🛠️ Cómo Levantar el Ambiente

### Prerrequisitos
- **Java 17** o superior instalado.
- **Maven 3.6+** (o usar el `mvnw` incluido).
- **Docker** instalado y ejecutándose.
- **MySQL** ejecutándose en el puerto estándar `3306`.

---

### Paso 1: Configurar las Bases de Datos MySQL
Todos los microservicios se conectan a MySQL en `localhost:3306` con las credenciales por defecto `root / root`.

Debes ingresar a tu cliente de base de datos MySQL y crear las siguientes bases de datos vacías antes de iniciar las aplicaciones (Hibernate se encargará de crear y actualizar las tablas automáticamente):

```sql
CREATE DATABASE pedido_service;
CREATE DATABASE stock_service;
CREATE DATABASE pago_service;
CREATE DATABASE email_service;
CREATE DATABASE factura_service;
```

---

### Paso 2: Levantar LocalStack
El contenedor de LocalStack debe llamarse `flaci` en Docker, para coincidir con la configuración del proyecto.

1. Corre el contenedor de LocalStack exponiendo los puertos requeridos:
   ```bash
   docker run -d --name flaci -p 4566:4566 -p 4510-4559:4510-4559 localstack/localstack
   ```

2. Crea las colas SQS necesarias dentro de LocalStack:
   ```bash
   docker exec -it flaci awslocal sqs create-queue --queue-name pedido-creado
   docker exec -it flaci awslocal sqs create-queue --queue-name stock-validado
   docker exec -it flaci awslocal sqs create-queue --queue-name pago-procesado
   docker exec -it flaci awslocal sqs create-queue --queue-name factura-generada
   ```

3. Crea el bucket de S3 para el almacenamiento de facturas:
   ```bash
   docker exec -it flaci awslocal s3 mb s3://facturas-bucket
   ```

---

### Paso 3: Mapear el Host `floci` (Importante)
En el código fuente de los productores/consumidores, las URLs de las colas están configuradas estáticamente apuntando a `http://floci:4566`. 
Para que el cliente SDK pueda resolver el host `floci` desde tu máquina local, debes agregar la siguiente línea a tu archivo de hosts del sistema operativo:

* **Windows:** Edita `C:\Windows\System32\drivers\etc\hosts` como Administrador.
* **Linux / macOS:** Edita `/etc/hosts` usando `sudo`.

Agrega al final del archivo:
```text
127.0.0.1 floci
```

---

### Paso 4: Compilar e Iniciar los Microservicios
En la raíz de cada servicio (`pedido-service`, `stock-service`, `pago-service`, `email-service`, `factura-service`), puedes compilar e iniciar la aplicación mediante tu IDE favorito o con la terminal:

```bash
# Compilar e iniciar cada servicio (repetir en cada carpeta de servicio)
./mvnw clean install
./mvnw spring-boot:run
```

*Nota: Al iniciar el servicio `stock-service` por primera vez, un cargador automático (`DataLoader`) sembrará la base de datos con los siguientes productos para pruebas:*
* **`RTX 5070`** (Stock disponible: 5)
* **`RX 9060 XT`** (Stock disponible: 10)
* **`Ryzen 9700X`** (Stock disponible: 20)

---

## 🧪 Casos de Uso y Flujos de Prueba

Puedes probar el flujo completo enviando peticiones HTTP tipo POST al puerto del `pedido-service` (`http://localhost:8080/pedidos`). A continuación se detallan los 3 escenarios principales de prueba:

### Caso 1: Compra Exitosa (Pago Aprobado)
Intentaremos comprar 2 unidades de la tarjeta gráfica `RTX 5070` (hay 5 en stock).

**1. Enviar Petición POST:**
* **URL:** `POST http://localhost:8080/pedidos`
* **Headers:** `Content-Type: application/json`
* **JSON Body:**
```json
{
  "cliente": "Juan Perez",
  "producto": "RTX 5070",
  "cantidad": 2
}
```

*Comando cURL de ejemplo:*
```bash
curl -X POST http://localhost:8080/pedidos -H "Content-Type: application/json" -d "{\"cliente\":\"Juan Perez\",\"producto\":\"RTX 5070\",\"cantidad\":2}"
```

**2. Comportamiento Esperado:**
* **`pedido-service`**: Registra el pedido en base de datos con estado `CREADO` y publica el mensaje en la cola `pedido-creado`.
* **`stock-service`**: Lee la cola, descuenta 2 unidades del producto `RTX 5070` en base de datos (quedarán 3) y publica `stockDisponible = true` en `stock-validado`.
* **`pago-service`**: Lee la validación de stock y procesa el pago. Al ser exitoso (probabilidad del 70%):
  - Loguea: `Pago para Pedido ID: X procesado. Aprobado: true. Mensaje: Pago aprobado`
  - Envía el evento a `pago-procesado`.
* **`email-service`**: Lee el evento de pago e imprime en su consola el correo enviado:
  ```text
  ====================================
  EMAIL ENVIADO
  ====================================
  Pedido #1
  Su compra fue aprobada.
  Estado: Pago aprobado
  ====================================
  ```
* **`factura-service`**: Lee el evento de pago aprobado, genera el PDF `factura-1.pdf` en su directorio local, lo sube al bucket `facturas-bucket` de LocalStack y publica el evento en `factura-generada`.

---

### Caso 2: Intento de Compra Sin Stock
Intentaremos comprar 10 unidades de la tarjeta gráfica `RTX 5070` (cuando el stock es menor).

**1. Enviar Petición POST:**
```bash
curl -X POST http://localhost:8080/pedidos -H "Content-Type: application/json" -d "{\"cliente\":\"Juan Perez\",\"producto\":\"RTX 5070\",\"cantidad\":10}"
```

**2. Comportamiento Esperado:**
* **`stock-service`**: Detecta que la cantidad solicitada (10) supera el stock disponible (3 o 5). Muestra una advertencia en los logs, no altera el stock, y publica `stockDisponible = false` a la cola `stock-validado`.
* **`pago-service`**: Recibe la validación de falta de stock y rechaza el pago automáticamente, publicando `aprobado = false` y motivo `"Sin stock"` en `pago-procesado`.
* **`email-service`**: Consume el evento e imprime en consola:
  ```text
  ====================================
  EMAIL ENVIADO
  ====================================
  Pedido #2
  Su pago fue rechazado.
  Motivo: Sin stock
  ====================================
  ```
* **`factura-service`**: Consume el evento, detecta que no está aprobado y aborta la generación de la factura. Registra el log:
  `Pago para Pedido ID: 2 no aprobado. No se genera factura.`

---

### Caso 3: Compra con Pago Rechazado (Tarjeta Rechazada)
Realizaremos una compra con stock disponible (por ejemplo, 1 unidad de `Ryzen 9700X`). 

**1. Enviar Petición POST:**
```bash
curl -X POST http://localhost:8080/pedidos -H "Content-Type: application/json" -d "{\"cliente\":\"Maria Gomez\",\"producto\":\"Ryzen 9700X\",\"cantidad\":1}"
```

**2. Comportamiento Esperado:**
* Dado que el pago tiene un 30% de probabilidad de ser rechazado de forma aleatoria, puedes repetir la petición hasta ver el caso de rechazo.
* **`pago-service`**: Desencadena la simulación de rechazo:
  - Loguea: `Pago para Pedido ID: X procesado. Aprobado: false. Mensaje: Tarjeta rechazada`
  - Envía el evento a `pago-procesado`.
* **`email-service`**: Consume el evento e imprime en consola:
  ```text
  ====================================
  EMAIL ENVIADO
  ====================================
  Pedido #3
  Su pago fue rechazado.
  Motivo: Tarjeta rechazada
  ====================================
  ```
* **`factura-service`**: Consume el evento de rechazo y no genera la factura.

---

## 🔍 Comandos Útiles de Verificación

Puedes verificar de forma directa el estado de la infraestructura mockeada de AWS usando la consola de comandos:

### 1. Consultar Mensajes en las Colas SQS
Puedes utilizar el script batch incluido en `pedido-service/check-quest.bat` (en entornos Windows) para consultar si hay mensajes pendientes de procesar o no eliminados en las colas.

De forma manual, puedes ver los mensajes de una cola ejecutando:
```bash
docker exec -it flaci awslocal sqs receive-message --queue-url http://localhost:4566/000000000000/pedido-creado
```
*(Reemplaza `pedido-creado` por `stock-validado`, `pago-procesado` o `factura-generada` según desees).*

### 2. Verificar Archivos en el Bucket S3
Para verificar que los archivos PDF de las facturas aprobadas se están subiendo correctamente al bucket en LocalStack S3, ejecuta:
```bash
# Listar los archivos dentro del bucket
docker exec -it flaci awslocal s3 ls s3://facturas-bucket
```

Para descargar un PDF generado del bucket local y comprobar su contenido:
```bash
docker exec -it flaci awslocal s3 cp s3://facturas-bucket/factura-1.pdf ./factura-descargada.pdf
```
