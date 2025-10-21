package com.qifly.core.transport;

import com.qifly.RpcApp;
import com.qifly.core.bootstrap.RpcBootstrap;
import com.qifly.service.MockUserService;
import com.qiflyyy.userservice.proto.GetUserReq;
import com.qiflyyy.userservice.proto.GetUserRes;
import com.qiflyyy.userservice.proto.UserService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NettyTest {

    private static List<RpcApp> serverApps = new ArrayList<>();
    private static final List<RpcApp> clientApps = new ArrayList<>();
    private static final List<UserService> userServices = new ArrayList<>();
    private static final int SERVER_COUNT = 3;
    private static final int CLIENT_COUNT = 5;
    private static final String baseUrl = "http://127.0.0.1:8500";


    @BeforeClass
    public static void init() throws InterruptedException {
        for (int i = 0; i < SERVER_COUNT; i++) {
            RpcApp serverApp = new RpcBootstrap()
                    .provider()
                    .service(UserService.class, new MockUserService())
                    .port(8080 + i)
                    .protocolType(1)
                    .and()
                    .registry()
                    .baseUrl(baseUrl)
                    .registry("ConsulRegistry")
                    .and()
                    .build();
            serverApp.start();
            serverApps.add(serverApp);
        }


        // 为了确保server注册上consul
        Thread.sleep(10000);

        for (int i = 0; i < CLIENT_COUNT; i++) {
            RpcApp clientApp = new RpcBootstrap()
                    .consumer()
                        .service(UserService.class)
                        .protocolType(1)
                    .and()
                        .registry()
                        .baseUrl(baseUrl)
                        .registry("ConsulRegistry")
                    .and()
                    .build();
            clientApp.start();
            clientApps.add(clientApp);
            UserService userService = clientApp.getConsumer("UserService");
            assertNotNull(userService);
            userServices.add(userService);
        }
    }

    @AfterClass
    public static void destroy() throws InterruptedException {
        for (RpcApp serverApp : serverApps) {
            serverApp.close();
        }
        for (RpcApp clientApp : clientApps) {
            clientApp.close();
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
