package br.com.jabolina.discoveryclient.service.impl;

import br.com.jabolina.discoveryclient.cluster.DistributedInstance;
import br.com.jabolina.discoveryclient.service.ServiceGenericService;
import br.com.jabolina.discoveryclient.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ServiceDescriptionServiceImpl< V > implements ServiceGenericService< V > {

    private final DistributedInstance< String, V > instance;

    @Autowired
    public ServiceDescriptionServiceImpl( DistributedInstance< String, V > instance ) {
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

    @Override
    public List< V > listServices() {
        return instance.getQueue( Constants.HAZEL_QUEUE_SERVICES )
                .parallelStream()
                .collect( Collectors.toList() );
    }


    @Override
    public boolean subscribe( V service ) {
        return offer( service, 1L, 3  );
    }

    @Override
    public boolean unsubscribe( String identifier ) {
        return instance.getQueue( Constants.HAZEL_QUEUE_SERVICES )
                .removeIf( description -> description.equals( identifier ) );
    }
}
