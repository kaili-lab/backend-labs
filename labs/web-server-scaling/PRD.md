# Spring Boot 模块化单体主实验设计文档

## 背景

原方案是按演进线的每个阶段创建独立项目，逐步把一个小系统演化成复杂系统。

现在已经明确调整为：
- `web-server-scaling` 不再创建多个 stage 项目
- `web-server-scaling` 主题下后续包含两个代码项目
- 第一个项目是 **Spring Boot 模块化单体项目**
- 第二个项目是基于这个单体项目拆出来的 **Spring Cloud 微服务项目**

因此，这份 PRD 不再描述某个单独 stage，而是：
**`web-server-scaling` 主题下第一个子项目，也就是 Spring Boot 模块化单体项目的设计文档。**

需要特别说明：
- `docs/capability-map/后端演进线.md` 仍然负责解释“这些能力为什么会出现”
- 但这条演进线不再等价于“代码要按 10 多个阶段一个个新建项目”

换句话说：
- 演进线是**知识解释顺序**
- 单体项目是**第一阶段的能力集中承载载体**
- 微服务项目是**第二阶段的服务边界与分布式问题承载载体**

---

## 项目定位

### 这个主实验要解决什么问题

这个主实验的目标不是做一个“最小起步 demo”，而是做一个：

- 业务语境清晰
- 模块边界明确
- 能同时承载事务、异步、线程池、缓存、稳定性、可观测性等能力
- 但又不提前引入微服务分布式复杂度

的 Spring Boot 模块化单体项目。

### 当前单体项目的异步边界

这份 PRD 对附属异步流程采用如下边界：
- 支付成功后的通知，不阻塞核心写链路
- 通知任务先写入业务化任务表，再由调度器和线程池异步执行
- 当前不引入 `Kafka`
- 当前也不引入通用 `outbox`

这样设计的原因是：
- 先把单体里的事务边界、异步任务、重试和多实例领取问题讲清楚
- 避免单体项目同时承担缓存、数据库、消息总线三种以上复杂度
- 把 `Kafka` 和事件总线放到后续微服务项目，更容易和服务边界一起观察

### 这个主实验不解决什么问题

这份 PRD 不处理以下问题：
- Kafka / 通用事件总线 / `outbox`
- 服务注册与发现
- 服务间远程调用治理
- 网关
- 分布式事务 / Saga
- 配置中心
- 跨服务链路追踪
- 因独立部署带来的服务协调问题

这些内容后续进入同一主题下的第二个项目，也就是 Spring Cloud 微服务项目，不塞进当前这个单体项目。

---

## 项目目标

### 业务目标

使用商城语境，围绕以下主链路建立一个可持续扩展的后端实验项目：
- 商品读取
- 下单
- 扣库存
- 创建支付记录
- 确认支付
- 发送通知

### 能力目标

这个主实验应集中覆盖以下能力：
- 请求链路与应用执行
- 参数校验与统一异常翻译
- 数据库建模、事务边界和状态流转
- 异步化
- 线程池、后台任务与任务编排
- PostgreSQL 使用与典型优化入口
- Redis 缓存
- 一致性与状态同步
- 稳定性治理
- 可观测性
- 多实例友好设计

### 实施原则

- 能力目标一次定义清楚
- 代码实现可以按依赖关系推进
- 但不再把能力拆成多个 stage 项目分别实现

### 和实施切片的关系

- `docs/slices/` 负责定义 `V1 ~ V4` 的训练路线与实施切片
- 当前 `V1 ~ V4` 只覆盖 `mall-monolith/`
- `mall-microservices/` 的实施切片在单体四个切片稳定后再单独规划

---

## 原演进阶段与当前主实验的映射

下面这张表保留原演进线的视角，但它现在表达的是“知识点在当前项目里的落位”，不是“代码实施顺序”。

