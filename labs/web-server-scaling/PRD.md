# 模块化单体商城项目 — Stage 1 设计文档

## 背景

项目需要一个代码实验来配合已有的后端演进线文档（`docs/capability-map/后端演进线.md`）。

原方案是每个演进阶段创建独立项目（stage-00 ~ stage-03），经讨论调整为：**一个模块化单体项目，逐步迭代，覆盖整条演进线的关键能力点。**

本次只实现 **Stage 1（全同步模型）**，目标是：搭好项目骨架，跑通一条完整的下单链路。

---

## 关键设计决策

### 1. Maven 多模块（而非单模块包分离）

理由：
- 模块边界在编译期强制执行——order 模块如果没有声明对 inventory 的 Maven 依赖，就无法 import inventory 的类
- 到阶段 10 拆服务时，每个模块已经是独立 artifact，迁移成本低

### 2. 独立的 mall-app 启动模块

理由：
- 业务模块保持纯库模块，不含 Spring Boot 打包逻辑
- mall-app 负责主类、配置文件、Docker Compose
- 依赖方向清晰：mall-app 依赖 mall-order，通过传递依赖获得其他所有模块

### 3. 模块间通过 Spring 依赖注入直接调用

理由：
- Stage 1 最简做法：mall-order 声明对 mall-inventory/mall-payment/mall-notification 的 Maven 依赖，直接注入 Service 接口
- 不需要额外的 API 模块，后续阶段 10 拆分时再抽接口模块

### 4. Flyway 迁移脚本统一放在 mall-app

理由：
- 所有 `.sql` 放在 `mall-app/src/main/resources/db/migration/`，用顺序版本号（V1、V2、V3...）
- 表名用前缀区分模块归属（`prd_`、`inv_`、`ord_`、`pay_`）
- 这是最简单的方式，避免多 location 版本号冲突

### 5. 跨模块不建数据库外键

理由：
- 模块之间通过业务标识（product_id、order_no）关联
- 为后续服务拆分保留独立性——拆分后不同模块可能在不同数据库

### 6. 下单和支付分离

理由：
- 创建订单后状态为 `CREATED`（待支付），不立即变 `PAID`
- 支付通过独立的确认接口完成，模拟支付回调
- 即使 Stage 1 还是同步模拟，流程和 API 上已经分开，为后续引入支付回调、异步化、MQ、最终一致性保留自然的演进边界

### 7. 商品信息和库存暂时同属 mall-inventory 模块

理由：
- Stage 1 不单独建 mall-product 模块，避免过度拆分
- 但在数据库表层面已经分开：`prd_product`（商品信息）和 `inv_inventory`（库存数量），通过 product_id 关联
- 后续如需单独演进商品读路径（缓存、热点），再拆模块，成本很低

---

## 项目结构

```
web-server-scaling/
├── pom.xml                     # 父 POM
├── mall-common/                # 通用模块：异常、响应体
│   ├── pom.xml
│   └── src/main/java/lab/backend/mall/common/
├── mall-inventory/             # 库存模块（Stage 1 暂含商品信息）
│   ├── pom.xml
│   └── src/main/java/lab/backend/mall/inventory/
├── mall-payment/               # 支付模块
│   ├── pom.xml
│   └── src/main/java/lab/backend/mall/payment/
├── mall-notification/          # 通知模块
│   ├── pom.xml
│   └── src/main/java/lab/backend/mall/notification/
├── mall-order/                 # 订单模块
│   ├── pom.xml
│   └── src/main/java/lab/backend/mall/order/
├── mall-app/                   # 启动模块
│   ├── pom.xml
│   ├── docker-compose.yml
│   └── src/
│       ├── main/
│       │   ├── java/lab/backend/mall/MallApplication.java
│       │   └── resources/
│       │       ├── application.yml
│       │       └── db/migration/   # Flyway 迁移脚本
│       └── test/                   # 集成测试
├── PRD.md                      # 本文件
└── README.md                   # 已有
```

---

## 模块依赖关系

```
mall-app
  └── mall-order
        ├── mall-inventory → mall-common
        ├── mall-payment → mall-common
        ├── mall-notification → mall-common
        └── mall-common
```

