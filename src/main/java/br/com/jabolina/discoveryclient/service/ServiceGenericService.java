package br.com.jabolina.discoveryclient.service;

import java.util.List;

public interface ServiceGenericService< V > {

    List< V > listServices();
    boolean subscribe( V service );
    boolean unsubscribe( String service );
}
