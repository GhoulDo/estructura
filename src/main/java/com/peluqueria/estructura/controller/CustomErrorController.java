package com.peluqueria.estructura.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
public class CustomErrorController implements ErrorController {

    private final ErrorAttributes errorAttributes;

    @Autowired
    public CustomErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping(path = "/error", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        WebRequest webRequest = new ServletWebRequest(request);

        // Incluir más detalles de error para entornos de desarrollo
        ErrorAttributeOptions options = ErrorAttributeOptions.defaults()
                .including(ErrorAttributeOptions.Include.MESSAGE)
                .including(ErrorAttributeOptions.Include.BINDING_ERRORS);

        Map<String, Object> errorAttributes = this.errorAttributes.getErrorAttributes(
                webRequest, options);

        HttpStatus status = getStatus(request);

        // Personalizar el mensaje según el código de estado
        String message = (String) errorAttributes.get("message");
        if (message == null || message.isEmpty()) {
            switch (status) {
                case NOT_FOUND:
                    message = "Recurso no encontrado";
                    break;
                case FORBIDDEN:
                    message = "Acceso prohibido";
                    break;
                case UNAUTHORIZED:
                    message = "No autorizado";
                    break;
                case BAD_REQUEST:
                    message = "Solicitud incorrecta";
                    break;
                default:
                    message = "Error en el servidor";
            }
            errorAttributes.put("message", message);
        }

        return new ResponseEntity<>(errorAttributes, status);
    }

    private HttpStatus getStatus(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        if (statusCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        try {
            return HttpStatus.valueOf(statusCode);
        } catch (Exception ex) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