mall-app 只显式依赖 mall-order，其余模块通过传递依赖获得。

**关键约束：mall-inventory、mall-payment、mall-notification 三者互不依赖。只有 mall-order 依赖它们。**

---

## Maven 依赖分配

| 模块 | 引入的 starter / 依赖 |
|------|----------------------|
| 父 POM | `spring-boot-starter-parent:3.5.11`, java 21, 内部模块版本管理 |
| mall-common | `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation` |
| mall-inventory | mall-common |
| mall-payment | mall-common |
| mall-notification | mall-common |
| mall-order | mall-common, mall-inventory, mall-payment, mall-notification |
| mall-app | mall-order（传递引入其他所有模块）, `postgresql`, `flyway-core`, `flyway-database-postgresql`, `spring-boot-starter-actuator`, `spring-boot-starter-test`(test scope), `spring-boot-maven-plugin` |

说明：`spring-boot-starter-web` 和 `spring-boot-starter-data-jpa` 放在 mall-common，所有业务模块通过依赖 mall-common 获得。

---

## 包结构

```
lab.backend.mall.common
    ├── exception/              # BusinessException
    └── model/                  # ApiResponse<T>

lab.backend.mall.inventory
    ├── entity/                 # ProductEntity, InventoryEntity
    ├── repository/             # ProductRepository, InventoryRepository
    ├── service/                # InventoryService（接口）, InventoryServiceImpl
    ├── dto/                    # ProductInfo
    └── controller/             # InventoryController

lab.backend.mall.payment
    ├── entity/                 # PaymentEntity
    ├── repository/             # PaymentRepository
    ├── service/                # PaymentService（接口）, PaymentServiceImpl
    └── dto/                    # PaymentResult

lab.backend.mall.notification
    └── service/                # NotificationService（接口）, NotificationServiceImpl

lab.backend.mall.order
    ├── entity/                 # OrderEntity, OrderItemEntity
    ├── repository/             # OrderRepository, OrderItemRepository
    ├── service/                # OrderService（接口）, OrderServiceImpl
    ├── dto/                    # CreateOrderRequest, OrderResponse
    └── controller/             # OrderController

lab.backend.mall                # mall-app 模块
    ├── MallApplication.java    # @SpringBootApplication(scanBasePackages = "lab.backend.mall")
    ├── facade/                 # PaymentFacade（跨模块用例编排，持有事务）
    └── controller/             # PaymentController（支付确认入口）
```

---

## 数据库表设计

### prd_product（商品信息，暂放 mall-inventory 模块）

| 列 | 类型 | 说明 |
|----|------|------|
| id | BIGSERIAL PK | |
| product_id | BIGINT UNIQUE NOT NULL | 商品业务标识 |
| name | VARCHAR(200) NOT NULL | 商品名 |
| price | DECIMAL(10,2) NOT NULL | 单价 |
| created_at | TIMESTAMP NOT NULL | |
| updated_at | TIMESTAMP NOT NULL | |

### inv_inventory（库存数量）

| 列 | 类型 | 说明 |
|----|------|------|
| id | BIGSERIAL PK | |
| product_id | BIGINT UNIQUE NOT NULL | 关联商品 |
| stock | INT NOT NULL DEFAULT 0 | 可用库存 |
| created_at | TIMESTAMP NOT NULL | |
| updated_at | TIMESTAMP NOT NULL | |

### ord_order（订单）

| 列 | 类型 | 说明 |
|----|------|------|
| id | BIGSERIAL PK | |
| order_no | VARCHAR(32) UNIQUE NOT NULL | 订单号 |
| total_amount | DECIMAL(12,2) NOT NULL | |
| status | VARCHAR(20) NOT NULL | CREATED / PAID / CANCELLED |
| created_at | TIMESTAMP NOT NULL | |
| updated_at | TIMESTAMP NOT NULL | |

### ord_order_item（订单明细）

