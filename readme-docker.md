# Guía de Ejecución con Docker Compose (`readme-docker.md`)

Esta guía detalla los comandos necesarios para compilar, iniciar, detener y diagnosticar la arquitectura de microservicios utilizando **Docker Compose**. Asimismo, incluye una sección de solución de errores comunes para resolver cualquier conflicto en tu entorno local.

---

## Comandos para Ejecutar el Proyecto

### 1. Iniciar todo el ecosistema (Compilación y Arranque)
Para compilar los microservicios en sus respectivos contenedores y levantar la base de datos MySQL y LocalStack por primera vez (o tras cambios en el código):

```bash
docker-compose up --build
```
* **Qué hace este comando:**
  1. Descarga e instala las dependencias de Maven dentro de cada contenedor para compilar cada microservicio y generar el archivo `.jar`.
  2. Levanta el contenedor de la base de datos `mysql-db`.
  3. Ejecuta el script `init.sql` para crear automáticamente las 5 bases de datos.
  4. Levanta el contenedor de LocalStack (`flaci`).
  5. Ejecuta el script `init-localstack.sh` para crear las colas de SQS y el bucket de S3.
  6. Inicia los microservicios en orden (esperando que MySQL y LocalStack estén saludables).

---

### 2. Iniciar en segundo plano (Detached Mode)
Si prefieres liberar la terminal y correr los contenedores en segundo plano:

```bash
docker-compose up -d --build
```
* Para ver los logs de los contenedores corriendo en segundo plano:
  ```bash
  docker-compose logs -f
  ```
  *(Puedes filtrar por un servicio en específico, por ejemplo: `docker-compose logs -f email-service`)*.

---

### 3. Apagar el ambiente
Para detener todos los contenedores y liberar los puertos sin borrar los datos guardados en la BD:

```bash
docker-compose down
```

---

### 4. Limpieza total y reinicio de datos
Para apagar los contenedores y **borrar los datos guardados** (volúmenes de MySQL, colas y buckets creados):

```bash
docker-compose down -v
```
*(Muy útil si deseas empezar una prueba desde cero y vaciar las bases de datos y colas).*

---

## Qué se espera ver (Salidas Exitosas)

Durante el arranque exitoso de `docker-compose up --build`, deberías observar las siguientes líneas clave en los logs de tu terminal:

1. **Inicialización de Base de Datos MySQL:**
   ```text
   mysql-db  | /usr/local/bin/docker-entrypoint.sh: running /docker-entrypoint-initdb.d/init.sql
   ```
   *(Indica que las bases de datos de los 5 servicios han sido creadas de forma automática).*

2. **Inicialización de LocalStack (Colas y Bucket S3):**
   ```text
   flaci     | Inicializando LocalStack...
   flaci     | { "QueueUrl": "http://floci:4566/000000000000/pedido-creado" }
   flaci     | { "QueueUrl": "http://floci:4566/000000000000/stock-validado" }
   flaci     | { "QueueUrl": "http://floci:4566/000000000000/pago-procesado" }
   flaci     | { "QueueUrl": "http://floci:4566/000000000000/factura-generada" }
   flaci     | make_bucket: facturas-bucket
   flaci     | LocalStack inicializado correctamente.
   ```

3. **Carga Inicial de Productos (`stock-service`):**
   ```text
   stock-service  | Stock descontado para Pedido ID: ... Nuevo stock para RTX 5070: ...
   ```
   *(Verás en el inicio que el `DataLoader` de stock-service se ejecuta sembrando la base de datos de productos).*

---

## Errores Comunes y Soluciones

### 1. Puerto 3306 ya está en uso
* **Síntomas:** El contenedor `mysql-db` falla al arrancar y muestra un error similar a:
  `Bind for 0.0.0.0:3306 failed: port is already allocated` o `port already in use`.
* **Causa:** Tienes una instancia local de MySQL ejecutándose directamente en tu máquina host y ocupando el puerto `3306`.
* **Solución:**
  * **Opción A (Recomendada):** Detén tu servicio local de MySQL.
    - *En Windows:* Abre `servicios.msc` (Services), busca el servicio `MySQL` y haz clic en **Detener**. O ejecuta en PowerShell de administrador: `net stop MySQL`.
    - *En Linux/macOS:* Ejecuta `sudo systemctl stop mysql` o `sudo service mysql stop`.
  * **Opción B:** Cambia el puerto de mapeo externo en `docker-compose.yaml` (ej. `"3307:3306"`). *Nota: Los servicios internos seguirán conectándose al puerto 3306 del contenedor `db` sin problemas.*

---

### 2. Conflicto de Nombre de Contenedor `/flaci`
* **Síntomas:** Error de conflicto al levantar docker-compose:
  `Conflict. The container name "/flaci" is already in use by container [ID]...`
* **Causa:** Ya existe un contenedor de LocalStack viejo creado manualmente fuera de Docker Compose con el nombre `flaci`.
* **Solución:** Fuerza la eliminación del contenedor viejo que causa el conflicto ejecutando:
  ```bash
  docker rm -f flaci
  ```
  Y vuelve a lanzar `docker-compose up --build`.

---

### 3. Error de fin de línea en Script de LocalStack (`\r: command not found`)
* **Síntomas:** Los logs de LocalStack muestran errores de sintaxis al ejecutar el script de inicialización:
  `init-localstack.sh: line 2: $'\r': command not found` o `syntax error near unexpected token`.
* **Causa:** El archivo `docker/localstack/init-localstack.sh` se guardó con finales de línea de Windows (CRLF) en lugar de finales de línea de Linux/macOS (LF).
* **Solución:**
  1. Abre el archivo `init-localstack.sh` en tu editor de código (como VS Code).
  2. En la barra inferior del editor, cambia la codificación de **CRLF** a **LF**.
  3. Guarda el archivo y ejecuta `docker-compose down -v` seguido de `docker-compose up --build`.

---

### 4. Peticiones HTTP o verificación de colas fallan con `Unknown Host: floci` (Desde tu máquina)
* **Síntomas:** Cuando haces una llamada cURL, Postman o ejecutas un comando CLI manual de AWS desde la terminal de tu máquina host, obtienes un error de DNS de que no se puede resolver el host `floci` o `flaci`.
* **Causa:** Las peticiones desde fuera de la red de Docker no conocen la dirección IP del contenedor `floci`.
* **Solución:** Debes agregar el mapeo de red local en tu archivo `hosts` del sistema operativo:
  1. Abre el archivo de hosts como Administrador/Root:
     - *Windows:* `C:\Windows\System32\drivers\etc\hosts`
     - *Linux/macOS:* `/etc/hosts`
  2. Añade las siguientes líneas al final del archivo:
     ```text
     127.0.0.1 floci
     127.0.0.1 flaci
     ```

---

### 5. Error de Permisos en el socket de Docker (`/var/run/docker.sock`)
* **Síntomas:** LocalStack muestra errores en los logs indicando que no puede conectarse al socket de Docker:
  `Permission denied` al acceder a `/var/run/docker.sock`.
* **Causa:** Docker Desktop no tiene habilitados los permisos para compartir el socket o la configuración requiere permisos elevados.
* **Solución:**
  1. Abre la interfaz de **Docker Desktop**.
  2. Ve a **Settings** (Icono de engranaje) -> **General**.
  3. Asegúrate de tener marcada la opción **"Expose daemon on tcp://localhost:2375 without TLS"** o estar usando la integración de WSL 2 en la configuración.
  4. Reinicia Docker Desktop y vuelve a ejecutar tu compose.
