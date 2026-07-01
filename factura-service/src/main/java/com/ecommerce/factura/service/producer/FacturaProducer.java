package com.ecommerce.factura.service.producer;

import org.springframework.stereotype.Service;

import com.ecommerce.factura.event.FacturaGeneradaEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
public class FacturaProducer {

    private final SqsClient sqsClient;
    private final ObjectMapper mapper;

    private final String queueUrl =
            "http://floci:4566/000000000000/factura-generada";

    public FacturaProducer(
            SqsClient sqsClient,
            ObjectMapper mapper) {

        this.sqsClient = sqsClient;
        this.mapper = mapper;
    }

    public void publicar(
            FacturaGeneradaEvent event)
            throws Exception {

        sqsClient.sendMessage(
                SendMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .messageBody(
                                mapper.writeValueAsString(event))
                        .build());
    }
}