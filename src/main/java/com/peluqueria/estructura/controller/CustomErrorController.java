package com.peluqueria.estructura.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", System.currentTimeMillis());
        errorDetails.put("path", path != null ? path.toString() : "unknown");
        errorDetails.put("status", status != null ? Integer.parseInt(status.toString()) : 500);
        errorDetails.put("error",
                HttpStatus.resolve(Integer.parseInt(status != null ? status.toString() : "500")).getReasonPhrase());
        errorDetails.put("message", message != null ? message.toString() : "Error inesperado");

        return new ResponseEntity<>(errorDetails,
                HttpStatus.valueOf(Integer.parseInt(status != null ? status.toString() : "500")));
    }
}
