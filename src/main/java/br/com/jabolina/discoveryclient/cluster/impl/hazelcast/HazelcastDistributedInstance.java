package br.com.jabolina.discoveryclient.cluster.impl.hazelcast;

import br.com.jabolina.discoveryclient.cluster.DistributedInstance;
import br.com.jabolina.discoveryclient.data.ServiceDescription;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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
    public void runWithLock( String lockName, Runnable runnable ) {
        ILock lock = null;

        try {
            lock = getLock( lockName );
            lock.tryLock( 5L, TimeUnit.SECONDS );

            if ( lock.isLocked() ) {
                runnable.run();
            }
        } catch ( InterruptedException e ) {
            LOGGER.error( "Error running with lock [{}]", lockName, e );
        } finally {
            if ( !Objects.isNull( lock ) && ( lock.isLocked() || lock.isLockedByCurrentThread() ) ) {
                lock.forceUnlock();
            }
        }
    }

    @Override
    public ILock getLock( String name ) {
        return instance.getLock( name );
    }

    @Override
    public IMap< K, V > getMap( String name ) {
        return instance.getMap( name );
    }

    @Override
    public < E, A > IMap< E, A > getGenericMap( String name ) {
        return instance.getMap( name );
    }

    @Override
    public IQueue< V > getQueue( String name ) {
        return instance.getQueue( name );
    }

    @Override
    public < A > IQueue< A > getGenericQueue( String name ) {
        return instance.getQueue( name );
    }

    @PreDestroy
    @Override
    public void destroy() {
        instance.shutdown();
    }
}
