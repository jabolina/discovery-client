package br.com.jabolina.discoveryclient.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus( HttpStatus.NOT_FOUND )
public class ServiceNotFoundException extends DiscoveryException {

    public ServiceNotFoundException() {
        super();
    }

    public ServiceNotFoundException( String message ) {
        super( message );
    }
}
