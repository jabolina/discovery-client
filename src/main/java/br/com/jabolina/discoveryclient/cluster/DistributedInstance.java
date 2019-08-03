package br.com.jabolina.discoveryclient.cluster;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;

public interface DistributedInstance<K, V> {

    boolean isLeader();
    boolean isRunning();

    Lock getLock( String name );

    ConcurrentMap< K, V > getMap( String name );
    BlockingQueue< V > getQueue( String name );

    void destroy();
}
