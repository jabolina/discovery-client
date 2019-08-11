package br.com.jabolina.discoveryclient.exception;

public class DiscoveryException extends RuntimeException {

    private String code;
    private Integer status;
    private String message;

    public DiscoveryException() {
        super();
    }

    public DiscoveryException( String message ) {
        super( message );
        this.message = message;
    }

    public DiscoveryException( String message, Throwable thrown ) {
        super( message, thrown );
    }

    public String getCode() {
        return code;
    }

    public DiscoveryException setCode( String code ) {
        this.code = code;
        return this;
    }

    public Integer getStatus() {
        return status;
    }

    public DiscoveryException setStatus( Integer status ) {
        this.status = status;
        return this;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public DiscoveryException setMessage( String message ) {
        this.message = message;
        return this;
    }
}
