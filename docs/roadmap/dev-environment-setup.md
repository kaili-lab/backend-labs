# 开发环境安装基线

## 目的
这份文档用于记录当前阶段真正需要准备的开发环境，避免一开始安装过多不必要的软件。

## 版本说明
本文中的安装目标版本，基于 `2026-03-07` 当天的官方信息。
如果未来开始真正搭建项目时，已经过了较长时间，应先重新核对官方文档，再决定是否继续沿用本文中的具体版本号。

因此这里的版本策略是：
- 先记录“当前默认基线”
- 真正执行安装前，再快速确认一次官方最新稳定信息
- 如果官方版本已经变化，优先更新文档，再安装

## 当前结论
前期只安装真正会用到的工具，不把所有中间件都提前装到本地。
能不装在 WSL Ubuntu 中的，就尽量不装在 WSL 中。
宿主机和 WSL 的职责要尽量分清，减少环境重复和后续维护成本。

## 环境职责划分

### Windows 10 宿主机负责什么
- 安装 `Docker Desktop`
- 提供 WSL2 运行基础
- 提供图形界面软件和宿主机网络环境
- 提供当前已经在使用的 `v2rayN` 代理能力

### WSL2 Ubuntu 负责什么
- 作为日常开发环境
- 存放代码和执行开发命令
- 运行 `java`、`mvn`、`git` 等开发工具
- 通过 Docker Desktop 提供的集成能力使用容器

### 为什么这样划分
这样做的目标是：
- 让宿主机负责系统级能力
- 让 WSL 负责开发体验和命令执行
- 避免在 Windows 和 WSL 中重复安装同一类基础设施

## 现在需要安装的内容

### 1. Windows 10 上安装 `Docker Desktop`
在 Windows 10 上安装 Docker Desktop，并开启 WSL 2 集成。
后续数据库、Redis、消息队列等依赖统一由 Docker 管理。

### 2. WSL Ubuntu 中安装 `JDK 25`
这是主实验当前确定的 Java 基线。
因为你主要在 WSL 中开发和执行命令，所以 `JDK` 应该安装在 WSL 中，而不是优先装在 Windows 中。

### 3. WSL Ubuntu 中安装 `Maven 3.9.x`
用于构建和管理 Java 项目依赖。
同样因为主要在 WSL 中执行构建命令，所以 Maven 也应安装在 WSL 中。

### 4. 保持 `WSL2 + Ubuntu` 作为主开发环境
你当前已经在 WSL 的 Ubuntu 中开发，这个方向是合适的。
代码开发继续在 WSL 中进行即可。

## 现在不需要安装的内容

### 1. 不需要在 WSL 中手动安装 PostgreSQL
当前阶段不建议直接在 WSL 里手动搭数据库服务。
等主线需要数据库时，优先通过 Docker Compose 启动。

### 2. 不需要在 WSL 中手动安装 Redis
Redis 也建议后续通过容器引入，而不是现在先做本地服务安装。

### 3. 不需要提前安装 Kafka
Kafka 目前还没有进入主线实现阶段。
现在提前安装，只会增加环境复杂度。

### 4. 不需要在 Windows 和 WSL 中重复安装同类基础设施
例如数据库、Redis、消息队列，不应该同时在 Windows 和 WSL 中各装一套。
这会让端口、数据目录、连接目标都变得更混乱。

### 5. 不需要提前安装全部中间件客户端
例如 `psql`、Redis CLI、Kafka CLI 等工具，只有在进入对应阶段且确实需要调试时再补充即可。

## 推荐的开发环境形态
当前更推荐以下组合：
- Windows 10 作为宿主系统
- WSL2 Ubuntu 作为日常开发环境
- Docker Desktop 负责容器运行
- Docker Compose 负责数据库、Redis、队列等依赖的编排
- Java 和 Maven 安装在 WSL Ubuntu 中

## 安装方式选择
当前优先推荐：
- `JDK`：使用 `SDKMAN` 安装
- `Maven`：优先使用 `SDKMAN` 安装
- 如果 `SDKMAN` 中没有目标版本或下载异常，再退回官方压缩包安装方式

这样选择的原因是：
- 对 WSL 中的开发环境更友好
- 更方便切换版本
- 更适合本地开发机，而不是生产机
- 能减少手工管理环境变量的成本

## 当前推荐安装步骤

### 1. 安装基础依赖
```bash
sudo apt update
sudo apt install -y curl zip unzip ca-certificates
```

### 2. 安装 `SDKMAN`
```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk version
```

