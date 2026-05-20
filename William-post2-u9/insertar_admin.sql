-- =============================================================
-- Script: insertar_admin.sql
-- Unidad 9 - Seguridad en Aplicaciones Web
-- =============================================================
-- Ejecutar despues de levantar la aplicacion al menos una vez,
-- para que Hibernate cree la tabla `usuarios`.
--
-- Uso desde la terminal:
--   $ mysql -u appuser -p estudiantes_db < insertar_admin.sql
--
-- Credenciales de admin que crea este script:
--   Email:       admin@universidad.edu
--   Contrasena:  admin123
--
-- IMPORTANTE: el hash BCrypt incluido es VALIDO para "admin123".
-- Si prefieres generar tu propio hash con el test GenerarHashTest,
-- reemplaza el valor en el INSERT.
-- =============================================================

USE estudiantes_db;

INSERT INTO usuarios (nombre, email, contrasenia, rol, activo)
VALUES (
    'Administrador',
    'admin@universidad.edu',
    '$2a$12$5lnlZPA/hQ4zaaXEnL.ULu5GctQs376.YRKqC.BVu1A1DYZRdxbwq',
    'ROLE_ADMIN',
    1
);

-- Verificar:
SELECT id, nombre, email, rol, activo FROM usuarios;
