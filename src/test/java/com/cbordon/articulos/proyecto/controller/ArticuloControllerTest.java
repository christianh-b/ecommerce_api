package com.cbordon.articulos.proyecto.controller;

import com.cbordon.articulos.proyecto.dto.request.ArticuloRequest;
import com.cbordon.articulos.proyecto.exception.ResourceNotFoundException;
import com.cbordon.articulos.proyecto.model.Articulo;
import com.cbordon.articulos.proyecto.security.JwtUtils;
import com.cbordon.articulos.proyecto.security.SecurityConfig;
import com.cbordon.articulos.proyecto.service.ArticuloService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ArticuloController.class)
@Import(SecurityConfig.class)
class ArticuloControllerTest {

    @Autowired MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean ArticuloService articuloService;
    // Requeridos por JwtAuthFilter que SecurityConfig inyecta en la cadena de filtros
    @MockitoBean JwtUtils jwtUtils;
    @MockitoBean UserDetailsService userDetailsService;

    private static final Articulo ARTICULO = new Articulo(1L, "Laptop", 1500.0, null);

    private ArticuloRequest buildRequest(String nombre, Double precio) {
        ArticuloRequest req = new ArticuloRequest();
        req.setNombre(nombre);
        req.setPrecio(precio);
        return req;
    }

    // ── GET /api/articulos ───────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/articulos: público → 200 con lista de artículos")
    void GET_lista_returns200() throws Exception {
        when(articuloService.listarArticulos()).thenReturn(List.of(ARTICULO));

        mockMvc.perform(get("/api/articulos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Laptop"))
                .andExpect(jsonPath("$[0].precio").value(1500.0));
    }

    @Test
    @DisplayName("GET /api/articulos: repositorio vacío → 200 con array vacío")
    void GET_lista_vacia_returns200EmptyArray() throws Exception {
        when(articuloService.listarArticulos()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/articulos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── GET /api/articulos/{id} ──────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/articulos/{id}: artículo existente → 200 con body")
    void GET_byId_existe_returns200() throws Exception {
        when(articuloService.buscarArticuloPorId(1L)).thenReturn(ARTICULO);

        mockMvc.perform(get("/api/articulos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Laptop"))
                .andExpect(jsonPath("$.precio").value(1500.0));
    }

    @Test
    @DisplayName("GET /api/articulos/{id}: artículo no existe → 404 con cuerpo de error")
    void GET_byId_noExiste_returns404() throws Exception {
        when(articuloService.buscarArticuloPorId(99L))
                .thenThrow(new ResourceNotFoundException("Artículo con id 99 no encontrado"));

        mockMvc.perform(get("/api/articulos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(containsString("99")));
    }

    // ── POST /api/articulos ──────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/articulos: rol ADMIN, datos válidos → 201 con body creado")
    void POST_articulo_conAdmin_returns201() throws Exception {
        when(articuloService.guardarArticulo(any())).thenReturn(ARTICULO);

        mockMvc.perform(post("/api/articulos")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest("Laptop", 1500.0))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Laptop"));
    }

    @Test
    @DisplayName("POST /api/articulos: sin autenticación → 401")
    void POST_articulo_sinAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/articulos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest("Laptop", 1500.0))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/articulos: rol USER → 403")
    void POST_articulo_conRoleUser_returns403() throws Exception {
        mockMvc.perform(post("/api/articulos")
                        .with(user("testuser").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest("Laptop", 1500.0))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/articulos: nombre en blanco → 400 con mensaje de validación")
    void POST_articulo_nombreBlanco_returns400() throws Exception {
        mockMvc.perform(post("/api/articulos")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest("", 1500.0))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("nombre")));
    }

    @Test
    @DisplayName("POST /api/articulos: precio negativo → 400 con mensaje de validación")
    void POST_articulo_precioNegativo_returns400() throws Exception {
        mockMvc.perform(post("/api/articulos")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest("Laptop", -1.0))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("precio")));
    }

    // ── PUT /api/articulos/{id} ──────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/articulos/{id}: rol ADMIN, existente → 200 con body actualizado")
    void PUT_articulo_conAdmin_returns200() throws Exception {
        Articulo actualizado = new Articulo(1L, "Laptop Pro", 2000.0, null);
        when(articuloService.actualizarArticulo(eq(1L), any())).thenReturn(actualizado);

        mockMvc.perform(put("/api/articulos/1")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest("Laptop Pro", 2000.0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Laptop Pro"))
                .andExpect(jsonPath("$.precio").value(2000.0));
    }

    @Test
    @DisplayName("PUT /api/articulos/{id}: artículo no existe → 404")
    void PUT_articulo_noExiste_returns404() throws Exception {
        when(articuloService.actualizarArticulo(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Artículo con id 99 no encontrado"));

        mockMvc.perform(put("/api/articulos/99")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest("Tablet", 1.0))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/articulos/{id}: sin autenticación → 401")
    void PUT_articulo_sinAuth_returns401() throws Exception {
        mockMvc.perform(put("/api/articulos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest("Tablet", 1.0))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/articulos/{id}: rol USER → 403")
    void PUT_articulo_conRoleUser_returns403() throws Exception {
        mockMvc.perform(put("/api/articulos/1")
                        .with(user("testuser").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest("Tablet", 1.0))))
                .andExpect(status().isForbidden());
    }

    // ── DELETE /api/articulos/{id} ───────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/articulos/{id}: rol ADMIN, existente → 204 sin cuerpo")
    void DELETE_articulo_conAdmin_returns204() throws Exception {
        doNothing().when(articuloService).borrarArticulo(1L);

        mockMvc.perform(delete("/api/articulos/1")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());
        verify(articuloService).borrarArticulo(1L);
    }

    @Test
    @DisplayName("DELETE /api/articulos/{id}: artículo no existe → 404")
    void DELETE_articulo_noExiste_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Artículo con id 99 no encontrado"))
                .when(articuloService).borrarArticulo(99L);

        mockMvc.perform(delete("/api/articulos/99")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/articulos/{id}: sin autenticación → 401")
    void DELETE_articulo_sinAuth_returns401() throws Exception {
        mockMvc.perform(delete("/api/articulos/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/articulos/{id}: rol USER → 403")
    void DELETE_articulo_conRoleUser_returns403() throws Exception {
        mockMvc.perform(delete("/api/articulos/1")
                        .with(user("testuser").roles("USER")))
                .andExpect(status().isForbidden());
    }
}
