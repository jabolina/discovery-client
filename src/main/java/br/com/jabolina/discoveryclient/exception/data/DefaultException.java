package br.com.jabolina.discoveryclient.exception.data;

public class DefaultException {

    private String message;
    private String title;
    private Integer status;

    public String getMessage() {
        return message;
    }

    public DefaultException setMessage( String message ) {
        this.message = message;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public DefaultException setTitle( String title ) {
        this.title = title;
        return this;
    }

    public Integer getStatus() {
        return status;
    }

    public DefaultException setStatus( Integer status ) {
        this.status = status;
        return this;
    }

    @Override
    public String toString() {
        return "DefaultException{" +
                "message='" + message + '\'' +
                ", title='" + title + '\'' +
                ", status=" + status +
                '}';
    }
}
