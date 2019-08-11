package br.com.jabolina.discoveryclient.exception;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DefaultRestErrorHandler implements ResponseErrorHandler {

    private static final String ERROR_TEMPLATE = "ERROR: Error proxying. CODE [%s]. STATUS [%s]. MESSAGE [%s]";

    @Override
    public boolean hasError( ClientHttpResponse res ) throws IOException {
        return !res.getStatusCode().is2xxSuccessful();
    }

    @Override
    public void handleError( ClientHttpResponse res ) throws IOException {
        throw new RestClientResponseException(
                String.format( ERROR_TEMPLATE, res.getStatusCode(), res.getStatusText(), res.getBody() ),
                res.getRawStatusCode(),
                res.getStatusText(), res.getHeaders(), res.getStatusText().getBytes(),
                StandardCharsets.UTF_8
        );
    }
}
