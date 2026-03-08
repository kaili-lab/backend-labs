package lab.backend.independentdemos.javaconcurrencylab.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import lab.backend.independentdemos.javaconcurrencylab.model.WeaponDemoResponse;
import lab.backend.independentdemos.javaconcurrencylab.model.WeaponTaskResult;
import org.springframework.stereotype.Service;

/**
 * 并发武器实验服务。
 *
 * 这个服务把不同并发实现方式统一封装成一组可调用的方法，
 * 方便通过 HTTP 接口做横向比较。
 */
@Service
public class ConcurrencyDemoService {

    /**
     * 所有示例都统一使用同一组模拟耗时，
     * 这样不同武器之间的对比才更公平。
     */
    private static final long TASK_DELAY_MS = 120L;

    private final SpringAsyncDemoService springAsyncDemoService;

    /**
     * 注入 Spring 异步实验服务。
     *
     * @param springAsyncDemoService Spring 异步实验服务
     */
    public ConcurrencyDemoService(SpringAsyncDemoService springAsyncDemoService) {
        this.springAsyncDemoService = springAsyncDemoService;
    }

    /**
     * 演示直接创建 `Thread` 的方式。
     *
     * @return 线程方式的实验结果
     */
    public WeaponDemoResponse runThreadDemo() {
        long startedAt = System.currentTimeMillis();
        List<WeaponTaskResult> tasks = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(2);
        AtomicReference<WeaponTaskResult> productTask = new AtomicReference<>();
        AtomicReference<WeaponTaskResult> inventoryTask = new AtomicReference<>();

        Thread firstThread = new Thread(() -> {
            productTask.set(runBlockingTask("product-info", TASK_DELAY_MS));
            latch.countDown();
        }, "plain-thread-product");
        Thread secondThread = new Thread(() -> {
            inventoryTask.set(runBlockingTask("inventory", TASK_DELAY_MS));
            latch.countDown();
        }, "plain-thread-inventory");

        firstThread.start();
        secondThread.start();
        await(latch);

        tasks.add(productTask.get());
        tasks.add(inventoryTask.get());

        return new WeaponDemoResponse(
            "Thread",
            System.currentTimeMillis() - startedAt,
            tasks,
            "直接创建线程适合理解线程最基础的行为，但不适合作为高并发 Web 服务的长期方案。"
        );
    }

    /**
     * 演示 `FutureTask` 的方式。
     *
     * @return FutureTask 方式的实验结果
     */
    public WeaponDemoResponse runFutureTaskDemo() {
        long startedAt = System.currentTimeMillis();
        FutureTask<WeaponTaskResult> firstTask = new FutureTask<>(callableTask("product-info", TASK_DELAY_MS));
        FutureTask<WeaponTaskResult> secondTask = new FutureTask<>(callableTask("inventory", TASK_DELAY_MS));

        // 这里仍然需要手动创建线程来承载 FutureTask，
        // 这正好说明 FutureTask 更偏“任务 + 结果”模型，而不是线程管理方案。
        new Thread(firstTask, "future-task-product").start();
        new Thread(secondTask, "future-task-inventory").start();

        return new WeaponDemoResponse(
            "FutureTask",
            System.currentTimeMillis() - startedAt,
            List.of(getFutureResult(firstTask), getFutureResult(secondTask)),
            "FutureTask 适合理解任务和结果的绑定方式，但工程上通常会进一步进入线程池。"
        );
    }

    /**
     * 演示 `ThreadPoolExecutor` 的方式。
     *
     * @return 线程池方式的实验结果
     */
    public WeaponDemoResponse runThreadPoolDemo() {
        long startedAt = System.currentTimeMillis();

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            2,
            2,
            30,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            runnable -> {
                // 手动设置线程名前缀，
                // 是为了让返回结果里能明显看到任务确实由线程池中的线程执行。
                Thread thread = new Thread(runnable);
                thread.setName("pool-demo-" + thread.threadId());
                return thread;
            }
        );

