package br.com.jabolina.discoveryclient.cluster.leader.service;

import br.com.jabolina.discoveryclient.cluster.DistributedInstance;
import br.com.jabolina.discoveryclient.data.ServiceDescription;
import br.com.jabolina.discoveryclient.util.Constants;
import com.hazelcast.core.ILock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class LeaderKeepAliveService {

    private static final Logger LOGGER = LoggerFactory.getLogger( LeaderKeepAliveService.class );

    private final DistributedInstance< ?, ?, ? extends ServiceDescription > distributedInstance;

    @Autowired
    public LeaderKeepAliveService( DistributedInstance< ?, ?, ? > distributedInstance ) {
        this.distributedInstance = distributedInstance;
    }

    private < T extends ServiceDescription > void verifyService( T service ) {
    }

    public void startServicesVerification() {
        if ( distributedInstance.isLeader() ) {
            ILock lock = null;

            try {
                lock = distributedInstance.getLock( Constants.HAZEL_LOCK_VERIFY );
                lock.tryLock(  5L, TimeUnit.SECONDS );

                if ( lock.isLocked() ) {
                    LOGGER.info( "Leader starting services verifications, instance [{}]", distributedInstance );

                    distributedInstance.getQueue( Constants.HAZEL_QUEUE_SERVICES ).parallelStream()
                            .forEach( this::verifyService );
                }
            } catch ( Exception ex ) {
                LOGGER.error( "Error verifying services", ex );
            } finally {
                if ( !Objects.isNull( lock ) ) {
                    lock.forceUnlock();
                }
            }
        }
    }
}
