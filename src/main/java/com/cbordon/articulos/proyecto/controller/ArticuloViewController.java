package com.cbordon.articulos.proyecto.controller;

import com.cbordon.articulos.proyecto.dto.request.ArticuloRequest;
import com.cbordon.articulos.proyecto.model.Articulo;
import com.cbordon.articulos.proyecto.service.ArticuloService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/articulos")
public class ArticuloViewController {

    private final ArticuloService articuloService;

    @Autowired
    public ArticuloViewController(ArticuloService articuloService) {
        this.articuloService = articuloService;
    }

    @GetMapping("/lista")
    public String listar(@RequestParam(required = false) String q, Model model) {
        boolean hayBusqueda = StringUtils.hasText(q);
        model.addAttribute("articulos", hayBusqueda
                ? articuloService.buscarPorNombre(q)
                : articuloService.listarArticulos());
        model.addAttribute("q", hayBusqueda ? q : "");
        return "lista";
    }

    @GetMapping("/agregar")
    public String mostrarFormularioAgregar(Model model) {
        model.addAttribute("articulo", new ArticuloRequest());
        return "agregar";
    }

    @PostMapping("/agregar")
    public String guardarArticulo(@Valid ArticuloRequest articulo, BindingResult result) {
        if (result.hasErrors()) {
            return "agregar";
        }
        articuloService.guardarArticulo(
                new Articulo(null, articulo.getNombre(), articulo.getPrecio(), articulo.getImagen()));
        return "redirect:/articulos/lista";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Articulo existente = articuloService.buscarArticuloPorId(id);
        ArticuloRequest form = new ArticuloRequest();
        form.setNombre(existente.getNombre());
        form.setPrecio(existente.getPrecio());
        form.setImagen(existente.getImagen());
        model.addAttribute("articulo", form);
        model.addAttribute("articuloId", id);
        return "editar";
    }

    @PostMapping("/editar/{id}")
    public String actualizarArticulo(@PathVariable Long id, @Valid ArticuloRequest articulo,
                                     BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("articuloId", id);
            return "editar";
        }
        articuloService.actualizarArticulo(id,
                new Articulo(null, articulo.getNombre(), articulo.getPrecio(), articulo.getImagen()));
        return "redirect:/articulos/lista";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarArticulo(@PathVariable Long id) {
        articuloService.borrarArticulo(id);
        return "redirect:/articulos/lista";
    }
}
