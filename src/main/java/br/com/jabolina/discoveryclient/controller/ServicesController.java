package br.com.jabolina.discoveryclient.controller;

import br.com.jabolina.discoveryclient.data.ServiceDescription;
import br.com.jabolina.discoveryclient.service.ServiceGenericService;
import br.com.jabolina.discoveryclient.util.Constants;
import br.com.jabolina.discoveryclient.util.EncDec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping( "/api/services/" )
public class ServicesController {

    private static final Logger LOGGER = LoggerFactory.getLogger( ServicesController.class );

    private final ServiceGenericService< ServiceDescription > service;

    @Autowired
    public ServicesController( ServiceGenericService< ServiceDescription > service ) {
        this.service = service;
    }

    @GetMapping
    public @ResponseBody
    ResponseEntity< List<ServiceDescription> > listAllServices() {
        return ResponseEntity.ok( service.listServices() );
    }

    @PostMapping( "/subscribe" )
    public @ResponseBody
    ResponseEntity< ServiceDescription > subscribeService( @RequestBody ServiceDescription description ) {
        description.setId( EncDec.jid( description.getName(), Constants.HAZEL_QUEUE_SERVICES ) );
        LOGGER.info( "Registering new service [{}]", service );

        if ( service.subscribe( description ) ) {
            return ResponseEntity.ok( description );
        }

        description.setId( "" );
        return ResponseEntity.status( HttpStatus.REQUEST_TIMEOUT ).body( null );
    }

    @GetMapping( "/unsubscribe" )
    public @ResponseBody
    ResponseEntity< ServiceDescription > unsubscribeService( @RequestParam String id ) {
        LOGGER.info( "Unsubscribing service with id [{}]", id );

        service.unsubscribe( id );
        return ResponseEntity.ok( new ServiceDescription().setId( id ) );
    }

}