| 列 | 类型 | 说明 |
|----|------|------|
| id | BIGSERIAL PK | |
| order_id | BIGINT NOT NULL FK → ord_order.id | |
| product_id | BIGINT NOT NULL | |
| product_name | VARCHAR(200) NOT NULL | 下单时快照 |
| quantity | INT NOT NULL | |
| unit_price | DECIMAL(10,2) NOT NULL | 下单时快照 |
| created_at | TIMESTAMP NOT NULL | |

### pay_payment（支付记录）

| 列 | 类型 | 说明 |
|----|------|------|
| id | BIGSERIAL PK | |
| order_no | VARCHAR(32) NOT NULL | 关联订单号（跨模块不建 FK） |
| amount | DECIMAL(12,2) NOT NULL | |
| status | VARCHAR(20) NOT NULL | PENDING / SUCCESS / FAILED |
| paid_at | TIMESTAMP | |
| created_at | TIMESTAMP NOT NULL | |

### notification 模块

Stage 1 无表，`NotificationService.sendOrderConfirmation()` 仅日志输出模拟通知。

---

## REST API

```
POST   /api/orders                          创建订单
GET    /api/orders/{orderNo}                查询订单详情
GET    /api/orders                          订单列表（简单分页）

POST   /api/payments/{orderNo}/confirm      确认支付（模拟支付回调）

GET    /api/inventory/{productId}           查询商品信息和库存（调试用）
```

### 创建订单

请求：
```json
{
  "items": [
    { "productId": 1001, "quantity": 2 },
    { "productId": 1002, "quantity": 1 }
  ]
}
```

响应（订单状态为 CREATED，等待支付）：
```json
{
  "code": 200,
  "data": {
    "orderNo": "ORD20260309001",
    "totalAmount": 347.00,
    "status": "CREATED",
    "items": [
      {
        "productId": 1001,
        "productName": "机械键盘",
        "quantity": 2,
        "unitPrice": 99.00
      },
      {
        "productId": 1002,
        "productName": "鼠标垫",
        "quantity": 1,
        "unitPrice": 149.00
      }
    ]
  }
}
```

### 确认支付

请求：
```
POST /api/payments/ORD20260309001/confirm
```

响应：
```json
{
  "code": 200,
  "data": {
    "orderNo": "ORD20260309001",
    "amount": 347.00,
    "status": "SUCCESS"
  }
}
```

支付确认后，订单状态从 `CREATED` 变为 `PAID`，同时触发通知（日志模拟）。

---

## 核心业务流程（Stage 1 全同步）

### 创建订单

```
POST /api/orders
  → OrderService.createOrder()
    → 遍历 items:
      → InventoryService.getProductInfo()     // 查商品信息和价格
      → InventoryService.checkStock()          // 检查库存
    → InventoryService.deductStock()           // 扣减库存
    → 保存 Order + OrderItems                  // 写订单（状态 CREATED）
    → PaymentService.createPayment()           // 创建支付记录（状态 PENDING）
    → 返回 OrderResponse
```

### 确认支付

```
POST /api/payments/{orderNo}/confirm
  → PaymentController（mall-app 模块，只做入参出参转换）
    → PaymentFacade.confirmPayment()           // mall-app 模块，@Transactional，编排跨模块用例
      → paymentService.confirmPayment(orderNo)            // 更新支付状态为 SUCCESS
      → orderService.markAsPaid(orderNo)                   // 更新订单状态为 PAID
      → notificationService.sendOrderConfirmation(orderNo) // 日志模拟通知
      → 返回 PaymentResult
```

两个流程各自在独立的数据库事务内完成。PaymentFacade 持有事务边界，保证支付状态和订单状态在同一事务内更新。

---

## 模块间接口设计

```java
// mall-inventory 模块
public interface InventoryService {
    ProductInfo getProductInfo(Long productId);
    boolean checkStock(Long productId, int quantity);
    void deductStock(Long productId, int quantity);
}

// mall-payment 模块
public interface PaymentService {
    PaymentResult createPayment(String orderNo, BigDecimal amount);
    PaymentResult confirmPayment(String orderNo);
}

// mall-notification 模块
public interface NotificationService {
    void sendOrderConfirmation(String orderNo);
}

// mall-order 模块（供 mall-payment 回调时更新订单状态）
public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request);
    OrderResponse getOrder(String orderNo);
    void markAsPaid(String orderNo);
}
```

