package br.com.jabolina.discoveryclient.configuration;

import br.com.jabolina.discoveryclient.cluster.IDistributedInstance;
import br.com.jabolina.discoveryclient.cluster.impl.redis.RedisDistributedInstance;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class RedissonConfiguration {

    private Config redissonConfig( Environment environment ) {
        Config config = new Config();
        config.setCodec( new JsonJacksonCodec() );
        config.useSingleServer()
                .setAddress( environment.getProperty( "discovery.redis.address" ) );
        //

        return config;
    }

    private RedissonClient customRedissonClient( Environment environment ) {
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        return Redisson.create( redissonConfig( environment ) );
    }

    @Qualifier( "redis-distributed-instance" )
    @Bean( "redis-distributed-instance" )
    public IDistributedInstance redisDistributedInstance( Environment environment ) {
        if ( environment.getProperty( "discovery.distribution.type", "" ).equals( "redis" ) ) {
            System.out.println("KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK");
            return new RedisDistributedInstance( customRedissonClient( environment ) );
        }

        return null;
    }

}
