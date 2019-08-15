package br.com.jabolina.discoveryclient.cluster;

import org.springframework.integration.leader.Context;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;

public interface IDistributedInstance extends Context {

    < I > I retrieveInstance();

    void elected();
    void destroy();
    boolean isRunning();

    void runWithLock( String lockName, Runnable runnable );

    Lock getLock( String name );
    < K, V > ConcurrentMap< K, V > getMap( String name );

    < E > BlockingQueue< E > getQueue( String name );
}