### 3. 安装 `JDK 25`
```bash
sdk install java 25-tem
sdk default java 25-tem
java -version
```

### 4. 安装 `Maven 3.9.13`
```bash
sdk install maven 3.9.13
sdk default maven 3.9.13
mvn -version
```

### 5. 如果 `SDKMAN` 中安装 Maven 失败，则改用 Apache 官方压缩包

## 宿主机和 WSL 的通信理解
这个项目里最常见的通信其实有 3 类：

### 1. WSL 中的开发命令访问 Docker 提供的容器
这是最主要的一类。
你的代码、`mvn` 命令、应用启动都在 WSL 中执行，但容器由 Docker Desktop 提供。
在正确启用 WSL 集成后，WSL 中直接使用 `docker` 和 `docker compose` 即可。

### 2. WSL 中的应用访问数据库、Redis 等容器
当前项目建议优先使用容器暴露出的本地端口，例如 `localhost:5432`、`localhost:6379`。
只要 Docker Desktop 与 WSL 集成正常，通常不需要额外去处理复杂的容器网络细节。

### 3. Windows 上的工具访问 WSL 或容器中的服务
例如你可能会用 Windows 上的浏览器、Postman、IDE 插件去访问本地服务。
当前阶段建议尽量保持：
- 开发命令在 WSL 中执行
- 服务端口按项目文档统一暴露
- 优先使用 `localhost` 访问，避免过早引入复杂的跨环境地址管理

## 代理与网络注意事项
你当前在 Windows 10 中使用 `v2rayN`，并开启了 `Tun` 模式。
WSL 的网络实际上会受到宿主机网络环境影响，所以这一点需要提前记住。

### 当前建议
- 不要急着在 WSL 中再安装一套代理工具
- 先验证当前宿主机代理能力是否已经足够支撑 WSL 中的开发工作
- 只有当 `mvn`、`docker pull`、`git` 等命令在 WSL 中确实出现网络问题时，再补充 WSL 侧代理配置

### 可能受影响的场景
- `sdk install` 下载失败
- `mvn dependency:resolve` 或构建下载依赖失败
- `docker pull` 拉镜像失败或速度异常
- `git clone` 或访问外部文档资源失败

### 处理原则
- 优先在 Windows / Docker Desktop 层解决代理问题
- 只有宿主机代理无法自然覆盖到 WSL 时，才补充 WSL 内的环境变量配置
- 不要同时叠加太多层代理配置，否则后续排查会很痛苦

### 如果后面真的需要补充 WSL 代理
优先考虑这类最小配置：
- `HTTP_PROXY`
- `HTTPS_PROXY`
- `NO_PROXY`

但这一步应该放到真正遇到问题时再做，而不是现在先做。

## 为什么这样更适合当前阶段
- 安装量更小
- 责任边界更清楚
- 可重建性更强
- 更容易随着主线逐步引入依赖
- 不会过早把精力消耗在环境运维和代理联调上

## 是否需要把安装步骤单独拆成新文档
当前不需要。

更合适的做法是：
- 先把“装什么、为什么这样装、宿主机和 WSL 怎么分工”集中记录在当前这份文档里
- 以后如果安装步骤真的变得很多，再考虑拆出去

## 当前建议的安装顺序
1. 确认 Windows 10 和 WSL2 版本满足 Docker 要求
2. 安装或更新 Docker Desktop，并启用 WSL 2 集成
3. 在 WSL Ubuntu 中安装 `JDK 25`
4. 在 WSL Ubuntu 中安装 `Maven 3.9.13`
5. 验证 `java`、`mvn`、`docker` 是否都可用
6. 只有在真实遇到网络问题时，再处理 WSL 侧代理配置

## 当前官方基线信息
截至 2026-03-07：
- Spring Boot 官方文档显示当前稳定版为 `4.0.3`，并要求至少 `Java 17`，兼容到 `Java 25`
- Oracle 官方路线图显示 `Java 25` 为 LTS
- Apache Maven 官方下载页显示 `3.9.13` 为最新稳定版本
- SDKMAN 官方安装页明确说明其支持 Linux、macOS 和 Windows 下的 WSL

## 官方核对入口
- SDKMAN Installation：`https://sdkman.io/install/`
- Oracle Java SE Support Roadmap：`https://www.oracle.com/java/technologies/java-se-support-roadmap.html`
- Apache Maven Download：`https://maven.apache.org/download.cgi`
- Spring Boot System Requirements：`https://docs.spring.io/spring-boot/system-requirements.html`
