package br.com.jabolina.discoveryclient.service;

import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public interface ProxyService {

    ResponseEntity proxyRequest( HttpServletRequest request ) throws IOException;
}
