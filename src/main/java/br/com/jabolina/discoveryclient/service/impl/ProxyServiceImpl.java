package br.com.jabolina.discoveryclient.service.impl;

import br.com.jabolina.discoveryclient.cluster.DistributedInstance;
import br.com.jabolina.discoveryclient.data.ServiceDescription;
import br.com.jabolina.discoveryclient.exception.ServiceNotFoundException;
import br.com.jabolina.discoveryclient.service.ProxyService;
import br.com.jabolina.discoveryclient.service.RoundRobin;
import br.com.jabolina.discoveryclient.service.ServiceGenericService;
import br.com.jabolina.discoveryclient.util.Constants;
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
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
public class ProxyServiceImpl extends HttpServlet implements ProxyService {

    private final ServiceGenericService< ? extends ServiceDescription > serviceGenericService;
    private final RestTemplate restTemplate;

    private final ConcurrentMap< String, Iterator< ? extends ServiceDescription > > roundRobinServices;

    @Autowired
    public ProxyServiceImpl(
            DistributedInstance< ?, ?, ? extends ServiceDescription > distributedInstance,
            ServiceGenericService< ? extends ServiceDescription > serviceGenericService, RestTemplate restTemplate
    ) {
        // TODO: use IQueue overriding the iterator
        this.roundRobinServices = distributedInstance.getGenericMap( Constants.ROUND_ROBIN_KEY );
        this.serviceGenericService = serviceGenericService;
        this.restTemplate = restTemplate;
    }

    private ServiceDescription pollService( @NotNull String name ) {
        if ( roundRobinServices.containsKey( name ) ) {
            return roundRobinServices.get( name ).next();
        }

        Iterator< ? extends ServiceDescription > services = RoundRobin.toIterator( retrieveServicesByName( name ) );

        roundRobinServices.put( name, services );
        return pollService( name );
    }

    private List< ? extends ServiceDescription > retrieveServicesByName( String name ) {
        return serviceGenericService.listServicesByName( name ).parallelStream()
                .filter( ServiceDescription::isEnabled )
                .filter( ServiceDescription::isActive )
                .collect( Collectors.toList() );
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
            throw new ServiceNotFoundException( String.format( "Service with name [%s] not found", name ) );
        }

        return proxy( request, service );
    }
}
