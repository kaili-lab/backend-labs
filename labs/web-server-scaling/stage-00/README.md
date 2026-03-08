# stage-00：最小可运行基线

## 阶段目标
这个阶段的目标不是解决高并发、高可用或高扩展问题，而是建立整个演化路线的起点。
它需要提供一个清晰、可运行、可验证的最小 Spring Boot Web 服务，作为后续所有阶段的对照组。

## 当前系统形态
- 单体应用
- 单实例运行
- 单个 Spring Boot Web 服务
- 不接数据库
- 不接 Redis
- 不接消息队列
- 只提供最基本的 HTTP 请求处理能力

## 这一阶段应该包含什么
- 一个最小可运行的 Spring Boot 项目
- 一个最基本的启动入口
- 一个最基础的 HTTP 接口，例如 `/ping` 或 `/healthz`
- 最少量的项目结构，用来表达请求处理链路
- 基础运行说明和验证方式

## 这一阶段不应该包含什么
- 不接入 PostgreSQL
- 不接入 Redis
- 不接入 Kafka 或其他消息队列
- 不引入复杂业务建模
- 不做复杂多模块结构
- 不提前引入线程池、缓存、限流、监控等后续阶段能力
- 不追求生产级完备性

## 为什么要这样克制
这样设计是为了避免一开始就把注意力分散到基础设施、业务细节和优化细节上。
`stage-00` 的核心价值不是“功能丰富”，而是“提供一个足够清楚的起点”。

## 推荐的最小能力
建议当前阶段只提供：
- 服务启动能力
- 基本 HTTP 路由
- 简单 JSON 或文本响应
- 最基础的配置文件

## 当前代码结构
- `src/main/java/.../WebServerScalingStage00Application.java`：启动入口
- `src/main/java/.../web/PingController.java`：最小 HTTP 接口
- `src/main/java/.../service/PingService.java`：最小服务层，用来表达请求链路
- `src/main/resources/application.yml`：基础配置
- `src/test/java/.../WebServerScalingStage00ApplicationTests.java`：最小接口测试

## 当前提供的接口
- `GET /ping`：返回最小 JSON 响应，表示服务可用
- `GET /healthz`：返回简化健康状态
- `GET /actuator/health`：由 Spring Boot Actuator 提供的健康检查端点

## 请求处理认知基线
这个阶段应该帮助回答这些最基础的问题：
- 一个 HTTP 请求是如何进入应用的
- Controller 如何接收请求并返回响应
- Spring Boot 最小 Web 服务的运行形态是什么
- 后续所有演化，是相对于这个最小系统发生的

## 运行方式
在 `labs/web-server-scaling/stage-00/` 目录下执行：

```bash
./mvnw spring-boot:run
```

默认端口为 `8080`。

## 验证方式
服务启动后，可以执行：

```bash
curl http://localhost:8080/ping
curl http://localhost:8080/healthz
curl http://localhost:8080/actuator/health
```

## 如何验证这个阶段达标
达到以下条件即可认为 `stage-00` 完成：
- 项目可以成功启动
- 本地可以访问最小接口并得到预期响应
- 文档能说明当前阶段目标和边界
- 后续阶段可以在这个基础上继续演化，而不需要推倒重来

## 当前阶段和后续阶段的关系
`stage-00` 不是为了证明系统已经“足够强”，而是为了让后续每一次变化都有参照物。
没有这个基线，后面就很难判断某个新技术到底解决了什么问题。

## 下一步方向
`stage-00` 完成后，下一步不是立刻补很多底层理论，而是进入 `stage-01`，开始讨论请求量增加时系统为什么会暴露并发压力。
