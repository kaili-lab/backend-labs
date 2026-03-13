# CompletableFuture 深度解析

*从 Thread → 线程池 → CompletableFuture 的演进与实践*

---

## 1. 历史演进：为什么会有 CompletableFuture？

### 1.1 第一阶段：Thread（JDK 1.0）

最原始的异步方式是直接 `new Thread()`：
```java
new Thread(() -> {
    String result = callRemoteService();
    System.out.println(result);
}).start();
```

痛点：
- 线程是昂贵资源，创建/销毁开销大
- `Runnable` 没有返回值，无法方便地获取结果
- 线程数量不可控，容易 OOM

### 1.2 第二阶段：线程池 + Future（JDK 1.5）
```java
ExecutorService pool = Executors.newFixedThreadPool(10);
Future<String> future = pool.submit(() -> callRemoteService());
// ... 做其他事 ...
String result = future.get(); // 阻塞，直到结果返回
```

解决了：线程复用、可以获取异步结果。

但 Future 依然有明显缺陷：
- `future.get()` 会阻塞当前线程
- 无法在任务完成时自动触发回调（无法做到"完成后执行下一步"）
- 多个 Future 之间的编排极其繁琐，比如"等 A 和 B 都完成后做 C"
- 异常处理麻烦：`get()` 抛受检异常，代码丑陋

### 1.3 第三阶段：CompletableFuture（JDK 1.8）

核心驱动力是两个字：**编排（Orchestration）**。

它解决的不只是"异步执行"，而是**异步流程的串联、并联、合并、异常处理**这一整套问题：

- 链式调用：A 完成 → 自动触发 B → 自动触发 C
- 并行合并：等 A、B 同时完成 → 合并结果做 C
- 任意完成：A、B 谁先完成就用谁的结果
- 非阻塞回调：不需要 `.get()` 阻塞等结果
- 统一异常处理：链式的 `exceptionally` / `handle`

> **一句话总结**：Thread 解决了异步执行；线程池解决了线程复用和获取结果；CompletableFuture 解决了异步任务的流程编排——这是从"能用"到"好用"的关键跨越。

---

## 2. 本质：CompletableFuture 是什么？

### 2.1 它不是线程，也不是线程池

CompletableFuture 本身是一个对象，代表"一个未来会完成的计算及其结果"。它的本质是：

| 角色 | 说明 |
|------|------|
| 状态机 | 内部维护三种状态：未完成（pending）、正常完成（completed）、异常完成（exceptionally） |
| 回调容器 | 持有注册的回调链，当状态变为完成时，依次触发这些回调 |
| Future 实现 | 实现了 `Future` 和 `CompletionStage` 接口，可当普通 Future 用，也支持链式编排 |

### 2.2 回调是谁触发的？

**是完成任务的那个线程触发的**，没有中心调度器。
```
线程池线程执行完任务
    → 将 CompletableFuture 状态置为 completed
    → 检查是否有注册的回调
    → thenApply：直接在本线程执行回调
    → thenApplyAsync：将回调提交到线程池，然后本线程才空闲
```

这和 Promise 的触发机制有本质区别：

| | Promise (JS) | CompletableFuture (Java) |
|---|---|---|
| 触发者 | Event Loop（单线程中心调度） | 完成任务的线程（无中心调度器） |
| 线程模型 | 单线程，无并发问题 | 多线程，需注意线程安全 |
| 回调执行 | 下一个事件循环 tick | 立即在当前线程或提交到线程池 |

### 2.3 和 Promise 是同一理念吗？

**是的**，两者都源于函数式编程中的 **Monad** 概念，核心理念完全一致：
把"值"和"针对这个值的后续操作"封装在一起，状态只能从 pending 单向流转到 settled，且不可逆。

Java 8 和 ES6 是同一时期（2014 年前后）的产物，是那个时代异步编程思想成熟的集中体现。

对应关系：

| Promise (ES6) | CompletableFuture (Java 8) |
|---|---|
| `new Promise((resolve, reject) => ...)` | `new CompletableFuture<>()` + `complete()` / `completeExceptionally()` |
| `.then(fn)` | `.thenApply(fn)` |
| `.then(() => anotherPromise)` | `.thenCompose(fn)` |
| `Promise.all([...])` | `CompletableFuture.allOf(...)` |
| `Promise.race([...])` | `CompletableFuture.anyOf(...)` |
| `.catch(fn)` | `.exceptionally(fn)` |
| `.finally(fn)` | `.whenComplete(fn)` |

