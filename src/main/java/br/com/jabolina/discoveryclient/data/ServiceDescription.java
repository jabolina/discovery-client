package br.com.jabolina.discoveryclient.data;

import java.io.Serializable;

public class ServiceDescription implements Serializable {

    private String id;
    private String name;
    private String baseUrl;

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

    @Override
    public String toString() {
        return "ServiceDescription{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", baseUrl='" + baseUrl + '\'' +
                '}';
    }
}
