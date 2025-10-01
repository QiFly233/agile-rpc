package com.qifly.core.transport;

import com.qifly.RpcApp;
import com.qifly.core.bootstrap.RpcBootstrap;
import com.qiflyyy.userservice.proto.GetUserReq;
import com.qiflyyy.userservice.proto.GetUserRes;
import com.qiflyyy.userservice.proto.UserService;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MultiNettyClientTest {

    private static RpcApp serverApp;
    private static final List<RpcApp> clientApps = new ArrayList<>();
    private static final List<UserService> userServices = new ArrayList<>();
    private static final int CLIENT_COUNT = 5;

    public static class MockUserService implements UserService {
        @Override
        public GetUserRes getUser(GetUserReq req) {
            return GetUserRes.newBuilder()
                    .setName("user-" + req.getId())
                    .build();
        }
    }

    @BeforeClass
    public static void init() throws InterruptedException {
        RpcBootstrap serverBootstrap = new RpcBootstrap()
                .addProvider(UserService.class, new MockUserService(), 8081)
                .addRegister("http://127.0.0.1:8500", 1);
        serverApp = serverBootstrap.build();
        serverApp.start();

        Thread.sleep(10000);

        for (int i = 0; i < CLIENT_COUNT; i++) {
            RpcBootstrap clientBootstrap = new RpcBootstrap()
                    .addConsumer(UserService.class)
                    .addRegister("http://127.0.0.1:8500", 1);
            RpcApp clientApp = clientBootstrap.build();
            clientApp.start();
            clientApps.add(clientApp);

            UserService userService = clientApp.getConsumer("UserService");
            assertNotNull(userService);
            userServices.add(userService);
        }
    }

    @Test
    public void testMultipleClientsConcurrentRequests() throws Exception {
        int callsPerClient = 20;
        ExecutorService pool = Executors.newFixedThreadPool(CLIENT_COUNT * 2);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<GetUserRes>> futures = new ArrayList<>();

        for (int i = 0; i < CLIENT_COUNT; i++) {
            final UserService userService = userServices.get(i);
            for (int j = 0; j < callsPerClient; j++) {
                final int id = 10000 + i * 1000 + j;
                futures.add(pool.submit(() -> {
                    start.await(5, TimeUnit.SECONDS);
                    return userService.getUser(GetUserReq.newBuilder().setId(id).build());
                }));
            }
        }

        start.countDown();

        for (int i = 0; i < CLIENT_COUNT; i++) {
            for (int j = 0; j < callsPerClient; j++) {
                int idx = i * callsPerClient + j;
                int expectId = 10000 + i * 1000 + j;
                GetUserRes res = futures.get(idx).get(10, TimeUnit.SECONDS);
                assertEquals("user-" + expectId, res.getName());
            }
        }

        pool.shutdownNow();
    }
}