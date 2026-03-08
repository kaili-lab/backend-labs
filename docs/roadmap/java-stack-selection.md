# Java 主实验技术栈选型

## 目的
这份文档用于明确 `labs/web-server-scaling/` 主实验的基础技术栈，减少后续搭建时的反复讨论。

## 版本说明
本文中的具体版本结论，基于 `2026-03-07` 当天的官方信息。
后续如果项目真正开始搭建的时间已经明显晚于这个日期，应先重新核对官方文档，再决定是否继续沿用当前版本。

当前文档的作用是：
- 先给出一个清晰、稳定的技术栈基线
- 避免在项目初期不断摇摆
- 为后续实际搭建提供默认参考

## 当前结论
主实验采用以下基线方案：
- `JDK 25`
- `Spring Boot 4.0.x`
- `Maven 3.9.x`
- `PostgreSQL 17`
- `Spring Data JPA + Hibernate`
- `Flyway`
- `Docker Compose`
- `Kafka` 暂定为后续阶段候选，不进入初始阶段

## 各项选择说明

### 1. JDK：`JDK 25`
截至 2026-03-07，`JDK 25` 是最近一个已经发布的 LTS 版本。
对于一个从零开始的新项目，它比 `JDK 21` 更适合作为当前主实验基线。

选择它的原因：
- 是当前较新的长期支持版本
- 适合作为未来一段时间的稳定基线
- 与当前 Spring Boot 官方兼容范围匹配

### 2. 框架：`Spring Boot 4.0.x`
截至 2026-03-07，Spring Boot 官方文档显示当前稳定版为 `4.0.3`。
因此主实验不再从 `Spring Boot 3` 起步，而是直接使用 `Spring Boot 4.0.x`。

选择它的原因：
- 是当前新的主线版本
- 适合新项目直接采用
- 与现代 Java 版本和 Spring Framework 7 生态保持一致

### 3. 构建工具：`Maven 3.9.x`
虽然 Spring Boot 4 只要求 `Maven 3.6.3+`，但当前 Maven 官方稳定发布版本已经是 `3.9.13`。
因此主实验推荐直接使用 `Maven 3.9.x`。

选择它的原因：
- Spring 生态中依然非常主流
- 配置和依赖管理更容易被多数 Java 开发者理解
- 对这个学习项目来说，额外变量更少

### 4. 数据库：`PostgreSQL 17`
数据库类型选择 `PostgreSQL`。
虽然 PostgreSQL 官方当前文档页已经显示 `18` 是 current，但本项目作为学习主实验，更推荐把数据库基线放在 `17`。

这样选择的原因：
- PostgreSQL 本身是非常成熟的关系型数据库
- 适合后续演示索引、事务、锁、查询优化、读写压力等主题
- 使用 `17` 可以减少“所有组件都追最新”带来的额外变量

### 5. 数据访问层：`Spring Data JPA + Hibernate`
当前主实验默认使用：
- 规范层：Jakarta Persistence
- ORM 实现：Hibernate
- Spring 集成方式：Spring Data JPA

这样选择的原因：
- 在 Spring 生态中足够主流
- 适合作为前期开发效率和工程规范的平衡点
- 可以帮助先把系统主线跑起来

但这里需要保留一个明确边界：
- 这不是说后续所有阶段都只能用 ORM
- 当主线进入数据库压力、SQL 优化、查询控制等阶段时，可以按需要引入 `JdbcTemplate`、原生 SQL，甚至后续再讨论 `jOOQ`

### 6. 数据库迁移：`Flyway`
只要主实验开始接入数据库，就应该把 schema 变更纳入版本控制。
因此推荐从数据库正式进入主线开始使用 `Flyway`。

选择它的原因：
- 能把数据库结构演进纳入项目版本历史
- 很适合这个“系统一步步演进”的学习主题
- 能减少手工改库带来的混乱

### 7. 外部依赖管理：`Docker Compose`
数据库、Redis、消息队列等外部依赖统一通过 `Docker Compose` 管理。

这样选择的原因：
- 更适合在 Windows + WSL2 环境下统一管理依赖
- 更容易重建、回滚和共享环境
- 有利于把注意力放在系统演化，而不是本地环境维护

### 8. 消息队列：`Kafka` 暂不提前接入
消息队列在本项目中是重要主题，但当前不应该过早进入主实验。

当前决定是：
- `Kafka` 作为第一候选
- 等主线真正走到异步化、削峰、事件驱动阶段时再正式确定

## 当前不建议的做法
- 不建议一开始就同时支持多套 Java Web 框架
- 不建议一开始就把 PostgreSQL、Redis、Kafka 全部接入
- 不建议把 ORM 固定成唯一长期方案，后续应允许按问题演进

## 后续还需要继续确认的内容
- Spring Boot 项目模块如何组织
- 第一阶段是否需要同时引入 `Testcontainers`
- PostgreSQL 具体镜像标签如何选择
- Kafka 在主线中对应的引入时机

## 官方核对入口
- Spring Boot System Requirements：`https://docs.spring.io/spring-boot/system-requirements.html`
- Oracle Java SE Support Roadmap：`https://www.oracle.com/java/technologies/java-se-support-roadmap.html`
- Apache Maven Download：`https://maven.apache.org/download.cgi`
