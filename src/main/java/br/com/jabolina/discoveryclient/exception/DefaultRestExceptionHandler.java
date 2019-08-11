package br.com.jabolina.discoveryclient.exception;

import br.com.jabolina.discoveryclient.exception.data.DefaultException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class DefaultRestExceptionHandler {

    @ExceptionHandler( ServiceNotFoundException.class )
    public ResponseEntity< DefaultException > handleServiceNotFoundException( ServiceNotFoundException ex ) {
        return new ResponseEntity<>(
                new DefaultException()
                        .setMessage( ex.getMessage() )
                        .setStatus( ex.getStatus() )
                        .setTitle( ex.getCode() ),
                HttpStatus.NOT_FOUND
        );
    }
}
