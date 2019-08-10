package br.com.jabolina.discoveryclient.controller;

import br.com.jabolina.discoveryclient.service.ProxyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Controller
@RequestMapping( "/api/proxy/" )
public class ProxyController {

    private static final Logger LOGGER = LoggerFactory.getLogger( ProxyController.class );

    private final ProxyService proxyService;

    @Autowired
    public ProxyController( ProxyService proxyService ) {
        this.proxyService = proxyService;
    }

    @GetMapping
    public @ResponseBody
    ResponseEntity proxyGETRequest( HttpServletRequest request ) throws IOException {
        LOGGER.info( "Proxying GET request [{}]", request );

        return proxyService.proxyRequest( request );
    }

    @PostMapping
    public @ResponseBody
    ResponseEntity proxyPOSTRequest( HttpServletRequest request ) throws IOException {
        LOGGER.info( "Proxying POST request [{}]", request );

        return proxyService.proxyRequest( request );
    }
}
