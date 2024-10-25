package org.example;

import io.grpc.ManagedChannel;
import org.example.models.GrpcClientConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import proto.FulfillmentServiceGrpc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

@SpringBootTest
@ContextConfiguration(classes = {GrpcClientConfig.class, TestConfig.class})
public class GrpcClientConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testManagedChannelBean() {
        ManagedChannel managedChannel = applicationContext.getBean(ManagedChannel.class);

        assertNotNull(managedChannel, "ManagedChannel bean should be created");
    }

    @Test
    public void testFulfillmentServiceBlockingStubBean() {
        FulfillmentServiceGrpc.FulfillmentServiceBlockingStub stub = applicationContext.getBean(FulfillmentServiceGrpc.FulfillmentServiceBlockingStub.class);

        assertNotNull(stub, "FulfillmentServiceBlockingStub bean should be created");
    }

    @Test
    public void testManagedChannelCreation() {
        GrpcClientConfig config = new GrpcClientConfig();
        ManagedChannel channel = config.managedChannel();

        assertNotNull(channel, "ManagedChannel should be created");
    }

    @Test
    public void testFulfillmentServiceBlockingStubCreation() {
        ManagedChannel mockChannel = mock(ManagedChannel.class);
        GrpcClientConfig config = new GrpcClientConfig();
        FulfillmentServiceGrpc.FulfillmentServiceBlockingStub stub = config.fulfillmentServiceBlockingStub(mockChannel);

        assertNotNull(stub, "FulfillmentServiceBlockingStub should be created");
    }
}