package br.com.jabolina.discoveryclient.cluster.impl.atomix;

import io.atomix.core.Atomix;
import io.atomix.core.lock.DistributedLock;
import io.atomix.core.map.AtomicMap;
import io.atomix.core.queue.DistributedQueue;
import io.atomix.protocols.raft.MultiRaftProtocol;
import io.atomix.protocols.raft.ReadConsistency;

@SuppressWarnings( "unchecked" )
class AtomixHelper {

    private final Atomix atomix;

    AtomixHelper( Atomix atomix ) {
        this.atomix = atomix;
    }

    private MultiRaftProtocol protocol() {
        return MultiRaftProtocol
                .builder()
                .withReadConsistency( ReadConsistency.LINEARIZABLE )
                .build();
    }

    DistributedLock getLock( String name ) {
        return atomix.getLock( name );
    }

    <K, V > AtomicMap< K, V > getMap( String name ) {
        return atomix.<K , V >atomicMapBuilder( name )
                .withProtocol( protocol() )
                .withKeyType( ( ( K ) new Object() ).getClass() )
                .withValueType( ( ( V ) new Object() ).getClass() )
                .build();
    }

    < E > DistributedQueue< E > getQueue( String name ) {
        return atomix.< E >queueBuilder( name )
                .withProtocol( protocol() )
                .withElementType( ( ( E ) new Object() ).getClass() )
                .build();
    }
}
