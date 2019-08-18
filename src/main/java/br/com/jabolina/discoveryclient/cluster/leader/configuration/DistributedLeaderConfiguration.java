package br.com.jabolina.discoveryclient.cluster.leader.configuration;

import br.com.jabolina.discoveryclient.cluster.IDistributedInstance;
import br.com.jabolina.discoveryclient.cluster.leader.event.LeaderElectionEventPublisher;
import br.com.jabolina.discoveryclient.cluster.leader.service.DistributedInstanceLeaderElection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DistributedLeaderConfiguration {

    @Bean( destroyMethod = "shutdown" )
    public DistributedInstanceLeaderElection leaderElection(
            IDistributedInstance instance,
            LeaderElectionEventPublisher publisher
    ) {
        return new DistributedInstanceLeaderElection( instance, publisher );
    }

}
