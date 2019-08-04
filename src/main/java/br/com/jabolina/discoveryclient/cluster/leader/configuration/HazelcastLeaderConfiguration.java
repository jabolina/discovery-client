package br.com.jabolina.discoveryclient.cluster.leader.configuration;

import br.com.jabolina.discoveryclient.cluster.DistributedInstance;
import br.com.jabolina.discoveryclient.configuration.SpringContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.integration.annotation.Role;
import org.springframework.integration.support.SmartLifecycleRoleController;

import java.util.Collections;
import java.util.Objects;

@Configuration
@DependsOn( "custom-hazelcast" )
public class HazelcastLeaderConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger( HazelcastLeaderConfiguration.class );

    private final DistributedInstance< ?, ?, ? > distributedInstance;

    @Autowired
    public HazelcastLeaderConfiguration(
            DistributedInstance< ?, ?, ? > distributedInstance ) {
        this.distributedInstance = distributedInstance;

        SmartLifecycleRoleController roleController = SpringContext.requireBean( SmartLifecycleRoleController.class );

        if ( !Objects.isNull( roleController ) ) {
            roleController.addLifecyclesToRole( "leader", Collections.singletonList( "post-leader-election" ) );
        }
    }

    @Bean( "post-leader-election" )
    @Role( "leader" )
    public void instanceIsLeader() {
        LOGGER.info( "Starting service in new leader" );

        distributedInstance.elected();

    }
}
