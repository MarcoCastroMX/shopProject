package com.marco.shopProject.core.exception;

import com.marco.shopProject.catalog.producto.exception.ProductoNoEncontradoException;
import com.marco.shopProject.identity.rol.exception.RolNotFoundException;
import com.marco.shopProject.catalog.sucursal.exception.SucursalNoEncontradaException;
import com.marco.shopProject.identity.user.exception.EmailAlreadyTakenException;
import com.marco.shopProject.identity.user.exception.SuperUserException;
import com.marco.shopProject.identity.user.exception.UserNotFoundException;
import com.marco.shopProject.sales.venta.exception.VentaNoEncontradaException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> tipoIncorrectoRecibido(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request){
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNPROCESSABLE_CONTENT.value())
                .error("Peticion Invalida")
                .message(String.format("Error: El parametro '%s' esperaba un tipo %s, pero recibio un '%s'",ex.getName(),ex.getRequiredType().getSimpleName(),ex.getValue().toString()))
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacionErronea(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ){
        String errores = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(" -"));

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Petion Invalida")
                .message("El objeto recibido no cumple con las caracteristicas minimas para ser aceptado: -" + errores)
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ProductoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> productoNoEncontrado(
            ProductoNoEncontradoException ex,
            HttpServletRequest request
    ){
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNPROCESSABLE_CONTENT.value())
                .error("Petion Invalida")
                .message(String.format("El id '%s'no corresponde a un id existente en base de datos",ex.getMessage()))
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(response);
    }

    @ExceptionHandler(SucursalNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> sucursalNoEncontrada(
            SucursalNoEncontradaException ex,
            HttpServletRequest request
    ){
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNPROCESSABLE_CONTENT.value())
                .error("Petion Invalida")
                .message(String.format("El id '%s'no corresponde a un id existente en base de datos",ex.getMessage()))
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(response);
    }

    @ExceptionHandler(VentaNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> ventaNoEncontrada(
            VentaNoEncontradaException ex,
            HttpServletRequest request
    ){
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNPROCESSABLE_CONTENT.value())
                .error("Petion Invalida")
                .message(String.format("El id '%s'no corresponde a un id existente en base de datos",ex.getMessage()))
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(response);
    }

    @ExceptionHandler(exception = {UsernameNotFoundException.class, UserNotFoundException.class})
    public ResponseEntity<ErrorResponse> usuarioNoEncontrado(
            UsernameNotFoundException ex,
            HttpServletRequest request
    ){
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Petion Invalida")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(RolNotFoundException.class)
    public ResponseEntity<ErrorResponse> rolNoEncontrado(
            UsernameNotFoundException ex,
            HttpServletRequest request
    ){
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Petion Invalida")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(EmailAlreadyTakenException.class)
    public ResponseEntity<ErrorResponse> emailYaUtilizado(
            UsernameNotFoundException ex,
            HttpServletRequest request
    ){
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Petion Invalida")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(SuperUserException.class)
    public ResponseEntity<ErrorResponse> intentoDeEliminacionDeSuperUsuario(
            UsernameNotFoundException ex,
            HttpServletRequest request
    ){
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Petion Invalida")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
