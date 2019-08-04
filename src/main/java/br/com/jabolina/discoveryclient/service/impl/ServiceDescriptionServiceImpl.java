package br.com.jabolina.discoveryclient.service.impl;

import br.com.jabolina.discoveryclient.cluster.DistributedInstance;
import br.com.jabolina.discoveryclient.data.ServiceDescription;
import br.com.jabolina.discoveryclient.service.ServiceGenericService;
import br.com.jabolina.discoveryclient.util.Constants;
import br.com.jabolina.discoveryclient.util.EncDec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ServiceDescriptionServiceImpl< V extends ServiceDescription > implements ServiceGenericService< V > {

    private final DistributedInstance< ?, String, V > instance;

    @Autowired
    public ServiceDescriptionServiceImpl( DistributedInstance< ?, String, V > instance ) {
       this.instance = instance;
    }

    private boolean offer( V description, long timeout, int tries ) {
        if ( tries > 0 ) {
            try {
                if ( !instance.getQueue( Constants.HAZEL_QUEUE_SERVICES )
                        .offer( description, timeout, TimeUnit.SECONDS ) ) {
                    return offer( description, timeout, tries - 1 );
                }

                return true;
            } catch ( InterruptedException ignore ) {
                return offer( description, timeout * 2L, tries - 1 );
            }
        }

        return false;
    }

    private boolean offer( V description ) {
        instance.getMap( Constants.HAZEL_MAP_SERVICES )
                .put( description.getId(), description );
        return true;
    }

    @Override
    public List< V > listServices() {
        return instance.getMap( Constants.HAZEL_MAP_SERVICES ).entrySet().parallelStream()
                .filter( entry -> entry.getValue().isEnabled() )
                .map( Map.Entry::getValue )
                .collect( Collectors.toList() );
    }


    @Override
    public boolean subscribe( V service ) {
        service.setId( EncDec.jid( service.getName(), Constants.HAZEL_MAP_SERVICES ) )
                .setEnabled( true );
        return offer( service );
    }

    @Override
    public boolean unsubscribe( String identifier ) {
        // Thread safe?
        V service = instance.getMap( Constants.HAZEL_MAP_SERVICES ).get( identifier );
        service.disable();
        return instance.getMap( Constants.HAZEL_MAP_SERVICES ).replace( identifier, service ) != null;
    }
}