        try {
            Future<WeaponTaskResult> firstTask = executor.submit(callableTask("product-info", TASK_DELAY_MS));
            Future<WeaponTaskResult> secondTask = executor.submit(callableTask("inventory", TASK_DELAY_MS));
            Future<WeaponTaskResult> thirdTask = executor.submit(callableTask("recommendations", TASK_DELAY_MS));

            return new WeaponDemoResponse(
                "ThreadPoolExecutor",
                System.currentTimeMillis() - startedAt,
                List.of(getFutureResult(firstTask), getFutureResult(secondTask), getFutureResult(thirdTask)),
                "线程池适合工程化控制线程复用、并发规模和任务排队，是业务系统最重要的并发基础设施之一。"
            );
        } finally {
            // 实验中每次临时创建的线程池都应在结束后关闭，
            // 否则会导致线程资源泄漏，影响后续实验观察。
            executor.shutdown();
        }
    }

    /**
     * 演示 `CompletableFuture` 的方式。
     *
     * @return CompletableFuture 方式的实验结果
     */
    public WeaponDemoResponse runCompletableFutureDemo() {
        long startedAt = System.currentTimeMillis();

        // 这里显式提供线程池，是为了强调：
        // CompletableFuture 擅长任务编排，但资源控制仍然依赖底层执行器。
        try (ExecutorService executor = Executors.newFixedThreadPool(3, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("cf-demo-" + thread.threadId());
            return thread;
        })) {
          // supplyAsync：把任务交给 CompletableFuture 的线程池去执行，然后返回一个结果凭证，以后可以拿它取结果
          // 任务什么时候开始执行？当任务被提交给线程池之后，有空余线程就会执行，没有就会在队列等待
            CompletableFuture<WeaponTaskResult> firstTask =
                CompletableFuture.supplyAsync(() -> runBlockingTask("product-info", TASK_DELAY_MS), executor);
            CompletableFuture<WeaponTaskResult> secondTask =
                CompletableFuture.supplyAsync(() -> runBlockingTask("inventory", TASK_DELAY_MS), executor);
            CompletableFuture<WeaponTaskResult> thirdTask =
                CompletableFuture.supplyAsync(() -> runBlockingTask("recommendations", TASK_DELAY_MS), executor);

            // allOf方法解释：把多个 CompletableFuture 合成一个“总 Future”，这个“总 Future”只有在 所有子任务都完成 时，才算完成
            // join方法解释：当前线程在这里等一下 - 等到这三个异步任务全部完成为止
            CompletableFuture.allOf(firstTask, secondTask, thirdTask).join();

            return new WeaponDemoResponse(
                "CompletableFuture",
                System.currentTimeMillis() - startedAt,
                List.of(firstTask.join(), secondTask.join(), thirdTask.join()),
                "CompletableFuture 更适合做异步任务编排和结果汇总，但不应忽略底层执行器的资源控制。"
            );
        }
    }

    /**
     * 演示 Spring `@Async` 的方式。
     *
     * @return Spring 异步方式的实验结果
     */
    public WeaponDemoResponse runSpringAsyncDemo() {
        long startedAt = System.currentTimeMillis();

        CompletableFuture<WeaponTaskResult> firstTask = springAsyncDemoService.runTask("send-mail", TASK_DELAY_MS);
        CompletableFuture<WeaponTaskResult> secondTask = springAsyncDemoService.runTask("write-audit-log", TASK_DELAY_MS);
        CompletableFuture<WeaponTaskResult> thirdTask = springAsyncDemoService.runTask("notify-operator", TASK_DELAY_MS);

        CompletableFuture.allOf(firstTask, secondTask, thirdTask).join();

        return new WeaponDemoResponse(
            "Spring @Async",
            System.currentTimeMillis() - startedAt,
            List.of(firstTask.join(), secondTask.join(), thirdTask.join()),
            "Spring 的异步能力更适合在业务层表达异步任务，但仍然需要显式管理执行器，避免默认线程池失控。"
        );
    }

    /**
     * 演示 JDK 21 虚拟线程的方式。
     *
     * @return 虚拟线程方式的实验结果
     */
    public WeaponDemoResponse runVirtualThreadDemo() {
        long startedAt = System.currentTimeMillis();

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<WeaponTaskResult> firstTask = executor.submit(callableTask("product-info", TASK_DELAY_MS));
            Future<WeaponTaskResult> secondTask = executor.submit(callableTask("inventory", TASK_DELAY_MS));
            Future<WeaponTaskResult> thirdTask = executor.submit(callableTask("recommendations", TASK_DELAY_MS));

            return new WeaponDemoResponse(
                "VirtualThread",
                System.currentTimeMillis() - startedAt,
                List.of(getFutureResult(firstTask), getFutureResult(secondTask), getFutureResult(thirdTask)),
                "虚拟线程降低的是线程使用成本，更适合大量阻塞型任务，但不等于下游资源没有上限。"
            );
        }
    }

    /**
     * 把阻塞任务包装成 `Callable`。
     *
     * @param taskName 任务名称
     * @param delayMs 模拟耗时
     * @return 可提交执行的任务
     */
    private Callable<WeaponTaskResult> callableTask(String taskName, long delayMs) {
        return () -> runBlockingTask(taskName, delayMs);
    }

    /**
     * 统一执行一个模拟阻塞任务。
     *
     * @param taskName 任务名称
     * @param delayMs 模拟耗时
     * @return 单个任务结果
     */
    private WeaponTaskResult runBlockingTask(String taskName, long delayMs) {
        long startedAt = System.currentTimeMillis();
        sleep(delayMs);
        return new WeaponTaskResult(
            taskName,
            Thread.currentThread().getName(),
            System.currentTimeMillis() - startedAt,
            taskName + " 完成"
        );
    }

    /**
     * 统一读取 `Future` 结果。
     *
     * @param future 未来结果
     * @return 任务执行结果
     */
    private WeaponTaskResult getFutureResult(Future<WeaponTaskResult> future) {
        try {
            return future.get();
        } catch (InterruptedException exception) {
            // 等待 Future 时被中断，说明当前线程状态已被外部打断，
            // 必须恢复中断标记，避免中断信号在上层丢失。
            Thread.currentThread().interrupt();
            throw new IllegalStateException("等待任务结果时线程被中断", exception);
        } catch (ExecutionException exception) {
            throw new IllegalStateException("任务执行失败", exception);
        }
    }

    /**
     * 统一等待多个线程完成。
     *
     * @param latch 倒计时门闩
     */
    private void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("等待线程完成时被中断", exception);
        }
    }

    /**
     * 统一模拟阻塞耗时。
     *
     * @param millis 休眠毫秒数
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("模拟任务被中断", exception);
        }
    }
}
