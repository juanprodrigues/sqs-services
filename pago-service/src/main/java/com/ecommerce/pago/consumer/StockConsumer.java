package com.ecommerce.pago.consumer;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ecommerce.pago.event.StockValidadoEvent;
import com.ecommerce.pago.service.PagoService;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

@Component
@EnableScheduling
public class StockConsumer {

    private final SqsClient sqsClient;
    private final PagoService pagoService;
    private final ObjectMapper mapper;

    private final String queueUrl =
            "http://floci:4566/000000000000/stock-validado";

    public StockConsumer(
            SqsClient sqsClient,
            PagoService pagoService,
            ObjectMapper mapper) {

        this.sqsClient = sqsClient;
        this.pagoService = pagoService;
        this.mapper = mapper;
    }

    @Scheduled(fixedDelay = 5000)
    public void escuchar() {

        ReceiveMessageResponse response =
                sqsClient.receiveMessage(
                        ReceiveMessageRequest.builder()
                                .queueUrl(queueUrl)
                                .maxNumberOfMessages(10)
                                .build());

        response.messages()
                .forEach(msg -> {

                    try {

                        StockValidadoEvent event =
                                mapper.readValue(
                                        msg.body(),
                                        StockValidadoEvent.class);

                        pagoService.procesar(event);

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