### 2.4 线程谁来出？

CompletableFuture **自己不创建线程**，执行任务的线程来源有两种：

- **默认**：`ForkJoinPool.commonPool()`（JVM 全局共享，线程数 = CPU 核数 - 1）
- **推荐**：传入自定义 `Executor`，控制线程池的大小和类型

> ⚠️ **生产环境强烈建议传入自定义线程池**。`commonPool` 被 JVM 内所有使用者共享，I/O 密集型任务（HTTP、数据库、Redis）会大量阻塞，很容易打满，影响其他组件甚至 GC。

### 2.5 与 Future 的核心区别

| 能力 | Future | CompletableFuture |
|------|--------|-------------------|
| 获取结果 | `get()`，阻塞 | `get()` 阻塞 / 回调非阻塞 |
| 完成时回调 | ❌ 不支持 | ✓ `thenApply` / `thenAccept` 等 |
| 多任务编排 | ❌ 极麻烦 | ✓ `allOf` / `anyOf` 等 |
| 手动完成 | ❌ 不支持 | ✓ `complete()` 手动触发 |
| 异常处理 | `get()` 抛受检异常 | ✓ `exceptionally` / `handle` |

---

## 3. 常用 API 全览

### 3.1 创建
```java
ExecutorService pool = Executors.newFixedThreadPool(10);

// 无返回值
CompletableFuture<Void> f1 = CompletableFuture.runAsync(() -> doWork(), pool);

// 有返回值（最常用）
CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> callService(), pool);

// 直接创建已完成的 Future（测试/快速返回常用）
CompletableFuture<String> f3 = CompletableFuture.completedFuture("已知结果");
```

### 3.2 结果转换：thenApply / thenApplyAsync
```java
CompletableFuture<Integer> result = CompletableFuture
    .supplyAsync(() -> "hello", pool)
    .thenApply(s -> s.length());

// thenApplyAsync：强制提交到线程池执行
CompletableFuture<Integer> result2 = CompletableFuture
    .supplyAsync(() -> "hello", pool)
    .thenApplyAsync(s -> s.length(), pool);
```

| 方法 | 回调执行线程 |
|------|-------------|
| `thenApply` | 完成上一个 Future 的线程（不确定，可能是调用方线程） |
| `thenApplyAsync` | 强制在指定（或默认）线程池执行 |

### 3.3 消费结果：thenAccept / thenRun
```java
// thenAccept：消费结果，无返回值
CompletableFuture.supplyAsync(() -> "result", pool)
    .thenAccept(s -> System.out.println("结果是：" + s));

// thenRun：不关心上一步结果，直接执行
CompletableFuture.supplyAsync(() -> "result", pool)
    .thenRun(() -> System.out.println("任务完成了"));
```

### 3.4 合并：thenCompose / thenCombine
```java
// thenCompose：A 的结果作为 B 的输入，避免双层嵌套
CompletableFuture<String> result = CompletableFuture
    .supplyAsync(() -> getUserId(), pool)
    .thenCompose(userId ->
        CompletableFuture.supplyAsync(() -> getUserInfo(userId), pool)
    );

// thenCombine：两个独立 Future 都完成后合并
CompletableFuture<String> userF  = CompletableFuture.supplyAsync(() -> getUser(), pool);
CompletableFuture<String> orderF = CompletableFuture.supplyAsync(() -> getOrder(), pool);

CompletableFuture<String> combined = userF.thenCombine(
    orderF,
    (user, order) -> user + " | " + order
);
```

### 3.5 多任务并行：allOf / anyOf
```java
// allOf：等所有完成（返回 Void，需手动 join 各子任务）
CompletableFuture.allOf(f1, f2, f3).join();
List<String> results = List.of(f1.join(), f2.join(), f3.join());

// anyOf：任意一个完成即触发
String first = (String) CompletableFuture.anyOf(f1, f2, f3).join();
```

