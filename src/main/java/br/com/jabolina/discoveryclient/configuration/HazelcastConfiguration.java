package br.com.jabolina.discoveryclient.configuration;

import br.com.jabolina.discoveryclient.cluster.IDistributedInstance;
import br.com.jabolina.discoveryclient.cluster.impl.hazelcast.HazelcastDistributedInstance;
import br.com.jabolina.discoveryclient.cluster.impl.hazelcast.MapEntryListener;
import br.com.jabolina.discoveryclient.cluster.impl.hazelcast.MemberListener;
import br.com.jabolina.discoveryclient.util.Constants;
import com.hazelcast.config.*;
import com.hazelcast.core.MembershipListener;
import com.hazelcast.instance.HazelcastInstanceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.UUID;

@EnableCaching
@Configuration
public class HazelcastConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger( HazelcastConfiguration.class );

    @Value( "${discovery.address.ip:localhost}" )
    private String clusterAddress;

    @Value( "${discovery.cluster.size:1}" )
    private String clusterSize;

    private MemberAttributeConfig memberAttributeConfig( String instance ) {
        MemberAttributeConfig memberConfig = new MemberAttributeConfig();
        memberConfig.setStringAttribute( Constants.HAZELCAST_MEMBER_PREFIX, instance );

        return memberConfig;
    }

    private EntryListenerConfig entryListener() {
        return new EntryListenerConfig()
                .setImplementation( new MapEntryListener() );
    }

    private MembershipListener membershipListener() {
        return new MemberListener();
    }

    private MapConfig initializeServicesMapConfig() {
        return new MapConfig()
                .setBackupCount( Integer.parseInt( clusterSize, 10 ) )
                .setReadBackupData( true )
                .setEvictionPolicy( EvictionPolicy.LRU )
                .setStatisticsEnabled( true )
                .setTimeToLiveSeconds( 0 )
                .setEntryListenerConfigs( Collections.singletonList( entryListener() ) );
    }

    @Bean
    public Config config() {
        String node = UUID.randomUUID().toString();
        LOGGER.info( "Creating Hazelcast configuration for node [{}]", node );

        Config config = new Config( node );

        config.getNetworkConfig()
                .setPort( 5700 )
                .setPortAutoIncrement( true );

        config.getNetworkConfig().getJoin().getAwsConfig().setEnabled( false );
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled( false );
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled( true )
                .addMember( clusterAddress );

        config.setMemberAttributeConfig( memberAttributeConfig( config.getInstanceName() ) );
        config.addListenerConfig( new ListenerConfig( membershipListener() ) );
        config.getMapConfigs().put( Constants.HAZEL_MAP_SERVICES, initializeServicesMapConfig() );

        return config;
    }

    @Bean( name = "custom-hazelcast" )
    public IDistributedInstance hazelcastInstance() {
        return new HazelcastDistributedInstance( HazelcastInstanceFactory.newHazelcastInstance( config() ) );
    }
}
