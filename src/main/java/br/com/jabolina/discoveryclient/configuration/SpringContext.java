package br.com.jabolina.discoveryclient.configuration;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component( "context-bean-getter" )
public class SpringContext implements ApplicationContextAware {

    public static ApplicationContext context;

    public static < T > T requireBean( Class< T > bean ) {
        if ( Objects.isNull( context ) ) {
            return null;
        }

        return context.getBean( bean );
    }

    public static < T > T requireBean( Class< T > bean, String name ) {
        if ( Objects.isNull( context ) ) {
            return null;
        }

        return bean.cast( context.getBean( name ) );
    }

    @Override
    public void setApplicationContext( ApplicationContext applicationContext ) throws BeansException {
        SpringContext.context = applicationContext;
    }
}
