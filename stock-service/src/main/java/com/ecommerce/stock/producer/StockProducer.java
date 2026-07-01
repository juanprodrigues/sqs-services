package com.ecommerce.stock.producer;

import org.springframework.stereotype.Service;

import com.ecommerce.stock.event.StockValidadoEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
public class StockProducer {

    private final SqsClient sqsClient;
    private final ObjectMapper mapper;

    private final String queueUrl =
            "http://floci:4566/000000000000/stock-validado";

    public StockProducer(
            SqsClient sqsClient,
            ObjectMapper mapper) {

        this.sqsClient = sqsClient;
        this.mapper = mapper;
    }

    public void publicar(
            StockValidadoEvent event)
            throws Exception {
System.out.println("StockProducer : "+ event.toString());
        sqsClient.sendMessage(
                SendMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .messageBody(
                                mapper.writeValueAsString(event))
                        .build());
    }
}