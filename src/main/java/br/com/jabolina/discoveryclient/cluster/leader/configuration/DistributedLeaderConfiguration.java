package br.com.jabolina.discoveryclient.cluster.leader.configuration;

import br.com.jabolina.discoveryclient.cluster.IDistributedInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.hazelcast.leader.LeaderInitiator;
import org.springframework.integration.hazelcast.lock.HazelcastLockRegistry;
import org.springframework.integration.support.leader.LockRegistryLeaderInitiator;
import org.springframework.integration.support.locks.LockRegistry;

@Configuration
public class DistributedLeaderConfiguration {

    private final IDistributedInstance distributedInstance;

    @Autowired
    public DistributedLeaderConfiguration( IDistributedInstance distributedInstance ) {
        this.distributedInstance = distributedInstance;
    }

    @Bean
    public LockRegistry lockRegistry() {
        return new HazelcastLockRegistry(
                distributedInstance.retrieveInstance()
        );
    }

    @Bean
    public LockRegistryLeaderInitiator registryLeaderInitiator( LockRegistry registry ) {
        return new LockRegistryLeaderInitiator( registry );
    }

    @Bean
    public LeaderInitiator leaderInitiator() {
        return new LeaderInitiator( distributedInstance.retrieveInstance() );
    }
}
