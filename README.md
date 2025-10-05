# agile-rpc
正如名字agile-rpc一样，这是一款目标为敏捷开发而提供轻量级 RPC 框架

## 主要功能
- 自定义协议，以更小的包体为目标 （✅）
- 客户端与服务端之间的远程调用如本地调用一样简单 （✅）
- 服务注册与发现（目前只支持consul）（✅）
- 路由与负载均衡 （✅）
- 链路追踪与监控 （❌）

## 主要机制
- 线程池统一管理
- 重试机制

## 快速开始
- 前置条件
  - JDK 17+
  - protobuf，服务之间的请求/响应体通过protobuf序列化/反序列化
- 创建与启动  
  一般的，一个服务器上启动一个应用（App），一个app可以包含一个服务提供者和多个消费者
  - 服务器A
      ```java
      RpcApp serverApp = new RpcBootstrap()
             .addProvider(UserService.class, new UserServiceImpl(), 8080)
             .addRegister("http://127.0.0.1:8500", 1)
             .build();
      ```
  - 服务器B
      ```java
      RpcApp clientApp = new RpcBootstrap()
             .addConsumer(UserService.class)
             .addRegister("http://127.0.0.1:8500", 1)
             .build();
      ```
  - 组合
      ```java
      RpcApp app = new RpcBootstrap()
             .addProvider(OrderService.class, new OrderServiceImpl(), 8080)
             .addConsumer(UserService.class)
             .addRegister("http://127.0.0.1:8500", 1)
             .build();
      ```
    
- 服务调用
    ```java
    UserService userService = clientApp.getConsumer("UserService"); // 通过类名即可获取
    GetUserRes res = userService.getUser(GetUserReq.newBuilder().setId(1).build());
    ```