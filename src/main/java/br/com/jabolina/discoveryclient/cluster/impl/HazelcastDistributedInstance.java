package br.com.jabolina.discoveryclient.cluster.impl;

import br.com.jabolina.discoveryclient.cluster.DistributedInstance;
import br.com.jabolina.discoveryclient.data.ServiceDescription;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;

public class HazelcastDistributedInstance< K, V extends ServiceDescription > implements DistributedInstance< HazelcastInstance, K, V > {

    private static final Logger LOGGER = LoggerFactory.getLogger( HazelcastDistributedInstance.class );

    private final HazelcastInstance instance;
    private boolean leader;

    public HazelcastDistributedInstance( HazelcastInstance instance ) {
        this.instance = instance;
    }

    @Override
    public HazelcastInstance retrieveInstance() {
        return instance;
    }

    @Override
    public void elected() {
        leader = true;
    }

    @Override
    public boolean isLeader() {
        return leader;
    }

    @Override
    public boolean isRunning() {
        return instance.getLifecycleService().isRunning();
    }

    @Override
    public ILock getLock( String name ) {
        return instance.getLock( name );
    }

    @Override
    public ConcurrentMap< K, V > getMap( String name ) {
        return instance.getMap( name );
    }

    @Override
    public BlockingQueue< V > getQueue( String name ) {
        return instance.getQueue( name );
    }

    @PreDestroy
    @Override
    public void destroy() {
        instance.shutdown();
    }
}
