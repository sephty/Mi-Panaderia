# Asistente Virtual Consultivo de Pastelería Artesanal

Sistema inteligente para personalización de pedidos de repostería artesanal, búsqueda semántica y **filtrado estricto de seguridad alimentaria** (exclusión determinista de alérgenos como gluten, lactosa, frutos secos y huevo).

---

## Arquitectura y Cómo Funciona

El sistema opera mediante 4 capas desacopladas:

1. **Frontend Web (`/frontend`)**:
   - Chat interactivo y responsivo en HTML5, CSS3 y Vanilla JavaScript.
   - Muestra las recomendaciones de la asesora, las tarjetas de pasteles con sus etiquetas de alérgenos y el botón para reservar.
   - Tablero visual en tiempo real de los pedidos registrados en cocina.

2. **Orquestador Conversacional n8n (`/qdrant`)**:
   - Recibe la consulta del usuario vía Webhook.
   - Genera el embedding de la pregunta mediante OpenRouter (`openai/text-embedding-3-small`).
   - Detecta alérgenos solicitados (ej. celíaco, sin gluten, sin frutos secos) y construye el filtro estricto para Qdrant.
   - Genera la respuesta empática con un LLM usando exclusivamente el contexto recuperado (RAG).

3. **Base de Datos Vectorial Qdrant (`/qdrant`)**:
   - Almacena el catálogo de pasteles con vectores de 1536 dimensiones e índices de payload (`keyword` para alérgenos y `integer` para porciones).
   - Aplica **filtrado determinista de seguridad alimentaria** (`must_not`), garantizando que jamás se sugiera un pastel con ingredientes restringidos.

4. **Backend Transaccional en Spring Boot (`/mvp`)**:
   - Desarrollado en Java 17 con Spring Boot 3 y Gradle.
   - Valida la capacidad de producción diaria de la cocina (límite por defecto: 5 pasteles al día).
   - Gestiona la persistencia de la entidad `Usuario` (quién ordena) y `Pedido` en una base de datos relacional H2 persistente en archivo local.

---

## Estructura del Proyecto

```text
Panaderia/
├── qdrant/                                   # Infraestructura y flujos de IA
│   └── docker-compose.yml                    # Despliegue de Qdrant conectado a la red de n8n
│   
├── n8n/
│  └── Chatbot RAG Pasteleria Artesanal.json # Flujo completo de n8n (RAG + Qdrant + Webhook)
│
├── mvp/                                      # Backend Java 17
│   ├── build.gradle                          # Dependencias: Web, JPA, H2, Validation, Lombok
│   ├── gradlew / gradlew.bat                 # Wrapper de Gradle
│   └── src/main/java/com/panaderia/mvp/      # Código fuente (Modelos, Controladores, Servicios)
│
├── frontend/                                 # Interfaz de usuario
│   ├── index.html                            # Estructura del chat y modales
│   ├── styles.css                            # Estilos artesanales y responsivos
│   └── app.js                                # Consumo de n8n y Spring Boot con fetch()
│
└── data/                                     # Catálogo base y datos de prueba
```

---

## Guía de Ejecución Paso a Paso

### 1. Levantar la Base de Datos Vectorial (Qdrant)
En tu terminal, ingresa a la carpeta `qdrant` y levanta el contenedor Docker:

```bash
cd qdrant
docker compose up -d
```

- Dashboard de Qdrant: `http://localhost:6333/dashboard`

---

### 2. Cargar el Flujo en n8n
1. Abre tu instancia de **n8n** (`http://localhost:5678`).
2. Haz clic en **"Import from File"** y selecciona:
   `qdrant/Chatbot RAG Pasteleria Artesanal.json` (o copia el contenido del archivo y pégalo con `Ctrl + V` en el lienzo).
3. Activa el flujo (botón **Active** arriba a la derecha).
   - Webhook activo en: `http://localhost:5678/webhook/pasteleria-chat`

---

### 3. Iniciar el Backend en Spring Boot
En una terminal nueva, entra a la carpeta `mvp` y ejecuta:

```powershell
cd mvp
.\gradlew.bat bootRun
```

- API REST disponible en: `http://localhost:8080/api/pedidos`
- Consola de base de datos H2: `http://localhost:8080/h2-console`
  - **JDBC URL**: `jdbc:h2:file:./data/panaderiadb`
  - **Usuario**: `sa`
  - **Contraseña**: *(en blanco)*

---

### 4. Abrir el Chat Web
Abre en tu navegador el archivo:
`frontend/index.html`

¡Listo! Ya puedes consultar por sabores en lenguaje natural, filtrar por alergias y agendar pedidos directamente en el sistema de cocina.

---

## 🔌 Endpoints de la API REST (Spring Boot)

| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `POST` | `/api/pedidos/validar-fecha` | Valida si hay cupo de producción en cocina para una fecha |
| `POST` | `/api/pedidos` | Registra un pedido confirmado, asocia/crea al usuario y descuenta cupo |
| `GET` | `/api/pedidos` | Lista todos los pedidos registrados en cocina |
| `GET` | `/api/pedidos/capacidad?fecha=YYYY-MM-DD` | Consulta cupos disponibles y límite para una fecha |
| `GET` | `/api/usuarios` | Lista los clientes registrados en el sistema |
