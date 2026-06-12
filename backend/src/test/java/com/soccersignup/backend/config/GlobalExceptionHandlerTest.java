package com.soccersignup.backend.config;

import com.soccersignup.backend.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void businessErrorsUseTheCanonicalResponseShape() {
        ResponseEntity<ErrorResponse> response =
                handler.handleConflict(new IllegalStateException("Game is full"));

        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Game is full");
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().fieldErrors()).isEmpty();
    }

    @Test
    void validationErrorsUseTheCanonicalResponseShape() throws Exception {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "registration");
        bindingResult.addError(new FieldError(
                "registration",
                "email",
                "Must be a valid email address"));

        Method method = TestController.class.getDeclaredMethod("register", Object.class);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                new MethodParameter(method, 0),
                bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidation(exception);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Validation failed");
        assertThat(response.getBody().fieldErrors())
                .containsEntry("email", "Must be a valid email address");
    }

    private static class TestController {
        @SuppressWarnings("unused")
        void register(Object request) {
        }
    }
}
