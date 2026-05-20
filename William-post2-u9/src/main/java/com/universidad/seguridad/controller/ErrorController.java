package com.universidad.seguridad.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador de errores personalizados.
 * (Post-Contenido 2 - Paso 2)
 */
@Controller
public class ErrorController {

    @GetMapping("/error/403")
    public String accesoDenegado() {
        return "error/403";
    }
}
