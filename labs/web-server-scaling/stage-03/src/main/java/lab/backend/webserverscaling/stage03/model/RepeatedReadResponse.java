package lab.backend.webserverscaling.stage03.model;

import java.util.List;

/**
 * 数据库重复读取实验响应。
 *
 * @param productId 商品 ID
 * @param times 重复次数
 * @param totalDurationMs 总耗时
 * @param durationsMs 每次读取耗时
 */
public record RepeatedReadResponse(
    long productId,
    int times,
    long totalDurationMs,
    List<Long> durationsMs
) {
}
