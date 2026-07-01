package com.ecommerce.email.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ecommerce.email.event.PagoProcesadoEvent;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    public void enviar(PagoProcesadoEvent event) {

        log.info("Enviando correo de notificación para Pedido ID: {}. Aprobado: {}", event.pedidoId(), event.aprobado());

        if(event.aprobado()) {

            System.out.println("""
                    
                    ====================================
                    EMAIL ENVIADO
                    ====================================
                    
                    Pedido #%d
                    
                    Su compra fue aprobada.
                    
                    Estado: %s
                    
                    ====================================
                    """
                    .formatted(
                            event.pedidoId(),
                            event.mensaje()
                    ));

        } else {

            System.out.println("""
                    
                    ====================================
                    EMAIL ENVIADO
                    ====================================
                    
                    Pedido #%d
                    
                    Su pago fue rechazado.
                    
                    Motivo:
                    %s
                    
                    ====================================
                    """
                    .formatted(
                            event.pedidoId(),
                            event.mensaje()
                    ));
        }
    }
}