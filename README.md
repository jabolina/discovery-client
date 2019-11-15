# Discovery Client

A distributed map implementation with leader election. The data is services that will subscribe and will be registered
amongst all available replicas, the elected leader will keep sending requests to verify if the service is alive.

For the map replication, 3 different modes can be used, `hazelcast`, `redis` or `atomix`. To use `redis` is needed to have
Redis installed.

When the application is up, the data can be saved like:

```bash
$ curl -d '{
    "name": "user",
    "baseUrl": "http://localhost:9091/api/user"
}' -H 'Content-Type: application/json' -X POST localhost:8080/api/services/subscribe
```

If the application is running on port `8080`.

### Replication

To run multiple replicas, for example, 3 replicas:

```bash
$ mvn spring-boot:run -Dserver.port=8080
$ mvn spring-boot:run -Dserver.port=8081
$ mvn spring-boot:run -Dserver.port=8082
```

When the application is up, services can be registered in one of the replicas and the data will be spread to all replicas,
so to retrieve the registered services, do:

```bash
$ curl localhost:8082/api/services/
```

On any of the replicas available.

#### Atomix

When using Atomix for data replication, the primitive used is a `DistributedMap` which is an implementation from Java
`Map`. Using this primitive, can be configured to use a consistent protocol, for this application Raft is used. The 
map uses a single partition, with <i>N</i> nodes. More can be found [here](https://atomix.io/docs/latest/user-manual/primitives/DistributedMap/).

### Properties

The properties can be placed on `src/main/resources/application.yml`, with the following information:

```yaml
discovery:
    address:
        ip: 127.0.0.1
        port: 8080
        complete: ${discovery.address.ip}:${discovery.address.port}
    cluster:
        size: 2
    distribution:
        type: atomix
    redis:
        address: 127.0.0.1:6379
    atomix:
        name: ${discovery.address.complete}
        members: ${discovery.address.ip}:8080, ${discovery.address.ip}:8081, ${discovery.address.ip}:8082
server:
    port: 8090
    address: 127.0.0.1
spring:
    autoconfigure:
        exclude: org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
    application:
        name: discovery-client
```

The property `discovery.distribution.type` will say what type is being used, in this example the will use Atomix
for the data replication.

The available options are:

    * atomix
    * hazelcast
    * redis

For Atomix, is needed to specify the address of the other nodes, in the property `discovery.atomix.members`. 
Hazelcast and Redis auto-discovery another nodes.

