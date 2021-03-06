package br.com.jabolina.discoveryclient.service.impl;

import br.com.jabolina.discoveryclient.cluster.IDistributedInstance;
import br.com.jabolina.discoveryclient.service.DistributedRoundRobin;
import br.com.jabolina.discoveryclient.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DistributedRoundRobinImpl implements DistributedRoundRobin {

    private Map< String, Integer > indexes;

    @Autowired
    public DistributedRoundRobinImpl( IDistributedInstance distributedInstance ) {
        this.indexes = distributedInstance.getMap( Constants.ROUND_ROBIN_KEY );
    }

    private Integer nextIndex( List< ? > list, String name ) {
        Integer idx = indexes.getOrDefault( name, 0 );

        return ( idx + 1 ) % list.size();
    }

    @Override
    public < T > T next( List<T> list, String name ) {
        if ( list.isEmpty() ) {
            return null;
        }

        Integer idx = nextIndex( list, name );
        indexes.put( name, idx );

        return list.get( idx );
    }
}
