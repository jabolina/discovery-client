package br.com.jabolina.discoveryclient.service.impl;

import br.com.jabolina.discoveryclient.cluster.DistributedInstance;
import br.com.jabolina.discoveryclient.data.ServiceDescription;
import br.com.jabolina.discoveryclient.exception.ServiceNotFoundException;
import br.com.jabolina.discoveryclient.service.ProxyService;
import br.com.jabolina.discoveryclient.service.RoundRobin;
import br.com.jabolina.discoveryclient.service.ServiceGenericService;
import br.com.jabolina.discoveryclient.util.Constants;
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
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
public class ProxyServiceImpl extends HttpServlet implements ProxyService {

    private static final Logger LOGGER = LoggerFactory.getLogger( ProxyServiceImpl.class );

    private final DistributedInstance< ?, ?, ? extends ServiceDescription > distributedInstance;
    private final ServiceGenericService< ? extends ServiceDescription > serviceGenericService;
    private final RestTemplate restTemplate;

    // TODO: fix inconsistency between replicas and itself
    private final ConcurrentMap< String, Iterator< ? extends ServiceDescription > > iterators;

    @Autowired
    public ProxyServiceImpl(
            DistributedInstance< ?, ?, ? extends ServiceDescription > distributedInstance,
            ServiceGenericService< ? extends ServiceDescription > serviceGenericService, RestTemplate restTemplate
    ) {
        this.distributedInstance = distributedInstance;
        this.serviceGenericService = serviceGenericService;
        this.restTemplate = restTemplate;

        this.iterators = new ConcurrentHashMap<>();
    }

    private List< ? extends ServiceDescription > retrieveServicesByName( String name ) {
        return serviceGenericService.listServicesByName( name ).parallelStream()
                .filter( ServiceDescription::isEnabled )
                .filter( ServiceDescription::isActive )
                .collect( Collectors.toList() );
    }

    @SuppressWarnings( "unchecked" )
    private Iterator< ? extends ServiceDescription > getServiceIterator( String name ) {
        BlockingQueue cachedQueue = distributedInstance.getQueue( Constants.ROUND_ROBIN_KEY + name );

        if ( cachedQueue.isEmpty() && !iterators.containsKey( name ) ) {
            List< ? extends ServiceDescription > values = retrieveServicesByName( name );
            cachedQueue.addAll( values );
            iterators.put( name, RoundRobin.toIterator( values ) );
        }

        return iterators.get( name );
    }

    private ServiceDescription pollService( @NotNull String name ) {
        Iterator< ? extends ServiceDescription > services = getServiceIterator( name );
        return services.next();
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
