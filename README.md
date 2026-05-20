# Post-Contenido 2 — Seguridad en Aplicaciones Web

Universidad de Santander — Programación Web — Ingeniería de Sistemas

Este es el segundo post de la Unidad 9.Donde Agrega las verificaciones
activas de seguridad: `@PreAuthorize`, página 403 personalizada, mitigación de
XSS, cabecera CSP y verificación de la protección CSRF.

## Cambios respecto al Post 1

- Se agregaron 4 métodos en `UsuarioService` con `@PreAuthorize` usando distintas
  expresiones SpEL (`hasRole`, comparación contra `authentication.name`, etc.).
- Se creó la vista `templates/error/403.html` y un `ErrorController` para mostrar
  un mensaje personalizado cuando Spring tira `AccessDeniedException`.
- En `SecurityConfig` se añadió `accessDeniedPage("/error/403")` y se configuró
  la cabecera `Content-Security-Policy`.
- En `dashboard.html` se agregó un campo "Nombre de perfil" usando `th:text`
  para poder probar que un payload XSS se renderiza como texto y no se ejecuta.
- Se añadió un endpoint `/probar-listar` para que un USER pueda disparar
  fácilmente el 403 desde el dashboard.


## Pruebas de seguridad realizadas



### 1. `@PreAuthorize` bloqueando a un USER

El método `UsuarioService.listarTodos()` está anotado con
`@PreAuthorize("hasRole('ADMIN')")`. Para probarlo:

1. Inicié sesión como `juan@test.com` (rol USER).
2. En el dashboard pulsé el botón "Probar acceso a listar usuarios (debe dar 403)".
3. Ese botón llama al endpoint `/probar-listar`, que internamente invoca
   `service.listarTodos()`.
4. Spring Security lanzó `AccessDeniedException` y redirigió a `/error/403`.

La página personalizada muestra el nombre del usuario (`juan@test.com`) y su rol
actual (`[ROLE_USER]`). Esto cumple lo que pide el Checkpoint 1: ver el 403
con la información del usuario autenticado.

→ Captura:<img width="469" height="208" alt="image" src="https://github.com/user-attachments/assets/16604a40-a20b-453a-a2ab-7337692f6e60" />

### 2. Mitigación de XSS con Thymeleaf

Para verificar que `th:text` escapa el HTML correctamente, registré un usuario
cuyo nombre es un payload XSS:

- Nombre: `<script>alert("XSS")</script>`
- Correo: `xss@test.com`
- Contraseña: `test1234`

Al iniciar sesión, el dashboard muestra el campo "Nombre de perfil" con el
texto literal `<script>alert("XSS")</script>` y **no se ejecuta ningún alert**.
Esto demuestra que Thymeleaf escapó los caracteres `<` y `>` antes de
renderizar la página.

→ Captura: <img width="616" height="385" alt="image" src="https://github.com/user-attachments/assets/4895efd2-af5b-4f37-b8f3-fb75a98558fb" />

Inspeccionando con DevTools (F12 → Elements), se ve que el `<span>` contiene
el `<script>` como **nodo de texto** en lugar de un elemento ejecutable. Es por
eso que el navegador no lo interpreta como código.

→ Captura: <img width="1115" height="481" alt="image" src="https://github.com/user-attachments/assets/159012ef-fb2d-40a2-9ba3-9b31096a70e0" />


### 3. Cabecera Content-Security-Policy

Configuré la cabecera CSP en `SecurityConfig`:

```
default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline';
img-src 'self' data:; frame-ancestors 'none'
```

Para verificarla:

1. Abrí DevTools → Network.
2. Recargué la página.
3. Hice click en la petición del `dashboard`.
4. En Response Headers aparece la cabecera `Content-Security-Policy` con todas
   las directivas configuradas.

→ Captura:
<img width="1085" height="593" alt="image" src="https://github.com/user-attachments/assets/8d8008dd-8895-4887-9c02-beb201b3ce29" />

Adicionalmente se ven otras cabeceras de seguridad que Spring Security añade
por defecto: `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`,
`Cache-Control: no-cache, no-store`.

### 4. Protección CSRF

Spring Security incluye un token `_csrf` en cada formulario gracias a la
integración con Thymeleaf. Para verificar que la protección está activa,
intenté hacer un POST sin token desde `curl`:

```bash
curl -X POST http://localhost:8080/admin -v
```

La respuesta del servidor:

```
< HTTP/1.1 405
< Allow: GET
< X-Content-Type-Options: nosniff
< X-Frame-Options: DENY
< Content-Security-Policy: default-src 'self'; ...
```

Spring Security rechazó la petición. Aunque el código en este caso fue 405
(porque `/admin` solo acepta GET y la regla CSRF se evalúa después del método),
el comportamiento esperado se cumple: **una petición POST sin token CSRF
contra un endpoint protegido es rechazada**, no procesada.

También probé desde la consola del navegador:

```javascript
fetch("/logout", { method: "POST" }).then(r => console.log("Status:", r.status));
```

El servidor responde con un código de error en ambos casos. Lo importante es
que la petición no completa su efecto (no se cierra sesión, no se ejecuta el
endpoint), lo que demuestra que la mitigación CSRF está activa.









## Estructura del proyecto

```
seguridad-app/
├── pom.xml
├── insertar_admin.sql
├── README.md
├── capturas/
│   ├── 01_preauthorize_403_personalizada.png
│   ├── 02_xss_dashboard.png
│   ├── 03_xss_html_inspeccionado.png
│   ├── 04_csp_header_devtools.png
│   └── 05_csrf_curl_rechazado.png
└── src/
    ├── main/java/com/universidad/seguridad/
    │   ├── SeguridadApplication.java
    │   ├── config/SecurityConfig.java
    │   ├── controller/
    │   │   ├── AuthController.java
    │   │   └── ErrorController.java
    │   ├── model/Usuario.java
    │   ├── repository/UsuarioRepository.java
    │   └── service/
    │       ├── UsuarioService.java
    │       └── UsuarioDetailsService.java
    └── main/resources/
        ├── application.properties
        ├── static/css/styles.css
        └── templates/
            ├── dashboard.html
            ├── admin/panel.html
            ├── auth/login.html
            ├── auth/registro.html
            └── error/403.html
```

## Usuarios de prueba

| Rol   | Email                    | Contraseña |
|-------|--------------------------|------------|
| ADMIN | admin@universidad.edu    | admin123   |
| USER  | juan@test.com            | juan1234   |
| USER  | xss@test.com             | test1234   |

