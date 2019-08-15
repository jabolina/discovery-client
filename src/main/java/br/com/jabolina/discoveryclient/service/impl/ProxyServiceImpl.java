package br.com.jabolina.discoveryclient.service.impl;

import br.com.jabolina.discoveryclient.data.ServiceDescription;
import br.com.jabolina.discoveryclient.exception.ServiceNotFoundException;
import br.com.jabolina.discoveryclient.service.DistributedRoundRobin;
import br.com.jabolina.discoveryclient.service.ProxyService;
import br.com.jabolina.discoveryclient.service.ServiceGenericService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Collectors;

@Service
public class ProxyServiceImpl extends HttpServlet implements ProxyService {

    private static final Logger LOGGER = LoggerFactory.getLogger( ProxyServiceImpl.class );

    private final ServiceGenericService< ? extends ServiceDescription > serviceGenericService;
    private final DistributedRoundRobin roundRobin;
    private final RestTemplate restTemplate;

    @Autowired
    public ProxyServiceImpl(
            ServiceGenericService< ? extends ServiceDescription > serviceGenericService,
            DistributedRoundRobin roundRobin,
            RestTemplate restTemplate
    ) {
        this.serviceGenericService = serviceGenericService;
        this.roundRobin = roundRobin;
        this.restTemplate = restTemplate;
    }

    private List< ? extends ServiceDescription > retrieveServicesByName( String name ) {
        return serviceGenericService.listServicesByName( name ).parallelStream()
                .filter( ServiceDescription::isEnabled )
                .filter( ServiceDescription::isActive )
                .collect( Collectors.toList() );
    }

    private ServiceDescription pollService( @NotNull String name ) {
        List< ? extends ServiceDescription > services = retrieveServicesByName( name );
        return roundRobin.next( services, name );
    }

    private HttpHeaders exchangeHeaders( HttpServletRequest request ) {
        return Collections.list( request.getHeaderNames() ).stream()
                .reduce( new HttpHeaders(), (header, name) -> {
                    header.set( name, request.getHeader( name ) );
                    return header;
                }, (header, name) -> header );
    }

    private ResponseEntity proxy( HttpServletRequest request, ServiceDescription service ) throws IOException {
        Scanner s = new Scanner( request.getInputStream() ).useDelimiter( "\\A" );
        String uri = service.getBaseUrl() + request.getRequestURI();

        LOGGER.info( "Proxying [{}]:[{}]", request.getMethod(), uri );

        return restTemplate.exchange(
                uri,
                HttpMethod.valueOf( request.getMethod() ),
                new HttpEntity<>( s.hasNext() ? s.next() : "", exchangeHeaders( request ) ),
                Object.class,
                request.getParameterMap()
        );
    }

    @Override
    public ResponseEntity proxyRequest( HttpServletRequest request ) throws IOException {
        String name = request.getParameter( "name" );
        ServiceDescription service = pollService( name );

        if ( Objects.isNull( service ) ) {
            throw new ServiceNotFoundException( String.format( "Service with name [%s] not found", name ) )
                    .setCode( "SERVICE_NOT_FOUND" ).setStatus( 404 );
        }

        return proxy( request, service );
    }
}
