package br.com.jabolina.discoveryclient.service.impl;

import br.com.jabolina.discoveryclient.cluster.DistributedInstance;
import br.com.jabolina.discoveryclient.data.ServiceDescription;
import br.com.jabolina.discoveryclient.service.ProxyService;
import br.com.jabolina.discoveryclient.service.RoundRobin;
import br.com.jabolina.discoveryclient.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
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

    private final DistributedInstance< ?, ?, ? extends ServiceDescription > distributedInstance;
    private final RestTemplate restTemplate;

    private final ConcurrentMap< String, Iterator< ? extends ServiceDescription > > roundRobinServices;

    @Autowired
    public ProxyServiceImpl(
            DistributedInstance< ?, ?, ? extends ServiceDescription > distributedInstance,
            RestTemplate restTemplate
    ) {
        this.distributedInstance = distributedInstance;
        this.roundRobinServices = distributedInstance.getGenericMap( Constants.ROUND_ROBIN_KEY );
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
        return distributedInstance.getMap( Constants.HAZEL_MAP_SERVICES ).values().parallelStream()
                .filter( service -> service.getName().equals( name ) )
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
        ServiceDescription service = pollService( request.getParameter( "name" ) );

        if ( Objects.isNull( service ) ) {
            return ResponseEntity.status( HttpStatus.BAD_REQUEST ).body( null );
        }

        return proxy( request, service );
    }
}
