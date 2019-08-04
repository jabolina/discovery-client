package br.com.jabolina.discoveryclient.cluster;

import br.com.jabolina.discoveryclient.data.ServiceDescription;
import com.hazelcast.core.ILock;
import org.springframework.integration.leader.Context;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;

public interface DistributedInstance< I, K, V extends ServiceDescription > extends Context {

    I retrieveInstance();

    void elected();
    void destroy();
    boolean isRunning();

    ILock getLock( String name );
    ConcurrentMap< K, V > getMap( String name );
    BlockingQueue< V > getQueue( String name );
}
