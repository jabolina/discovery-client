package br.com.jabolina.discoveryclient.cluster.impl;

import br.com.jabolina.discoveryclient.cluster.DistributedInstance;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;

public class HazelcastDistributedInstance implements DistributedInstance {

    private static final Logger LOGGER = LoggerFactory.getLogger( HazelcastDistributedInstance.class );

    private final HazelcastInstance instance;

    @Autowired
    public HazelcastDistributedInstance( HazelcastInstance instance ) {
        this.instance = instance;
    }


    @Override
    public boolean isLeader() {
        Member latest = instance.getCluster().getMembers().iterator().next();
        return latest.localMember();
    }

    @Override
    public boolean isRunning() {
        return instance.getLifecycleService().isRunning();
    }

    @Override
    public Lock getLock( String name ) {
        return instance.getLock( name );
    }

    @Override
    public < K, V > ConcurrentMap< K, V > getMap( String name ) {
        return instance.getMap( name );
    }

    @Override
    public < K > BlockingQueue< K > getQueue( String name ) {
        return instance.getQueue( name );
    }

    @Override
    public void destroy() {
        Hazelcast.shutdownAll();
    }
}
