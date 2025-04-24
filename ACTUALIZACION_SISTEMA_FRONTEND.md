# ACTUALIZACIÓN DEL SISTEMA: NUEVAS FUNCIONALIDADES

## RESUMEN DE CAMBIOS

Estimado equipo de frontend, hemos realizado importantes actualizaciones en el sistema de gestión de la peluquería canina, específicamente en el módulo de facturación. Las nuevas implementaciones incluyen:

1. **Sistema de carrito de compras** para clientes
2. **Proceso de checkout** para finalizar compras
3. **Facturación de productos** directa por parte de clientes
4. **Verificación automática de stock** durante el proceso de compra
5. **Actualización de permisos** en las rutas de API

## NUEVOS FLUJOS DE FACTURACIÓN

### 1. FLUJO DE FACTURACIÓN PARA CLIENTES (NUEVO)

Ahora los clientes pueden:

#### A. Compra de productos mediante carrito:

```
Cliente selecciona productos → Agrega al carrito → Revisa carrito → 
Proceso checkout → Confirma compra → Factura generada automáticamente
```

#### Endpoints importantes:

- **Carrito**
  - `GET /api/carrito`: Ver el contenido actual del carrito
  - `POST /api/carrito/agregar`: Agregar un producto al carrito
  - `PUT /api/carrito/actualizar`: Actualizar cantidad de un producto
  - `DELETE /api/carrito/eliminar/{productoId}`: Eliminar producto del carrito
  - `DELETE /api/carrito/vaciar`: Vaciar el carrito completamente

- **Checkout**
  - `GET /api/checkout/resumen`: Obtener resumen antes de confirmar
  - `POST /api/checkout/confirmar`: Confirmar la compra y generar factura

#### Diagrama de secuencia:
```
CLIENTE                           SISTEMA                           BASE DE DATOS
   |                                |                                    |
   |---- Explorar productos ------->|                                    |
   |<--- Lista de productos --------|                                    |
   |                                |                                    |
   |---- Agregar al carrito ------->|                                    |
   |                                |---- Verificar stock ------------->|
   |                                |<--- Confirmación stock ------------|
   |<--- Producto agregado ---------|                                    |
   |                                |                                    |
   |---- Solicitar checkout ------->|                                    |
   |<--- Resumen de compra ---------|                                    |
   |                                |                                    |
   |---- Confirmar compra --------->|                                    |
   |                                |                                    |
   |                                |---- Crear factura ---------------->|
   |                                |<--- Factura creada ----------------|
   |                                |                                    |
   |                                |---- Actualizar stock ------------->|
   |                                |<--- Stock actualizado -------------|
   |                                |                                    |
   |<--- Factura generada ----------|                                    |
```

### 2. FLUJO DE FACTURACIÓN PARA ADMINISTRADORES (EXISTENTE)

El flujo para administradores sigue siendo el mismo:

```
Admin selecciona cita completada → Factura servicio → Opcionalmente agrega productos → 
Sistema genera factura → Cliente realiza pago → Admin marca factura como pagada
```

#### Endpoints importantes:
- `POST /api/facturacion-unificada/facturar-cita/{citaId}`: Facturar una cita con servicio
- `PUT /api/facturacion-unificada/agregar-productos/{facturaId}`: Agregar productos a factura existente
- `PUT /api/facturacion-unificada/pagar/{facturaId}`: Marcar factura como pagada

## INTEGRACIÓN FRONTEND

### Componentes Sugeridos:

1. **Catálogo de Productos**:
   - Vista de productos con opciones de filtrado
   - Botón "Agregar al carrito" en cada producto
   - Mostrar disponibilidad de stock

2. **Componente Carrito**:
   - Sidebar o modal para mostrar productos en carrito
   - Opciones para aumentar/reducir cantidades
   - Botón para eliminar productos
   - Botón para proceder al checkout

3. **Página de Checkout**:
   - Resumen de productos a comprar
   - Formulario para datos adicionales (método de pago, etc.)
   - Botón para confirmar compra

4. **Historial de Facturas del Cliente**:
   - Lista de facturas pasadas
   - Opción para ver detalles de cada factura
   - Estado de la factura (PENDIENTE/PAGADA)

### Ejemplo de Implementación:

#### Agregar producto al carrito (React):

```javascript
const agregarAlCarrito = async (productoId, cantidad) => {
  try {
    const response = await axios.post('/api/carrito/agregar', 
      { productoId, cantidad },
      { 
        headers: { 
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}` 
        } 
      }
    );
    
    // Actualizar estado local del carrito
    setCarrito(response.data);
    toast.success('Producto agregado al carrito');
    
  } catch (error) {
    console.error('Error al agregar al carrito:', error);
    toast.error(error.response?.data?.mensaje || 'Error al agregar producto');
  }
};
```

#### Ver carrito (React):

```javascript
const obtenerCarrito = async () => {
  try {
    const response = await axios.get('/api/carrito', {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    
    setCarrito(response.data);
    
  } catch (error) {
    console.error('Error al obtener el carrito:', error);
    toast.error('No se pudo cargar el carrito');
  }
};
```

#### Proceso de Checkout (React):

```javascript
const confirmarCompra = async (datosPago) => {
  try {
    // 1. Obtener resumen
    const resumen = await axios.get('/api/checkout/resumen', {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    
    if (!resumen.data.stockDisponible) {
      toast.error('Algunos productos no tienen stock suficiente');
      return;
    }
    
    // 2. Confirmar compra
    const respuesta = await axios.post('/api/checkout/confirmar', 
      datosPago,
      { headers: { 'Authorization': `Bearer ${token}` } }
    );
    
    // 3. Manejar respuesta exitosa
    toast.success('¡Compra realizada con éxito!');
    navigate(`/facturas/${respuesta.data.id}`);
    
  } catch (error) {
    console.error('Error en el checkout:', error);
    toast.error(error.response?.data?.mensaje || 'Error al procesar el pago');
  }
};
```

## SEGURIDAD Y PERMISOS

Se han actualizado los permisos de seguridad para permitir a los clientes:

- Ver y gestionar su propio carrito de compras
- Crear facturas a través del proceso de checkout
- Ver sus propias facturas y los detalles de las mismas

Los administradores mantienen acceso completo a todo el sistema, incluyendo:

- Ver todas las facturas de todos los clientes
- Generar facturas a partir de citas
- Marcar facturas como pagadas

## MANEJO DE ERRORES

Es importante gestionar adecuadamente las respuestas de error:

- **Error 400**: Datos inválidos o restricciones de negocio (ej: stock insuficiente)
- **Error 401/403**: Problemas de autenticación o autorización
- **Error 404**: Recurso no encontrado
- **Error 500**: Error interno del servidor

## PRÓXIMOS PASOS

1. Implementar las nuevas vistas y componentes en la interfaz
2. Actualizar el estado global para manejar el carrito
3. Integrar los nuevos endpoints con los componentes de UI
4. Realizar pruebas de integración end-to-end
5. Desplegar la nueva versión con las funcionalidades completas

## CONTACTO

Para cualquier duda técnica relacionada con la integración, favor contactar al equipo de backend a través del canal Slack #backend-soporte.

---

¡Gracias por su atención! Estas mejoras permitirán a los clientes realizar compras directamente desde la plataforma, mejorando la experiencia de usuario y optimizando el proceso de facturación.
