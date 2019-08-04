# discovery-client
A raw implementation of discovery client without Eureka.

This implementation uses distributed queues to save every registered service. Multiple instances can be executed, with 
Hazelcast all registered services will be synchronized between the replicas.

To verify the if the registered services are available, the replica leader will keep issuing requests to the subscribe
services every 1 minute. At the moment is using `RestTemplate`, it will be nice to use the new `WebClient` to this
verification.

When one of the replica dies, all others replicas are notified, and a new leader is elected to verify the services.
