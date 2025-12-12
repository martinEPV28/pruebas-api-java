package com.example.productapi.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador que genera una especificación OpenAPI 3.0.0 completa.
 * Documenta todos los endpoints del API con esquemas, parámetros y respuestas.
 */
@RestController
@RequestMapping("/api-docs")
public class OpenApiController {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Retorna la especificación OpenAPI en formato JSON.
     * Ruta: GET /api-docs/openapi
     */
    @GetMapping(path = "/openapi", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getOpenApiSpec() {
        try {
            ObjectNode openapi = mapper.createObjectNode();

            // OpenAPI version
            openapi.put("openapi", "3.0.0");

            // Info
            ObjectNode info = openapi.putObject("info");
            info.put("title", "Product API");
            info.put("version", "1.0.0");
            info.put("description", "API para comparar productos con autenticación JWT y persistencia en PostgreSQL");
            
            ObjectNode contact = info.putObject("contact");
            contact.put("name", "Desarrollo");
            contact.put("url", "https://example.com");
            contact.put("email", "dev@example.com");
            
            ObjectNode license = info.putObject("license");
            license.put("name", "MIT");
            license.put("url", "https://opensource.org/licenses/MIT");

            // Servers
            ArrayNode servers = openapi.putArray("servers");
            ObjectNode server = servers.addObject();
            server.put("url", "http://localhost:8080");
            server.put("description", "Local development server");

            // Paths
            ObjectNode paths = openapi.putObject("paths");
            
            // ============ PRODUCTOS ============
            
            // /api/products - GET y POST
            ObjectNode productsPath = paths.putObject("/api/products");
            
            // GET /api/products - Listar todos
            ObjectNode getProducts = productsPath.putObject("get");
            getProducts.put("summary", "Listar todos los productos");
            getProducts.put("operationId", "getProducts");
            getProducts.put("tags", mapper.createArrayNode().add("Products"));
            getProducts.putArray("security").addObject().putArray("bearerAuth");
            ObjectNode prodResponses = getProducts.putObject("responses");
            ObjectNode prod200 = prodResponses.putObject("200");
            prod200.put("description", "Lista de productos obtenida exitosamente");
            prodResponses.putObject("401").put("description", "Token JWT inválido o expirado");

            // POST /api/products - Crear nuevo
            ObjectNode postProduct = productsPath.putObject("post");
            postProduct.put("summary", "Crear nuevo producto");
            postProduct.put("operationId", "createProduct");
            postProduct.put("tags", mapper.createArrayNode().add("Products"));
            postProduct.putArray("security").addObject().putArray("bearerAuth");
            ObjectNode createProdBody = postProduct.putObject("requestBody");
            createProdBody.put("required", true);
            ObjectNode createProdContent = createProdBody.putObject("content");
            ObjectNode createProdJson = createProdContent.putObject("application/json");
            createProdJson.putObject("schema").put("$ref", "#/components/schemas/Product");
            ObjectNode postResponses = postProduct.putObject("responses");
            postResponses.putObject("201").put("description", "Producto creado exitosamente");
            postResponses.putObject("400").put("description", "Datos inválidos");
            postResponses.putObject("401").put("description", "Token JWT inválido");

            // /api/products/{id} - GET, PUT, DELETE
            ObjectNode productByIdPath = paths.putObject("/api/products/{id}");
            
            // GET /api/products/{id} - Obtener por ID
            ObjectNode getProductById = productByIdPath.putObject("get");
            getProductById.put("summary", "Obtener producto por ID");
            getProductById.put("operationId", "getProductById");
            getProductById.put("tags", mapper.createArrayNode().add("Products"));
            getProductById.putArray("security").addObject().putArray("bearerAuth");
            ArrayNode getParams = getProductById.putArray("parameters");
            ObjectNode getIdParam = getParams.addObject();
            getIdParam.put("name", "id");
            getIdParam.put("in", "path");
            getIdParam.put("required", true);
            getIdParam.put("schema", mapper.createObjectNode().put("type", "integer"));
            ObjectNode byIdResponses = getProductById.putObject("responses");
            byIdResponses.putObject("200").put("description", "Producto encontrado");
            byIdResponses.putObject("404").put("description", "Producto no encontrado");
            byIdResponses.putObject("401").put("description", "Token JWT inválido");

            // PUT /api/products/{id} - Actualizar
            ObjectNode putProduct = productByIdPath.putObject("put");
            putProduct.put("summary", "Actualizar producto");
            putProduct.put("operationId", "updateProduct");
            putProduct.put("tags", mapper.createArrayNode().add("Products"));
            putProduct.putArray("security").addObject().putArray("bearerAuth");
            ArrayNode putParams = putProduct.putArray("parameters");
            ObjectNode putIdParam = putParams.addObject();
            putIdParam.put("name", "id");
            putIdParam.put("in", "path");
            putIdParam.put("required", true);
            putIdParam.put("schema", mapper.createObjectNode().put("type", "integer"));
            ObjectNode updateProdBody = putProduct.putObject("requestBody");
            updateProdBody.put("required", true);
            ObjectNode updateProdContent = updateProdBody.putObject("content");
            ObjectNode updateProdJson = updateProdContent.putObject("application/json");
            updateProdJson.putObject("schema").put("$ref", "#/components/schemas/Product");
            ObjectNode putResponses = putProduct.putObject("responses");
            putResponses.putObject("200").put("description", "Producto actualizado");
            putResponses.putObject("404").put("description", "Producto no encontrado");
            putResponses.putObject("401").put("description", "Token JWT inválido");

            // DELETE /api/products/{id} - Eliminar
            ObjectNode deleteProduct = productByIdPath.putObject("delete");
            deleteProduct.put("summary", "Eliminar producto");
            deleteProduct.put("operationId", "deleteProduct");
            deleteProduct.put("tags", mapper.createArrayNode().add("Products"));
            deleteProduct.putArray("security").addObject().putArray("bearerAuth");
            ArrayNode delParams = deleteProduct.putArray("parameters");
            ObjectNode delIdParam = delParams.addObject();
            delIdParam.put("name", "id");
            delIdParam.put("in", "path");
            delIdParam.put("required", true);
            delIdParam.put("schema", mapper.createObjectNode().put("type", "integer"));
            ObjectNode delResponses = deleteProduct.putObject("responses");
            delResponses.putObject("200").put("description", "Producto eliminado");
            delResponses.putObject("404").put("description", "Producto no encontrado");
            delResponses.putObject("401").put("description", "Token JWT inválido");

            // ============ AUTENTICACIÓN ============
            
            // POST /api/auth/token
            ObjectNode authPath = paths.putObject("/api/auth/token");
            ObjectNode postAuth = authPath.putObject("post");
            postAuth.put("summary", "Generar token JWT");
            postAuth.put("operationId", "generateToken");
            postAuth.put("tags", mapper.createArrayNode().add("Authentication"));
            postAuth.put("description", "Autentica un usuario y retorna un token JWT válido por 24 horas");
            // Request body
            ObjectNode authBody = postAuth.putObject("requestBody");
            authBody.put("required", true);
            ObjectNode authContent = authBody.putObject("content");
            ObjectNode authJson = authContent.putObject("application/json");
            ObjectNode authSchema = authJson.putObject("schema");
            authSchema.put("type", "object");
            ObjectNode authProps = authSchema.putObject("properties");
            authProps.putObject("username").put("type", "string").put("example", "admin");
            authProps.putObject("password").put("type", "string").put("example", "password");
            authSchema.putArray("required").add("username").add("password");
            ObjectNode authResponses = postAuth.putObject("responses");
            ObjectNode auth200 = authResponses.putObject("200");
            auth200.put("description", "Token generado exitosamente");
            authResponses.putObject("401").put("description", "Credenciales inválidas");
            authResponses.putObject("400").put("description", "Datos faltantes o inválidos");

            // ============ USUARIOS ============
            
            // GET /api/users/username/{username} - Validar usuario por username
            ObjectNode userByUsernamePath = paths.putObject("/api/users/username/{username}");
            ObjectNode getUserByUsername = userByUsernamePath.putObject("get");
            getUserByUsername.put("summary", "Validar y obtener usuario por username");
            getUserByUsername.put("operationId", "getUserByUsername");
            getUserByUsername.put("description", "Valida la existencia de un usuario por su nombre de usuario");
            getUserByUsername.put("tags", mapper.createArrayNode().add("Users"));
            getUserByUsername.putArray("security").addObject().putArray("bearerAuth");
            ArrayNode usernameParams = getUserByUsername.putArray("parameters");
            ObjectNode usernameParam = usernameParams.addObject();
            usernameParam.put("name", "username");
            usernameParam.put("in", "path");
            usernameParam.put("required", true);
            usernameParam.put("description", "Nombre de usuario a validar");
            ObjectNode usernameSchema = usernameParam.putObject("schema");
            usernameSchema.put("type", "string");
            usernameSchema.put("example", "admin");
            ObjectNode userByUsernameResponses = getUserByUsername.putObject("responses");
            ObjectNode user200 = userByUsernameResponses.putObject("200");
            user200.put("description", "Usuario encontrado y validado");
            ObjectNode user200Content = user200.putObject("content");
            ObjectNode user200Json = user200Content.putObject("application/json");
            user200Json.putObject("schema").put("$ref", "#/components/schemas/User");
            userByUsernameResponses.putObject("404").put("description", "Usuario no encontrado");
            userByUsernameResponses.putObject("401").put("description", "Token JWT inválido o expirado");

            // Components
            ObjectNode components = openapi.putObject("components");
            
            // Security Schemes
            ObjectNode securitySchemes = components.putObject("securitySchemes");
            ObjectNode bearerAuth = securitySchemes.putObject("bearerAuth");
            bearerAuth.put("type", "http");
            bearerAuth.put("scheme", "bearer");
            bearerAuth.put("bearerFormat", "JWT");
            bearerAuth.put("description", "Token JWT obtenido desde POST /api/auth/token");

            // Schemas
            ObjectNode schemas = components.putObject("schemas");
            
            // Product Schema
            ObjectNode productSchema = schemas.putObject("Product");
            productSchema.put("type", "object");
            ObjectNode productProps = productSchema.putObject("properties");
            productProps.putObject("id").put("type", "integer");
            productProps.putObject("name").put("type", "string").put("example", "Laptop Dell");
            productProps.putObject("price").put("type", "number").put("example", 1299.99);
            productProps.putObject("rating").put("type", "number").put("example", 4.5);
            productProps.putObject("imageUrl").put("type", "string");
            productProps.putObject("description").put("type", "string");
            productProps.putObject("specifications").put("type", "object");
            productProps.putObject("createdAt").put("type", "string").put("format", "date-time");
            productProps.putObject("updatedAt").put("type", "string").put("format", "date-time");

            // User Schema
            ObjectNode userSchema = schemas.putObject("User");
            userSchema.put("type", "object");
            ObjectNode userProps = userSchema.putObject("properties");
            userProps.putObject("id").put("type", "integer");
            userProps.putObject("username").put("type", "string").put("example", "admin");
            userProps.putObject("active").put("type", "boolean").put("example", true);
            userProps.putObject("createdAt").put("type", "string").put("format", "date-time");
            userProps.putObject("updatedAt").put("type", "string").put("format", "date-time");

            // Error Schema
            ObjectNode errorSchema = schemas.putObject("Error");
            errorSchema.put("type", "object");
            ObjectNode errorProps = errorSchema.putObject("properties");
            errorProps.putObject("status").put("type", "integer");
            errorProps.putObject("message").put("type", "string");
            errorProps.putObject("error").put("type", "string");
            errorProps.putObject("timestamp").put("type", "string").put("format", "date-time");

            return ResponseEntity.ok(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(openapi));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}

