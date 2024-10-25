package org.example.models;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import proto.FulfillmentServiceGrpc;

@Configuration
public class GrpcClientConfig {

    @Bean
    public ManagedChannel managedChannel() {
        return ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();
    }

    @Bean
    public FulfillmentServiceGrpc.FulfillmentServiceBlockingStub fulfillmentServiceBlockingStub(ManagedChannel managedChannel) {
        return FulfillmentServiceGrpc.newBlockingStub(managedChannel);
    }
}