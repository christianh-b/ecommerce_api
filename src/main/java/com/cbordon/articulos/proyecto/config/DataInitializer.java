package com.cbordon.articulos.proyecto.config;

import com.cbordon.articulos.proyecto.model.Articulo;
import com.cbordon.articulos.proyecto.repository.ArticuloRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner cargarDatosIniciales(ArticuloRepository repo) {
        return args -> {
            if (repo.count() > 0) return;

            repo.saveAll(List.of(
                new Articulo(null, "Samsung Galaxy S24 128GB", 1_299_999.00,
                    "/images/galaxy_s24.jpg"),
                new Articulo(null, "PlayStation 5 Slim 1TB", 699_999.00,
                    "/images/ps5_slim.webp"),
                new Articulo(null, "Smart TV LG 55\" 4K OLED WebOS", 749_999.00,
                    "/images/smart_tv.webp"),
                new Articulo(null, "Nike Air Max 270", 89_999.00,
                    "/images/airmax_270.webp"),
                new Articulo(null, "Apple AirPods Pro 2da Generación", 219_999.00,
                    "/images/airpods2.webp"),
                new Articulo(null, "MacBook Air M3 13\" Medianoche", 899_999.00,
                    "/images/macbook_m3.webp")
            ));
        };
    }
}
