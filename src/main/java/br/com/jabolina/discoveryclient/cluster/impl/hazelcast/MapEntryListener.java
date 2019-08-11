package br.com.jabolina.discoveryclient.cluster.impl.hazelcast;

import br.com.jabolina.discoveryclient.cluster.DistributedInstance;
import br.com.jabolina.discoveryclient.configuration.SpringContext;
import br.com.jabolina.discoveryclient.data.ServiceDescription;
import br.com.jabolina.discoveryclient.service.RoundRobin;
import br.com.jabolina.discoveryclient.service.ServiceGenericService;
import br.com.jabolina.discoveryclient.service.impl.ServiceDescriptionServiceImpl;
import br.com.jabolina.discoveryclient.util.Constants;
import com.hazelcast.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MapEntryListener< V extends ServiceDescription > implements EntryListener< String, V > {

    private static final Logger LOGGER = LoggerFactory.getLogger( MapEntryListener.class );

    private IMap< String, Iterator< ? extends ServiceDescription > > roundRobinServices;
    private ServiceGenericService< ? extends ServiceDescription > serviceGenericService;

    @SuppressWarnings( "unchecked" )
    private void initialise() {
        if ( Objects.isNull(roundRobinServices) ) {
            DistributedInstance instance = Objects.requireNonNull( SpringContext.requireBean( DistributedInstance.class ) );
            serviceGenericService = Objects.requireNonNull( SpringContext.requireBean( ServiceDescriptionServiceImpl.class ) );
            roundRobinServices = ( IMap ) instance.getGenericMap( Constants.ROUND_ROBIN_KEY );
        }
    }

    private List<ServiceDescription > retrieveServicesByName( String name ) {
        return serviceGenericService.listServicesByName( name ).parallelStream()
                .filter( ServiceDescription::isEnabled )
                .filter( ServiceDescription::isActive )
                .collect( Collectors.toList() );
    }

    @Override
    public void entryAdded( EntryEvent< String, V > entryEvent ) {
        LOGGER.info( "New entry of map [{}]", entryEvent );
        initialise();
        ServiceDescription service = entryEvent.getValue();

        if ( roundRobinServices.containsKey( service.getName() ) ) {
            List< ServiceDescription > services = retrieveServicesByName( service.getName() );
            services.add( service );
            roundRobinServices.replace( service.getName(), RoundRobin.toIterator( services ) );
        }
    }

    @Override
    public void entryEvicted( EntryEvent< String, V > entryEvent ) {
        LOGGER.info( "Entry evicted [{}]", entryEvent );
    }

    @Override
    public void entryRemoved( EntryEvent< String, V > entryEvent ) {
        LOGGER.info( "Entry removed [{}]", entryEvent );
    }

    @Override
    public void entryUpdated( EntryEvent< String, V > entryEvent ) {
        LOGGER.info( "Entry updated [{}]", entryEvent );
        initialise();

        ServiceDescription service = entryEvent.getValue();

        if ( roundRobinServices.containsKey( service.getName() ) ) {
            roundRobinServices.evict( service.getName() );
        }
    }

    @Override
    public void mapCleared( MapEvent mapEvent ) {
        LOGGER.info( "Map cleared [{}]", mapEvent );
    }

    @Override
    public void mapEvicted( MapEvent mapEvent ) {
        LOGGER.info( "Map evicted [{}]", mapEvent );
    }
}