### 3.6 超时控制（JDK 9+）
```java
// orTimeout：超时后以 TimeoutException 异常完成
CompletableFuture<String> f = CompletableFuture
    .supplyAsync(() -> callSlowService(), pool)
    .orTimeout(200, TimeUnit.MILLISECONDS);

// completeOnTimeout：超时后用默认值完成，不抛异常
CompletableFuture<String> f2 = CompletableFuture
    .supplyAsync(() -> callSlowService(), pool)
    .completeOnTimeout("默认值", 200, TimeUnit.MILLISECONDS);
```

> JDK 8 没有这两个方法，需要手动用 `ScheduledExecutorService` 实现，是一个常见坑点。

### 3.7 异常处理
```java
// exceptionally：出错时提供默认值（类似 catch）
CompletableFuture<String> r1 = CompletableFuture
    .supplyAsync(() -> { throw new RuntimeException("出错"); }, pool)
    .exceptionally(ex -> { log.error("捕获异常", ex); return "默认值"; });

// handle：不管成功失败都执行，可以改变结果
CompletableFuture<String> r2 = CompletableFuture
    .supplyAsync(() -> callService(), pool)
    .handle((value, ex) -> {
        if (ex != null) { log.error("异常", ex); return "兜底值"; }
        return value;
    });

// whenComplete：监听结果但不改变结果（只做副作用）
CompletableFuture<String> r3 = CompletableFuture
    .supplyAsync(() -> callService(), pool)
    .whenComplete((value, ex) -> {
        if (ex != null) log.error("失败", ex);
        else log.info("成功：" + value);
    });
```

---

## 4. 电商场景实战

### 场景 A：商品详情页聚合（allOf 并行）

用户打开商品页，需要同时获取：商品基本信息、库存、价格、评论、推荐商品。
五个接口相互独立，串行约 1000ms，并行只需 200ms。
```java
ExecutorService pool = Executors.newFixedThreadPool(20);

CompletableFuture<Product>    productF    = CompletableFuture.supplyAsync(() -> productService.get(productId), pool);
CompletableFuture<Stock>      stockF      = CompletableFuture.supplyAsync(() -> stockService.get(productId), pool);
CompletableFuture<Price>      priceF      = CompletableFuture.supplyAsync(() -> priceService.get(productId), pool);
CompletableFuture<List<Review>> reviewsF  = CompletableFuture.supplyAsync(() -> reviewService.get(productId), pool);
CompletableFuture<List<Product>> recF     = CompletableFuture.supplyAsync(() -> recService.get(productId), pool);

// 等所有完成
CompletableFuture.allOf(productF, stockF, priceF, reviewsF, recF).join();

ProductDetailVO vo = new ProductDetailVO(
    productF.join(), stockF.join(), priceF.join(), reviewsF.join(), recF.join()
);
```

### 场景 B：下单流程串行依赖（thenCompose 链）

创建订单 → 拿到订单 ID → 扣减库存 → 发送消息通知。每一步依赖上一步的结果。
```java
CompletableFuture<Void> orderFlow = CompletableFuture
    .supplyAsync(() -> orderService.create(cart), pool)         // 创建订单，返回 orderId
    .thenCompose(orderId ->
        CompletableFuture.supplyAsync(() -> {
            stockService.deduct(orderId);                        // 扣减库存
            return orderId;
        }, pool)
    )
    .thenCompose(orderId ->
        CompletableFuture.runAsync(() ->
            messageService.sendOrderCreated(orderId), pool      // 发送消息
        )
    )
    .exceptionally(ex -> {
        log.error("下单流程失败", ex);
        compensationService.rollback();                         // 补偿回滚
        return null;
    });
```

### 场景 C：风控超时兜底（completeOnTimeout）

调用风控服务，200ms 内没返回就用默认策略放行，不能让风控拖慢主流程。
```java
RiskResult result = CompletableFuture
    .supplyAsync(() -> riskService.check(order), pool)
    .completeOnTimeout(RiskResult.PASS, 200, TimeUnit.MILLISECONDS) // 超时默认放行
    .exceptionally(ex -> {
        log.error("风控服务异常", ex);
        return RiskResult.PASS;                                      // 异常也默认放行
    })
    .join();
```

### 场景 D：多仓库存查询取最快结果（anyOf）

