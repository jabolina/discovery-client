package br.com.jabolina.discoveryclient.service.impl;

import br.com.jabolina.discoveryclient.cluster.IDistributedInstance;
import br.com.jabolina.discoveryclient.data.ServiceDescription;
import br.com.jabolina.discoveryclient.service.ServiceGenericService;
import br.com.jabolina.discoveryclient.util.Constants;
import br.com.jabolina.discoveryclient.util.EncDec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ServiceDescriptionServiceImpl implements ServiceGenericService {

    private final IDistributedInstance instance;

    @Autowired
    public ServiceDescriptionServiceImpl( IDistributedInstance instance ) {
       this.instance = instance;
    }

    private boolean offer( ServiceDescription description ) {
        instance.getMap( Constants.HAZEL_MAP_SERVICES )
                .put( description.getId(), description );
        return true;
    }

    @Override
    public List< ServiceDescription > listServicesByName( String name ) {
        return instance.< String, ServiceDescription >getMap( Constants.HAZEL_MAP_SERVICES ).values().parallelStream()
                .filter( service -> service.getName().equals( name ) )
                .collect( Collectors.toList() );
    }

    @Override
    public List< ServiceDescription > listServices() {
        return instance.< String, ServiceDescription >getMap( Constants.HAZEL_MAP_SERVICES ).entrySet().parallelStream()
                .filter( entry -> entry.getValue().isEnabled() )
                .map( Map.Entry::getValue )
                .collect( Collectors.toList() );
    }


    @Override
    public boolean subscribe( ServiceDescription service ) {
        service.setId( EncDec.jid( service.getName(), Constants.HAZEL_MAP_SERVICES ) )
                .setEnabled( true ).setActive( true );
        return offer( service );
    }

    @Override
    public boolean unsubscribe( String identifier ) {
        instance.runWithLock( Constants.HAZEL_LOCK_VERIFY, () -> {
            ServiceDescription service = instance.< String, ServiceDescription >getMap( Constants.HAZEL_MAP_SERVICES ).get( identifier );
            service.disable();
            instance.getMap( Constants.HAZEL_MAP_SERVICES ).replace( identifier, service );
        } );

        return instance.getMap( Constants.HAZEL_MAP_SERVICES ).get( identifier ) != null;
    }
}
