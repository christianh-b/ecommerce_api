package com.cbordon.articulos.proyecto.controller;

import com.cbordon.articulos.proyecto.dto.request.ArticuloRequest;
import com.cbordon.articulos.proyecto.dto.response.ArticuloResponse;
import com.cbordon.articulos.proyecto.model.Articulo;
import com.cbordon.articulos.proyecto.service.ArticuloService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Artículos", description = "CRUD de artículos del catálogo")
@RestController
@RequestMapping("/api/articulos")
public class ArticuloController {

    private final ArticuloService articuloService;

    public ArticuloController(ArticuloService articuloService) {
        this.articuloService = articuloService;
    }

    @Operation(summary = "Listar todos los artículos", description = "Endpoint público — no requiere token.")
    @ApiResponse(responseCode = "200", description = "Lista de artículos")
    @GetMapping
    public ResponseEntity<List<ArticuloResponse>> listar() {
        List<ArticuloResponse> response = articuloService.listarArticulos()
                .stream()
                .map(ArticuloResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener artículo por ID", description = "Endpoint público — no requiere token.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Artículo encontrado"),
        @ApiResponse(responseCode = "404", description = "Artículo no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ArticuloResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(ArticuloResponse.fromEntity(articuloService.buscarArticuloPorId(id)));
    }

    @Operation(summary = "Crear artículo", description = "Requiere ROLE_ADMIN. Incluir token en Authorize.")
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Artículo creado"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "401", description = "Sin autenticación"),
        @ApiResponse(responseCode = "403", description = "Rol insuficiente — se requiere ADMIN")
    })
    @PostMapping
    public ResponseEntity<ArticuloResponse> guardar(@Valid @RequestBody ArticuloRequest request) {
        Articulo nuevo = new Articulo(null, request.getNombre(), request.getPrecio(), request.getImagen());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ArticuloResponse.fromEntity(articuloService.guardarArticulo(nuevo)));
    }

    @Operation(summary = "Actualizar artículo", description = "Requiere ROLE_ADMIN. Incluir token en Authorize.")
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Artículo actualizado"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "401", description = "Sin autenticación"),
        @ApiResponse(responseCode = "403", description = "Rol insuficiente — se requiere ADMIN"),
        @ApiResponse(responseCode = "404", description = "Artículo no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ArticuloResponse> actualizar(@PathVariable Long id, @Valid @RequestBody ArticuloRequest request) {
        Articulo actualizado = new Articulo(null, request.getNombre(), request.getPrecio(), request.getImagen());
        return ResponseEntity.ok(ArticuloResponse.fromEntity(articuloService.actualizarArticulo(id, actualizado)));
    }

    @Operation(summary = "Eliminar artículo", description = "Requiere ROLE_ADMIN. Incluir token en Authorize.")
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Artículo eliminado"),
        @ApiResponse(responseCode = "401", description = "Sin autenticación"),
        @ApiResponse(responseCode = "403", description = "Rol insuficiente — se requiere ADMIN"),
        @ApiResponse(responseCode = "404", description = "Artículo no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        articuloService.borrarArticulo(id);
        return ResponseEntity.noContent().build();
    }
}
