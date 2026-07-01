package com.ecommerce.pedido;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = PedidoServiceApplication.class)
@ActiveProfiles("test")
class PedidoServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}