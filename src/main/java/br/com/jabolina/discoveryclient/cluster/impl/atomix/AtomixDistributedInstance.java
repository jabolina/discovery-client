package br.com.jabolina.discoveryclient.cluster.impl.atomix;

import br.com.jabolina.discoveryclient.cluster.IDistributedInstance;
import com.google.errorprone.annotations.DoNotCall;
import io.atomix.core.Atomix;
import io.atomix.core.lock.AsyncDistributedLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
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
    @DoNotCall
    public < K, V > ConcurrentMap< K, V > getMap( String name ) {
        return ( ConcurrentMap< K, V > ) helper.<K, V>getMap( name );
    }

    @Override
    @DoNotCall
    public < E > BlockingQueue< E > getQueue( String name ) {
        return ( BlockingQueue< E >) helper.getQueue( name );
    }

    @Override
    public < M, K, V > M getCustomMap( String name ) {
        return ( M ) helper.<K, V >getMap( name );
    }

    @Override
    public < Q, E > Q getCustomQueue( String name ) {
        return ( Q ) helper.< E >getQueue( name ) ;
    }

    @Override
    public boolean isLeader() {
        return leader;
    }
}
