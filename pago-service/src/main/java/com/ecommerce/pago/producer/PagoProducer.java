package com.ecommerce.pago.producer;

import org.springframework.stereotype.Service;

import com.ecommerce.pago.event.PagoProcesadoEvent;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
@Service
public class PagoProducer {

    private final SqsClient sqsClient;
    private final ObjectMapper mapper;

    private final String queueUrl =
            "http://floci:4566/000000000000/pago-procesado";

    public PagoProducer(
            SqsClient sqsClient,
            ObjectMapper mapper) {

        this.sqsClient = sqsClient;
        this.mapper = mapper;
    }

    public void publicar(
            PagoProcesadoEvent event)
            throws Exception {

        sqsClient.sendMessage(
                SendMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .messageBody(
                                mapper.writeValueAsString(event))
                        .build());
    }
}