package br.com.jabolina.discoveryclient.cluster;

import org.springframework.integration.leader.Context;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;

public interface DistributedInstance< I, K, V> extends Context {

    I retrieveInstance();

    void elected();
    boolean isLeader();
    boolean isRunning();

    Lock getLock( String name );

    ConcurrentMap< K, V > getMap( String name );
    BlockingQueue< V > getQueue( String name );

    void destroy();
}
