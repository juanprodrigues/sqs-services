package com.ecommerce.email.service;

import com.ecommerce.email.event.PagoProcesadoEvent;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public void enviar(PagoProcesadoEvent event) {

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