注意：确认支付需要同时操作支付记录、订单状态和通知，涉及跨模块调用。如果把编排逻辑放在 mall-payment 模块，就会产生 mall-payment → mall-order 的循环依赖。

**解决方式**：在 mall-app 模块中引入 `PaymentFacade`（应用服务），负责编排"确认支付"这个跨模块用例。

- **PaymentFacade**（mall-app 模块）：注入 PaymentService、OrderService、NotificationService，持有 `@Transactional`，编排确认支付流程
- **PaymentController**（mall-app 模块）：只做 HTTP 入参出参转换，调用 PaymentFacade
- **PaymentService**（mall-payment 模块）：只负责支付记录的 CRUD，不反向调用其他模块

这样的分层：
- Controller 不承担业务逻辑和事务管理
- Facade 负责跨模块编排和事务边界
- 各模块 Service 只负责自己领域内的操作

最终模块职责：
- mall-payment 模块：PaymentEntity、PaymentRepository、PaymentService（纯支付记录操作）
- mall-app 模块：PaymentFacade（跨模块编排 + 事务）、PaymentController（HTTP 入口）

---

## 基础设施

### Docker Compose

`mall-app/docker-compose.yml`，Stage 1 只启动 PostgreSQL 17：

```yaml
services:
  postgres:
    image: postgres:17
    container_name: mall-postgres
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: mall
      POSTGRES_USER: mall
      POSTGRES_PASSWORD: mall123
    volumes:
      - mall-pgdata:/var/lib/postgresql/data

volumes:
  mall-pgdata:
```

后续阶段按需添加 Redis、Kafka 等。

### Flyway 迁移脚本

```
mall-app/src/main/resources/db/migration/
  V1__create_prd_product.sql
  V2__create_inv_inventory.sql
  V3__create_ord_tables.sql
  V4__create_pay_payment.sql
  V5__seed_test_data.sql          # 插入几条测试商品和库存数据
```

### 技术栈基线

- JDK 21, Spring Boot 3.5.11, Maven 3.9.x
- PostgreSQL 17, Spring Data JPA + Hibernate, Flyway
- Docker / Docker Compose 管理外部依赖

---

## Stage 1 的边界

**Stage 1 只演示 happy path（成功链路）。**

以下场景明确不在 Stage 1 处理范围内：
- 未支付订单的超时关闭和库存释放（当前创建订单时直接扣库存，如果不支付，库存不会自动恢复）
- 并发控制（库存超卖留给后续阶段）
- 异步处理
- 缓存
- 认证授权
- 复杂订单状态机（当前只有 CREATED → PAID 两个状态流转）
- 全局错误码体系

---

## 验证方式

### 手动验证

1. `cd mall-app && docker compose up -d` 启动 PostgreSQL
2. 项目根目录 `mvn clean package -DskipTests` 编译通过
3. `mvn spring-boot:run -pl mall-app` 启动应用
4. `curl localhost:8080/api/inventory/1001` 能查到测试商品
5. `curl -X POST localhost:8080/api/orders -H 'Content-Type: application/json' -d '{"items":[{"productId":1001,"quantity":2}]}'` 创建订单成功，状态为 CREATED
6. `curl -X POST localhost:8080/api/payments/ORD.../confirm` 确认支付
7. `curl localhost:8080/api/orders/{orderNo}` 查到订单状态为 PAID
8. 应用日志中能看到 notification 模块的通知输出

### 自动化验证

mall-app 模块中保留一个集成级 smoke test（`@SpringBootTest` + TestRestTemplate），覆盖主链路：
1. 查询商品库存
2. 创建订单
3. 确认支付
4. 查询订单，验证状态为 PAID

**测试数据库策略：**
- Stage 1 测试复用本地 Docker Compose 提供的 PostgreSQL，通过 `test` profile 指向独立的测试数据库（`mall_test`）
- Docker Compose 中通过 init script 同时创建 `mall` 和 `mall_test` 两个数据库
- 后续如需 CI 环境再考虑 Testcontainers
