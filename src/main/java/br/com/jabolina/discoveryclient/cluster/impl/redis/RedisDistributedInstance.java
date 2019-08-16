package br.com.jabolina.discoveryclient.cluster.impl.redis;

import br.com.jabolina.discoveryclient.cluster.IDistributedInstance;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public class RedisDistributedInstance implements IDistributedInstance {

    private static final Logger LOGGER = LoggerFactory.getLogger( RedisDistributedInstance.class );

    private final RedissonClient redissonInstance;
    private boolean leader;

    public RedisDistributedInstance( RedissonClient redissonInstance ) {
        this.redissonInstance = redissonInstance;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public RedissonClient retrieveInstance() {
        return redissonInstance;
    }

    @Override
    public void elected() {
        LOGGER.info( "Elected leader" );
        leader = true;
    }

    @Override
    public void destroy() {
        redissonInstance.shutdown();
    }

    @Override
    public boolean isRunning() {
        return !redissonInstance.isShutdown();
    }

    @Override
    public void runWithLock( String lockName, Runnable runnable ) {
        RLock lock = null;

        try {
            lock = redissonInstance.getLock( lockName );
            lock.tryLock( 5L, TimeUnit.SECONDS );

            if ( lock.isLocked() ) {
                runnable.run();
            }
        } catch ( InterruptedException e ) {
            LOGGER.info( "Error running with Redis lock [{}]", lockName, e );
        } finally {
            if ( !Objects.isNull( lock ) ) {
                lock.forceUnlock();
            }
        }
    }

    @Override
    public Lock getLock( String name ) {
        return redissonInstance.getLock( name );
    }

    @Override
    public < K, V > ConcurrentMap< K, V > getMap( String name ) {
        return redissonInstance.getMap( name );
    }

    @Override
    public < E > BlockingQueue< E > getQueue( String name ) {
        return redissonInstance.getBlockingQueue( name );
    }

    @Override
    public boolean isLeader() {
        return leader;
    }
}
