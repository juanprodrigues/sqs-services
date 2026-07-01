package com.ecommerce.email.consumer;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ecommerce.email.event.PagoProcesadoEvent;
import com.ecommerce.email.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

@Component
public class PagoConsumer {

    private final SqsClient sqsClient;
    private final EmailService emailService;
    private final ObjectMapper mapper;

    private final String queueUrl =
            "http://floci:4566/000000000000/pago-procesado";

    public PagoConsumer(
            SqsClient sqsClient,
            EmailService emailService,
            ObjectMapper mapper) {

        this.sqsClient = sqsClient;
        this.emailService = emailService;
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

                        PagoProcesadoEvent event =
                                mapper.readValue(
                                        msg.body(),
                                        PagoProcesadoEvent.class);

                        emailService.enviar(event);

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