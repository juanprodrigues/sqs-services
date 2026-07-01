package com.ecommerce.pedido.controller;

import com.ecommerce.pedido.dto.CrearPedidoRequest;
import com.ecommerce.pedido.entity.Pedido;
import com.ecommerce.pedido.service.PedidoService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService service;

    public PedidoController(
            PedidoService service) {

        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Pedido> crear(
            @RequestBody CrearPedidoRequest dto)
            throws Exception {

        return ResponseEntity.ok(
                service.crear(dto));
    }
}