package com.universidad.seguridad;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Clase temporal para generar el hash BCrypt del usuario ADMIN.
 *
 * Cómo usarla:
 *  1. Hacer click derecho sobre el método generarHashAdmin() en VS Code y
 *     ejecutar "Run Test" (requiere la extensión Java Test Runner) o ejecutar:
 *         ./mvnw test -Dtest=GenerarHashTest#generarHashAdmin
 *  2. Copiar el hash impreso en la consola.
 *  3. Insertar el usuario ADMIN en MySQL con ese hash (ver README.md, Paso 7).
 */
@SpringBootTest
class GenerarHashTest {

    @Autowired
    PasswordEncoder encoder;

    @Test
    void generarHashAdmin() {
        String hash = encoder.encode("admin123");
        System.out.println("\n========================================");
        System.out.println("HASH BCrypt para 'admin123':");
        System.out.println(hash);
        System.out.println("========================================\n");
        // Copiar el hash resultante para el INSERT de MySQL
    }
}
