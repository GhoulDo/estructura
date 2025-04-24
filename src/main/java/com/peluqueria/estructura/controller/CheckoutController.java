package com.peluqueria.estructura.controller;

import com.peluqueria.estructura.dto.CheckoutResumenDTO;
import com.peluqueria.estructura.entity.Factura;
import com.peluqueria.estructura.service.CheckoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    // Endpoint para obtener el resumen del checkout
    @GetMapping("/resumen")
    public ResponseEntity<CheckoutResumenDTO> obtenerResumen(Authentication auth) {
        return ResponseEntity.ok(checkoutService.obtenerResumen(auth));
    }
    
    // Endpoint para confirmar el checkout y crear la factura
    @PostMapping("/confirmar")
    public ResponseEntity<Factura> confirmarCheckout(
            Authentication auth,
            @RequestBody Map<String, String> checkoutInfo) {
        
        return ResponseEntity.ok(checkoutService.confirmarCheckout(auth, checkoutInfo));
    }
}
