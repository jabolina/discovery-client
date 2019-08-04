package br.com.jabolina.discoveryclient.cluster;

import br.com.jabolina.discoveryclient.data.ServiceDescription;
import org.springframework.integration.leader.Context;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;

public interface DistributedInstance< I, K, V extends ServiceDescription > extends Context {

    I retrieveInstance();

    void elected();
    void destroy();
    boolean isRunning();

    Lock getLock( String name );
    ConcurrentMap< K, V > getMap( String name );
    BlockingQueue< V > getQueue( String name );
}
