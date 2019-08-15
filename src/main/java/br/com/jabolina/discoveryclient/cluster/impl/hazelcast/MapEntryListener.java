package br.com.jabolina.discoveryclient.cluster.impl.hazelcast;

import br.com.jabolina.discoveryclient.data.ServiceDescription;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.MapEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapEntryListener< V extends ServiceDescription > implements EntryListener< String, V > {

    private static final Logger LOGGER = LoggerFactory.getLogger( MapEntryListener.class );

    @Override
    public void entryAdded( EntryEvent< String, V > entryEvent ) {
        LOGGER.info( "New entry of map [{}]", entryEvent );
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