查询离用户最近的三个仓库哪个有货，谁先返回用谁。
```java
CompletableFuture<Stock> bj = CompletableFuture.supplyAsync(() -> warehouseService.query("北京", productId), pool);
CompletableFuture<Stock> sh = CompletableFuture.supplyAsync(() -> warehouseService.query("上海", productId), pool);
CompletableFuture<Stock> gz = CompletableFuture.supplyAsync(() -> warehouseService.query("广州", productId), pool);

Stock fastest = (Stock) CompletableFuture.anyOf(bj, sh, gz).join();
```

---

## 5. 踩坑指南

### 坑 1：默认线程池是 ForkJoinPool.commonPool()
```java
// ❌ 危险：I/O 任务用 commonPool，线程数只有 CPU核数-1
CompletableFuture.supplyAsync(() -> httpClient.get(url));

// ✅ 正确：I/O 任务用专用线程池
ExecutorService ioPool = Executors.newFixedThreadPool(50);
CompletableFuture.supplyAsync(() -> httpClient.get(url), ioPool);
```

### 坑 2：get() vs join() 的区别

| 方法 | 异常类型 | 建议 |
|------|----------|------|
| `get()` | 受检异常（`ExecutionException`），必须 try-catch | 兼容旧代码时用 |
| `join()` | 非受检异常（`CompletionException`），可不 try-catch | 新代码推荐 |

> ⚠️ 两者都会阻塞当前线程。在回调（如 `thenApply`）中调用 `get()/join()` 可能造成死锁或线程池耗尽。

### 坑 3：thenApply 的执行线程不确定

- `thenApply` 的回调在"完成上一个 Future 的线程"上执行
- 如果上一个 Future 已经完成，回调可能跑在**调用方线程**（如 main 线程）上
- CPU 密集型回调用 `thenApplyAsync` 确保始终在线程池执行

### 坑 4：allOf 不收集结果
```java
// ❌ 错误：allOf 返回 Void，强转会 ClassCastException
String result = (String) CompletableFuture.allOf(f1, f2).join();

// ✅ 正确：allOf 后分别 join 各子任务
CompletableFuture.allOf(f1, f2).join();
List<String> results = List.of(f1.join(), f2.join());
```

### 坑 5：异常被静默吞掉
```java
// ❌ 危险：没有 get/join，没有 exceptionally，异常消失
CompletableFuture.runAsync(() -> {
    throw new RuntimeException("出错了");
}, pool);

// ✅ 最佳实践：总是加异常处理
CompletableFuture.runAsync(() -> doWork(), pool)
    .exceptionally(ex -> { log.error("任务失败", ex); return null; });
```

### 坑 6：线程池过早关闭
```java
// ❌ 危险：shutdown 后异步任务可能未完成
CompletableFuture.supplyAsync(() -> slowTask(), pool);
pool.shutdown();

// ✅ 正确：等任务完成后再关闭
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> slowTask(), pool);
future.join();
pool.shutdown();
```

### 坑 7：JDK 8 没有超时 API

`orTimeout` 和 `completeOnTimeout` 是 JDK 9 才加入的。JDK 8 项目中需要手动实现：
```java
// JDK 8 的超时兜底写法（繁琐）
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> callSlowService(), pool);

scheduler.schedule(() -> future.complete("默认值"), 200, TimeUnit.MILLISECONDS);
```

---

## 6. 快速决策速查

| 场景 | API |
|------|-----|
| 异步执行，无返回值 | `runAsync(task, pool)` |
| 异步执行，有返回值 | `supplyAsync(task, pool)` |
| 对结果做变换 | `thenApply` / `thenApplyAsync` |
| A 的结果传给 B（B 也返回 Future） | `thenCompose` |
| A 和 B 独立并行，合并结果 | `thenCombine` |
| 等所有任务完成 | `allOf(...).join()` |
| 任意一个完成即可 | `anyOf(...)` |
| 超时后抛异常 | `orTimeout(...)` JDK 9+ |
| 超时后用默认值 | `completeOnTimeout(...)` JDK 9+ |
| 出错时提供默认值 | `exceptionally` |
| 不管成功失败都处理 | `handle` |
| 记录日志、不改变结果 | `whenComplete` |

---

*文档持续更新中，review 后根据问题补充细节。*