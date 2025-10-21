# agile-rpc
正如其名，agile-rpc是一款面向敏捷开发而提供轻量级 RPC 框架，目标是开箱即用，同时提供服务调用和服务治理的能力。

## 写在前面
大家好呀！👏

这个 RPC 框架是我业余时间所写，源自于我学习和工作中的沉淀。作为一名刚入行的萌新，这个框架肯定不一定成熟，但请一定见谅。我一定会保持热爱，持续跟进与改进这个框架。😄  

如果你对这个框架有需求、问题或者改进建议，请一定要私信我或者提交Issue，我一定会积极响应大家的每一条反馈！🤗

同时也期待大佬们的指导与交流，一起让它变得更健壮！💪

## 主要功能
- 自定义协议，以更小的包体为目标 （✅）
- 客户端与服务端之间的远程调用如本地调用一样简单 （✅）
- 服务注册与发现（目前只支持consul）（✅）
- 路由与负载均衡 （✅）
- 链路追踪与监控 （❌）

## 主要机制
- 线程池统一管理
- 重试机制

## 技术文档
- [技术文档](doc/目录.md)

## 快速开始
- 前置条件
  - JDK 17+
  - protobuf，服务之间的请求/响应体通过protobuf序列化/反序列化
- 创建与启动  
  一般的，一个服务器上启动一个应用（App），一个app可以包含一个服务提供者和多个消费者
  - 服务器A
    ```java
    RpcApp serverApp = new RpcBootstrap()
        .registry()
            .id("consul")
            .baseUrl("http://127.0.0.1:8500")
            .registry("ConsulRegistry")
        .and()
        .provider()
            .service(UserService.class, new MockUserService())
            .port(8080)
            .protocolType(1)
            .registry("consul")
        .and()
        .build();
    serverApp.start();
    ```
  - 服务器B
    ```java
    RpcApp clientApp = new RpcBootstrap()
        .registry()
            .id("consul")
            .baseUrl("http://127.0.0.1:8500")
            .registry("ConsulRegistry")
        .and()
        .consumer()
            .service(UserService.class)
            .protocolType(1)
            .registry("consul")
        .and()
        .build();
    clientApp.start();
    ```
  - 组合
    ```java
    RpcApp rpcApp = new RpcBootstrap()
        .registry()
            .id("consul")
            .baseUrl("http://127.0.0.1:8500")
            .registry("ConsulRegistry")
        .and()
        .provider()
            .service(UserService.class, new MockUserService())
            .port(8080)
            .protocolType(1)
            .registry("consul")
        .and()
        .consumer()
            .service(UserService.class)
            .protocolType(1)
            .registry("consul")
        .build();
    rpcApp.start();
    ```
    
- 服务调用
    ```java
    UserService userService = clientApp.getConsumer("UserService"); // 通过类名即可获取
    GetUserRes res = userService.getUser(GetUserReq.newBuilder().setId(1).build());
    ```