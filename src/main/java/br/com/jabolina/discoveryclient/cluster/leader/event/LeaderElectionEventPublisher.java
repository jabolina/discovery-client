package br.com.jabolina.discoveryclient.cluster.leader.event;

import br.com.jabolina.discoveryclient.cluster.IDistributedInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class LeaderElectionEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public LeaderElectionEventPublisher( ApplicationEventPublisher eventPublisher ) {
        this.eventPublisher = eventPublisher;
    }

    public void publishNewElectedLeader( IDistributedInstance instance ) {
        LeaderElectionEvent event = new LeaderElectionEvent( this, instance );
        eventPublisher.publishEvent( event );
    }
}