| 原演进阶段 | 当前在主实验中的落位 |
|-----------|----------------------|
| 阶段 1：同步模型 | 订单、库存、支付的核心同步写链路 |
| 阶段 2：异步化 | 通知和后台任务从主链路拆出 |
| 阶段 3：线程池 | 统一的异步任务执行器与后台任务调度 |
| 阶段 4：性能优化 | 连接池、分页、索引、慢 SQL 观察 |
| 阶段 5：数据库优化 | 表结构设计、事务边界、索引与读写路径治理 |
| 阶段 6：缓存 | Redis 缓存热点商品和读路径数据 |
| 阶段 7：一致性 | 订单、支付、缓存、通知任务之间的状态同步 |
| 阶段 8：稳定性 | 超时、重试、熔断、限流、降级 |
| 阶段 9：MQ 解耦 | 不在当前单体 PRD 中实现，后续进入 Spring Cloud 微服务项目 |
| 阶段 10：服务拆分 | 在同一主题下转入后续 Spring Cloud 微服务项目 |
| 阶段 11：可观测性 | 日志、健康检查、指标、链路追踪入口 |

---

## 设计原则

### 1. 模块化单体，不是伪微服务

- 当前项目是单应用启动
- 模块边界在编译期和包结构上明确
- 但不引入远程调用、服务注册、网关等分布式复杂度

### 2. 演进线用于解释，不用于拆项目

- 文档继续保留“为什么出现线程池、缓存、消息驱动”这条因果线
- 代码则只实现当前单体项目需要承载的那部分能力

### 3. 业务模块边界要真实存在

- 订单、库存、支付、通知按真实业务边界拆分模块
- 模块之间通过接口调用，不直接访问彼此的数据库表

### 4. 跨模块关联用业务标识，不用跨模块外键

- 订单、库存、支付之间通过 `product_id`、`order_no` 等业务标识关联
- 跨模块不建数据库外键
- 模块内部允许保留必要外键，例如 `ord_order_item -> ord_order`

### 5. 核心写链路保持事务清晰，附属流程尽量异步化

- 下单、支付确认这类核心状态变化必须先定义清楚事务边界
- 通知、后台处理这类附属流程应从主链路中剥离

### 6. 单体里的异步先用任务表 + 调度 / 线程池

- 支付成功后，先在事务内创建通知任务
- 调度器负责领取待执行任务
- 线程池负责真正执行通知
- 当前不提前引入 `Kafka / outbox`

### 7. 先把单体能力用深，再基于它拆出微服务项目

- 线程池、缓存、事务、一致性、稳定性、可观测性都可以在单体项目里演示
- 不为了“展示高级架构”而提前拆成微服务

---

## 技术栈与基础设施

### 技术栈基线

- `JDK 21`
- `Spring Boot 3.5.11`
- `Maven 3.9.x`
- `Spring MVC`
- `Spring Data JPA + Hibernate`
- `Flyway`
- `Spring Boot Actuator`
- `Micrometer`

### 基础设施

当前主实验通过 Docker Compose 统一管理：
- `PostgreSQL 17`
- `Redis`

说明：
- PostgreSQL 承担主业务数据存储和通知任务持久化
- Redis 承担热点读路径缓存，以及后续可能的幂等 / 限流辅助能力
- 当前单体项目不包含 `Kafka`
- 当前单体项目也不包含通用 `outbox`

---

## 项目结构

```text
web-server-scaling/
├── README.md
├── PRD.md
├── docs/
│   ├── README.md
│   ├── slices/
│   ├── designs/
│   └── execution/
├── mall-monolith/              # 当前这个 PRD 描述的 Spring Boot 单体项目
│   ├── pom.xml
│   ├── mall-common/
│   ├── mall-inventory/
│   ├── mall-order/
│   ├── mall-payment/
│   ├── mall-notification/
│   └── mall-app/
└── mall-microservices/         # 后续基于单体拆分出来的 Spring Cloud 项目
```

### 目录职责

```text
mall-monolith/mall-app/
├── src/main/java/lab/backend/mall/
│   ├── MallApplication.java
│   ├── config/                 # 线程池、缓存、调度、稳定性、观测配置
│   ├── facade/                 # 跨模块用例编排
│   └── controller/             # 非单一领域控制器入口
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/
├── src/test/
│   ├── java/
│   └── resources/
└── docker/
    └── postgres/init/
```

补充说明：

- `docs/slices/`：定义单体阶段的 `V1 ~ V4` 训练切片，只回答“先做什么、后做什么、每个阶段引入什么压力”
- `docs/designs/`：存放当前切片需要单独说明的关键方案设计，不为每个小功能都创建文档
- `docs/execution/`：存放真正开始编码前的执行清单与批次记录

