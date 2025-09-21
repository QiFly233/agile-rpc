package com.qiflyyy.userservice.proto;

public interface UserService {


    int getUserRpcId = 1;
    GetUserRes getUser(GetUserReq req);

}
