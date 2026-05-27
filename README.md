# E-Commerce REST API

API RESTful para gestión de productos de e-commerce construida con Spring Boot 4, autenticación JWT y arquitectura en capas. Incluye suite completa de tests con Mockito + JUnit 5.

[![API en vivo](https://img.shields.io/badge/API-en%20vivo-brightgreen?logo=render)](https://ecommerce-api-i8r7.onrender.com/api/articulos)
[![Swagger UI](https://img.shields.io/badge/Swagger-UI-85EA2D?logo=swagger)](https://ecommerce-api-i8r7.onrender.com/swagger-ui.html)

![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.0-brightgreen?logo=springboot)
![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-blue?logo=springsecurity)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14+-blue?logo=postgresql)
![JUnit](https://img.shields.io/badge/Tests-46%20tests-success?logo=junit5)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

---

## Características

- **CRUD completo** de artículos (nombre, precio, imagen) con validación de entrada en API y vistas
- **Documentación interactiva** con Swagger UI — autenticación JWT integrada en la UI
- **Búsqueda por nombre** — `GET /articulos/lista?q=término`, insensible a mayúsculas
- **Datos de demostración precargados** — 6 productos con imágenes locales al iniciar con BD vacía
- **Autenticación JWT stateless** — registro, login y tokens Bearer
- **Control de acceso por roles** — `ROLE_USER` y `ROLE_ADMIN`
- **Manejo global de excepciones** con respuestas de error uniformes en JSON
- **Patrón DTO** — desacoplamiento total entre la capa HTTP y la entidad JPA
- **46 tests** con Mockito, JUnit 5 y MockMvc — servicios y controladores cubiertos
- **Vistas Thymeleaf** con diseño estilo MercadoLibre (Tailwind CSS)

---

## Stack tecnológico

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Java 17 |
| Framework | Spring Boot 4.0.0 |
| Seguridad | Spring Security + JJWT 0.12.6 |
| Persistencia | Spring Data JPA + Hibernate |
| Base de datos | PostgreSQL 14+ |
| Validación | Jakarta Bean Validation |
| Vistas | Thymeleaf + Tailwind CSS |
| Documentación | SpringDoc OpenAPI 3 (Swagger UI) |
| Build | Maven |
| Testing | JUnit 5 + Mockito 5 + MockMvc |

---

## Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│                        Cliente HTTP                          │
└────────────────────────────┬────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────┐
│              Spring Security Filter Chain                    │
│  JwtAuthFilter → extrae y valida el Bearer token             │
└────────────────────────────┬────────────────────────────────┘
                             │
        ┌────────────────────┼───────────────────┐
        │                    │                   │
┌───────▼──────┐   ┌─────────▼──────┐  ┌────────▼─────────┐
│AuthController│   │ArticuloCtrl    │  │ArticuloViewCtrl  │
│  /api/auth   │   │ /api/articulos │  │  /articulos      │
└───────┬──────┘   └────────┬───────┘  └────────┬─────────┘
        │                   │                   │
        │             [DTO ↕ Entity mapping]     │
        │                   │                   │
┌───────▼──────┐   ┌────────▼───────┐           │
│ AuthService  │   │ArticuloService │◄──────────┘
└───────┬──────┘   └────────┬───────┘
        │                   │
┌───────▼──────┐   ┌────────▼───────┐
│UsuarioRepo   │   │ArticuloRepo    │
└───────┬──────┘   └────────┬───────┘
        │                   │
        └─────────┬─────────┘
                  │
         ┌────────▼───────┐
         │   MySQL DB      │
         └────────────────┘
```

---

## Estructura del proyecto

```
src/
├── main/
│   ├── java/com/cbordon/articulos/proyecto/
│   │   ├── config/
│   │   │   ├── DataInitializer.java         # Carga 6 productos demo al iniciar con BD vacía
│   │   │   └── OpenApiConfig.java           # Swagger UI — info de la API y esquema JWT Bearer
│   │   ├── controller/
│   │   │   ├── ArticuloController.java      # REST API — /api/articulos
│   │   │   ├── ArticuloViewController.java  # Vistas Thymeleaf — /articulos
│   │   │   └── AuthController.java          # Autenticación — /api/auth
│   │   ├── dto/
│   │   │   ├── request/
│   │   │   │   ├── ArticuloRequest.java     # Entrada validada (nombre, precio, imagen?)
│   │   │   │   ├── LoginRequest.java
│   │   │   │   └── RegisterRequest.java
│   │   │   └── response/
│   │   │       ├── ApiErrorResponse.java    # Formato uniforme de errores
│   │   │       ├── ArticuloResponse.java
│   │   │       └── AuthResponse.java        # Token + username + role
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.java          # @RestControllerAdvice
│   │   │   ├── ResourceNotFoundException.java
│   │   │   └── UsernameAlreadyExistsException.java  # Dispara 409 al registrar username duplicado
│   │   ├── model/
│   │   │   ├── Articulo.java
│   │   │   ├── Role.java                    # Enum: USER | ADMIN
│   │   │   └── Usuario.java                 # Implementa UserDetails
│   │   ├── repository/
│   │   │   ├── ArticuloRepository.java
│   │   │   └── UsuarioRepository.java
│   │   ├── security/
│   │   │   ├── JwtAuthFilter.java           # OncePerRequestFilter
│   │   │   ├── JwtUtils.java                # Generación y validación HS256
│   │   │   ├── SecurityConfig.java          # Reglas de acceso + beans
│   │   │   └── UserDetailsServiceImpl.java
│   │   └── service/
│   │       ├── ArticuloService.java + Impl
│   │       └── AuthService.java + Impl
│   └── resources/
│       ├── static/
│       │   └── images/                      # Imágenes de los productos demo
│       └── templates/                       # Vistas Thymeleaf (lista, agregar, editar)
└── test/java/com/cbordon/articulos/proyecto/
    ├── controller/
    │   ├── ArticuloControllerTest.java  # 17 tests
    │   └── AuthControllerTest.java      # 11 tests
    └── service/
        ├── ArticuloServiceImplTest.java # 11 tests
        └── AuthServiceImplTest.java     # 7 tests
```

---

## Prerrequisitos

- Java 17+
- Maven 3.8+
- PostgreSQL 14+

---

## Instalación y configuración

### 1. Clonar el repositorio

```bash
git clone https://github.com/christianh-b/ecommerce-api.git
cd ecommerce-api
```

### 2. Crear la base de datos

```sql
CREATE DATABASE articulos_db;
```

### 3. Configurar variables de entorno

Las credenciales se leen de variables de entorno con valores por defecto para desarrollo local. Para producción, define las siguientes variables antes de arrancar:

| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `DB_URL` | URL JDBC de la base de datos | `jdbc:postgresql://host:5432/articulos_db` |
| `DB_USERNAME` | Usuario de MySQL | `app_user` |
| `DB_PASSWORD` | Contraseña de MySQL | `contraseña_segura` |
| `JWT_SECRET` | Clave Base64 para firmar tokens HS256 (mín. 32 bytes) | `<base64>` |

Para desarrollo local no hace falta definir ninguna — el archivo `application.properties` ya incluye defaults que apuntan a `localhost`.

> **Importante:** Nunca commitas credenciales de producción al repositorio. El `.gitignore` ya excluye `.env` y archivos de secrets.

### 4. Ejecutar la aplicación

```bash
mvn spring-boot:run
```

La API estará disponible en `http://localhost:8081`.

La documentación interactiva Swagger UI estará en `http://localhost:8081/swagger-ui.html`.

**Producción (Render):**
- API: `https://ecommerce-api-i8r7.onrender.com/api/articulos`
- Swagger UI: `https://ecommerce-api-i8r7.onrender.com/swagger-ui.html`

> Al arrancar con la base de datos vacía, `DataInitializer` inserta automáticamente 6 productos de demostración con sus imágenes. Si la tabla ya tiene datos, la carga se omite.

---

## Documentación de la API

### Autenticación

#### `POST /api/auth/register` — Registrar usuario

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{ "username": "juan", "password": "secreto123" }'
```

**Respuesta `201 Created`:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "juan",
  "role": "USER"
}
```

#### `POST /api/auth/login` — Iniciar sesión

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{ "username": "juan", "password": "secreto123" }'
```

**Respuesta `200 OK`:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "juan",
  "role": "USER"
}
```

---

### Artículos

Usar el token en el header: `Authorization: Bearer <token>`

#### `GET /api/articulos` — Listar todos *(público)*

```bash
curl http://localhost:8080/api/articulos
```

**Respuesta `200 OK`:**
```json
[
  { "id": 1, "nombre": "Samsung Galaxy S24 128GB", "precio": 1299999.00, "imagen": "/images/galaxy_s24.jpg" },
  { "id": 2, "nombre": "PlayStation 5 Slim 1TB",   "precio":  699999.00, "imagen": "/images/ps5_slim.webp" }
]
```

#### `GET /api/articulos/{id}` — Obtener por ID *(público)*

```bash
curl http://localhost:8080/api/articulos/1
```

**Respuestas:** `200 OK` con el artículo / `404 Not Found` si no existe

#### `POST /api/articulos` — Crear artículo *(requiere ROLE_ADMIN)*

```bash
curl -X POST http://localhost:8080/api/articulos \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{ "nombre": "Teclado Mecánico", "precio": 120.00, "imagen": "/images/teclado.jpg" }'
```

El campo `imagen` es opcional; puede omitirse o enviarse `null`.

**Respuesta `201 Created`:**
```json
{ "id": 3, "nombre": "Teclado Mecánico", "precio": 120.00, "imagen": "/images/teclado.jpg" }
```

#### `PUT /api/articulos/{id}` — Actualizar artículo *(requiere ROLE_ADMIN)*

```bash
curl -X PUT http://localhost:8080/api/articulos/3 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{ "nombre": "Teclado Mecánico RGB", "precio": 135.00, "imagen": "/images/teclado_rgb.jpg" }'
```

**Respuestas:** `200 OK` con artículo actualizado / `404 Not Found` si no existe

#### `DELETE /api/articulos/{id}` — Eliminar artículo *(requiere ROLE_ADMIN)*

```bash
curl -X DELETE http://localhost:8080/api/articulos/3 \
  -H "Authorization: Bearer <token>"
```

**Respuestas:** `204 No Content` / `404 Not Found` si no existe

---

### Tabla de permisos

| Endpoint | Método | Acceso |
|----------|--------|--------|
| `/api/auth/register` | POST | Público |
| `/api/auth/login` | POST | Público |
| `/api/articulos` | GET | Público |
| `/api/articulos/{id}` | GET | Público |
| `/api/articulos` | POST | `ROLE_ADMIN` |
| `/api/articulos/{id}` | PUT | `ROLE_ADMIN` |
| `/api/articulos/{id}` | DELETE | `ROLE_ADMIN` |
| `/articulos/**` | — | Público (UI web) |
| `/images/**` | — | Público (imágenes estáticas) |
| `/swagger-ui/**` | — | Público (documentación interactiva) |
| `/v3/api-docs/**` | — | Público (spec OpenAPI JSON) |

---

### Respuestas de error

Todos los errores siguen el mismo formato JSON:

```json
{
  "timestamp": "2025-05-26 14:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Artículo con id 99 no encontrado",
  "path": "/api/articulos/99"
}
```

| Código | Causa |
|--------|-------|
| `400 Bad Request` | Validación fallida (nombre vacío, precio negativo, etc.) |
| `401 Unauthorized` | Token ausente, inválido o expirado |
| `403 Forbidden` | Rol insuficiente para la operación |
| `404 Not Found` | Recurso inexistente |
| `409 Conflict` | Username ya registrado |
| `500 Internal Server Error` | Error inesperado del servidor |

---

## Flujo de autenticación

```
Cliente                   Servidor
  │                          │
  │── POST /api/auth/login ──►│
  │   { username, password }  │  AuthenticationManager.authenticate()
  │                          │  → BCryptPasswordEncoder verifica hash
  │                          │  → JwtUtils.generateToken() (HS256)
  │◄── { token, role } ──────│
  │                          │
  │── GET /api/articulos ────►│  (request público — sin token)
  │◄── [ lista ] ────────────│
  │                          │
  │── DELETE /api/art/1 ─────►│  Authorization: Bearer eyJ...
  │   Bearer token            │  JwtAuthFilter extrae y valida
  │                          │  SecurityContext ← Authentication
  │                          │  SecurityConfig verifica ROLE_ADMIN
  │◄── 204 No Content ───────│
```

---

## Tests

### Ejecutar todos los tests

```bash
mvn test
```

### Cobertura por capa

| Archivo | Tests | Tecnología |
|---------|-------|-----------|
| `ArticuloServiceImplTest` | 11 | Mockito puro + `@InjectMocks` |
| `AuthServiceImplTest` | 7 | Mockito puro + `@InjectMocks` |
| `ArticuloControllerTest` | 17 | `@WebMvcTest` + MockMvc + `SecurityMockMvcRequestPostProcessors` |
| `AuthControllerTest` | 11 | `@WebMvcTest` + MockMvc |
| **Total** | **46** | JUnit 5 + Mockito 5 |

Los tests de controlador validan:
- Respuestas HTTP correctas para cada endpoint
- Reglas de seguridad (401 sin token, 403 con rol incorrecto)
- Validación de entrada (400 con campos inválidos)
- Formato del cuerpo de error del `GlobalExceptionHandler`

> Los tests de servicio y controlador corren **sin base de datos**. Solo `ProyectoArticulosFinalApplicationTests` requiere MySQL y está marcado como `@Disabled`.

---

## Vistas web (Thymeleaf)

La aplicación incluye una interfaz web en `/articulos` con diseño estilo MercadoLibre (Tailwind CSS):

| Ruta | Descripción |
|------|-------------|
| `/articulos/lista` | Grid de cards con todos los artículos |
| `/articulos/lista?q=término` | Búsqueda de artículos por nombre (insensible a mayúsculas) |
| `/articulos/agregar` | Formulario para publicar un nuevo artículo |
| `/articulos/editar/{id}` | Formulario para editar un artículo existente |

---
## Autor

**Christian Bordon**  
[GitHub](https://github.com/christianh-b/) · [LinkedIn](https://www.linkedin.com/in/christianh-bordon/)