---

## 模块职责、能力映射与注释重点

| 模块 | 当前主要职责 | 对应能力 / 解决的问题 | 在代码中要重点解释什么 |
|------|--------------|-----------------------|------------------------|
| `mall-common` | 提供 `ApiResponse`、`BusinessException`、统一异常翻译、公共校验支持 | 请求链路与应用执行；解决“Web 边界处理不要在每个模块重复写” | 说明什么属于横切能力，为什么业务规则不能下沉到 common |
| `mall-inventory` | 管理商品信息读取、库存校验、库存扣减；承载商品读路径缓存 | 数据与状态管理、缓存、并发控制入口；解决“订单链路依赖商品价格和库存状态” | 说明为什么商品信息和库存数量分表；说明哪些数据走缓存、哪些必须回源数据库 |
| `mall-order` | 创建订单、保存订单快照、查询订单、分页列表、订单状态流转 | 数据与状态管理、事务边界、数据库优化；解决“主写链路如何稳定落库” | 说明为什么订单必须保存商品快照；说明事务边界为什么放在订单用例上 |
| `mall-payment` | 管理支付记录、支付状态变化、支付确认幂等；对外模拟支付回调入口语义 | 数据与状态管理、一致性、稳定性；解决“支付状态为什么不能和订单状态混为一体” | 说明支付状态独立存在的原因；说明幂等、防重复确认的业务含义 |
| `mall-notification` | 承担支付成功后的附属动作；通过通知任务表、定时扫描和线程池执行通知 | 异步化、线程池、后台任务、重试；解决“为什么通知不应该阻塞核心链路” | 说明为什么任务要先落库再异步执行；说明多实例下为什么要避免重复领取 |
| `mall-app` | 提供启动、装配、配置、观察、测试，以及跨模块 facade 和调度入口 | 请求链路与应用执行、稳定性、可观测性、架构取舍；解决“谁持有跨模块事务和基础设施配置” | 说明为什么 facade 放在 app；说明配置层与业务层的边界 |

---

## 模块依赖关系

```text
mall-app
  ├── mall-common
  ├── mall-inventory
  ├── mall-order
  ├── mall-payment
  └── mall-notification

mall-order
  ├── mall-common
  ├── mall-inventory
  └── mall-payment

mall-inventory
  └── mall-common

mall-payment
  └── mall-common

mall-notification
  └── mall-common
```

### 依赖约束

- 业务模块之间不形成环依赖
- `mall-order` 可以依赖库存、支付接口，因为它承载核心写链路编排
- `mall-payment`、`mall-inventory`、`mall-notification` 彼此不直接依赖
- `mall-app` 作为应用装配层可以依赖多个业务模块，但不承载具体领域规则

---

## 关键能力的落位方式

| 能力点 | 当前方案 | 主要落位 |
|--------|----------|----------|
| 请求链路 | `Controller -> Facade / Service -> Repository` | `mall-app`、各业务模块 |
| 参数校验 | `jakarta.validation` + `@Valid` | DTO、Controller |
| 统一异常 | `BusinessException` + `GlobalExceptionHandler` | `mall-common` |
| 事务 | 本地事务，清晰标注用例边界 | `OrderService`、`PaymentFacade` |
| 异步化 | 非核心附属流程从主链路剥离 | `mall-notification`、`mall-app` |
| 线程池 | 独立执行器，区分通知执行和后台任务 | `mall-app/config` |
| 后台任务 | 基于任务表 + 定时扫描 + 任务领取 | `mall-notification`、`mall-app` |
| 缓存 | Redis Cache Aside，用于商品和热点读路径 | `mall-inventory` |
| 一致性 | 通过事务边界 + 通知任务落库衔接核心链路和附属动作 | `mall-order`、`mall-payment`、`mall-notification` |
| 稳定性 | 超时、重试、熔断、限流落在外部适配层 | `mall-app/config`、外部适配器 |
| 可观测性 | 结构化日志、Actuator、Micrometer、trace 入口 | `mall-app` |
| 多实例友好 | 应用无状态化，任务领取避免重复消费 | 整体设计约束 |

---

## 关键业务流程

### 1. 商品读取与缓存

