package br.com.jabolina.discoveryclient.cluster;

import org.springframework.integration.leader.Context;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.Lock;

public interface IDistributedInstance extends Context {

    < I > I retrieveInstance();

    void elected();
    void destroy();
    boolean isRunning();

    void runWithLock( String lockName, Runnable runnable );
    Lock getLock( String name );

    < K, V > Map< K, V > getMap( String name );
    < E > Queue< E > getQueue( String name );

}
