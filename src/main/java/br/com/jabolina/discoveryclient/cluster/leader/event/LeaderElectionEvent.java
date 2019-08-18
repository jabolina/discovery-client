package br.com.jabolina.discoveryclient.cluster.leader.event;

import br.com.jabolina.discoveryclient.cluster.IDistributedInstance;
import org.springframework.context.ApplicationEvent;

public class LeaderElectionEvent extends ApplicationEvent {

    private final IDistributedInstance electedLeader;

    public LeaderElectionEvent( Object source, IDistributedInstance instance ) {
        super( source );
        this.electedLeader = instance;
    }

    public IDistributedInstance getElectedLeader() {
        return electedLeader;
    }
}
