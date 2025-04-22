# Instrucciones para Despliegue en Render

## Configuración Inicial

1. Crea una nueva cuenta en [Render](https://render.com) si aún no tienes una.
2. Conecta tu repositorio de GitHub a Render.

## Crear Web Service

1. En el dashboard de Render, haz clic en "New" y selecciona "Web Service".
2. Conecta con el repositorio que contiene tu proyecto Spring Boot.
3. Configura los siguientes valores:
   - **Name**: peluqueriacanina-api
   - **Runtime**: Java
   - **Build Command**: `./mvnw clean package -DskipTests`
   - **Start Command**: `java -jar target/estructura-0.0.1-SNAPSHOT.jar`

## Variables de Entorno

Configura las siguientes variables de entorno en la sección "Environment" de tu Web Service:

```
PORT=8080
MONGODB_URI=tu_url_de_mongo_atlas (ejemplo: mongodb+srv://usuario:contraseña@cluster.mongodb.net/peluqueria_canina)
JWT_SECRET=una_clave_secreta_muy_larga_y_segura_para_jwts
CORS_ALLOWED_ORIGINS=http://localhost:3000,https://peluqueriacanina-app.onrender.com
```

## Configuración avanzada

1. En la sección "Advanced" configura:
   - Health Check Path: `/api/health`
   - Auto Deploy: Yes

## Solución de problemas CORS

Si continúas experimentando problemas de CORS después de desplegar tu aplicación, sigue estos pasos:

1. Verifica los logs en Render para encontrar mensajes de error específicos.
2. Confirma que la URL de tu front-end está incluida en la variable `CORS_ALLOWED_ORIGINS`.
3. Realiza una petición de prueba a la API usando el endpoint `/api/health`.

## Prueba de conexión

Una vez desplegado, puedes probar la API con:

```bash
curl -v https://peluqueriacanina-api.onrender.com/api/health
```

Deberías recibir un código 200 y un mensaje indicando que el servidor está funcionando correctamente.

## Errores comunes y soluciones

### Error 1: No se puede conectar a MongoDB

**Solución**: Verifica que la URL de MongoDB es correcta y que la IP de Render está en la lista blanca de MongoDB Atlas.

### Error 2: Problemas de CORS

**Solución**: 
1. Asegúrate de que la URL exacta del front-end está en las variables de entorno.
2. Verifica en los logs si el servidor está reconociendo correctamente los orígenes permitidos.
3. Si usas credenciales en las peticiones, asegúrate de configurar `withCredentials: true` en el cliente.

### Error 3: Error en página de error

**Solución**:
Este error es causado generalmente por un problema de manejo de rutas. Verifica que todas las rutas estén correctamente definidas y que el controlador de errores personalizado esté funcionando.

## Logs y Monitorización

Render proporciona logs en tiempo real. Accede a ellos desde el panel de control de tu servicio para diagnosticar problemas.

## Nota importante sobre puertos

Render asignará automáticamente un puerto mediante la variable de entorno `PORT`. La aplicación está configurada para usar esta variable, así que no necesitas cambiar nada en el código.