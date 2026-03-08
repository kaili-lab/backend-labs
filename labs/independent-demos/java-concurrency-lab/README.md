# Java 并发武器库实验

这个实验项目用于集中演示 Java 后端里常见并发武器的行为差异。

## 这个实验的职责
- 为 `docs/java-topics/Java并发与高性能编程/` 下的并发专题提供代码演示
- 统一比较不同并发实现方式的使用姿势
- 观察线程名称、总耗时和任务组织方式的差异

## 当前已实现的武器
- `Thread`
- `FutureTask`
- `ThreadPoolExecutor`
- `CompletableFuture`
- Spring `@Async` + `ThreadPoolTaskExecutor`
- JDK 21 虚拟线程

## 运行方式
在 `labs/independent-demos/java-concurrency-lab/` 目录下执行：

```bash
./mvnw spring-boot:run
```

## 测试方式
执行：

```bash
./mvnw test
```

## 接口列表
- `GET /demos/concurrency/thread`
- `GET /demos/concurrency/future-task`
- `GET /demos/concurrency/thread-pool`
- `GET /demos/concurrency/completable-future`
- `GET /demos/concurrency/spring-async`
- `GET /demos/concurrency/virtual-thread`

## 观察重点
每个接口都会返回：
- 当前武器名称
- 总耗时
- 每个子任务的线程名
- 每个子任务的耗时
- 对该武器的一句总结

## 这轮实验的目标
这轮先做最小可运行版本，重点看清：
- 哪些武器更偏底层
- 哪些武器更偏工程管理
- 哪些武器更偏任务编排
- Spring 异步和 JDK 线程池是什么关系
- 虚拟线程的定位到底是什么
