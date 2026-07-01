package com.ecommerce.factura.consumer;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ecommerce.factura.event.PagoProcesadoEvent;
import com.ecommerce.factura.service.FacturaService;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

@Component
public class PagoConsumer {

    private final SqsClient sqsClient;
    private final FacturaService facturaService;
    private final ObjectMapper mapper;

    private final String queueUrl =
            "http://floci:4566/000000000000/pago-procesado";

    public PagoConsumer(
            SqsClient sqsClient,
            FacturaService facturaService,
            ObjectMapper mapper) {

        this.sqsClient = sqsClient;
        this.facturaService = facturaService;
        this.mapper = mapper;
    }

    @Scheduled(fixedDelay = 5000)
    public void escuchar() {
    	System.out.println(" FACTURA CONSUMER INICIADO");
        ReceiveMessageResponse response =
                sqsClient.receiveMessage(
                        ReceiveMessageRequest.builder()
                                .queueUrl(queueUrl)
                                .build());

        response.messages()
                .forEach(msg -> {

                    try {

                        PagoProcesadoEvent event =
                                mapper.readValue(
                                        msg.body(),
                                        PagoProcesadoEvent.class);

                        facturaService.procesar(event);

                        sqsClient.deleteMessage(
                                DeleteMessageRequest.builder()
                                        .queueUrl(queueUrl)
                                        .receiptHandle(
                                                msg.receiptHandle())
                                        .build());

                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                });
    }
}