package br.com.jabolina.discoveryclient.configuration;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

@Configuration
public class RestTemplateConfiguration {

    @Bean
    public RestTemplate restTemplate( Environment environment ) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        RestTemplate restTemplate = new RestTemplate( clientHttpRequestFactory( environment ) );
        restTemplate.getMessageConverters().add( new ByteArrayHttpMessageConverter() );

        return restTemplate;
    }

    @Bean
    public HttpComponentsClientHttpRequestFactory clientHttpRequestFactory( Environment environment )
            throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout( Integer.parseInt( environment.getProperty( "webdav.connectionRequestTimeout", "5000" ) ) );
        factory.setConnectTimeout( Integer.parseInt( environment.getProperty( "webdav.connectionTimeout", "5000" ) ) );
        factory.setReadTimeout( Integer.parseInt( environment.getProperty( "webdav.readTimeout", "5000" ) ) );
        factory.setHttpClient( httpClient() );

        return factory;
    }

    @Bean
    public CloseableHttpClient httpClient() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return HttpClients.custom()
                .setSSLSocketFactory( socketFactory() )
                .build();
    }

    @Bean
    public SSLConnectionSocketFactory socketFactory() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        TrustStrategy strategy = ( X509Certificate[] chain, String authType ) -> true;
        SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial( null, strategy )
                .build();

        return new SSLConnectionSocketFactory(
                sslContext,
                new NoopHostnameVerifier()
        );
    }
}
