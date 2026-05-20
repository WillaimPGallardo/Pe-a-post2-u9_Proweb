package com.universidad.seguridad.controller;

import com.universidad.seguridad.model.Usuario;
import com.universidad.seguridad.repository.UsuarioRepository;
import com.universidad.seguridad.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UsuarioService service;
    private final UsuarioRepository repo;

    public AuthController(UsuarioService service, UsuarioRepository repo) {
        this.service = service;
        this.repo = repo;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String mostrarLogin() {
        return "auth/login";
    }

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "auth/registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute Usuario usuario,
                            BindingResult result) {
        if (result.hasErrors()) return "auth/registro";
        try {
            service.registrar(usuario);
            return "redirect:/login?registrado";
        } catch (RuntimeException e) {
            result.rejectValue("email", "error.email", e.getMessage());
            return "auth/registro";
        }
    }

    /**
     * Dashboard del usuario autenticado.
     * Carga el nombre real del usuario desde BD para demostrar la
     * mitigacion XSS de Thymeleaf con th:text (Post 2 - Paso 3).
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        model.addAttribute("usuario", auth.getName());
        model.addAttribute("roles", auth.getAuthorities());
        // Recuperar el nombre real (potencialmente con payload XSS) desde BD
        String nombrePerfil = repo.findByEmail(auth.getName())
                .map(Usuario::getNombre)
                .orElse(auth.getName());
        model.addAttribute("nombrePerfil", nombrePerfil);
        return "dashboard";
    }

    /**
     * Panel de administracion: lista usuarios.
     * El metodo service.listarTodos() esta protegido con @PreAuthorize("hasRole('ADMIN')"),
     * por lo que un USER que llegue aqui (sorteando la regla de URL)
     * obtendra AccessDeniedException -> /error/403.
     */
    @GetMapping("/admin")
    public String adminPanel(Model model) {
        model.addAttribute("usuarios", service.listarTodos());
        return "admin/panel";
    }

    /**
     * Endpoint de prueba para Checkpoint 1 (Post 2):
     * intenta llamar a service.listarTodos() siendo USER.
     * Debe lanzar AccessDeniedException -> redirige a /error/403
     * gracias a accessDeniedPage("/error/403").
     */
    @GetMapping("/probar-listar")
    public String probarListar(Model model) {
        model.addAttribute("usuarios", service.listarTodos());
        return "admin/panel";
    }
}
