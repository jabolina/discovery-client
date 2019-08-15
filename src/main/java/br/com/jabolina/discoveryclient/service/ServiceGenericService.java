package br.com.jabolina.discoveryclient.service;

import br.com.jabolina.discoveryclient.data.ServiceDescription;

import java.util.List;

public interface ServiceGenericService {

    List< ServiceDescription > listServicesByName( String name );
    List< ServiceDescription > listServices();
    boolean subscribe( ServiceDescription service );
    boolean unsubscribe( String service );
}
