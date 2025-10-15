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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NettyTest {

    private static RpcApp serverApp;
    private static RpcApp clientApp;
    private static UserService userService;

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
                .addProvider(UserService.class, new MockUserService(), 8080)
                .addRegister("http://127.0.0.1:8500", "ConsulRegistry");
        serverApp = serverBootstrap.build();
        serverApp.start();

        // 为了确保server注册上consul
        Thread.sleep(10000);

        RpcBootstrap clientBootstrap = new RpcBootstrap()
                .addConsumer(UserService.class)
                .addRegister("http://127.0.0.1:8500", "ConsulRegistry");
        clientApp = clientBootstrap.build();
        clientApp.start();

        userService = clientApp.getConsumer("UserService");
        assertNotNull(userService);
    }

    @Test
    public void testSingleRequestResponse() {
        GetUserReq req = GetUserReq.newBuilder().setId(1).build();
        GetUserRes res = userService.getUser(req);
        assertNotNull(res);
        assertEquals("user-1", res.getName());
    }

    @Test
    public void testConcurrentRequests() throws Exception {
        int total = 20;
        ExecutorService pool = Executors.newFixedThreadPool(8);
        List<Future<GetUserRes>> futures = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            final int id = i + 100;
            futures.add(pool.submit(() -> userService.getUser(
                    GetUserReq.newBuilder().setId(id).build())));
        }
        for (int i = 0; i < total; i++) {
            GetUserRes r = futures.get(i).get(5, TimeUnit.SECONDS);
            int expectedId = i + 100;
            assertEquals("user-" + expectedId, r.getName());
        }
        pool.shutdownNow();
    }

    @Test
    public void testSequentialMultipleCalls() {
        for (int i = 1; i <= 5; i++) {
            GetUserRes res = userService.getUser(GetUserReq.newBuilder().setId(i).build());
            assertEquals("user-" + i, res.getName());
        }
    }
}
