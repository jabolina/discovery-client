package br.com.jabolina.discoveryclient.cluster.impl.atomix;

import br.com.jabolina.discoveryclient.cluster.IDistributedInstance;
import io.atomix.core.Atomix;
import io.atomix.core.lock.AsyncDistributedLock;
import io.atomix.core.map.DistributedMap;
import io.atomix.core.queue.DistributedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Lock;

@SuppressWarnings( "unchecked" )
public class AtomixDistributedInstance implements IDistributedInstance {

    private static final Logger LOGGER = LoggerFactory.getLogger( AtomixDistributedInstance.class );

    private final Atomix instance;
    private final AtomixHelper helper;
    private boolean leader;

    public AtomixDistributedInstance( Atomix instance ) {
        this.instance = instance;
        this.helper = new AtomixHelper( instance );
        this.leader = false;

        instance.start().join();
    }


    @Override
    public Atomix retrieveInstance() {
        return instance;
    }

    @Override
    public void elected() {
        this.leader = true;
    }

    @Override
    public void destroy() {
        instance.stop();
    }

    @Override
    public boolean isRunning() {
        return instance.isRunning();
    }

    @Override
    public void runWithLock( String lockName, Runnable runnable ) {
        AsyncDistributedLock lock = helper.getLock( lockName ).async();
        lock.lock().thenAccept( id -> {
            LOGGER.info( "Locked [{}]", id );
            runnable.run();
            lock.unlock().thenAccept( ignore -> LOGGER.info( "Unlocked [{}]", id ) );
        } );
    }

    @Override
    public Lock getLock( String name ) {
        return helper.getLock( name );
    }

    @Override
    public < K, V > DistributedMap< K, V > getMap( String name ) {
        return helper.getMap( name );
    }

    @Override
    public < E > DistributedQueue< E > getQueue( String name ) {
        return helper.getQueue( name );
    }

    @Override
    public boolean isLeader() {
        return leader;
    }
}
