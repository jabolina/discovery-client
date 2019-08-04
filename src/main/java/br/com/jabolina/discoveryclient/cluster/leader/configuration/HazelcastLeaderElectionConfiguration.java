package br.com.jabolina.discoveryclient.cluster.leader.configuration;

import br.com.jabolina.discoveryclient.cluster.DistributedInstance;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.integration.hazelcast.leader.LeaderInitiator;
import org.springframework.integration.hazelcast.lock.HazelcastLockRegistry;
import org.springframework.integration.support.leader.LockRegistryLeaderInitiator;
import org.springframework.integration.support.locks.LockRegistry;

@Configuration
@DependsOn( "custom-hazelcast" )
public class HazelcastLeaderElectionConfiguration {

    private final DistributedInstance< HazelcastInstance, ?, ? > distributedInstance;

    @Autowired
    public HazelcastLeaderElectionConfiguration( DistributedInstance< HazelcastInstance, ?, ? > distributedInstance ) {
        this.distributedInstance = distributedInstance;
    }

    @Bean
    public LockRegistry lockRegistry(  ) {
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
