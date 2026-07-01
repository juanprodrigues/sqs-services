package com.ecommerce.stock.loader;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ecommerce.stock.entity.Producto;
import com.ecommerce.stock.repository.ProductoRepository;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner init(
            ProductoRepository repository) {

        return args -> {

            if(repository.count() == 0){

                repository.save(
                        new Producto(
                                "RTX 5070",
                                5));

                repository.save(
                        new Producto(
                                "RX 9060 XT",
                                10));

                repository.save(
                        new Producto(
                                "Ryzen 9700X",
                                20));
            }
        };
    }
}