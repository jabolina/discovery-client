package br.com.jabolina.discoveryclient.configuration;

import br.com.jabolina.discoveryclient.cluster.IDistributedInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import java.util.Objects;

@Configuration
@DependsOn( "context-bean-getter" )
public class DistributedInstanceConfiguration {

    @Primary
    @Bean
    public IDistributedInstance customDistributedInstance( Environment environment ) {
        String imdg = environment.getProperty( "discovery.distribution.type", "hazelcast" );
        return Objects.requireNonNull(
                SpringContext.requireBean( IDistributedInstance.class, imdg + "-distributed-instance" )
        );
    }
}
