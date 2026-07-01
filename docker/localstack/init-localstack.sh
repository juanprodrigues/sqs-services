#!/bin/bash
echo "Inicializando LocalStack..."

# Crear colas SQS
awslocal sqs create-queue --queue-name pedido-creado
awslocal sqs create-queue --queue-name stock-validado
awslocal sqs create-queue --queue-name pago-procesado
awslocal sqs create-queue --queue-name factura-generada

# Crear bucket S3
awslocal s3 mb s3://facturas-bucket

echo "LocalStack inicializado correctamente."
