# Web Server Scaling 文档分层

这个目录补充 `labs/web-server-scaling/PRD.md`，把“总蓝图”拆成更适合推进实施的几层文档。

## 当前边界

- `PRD.md` 继续负责 `web-server-scaling` 主题级总蓝图
- `docs/slices/` 当前只定义 `mall-monolith/` 的 `V1 ~ V4`
- `docs/designs/` 当前只服务于单体阶段的关键方案设计
- `docs/execution/` 当前只服务于单体阶段的执行清单与批次记录
- `mall-microservices/` 暂不进入这四个切片，等单体阶段稳定后再单独规划

这样设计的原因是：

- 先把单体里的事务、一致性、异步、缓存、稳定性和可观测性练扎实
- 避免把服务边界复杂度提前混入单体阶段
- 保持“总蓝图、推进路线、关键设计、具体执行”四层职责清晰

## 四层文档分别回答什么

### 1. `PRD.md`

回答：

- 这个主题最终要包含什么
- 当前明确不包含什么
- 模块边界、技术边界和主业务链路是什么

### 2. `docs/slices/`

回答：

- 当前先做哪一段
- 这一段要引入什么压力
- 这一段主要训练什么能力
- 完成后怎么判断可以进入下一段

### 3. `docs/designs/`

回答：

- 某个关键问题具体怎么实现
- 有哪些候选方案
- 当前为什么选这个方案
- 代价、边界和后续演进点是什么

### 4. `docs/execution/`

回答：

- 这一次编码具体做哪些任务
- 涉及哪些文件
- 先后顺序是什么
- 这一批完成了什么、还缺什么

## 建议阅读顺序

1. 先读 `labs/web-server-scaling/PRD.md`
2. 再读 `labs/web-server-scaling/docs/slices/README.md`
3. 然后进入当前切片文档，例如 `labs/web-server-scaling/docs/slices/V1-跑通核心交易链路.md`
4. 如果该切片出现关键取舍，再补读 `labs/web-server-scaling/docs/designs/`
5. 真正开始实现前，再读 `labs/web-server-scaling/docs/execution/`

## 当前约定

- `V1 ~ V4` 只覆盖 `mall-monolith/`
- 不把微服务、Kafka、Saga、服务治理提前塞进单体切片
- 不为每个小功能点创建设计文档
- 只为有明显取舍、边界或风险的问题补设计文档
