package br.com.jabolina.discoveryclient.configuration;

import br.com.jabolina.discoveryclient.cluster.IDistributedInstance;
import br.com.jabolina.discoveryclient.cluster.impl.atomix.AtomixDistributedInstance;
import io.atomix.cluster.Node;
import io.atomix.cluster.discovery.BootstrapDiscoveryProvider;
import io.atomix.cluster.discovery.NodeDiscoveryProvider;
import io.atomix.core.Atomix;
import io.atomix.primitive.partition.ManagedPartitionGroup;
import io.atomix.protocols.raft.partition.RaftPartitionGroup;
import io.atomix.storage.StorageLevel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Configuration
public class AtomixConfiguration {

    @Value( "${discovery.address.complete}" )
    private String clusterAddr;

    @Value( "${discovery.atomix.members}" )
    private String[] members;

    @Value( "${discovery.atomix.name}" )
    private String name;

    @Value( "${spring.application.name}" )
    private String clusterName;

    private String diskPath( String posfix ) {
        return "./cluster/" + name + "/" + UUID.randomUUID().toString() + posfix;
    }

    private String memberId( String s ) {
        return s; // EncDec.encode( s );
    }

    private List< String > clusterMemberIds() {
        return Arrays.stream( members )
                .map( this::memberId )
                .collect( Collectors.toList() );
    }

    @Bean
    public List< Node > clusterMembers() {
        return Arrays.stream( members )
                .map( addr -> Node.builder()
                        .withId( memberId( addr ) )
                        .withAddress( addr )
                        .build()
                ).collect( Collectors.toList() );
    }

    @Bean
    public NodeDiscoveryProvider membershipProvider() {
        return BootstrapDiscoveryProvider
                .builder()
                .withNodes( clusterMembers() )
                .build();
    }

    @Bean
    public ManagedPartitionGroup managementGroup() {
        return RaftPartitionGroup
                .builder( clusterName )
                .withDataDirectory( new File( diskPath( "-group" ) ) )
                .withNumPartitions( 1 )
                .withMembers( clusterMemberIds() )
                .build();
    }

    @Bean
    public ManagedPartitionGroup partitionGroup() {
        return RaftPartitionGroup
                .builder( clusterName + "-data" )
                .withStorageLevel( StorageLevel.DISK )
                .withDataDirectory( new File( diskPath( "-data" ) ) )
                .withNumPartitions( 1 )
                .withMembers( clusterMemberIds() )
                .build();
    }

    @Bean
    public Atomix atomixInstance() {
        return Atomix.builder()
                .withClusterId( clusterName )
                .withMemberId( memberId( name ) )
                .withAddress( clusterAddr )
                .withMembershipProvider( membershipProvider() )
                .withManagementGroup( managementGroup() )
                .withPartitionGroups( partitionGroup() )
                .build();
    }

    @Qualifier( "atomix-distributed-instance" )
    @Bean( "atomix-distributed-instance" )
    public IDistributedInstance atomixDistributedInstance( Environment environment ) {
        if ( environment.getProperty( "discovery.distribution.type", "" ).equals( "atomix" ) ) {
            return new AtomixDistributedInstance( atomixInstance() );
        }

        return null;
    }
}