```text
GET /api/inventory/{productId}
  → InventoryController
    → InventoryService.getProductInfo()
      → 先查 Redis
      → miss 时回源 PostgreSQL
      → 回填缓存
```

这个流程主要用于承载：
- 商品读路径
- Redis 缓存
- 热点数据观察
- 后续缓存一致性讨论

### 2. 创建订单

```text
POST /api/orders
  → OrderController
    → OrderService.createOrder()
      → 查商品信息
      → 校验库存
      → 扣减库存
      → 保存 Order + OrderItems
      → 创建 PaymentRecord（PENDING）
      → 返回 CREATED 状态订单
```

`OrderService.createOrder()` 整体在一个 `@Transactional` 内执行，保证：
- 库存扣减
- 订单写入
- 支付记录创建

要么一起成功，要么一起回滚。

这个流程主要用于承载：
- 同步主写链路
- 本地事务
- 订单快照
- 写路径状态变化

### 3. 确认支付

```text
POST /api/payments/{orderNo}/confirm
  → PaymentController
    → PaymentFacade.confirmPayment()
      → paymentService.confirmPayment(orderNo)
      → orderService.markAsPaid(orderNo)
      → notificationTaskService.enqueueOrderPaidNotification(orderNo)
      → 返回 SUCCESS
```

`PaymentFacade.confirmPayment()` 持有 `@Transactional`，用于保证：
- 支付状态更新
- 订单状态更新
- 通知任务创建

在同一事务内完成。

这个流程主要用于承载：
- 支付状态与订单状态分离
- 幂等确认
- 一致性边界
- 附属异步流程的任务化切分

### 4. 支付成功后的异步通知

```text
NotificationTaskScheduler 定时扫描待执行任务
  → 领取任务（避免多实例重复领取）
    → 提交到 notificationExecutor
      → NotificationService.sendOrderConfirmation()
      → 成功时更新任务状态为 SUCCESS
      → 失败时递增 retry_count 并设置 next_retry_at
```

这个流程主要用于承载：
- 异步化
- 线程池执行
- 任务持久化
- 失败重试
- 多实例领取控制

---

## 数据模型

### prd_product

| 列 | 类型 | 说明 |
|----|------|------|
| id | BIGSERIAL PK | 内部主键 |
| product_id | BIGINT UNIQUE NOT NULL | 商品业务标识 |
| name | VARCHAR(200) NOT NULL | 商品名 |
| price | DECIMAL(10,2) NOT NULL | 单价 |
| created_at | TIMESTAMP NOT NULL | |
| updated_at | TIMESTAMP NOT NULL | |

说明：
- 跨模块关联统一使用 `product_id`
- `id` 只作为表内部主键使用

### inv_inventory

| 列 | 类型 | 说明 |
|----|------|------|
| id | BIGSERIAL PK | |
| product_id | BIGINT UNIQUE NOT NULL | 关联商品业务标识 |
| stock | INT NOT NULL DEFAULT 0 | 可用库存 |
| created_at | TIMESTAMP NOT NULL | |
| updated_at | TIMESTAMP NOT NULL | |

### ord_order

| 列 | 类型 | 说明 |
|----|------|------|
| id | BIGSERIAL PK | |
| order_no | VARCHAR(32) UNIQUE NOT NULL | 订单号 |
| total_amount | DECIMAL(12,2) NOT NULL | |
| status | VARCHAR(20) NOT NULL | CREATED / PAID / CANCELLED |
| created_at | TIMESTAMP NOT NULL | |
| updated_at | TIMESTAMP NOT NULL | |

### ord_order_item

| 列 | 类型 | 说明 |
|----|------|------|
| id | BIGSERIAL PK | |
| order_id | BIGINT NOT NULL FK → ord_order.id | 模块内允许外键 |
| product_id | BIGINT NOT NULL | 商品业务标识 |
| product_name | VARCHAR(200) NOT NULL | 下单时快照 |
| quantity | INT NOT NULL | |
| unit_price | DECIMAL(10,2) NOT NULL | 下单时快照 |
| created_at | TIMESTAMP NOT NULL | |

### pay_payment

