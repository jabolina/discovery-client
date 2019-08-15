package br.com.jabolina.discoveryclient.cluster.leader;

import br.com.jabolina.discoveryclient.cluster.IDistributedInstance;
import br.com.jabolina.discoveryclient.cluster.leader.service.LeaderKeepAliveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.integration.leader.event.OnGrantedEvent;
import org.springframework.integration.leader.event.OnRevokedEvent;
import org.springframework.stereotype.Component;

@Component
public class LeaderElectionEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger( LeaderElectionEventListener.class );
    private final IDistributedInstance distributedInstance;
    private final LeaderKeepAliveService keepAliveService;

    @Autowired
    public LeaderElectionEventListener(
            IDistributedInstance distributedInstance,
            LeaderKeepAliveService keepAliveService
    ) {
        this.distributedInstance = distributedInstance;
        this.keepAliveService = keepAliveService;
    }

    @EventListener( OnGrantedEvent.class )
    public void leaderElected( OnGrantedEvent event ) {
        LOGGER.info( "New instance leader elected: [{}]", event );
        distributedInstance.elected();
        keepAliveService.startServicesVerification();
    }

    @EventListener( OnRevokedEvent.class )
    public void leaderRevoked( OnRevokedEvent event ) {
        LOGGER.info( "Instance leader revoked: [{}]", event );
        keepAliveService.stop();
    }
}
