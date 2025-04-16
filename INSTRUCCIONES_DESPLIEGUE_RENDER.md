# Instrucciones para Desplegar en Render

Este documento contiene instrucciones paso a paso para desplegar esta aplicación Spring Boot de peluquería canina en Render.

## Pasos Previos

1. Asegúrate de tener tu cuenta de MongoDB Atlas configurada según las instrucciones del archivo `DESPLIEGUE_EN_RENDER.txt`.
2. Verifica que tus colecciones estén creadas en MongoDB Atlas.
3. Compila la aplicación localmente para verificar que todo funciona correctamente.

## Pasos para el Despliegue

### 1. Compilar el proyecto

```bash
./mvnw clean package -DskipTests
```

### 2. Iniciar sesión en Render

1. Visita https://render.com/ e inicia sesión en tu cuenta
2. Si no tienes una cuenta, regístrate para obtener una (puedes usar GitHub para un registro rápido)

### 3. Crear un nuevo Web Service

1. Haz clic en "New+" en la parte superior derecha
2. Selecciona "Web Service"
3. Conecta tu repositorio Git o sube directamente el código
   - Si usas GitHub, tendrás que autorizar a Render para acceder a tu repositorio

### 4. Configurar el servicio

Completa la información del formulario:

- **Name**: PeluqueriaCanina-API
- **Region**: Selecciona la más cercana a tus usuarios
- **Branch**: main (o la rama que estés usando)
- **Root Directory**: (déjalo en blanco si tu proyecto está en la raíz)
- **Runtime**: Docker

### 5. Configurar variables de entorno

En la sección "Advanced", añade las siguientes variables de entorno:

- `SPRING_DATA_MONGODB_URI`: mongodb+srv://joooohanenciso:kRyEZZOUseC0pSC7@cluster0.hw1quvh.mongodb.net/peluqueria_canina?retryWrites=true&w=majority&appName=Cluster0
- `JWT_SECRET`: tu_clave_secreta_muy_larga_y_segura
- `SERVER_PORT`: 8080
- `SPRING_PROFILES_ACTIVE`: prod

### 6. Configurar el plan

- Para pruebas: selecciona el plan **Free**
- Para producción: selecciona al menos el plan **Individual** ($7/mes) para mejor rendimiento

### 7. Crear el servicio

Haz clic en "Create Web Service" y espera a que Render compile y despliegue tu aplicación (puede tardar unos minutos).

### 8. Verificar el despliegue

- Una vez finalizado el despliegue, Render te proporcionará una URL (como https://peluqueriacanina-api.onrender.com)
- Verifica que la aplicación está funcionando correctamente haciendo una petición a:
  ```
  https://peluqueriacanina-api.onrender.com/api/health
  ```
  Deberías recibir un JSON con el estado "UP" y la hora actual.

## Configuración para Flutter/Dart

Para conectar tu aplicación Flutter:

1. Usa el paquete `dio` o `http` para las peticiones HTTP
2. Configura la URL base a tu servicio de Render
3. Implementa interceptores para gestionar los tokens JWT
4. Usa almacenamiento seguro para los tokens con `flutter_secure_storage`

Ejemplo básico de configuración en Flutter:

```dart
import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

class ApiService {
  final Dio _dio = Dio();
  final FlutterSecureStorage _storage = FlutterSecureStorage();
  
  ApiService() {
    _dio.options.baseUrl = 'https://peluqueriacanina-api.onrender.com/api';
    _dio.interceptors.add(InterceptorsWrapper(
      onRequest: (options, handler) async {
        final token = await _storage.read(key: 'jwt_token');
        if (token != null) {
          options.headers['Authorization'] = 'Bearer $token';
        }
        return handler.next(options);
      },
      onError: (error, handler) async {
        if (error.response?.statusCode == 401) {
          // Manejar token expirado
          // Redirigir al login
        }
        return handler.next(error);
      }
    ));
  }
  
  // Ejemplo de método de login
  Future<bool> login(String username, String password) async {
    try {
      final response = await _dio.post('/auth/login', 
        data: {'username': username, 'password': password});
      
      if (response.statusCode == 200) {
        final token = response.data['token'];
        await _storage.write(key: 'jwt_token', value: token);
        return true;
      }
      return false;
    } catch (e) {
      print('Error en login: $e');
      return false;
    }
  }
}
```

## Problemas comunes y soluciones

1. **Error de conexión con MongoDB Atlas**:
   - Verifica que la IP de Render esté en la lista de acceso de MongoDB Atlas
   - Comprueba la cadena de conexión y credenciales

2. **El servidor tarda en responder la primera vez**:
   - Normal en el plan gratuito de Render (el servicio "duerme" tras 15 minutos de inactividad)
   - Implementa una pantalla de carga o splash screen en tu app Flutter

3. **Problemas CORS**:
   - Verifica la configuración CORS en `CorsConfig.java`
   - Asegúrate de que todos los orígenes necesarios estén permitidos