package lab.backend.independentdemos.javaconcurrencylab.model;

import java.util.List;

/**
 * 表示某一种并发武器的实验响应。
 *
 * @param weapon 当前实验的武器名称
 * @param totalDurationMs 整体实验耗时
 * @param tasks 各个子任务的结果明细
 * @param summary 对该武器的一句总结
 */
public record WeaponDemoResponse(
    String weapon,
    long totalDurationMs,
    List<WeaponTaskResult> tasks,
    String summary
) {
}