| 列 | 类型 | 说明 |
|----|------|------|
| id | BIGSERIAL PK | |
| order_no | VARCHAR(32) NOT NULL | 关联订单号 |
| amount | DECIMAL(12,2) NOT NULL | |
| status | VARCHAR(20) NOT NULL | PENDING / SUCCESS / FAILED |
| paid_at | TIMESTAMP | |
| created_at | TIMESTAMP NOT NULL | |
| updated_at | TIMESTAMP NOT NULL | |

### ntf_notification_task

| 列 | 类型 | 说明 |
|----|------|------|
| id | BIGSERIAL PK | |
| order_no | VARCHAR(32) NOT NULL | 对应订单号 |
| channel | VARCHAR(32) NOT NULL | 通知渠道 |
| status | VARCHAR(20) NOT NULL | PENDING / PROCESSING / SUCCESS / FAILED |
| retry_count | INT NOT NULL DEFAULT 0 | 已重试次数 |
| next_retry_at | TIMESTAMP | 下次重试时间 |
| last_error | VARCHAR(500) | 最近一次失败原因 |
| created_at | TIMESTAMP NOT NULL | |
| updated_at | TIMESTAMP NOT NULL | |

说明：
- 建议对 `(order_no, channel)` 建唯一约束，防止重复创建同类通知任务
- 任务领取时建议使用 `FOR UPDATE SKIP LOCKED` 或等价的原子状态更新方式

---

## REST API

```text
GET    /api/inventory/{productId}           查询商品信息和库存

POST   /api/orders                          创建订单
GET    /api/orders/{orderNo}                查询订单详情
GET    /api/orders                          订单列表（分页）

POST   /api/payments/{orderNo}/confirm      确认支付

GET    /actuator/health                     健康检查
GET    /actuator/metrics                    指标入口
```

---

## 最小工程基线

### 统一响应与异常翻译

- Controller 统一返回 `ApiResponse<T>`
- 业务异常统一抛 `BusinessException`
- `mall-common` 提供 `GlobalExceptionHandler`

### 参数校验

- DTO 使用 `jakarta.validation`
- Controller 统一使用 `@Valid`
- 至少覆盖空值、数量非法、商品列表为空等基础规则

### 线程池与调度

至少定义这些执行器 / 调度器：
- `notificationExecutor`
- `taskScannerScheduler`
- `backgroundJobExecutor`

注释中要说明：
- 这个执行器或调度器服务于哪类任务
- 为什么不能复用同一个无边界线程池

### 通知任务调度

- `mall-notification` 提供任务扫描入口
- 只扫描 `status in (PENDING, FAILED)` 且 `next_retry_at <= now()` 的任务
- 任务领取必须考虑多实例重复领取问题
- 当前优先使用数据库行锁或等价的原子状态更新解决

### 稳定性

稳定性能力优先放在外部适配层：
- 超时
- 重试
- 熔断
- 限流

注释中要说明：
- 哪些操作属于核心写链路，不能随便重试
- 哪些操作属于附属流程，适合做失败兜底

### 可观测性

至少补齐：
- 结构化日志
- `orderNo` / `productId` / `taskId` 等关键业务键
- `/actuator/health`
- Micrometer 指标入口

---

## 测试与验证

### 手动验证

1. `cd mall-app && docker compose up -d`
2. 启动应用
3. 访问 `/actuator/health`，确认应用和数据库可用
4. 查询商品，验证缓存读路径正常
5. 创建订单，验证订单和支付记录创建成功
6. 确认支付，验证订单状态更新成功
7. 检查通知任务创建、执行和重试状态
8. 检查日志与指标是否可见

### 自动化验证

至少保留一个集成级 smoke test，覆盖：
1. 查询商品
2. 创建订单
3. 确认支付
4. 校验订单状态
5. 校验通知任务状态或通知结果

### 测试数据库策略

- 测试默认复用本地 Docker Compose 提供的基础设施
- PostgreSQL 使用独立测试数据库 `mall_test`
- 后续如果进入 CI，再补 `Testcontainers`

---

## 当前明确不放进这个 PRD 的内容

- Kafka / 消息队列
- 通用事件总线 / `outbox`
- 微服务拆分
- 服务注册发现
- API 网关
- 跨服务分布式事务
- 配置中心
- 认证授权体系
- 完整的运营后台和前端页面

这些内容不是不重要，而是不应污染当前这个“Spring Boot 模块化单体主实验”的边界。
