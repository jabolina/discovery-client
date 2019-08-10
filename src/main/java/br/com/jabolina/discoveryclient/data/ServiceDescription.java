package br.com.jabolina.discoveryclient.data;

import java.io.Serializable;
import java.util.Objects;

public class ServiceDescription implements Serializable {

    private String id;
    private String name;
    private String baseUrl;
    private boolean enabled;
    private boolean active;

    public String getId() {
        return id;
    }

    public ServiceDescription setId( String id ) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ServiceDescription setName( String name ) {
        this.name = name;
        return this;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public ServiceDescription setBaseUrl( String baseUrl ) {
        this.baseUrl = baseUrl;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public ServiceDescription setEnabled( boolean enabled ) {
        this.enabled = enabled;
        return this;
    }

    public boolean disable() {
        this.enabled = false;
        return true;
    }

    public boolean isActive() {
        return active;
    }

    public ServiceDescription setActive( boolean active ) {
        this.active = active;
        return this;
    }

    @Override
    public boolean equals( Object o ) {
        if ( o instanceof String ) return enabled && o.equals( id );
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        ServiceDescription that = (ServiceDescription) o;
        return Objects.equals( id, that.id ) &&
                Objects.equals( name, that.name ) &&
                Objects.equals( baseUrl, that.baseUrl );
    }

    @Override
    public int hashCode() {
        return Objects.hash( id, name, baseUrl );
    }

    @Override
    public String toString() {
        return "ServiceDescription{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", baseUrl='" + baseUrl + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
