@echo off

set LOCALSTACK=http://localhost:4566

echo ============================
echo PEDIDO-CREADO
echo ============================
docker exec -it flaci awslocal sqs receive-message --queue-url %LOCALSTACK%/000000000000/pedido-creado

echo.
echo ============================
echo STOCK-VALIDADO
echo ============================
docker exec -it flaci awslocal sqs receive-message --queue-url %LOCALSTACK%/000000000000/stock-validado

echo.
echo ============================
echo PAGO-PROCESADO
echo ============================
docker exec -it flaci awslocal sqs receive-message --queue-url %LOCALSTACK%/000000000000/pago-procesado

echo.
echo ============================
echo FACTURA-GENERADA
echo ============================
docker exec -it flaci awslocal sqs receive-message --queue-url %LOCALSTACK%/000000000000/factura-generada

pause