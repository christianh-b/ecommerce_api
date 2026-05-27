package com.cbordon.articulos.proyecto.dto.response;

import com.cbordon.articulos.proyecto.model.Articulo;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ArticuloResponse {

    private Long id;
    private String nombre;
    private Double precio;
    private String imagen;

    public static ArticuloResponse fromEntity(Articulo articulo) {
        return new ArticuloResponse(articulo.getId(), articulo.getNombre(), articulo.getPrecio(), articulo.getImagen());
    }
}
