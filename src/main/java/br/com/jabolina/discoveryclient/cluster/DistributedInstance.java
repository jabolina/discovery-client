package br.com.jabolina.discoveryclient.cluster;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;

public interface DistributedInstance {

    boolean isLeader();
    boolean isRunning();

    Lock getLock( String name );

    < K, V > ConcurrentMap< K, V > getMap( String name );
    < K >BlockingQueue< K > getQueue( String name );

    void destroy();
}
