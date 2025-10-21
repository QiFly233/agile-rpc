package com.qifly.service;

import com.qiflyyy.userservice.proto.GetUserReq;
import com.qiflyyy.userservice.proto.GetUserRes;
import com.qiflyyy.userservice.proto.UserService;

public class MockUserService implements UserService {

    @Override
    public GetUserRes getUser(GetUserReq req) {
        return GetUserRes.newBuilder()
                .setName("user-" + req.getId())
                .build();
    }
}
