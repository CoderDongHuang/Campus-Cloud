# 校享云 (ShareCampus Cloud)

> 高校智慧后勤与生活服务平台 — 多租户 SaaS · 微服务 · 三端协同

[![Java](https://img.shields.io/badge/Java-21-orange)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-green)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.1-blue)](https://spring.io/projects/spring-cloud)
[![Vue](https://img.shields.io/badge/Vue-3.5-4FC08D?logo=vue.js)](https://vuejs.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow)](./LICENSE)

---

## 📖 项目简介

针对高校后勤服务（报修、保洁、洗衣、配送等）长期依赖微信群预约、纸质工单派发、流程不透明的问题，从 0 到 1 构建了一套 **多租户 SaaS 平台**，实现 **服务浏览 → 下单 → 支付 → 派单 → 服务 → 评价 → 结算** 全链路数字化。

```
 ┌──────────────────────────────────────────────────────────┐
 │                    校享云 · 三端协同                       │
 ├──────────────┬──────────────────┬─────────────────────────┤
 │  � 学生端    │   � 师傅端       │   � 校方管理后台       │
 │  H5 / 小程序  │   H5 / App        │   PC Web (Admin)       │
 ├──────────────┼──────────────────┼─────────────────────────┤
 │ 浏览服务·下单 │  接单·拒单·完工   │  数据大盘·商品管理       │
 │ 支付·评价·IM  │  收益·提现·认证   │  订单审核·优惠券·结算    │
 └──────────────┴──────────────────┴─────────────────────────┘
```

**架构亮点**：Spring Cloud Alibaba 微服务全家桶、三层安全防御（Gateway → 下游 Filter → 租户拦截）、订单状态机模式、ShardingSphere 分库分表、Redis + Lua 原子抢券、Canal + MQ → ES 搜索同步、Netty WebSocket 即时通讯。

---

## 🛠️ 技术栈

### 后端

| 层级 | 技术 |
|------|------|
| 运行环境 | Java 21 LTS |
| 核心框架 | Spring Boot 3.2.5 · Spring Cloud 2023.0.1 · Spring Cloud Alibaba 2023.0.1.0 |
| 服务治理 | Nacos 2.3（注册/配置）· Spring Cloud Gateway · OpenFeign · Sentinel |
| 数据存储 | MySQL 8.0 · ShardingSphere-JDBC 5.4 · Redis 7.2 · Elasticsearch 8.11 · MongoDB 6.0 |
| 消息队列 | RabbitMQ 3.12（延迟队列 · 死信队列 · 事件驱动） |
| 数据同步 | Canal 1.1.7（binlog → MQ → ES） |
| 定时任务 | XXL-JOB 2.4 |
| 对象存储 | MinIO |
| 安全 | Spring Security · JWT (jjwt 0.12) · BCrypt · Jasypt 配置加密 |
| ORM | MyBatis-Plus 3.5.5 · Druid 连接池 |
| 工具库 | Hutool 5.8 · MapStruct 1.5 · Redisson 3.30 · Lombok |
| API 文档 | Knife4j 4.5（Gateway 聚合模式） |

### 前端

| 层级 | 技术 |
|------|------|
| 框架 | Vue 3.5 + Composition API + TypeScript |
| 构建 | Vite 6 · pnpm monorepo · Turborepo |
| Admin | Element Plus 2.9 · ECharts 5 · UnoCSS |
| H5 | Vant 4 · WebSocket 心跳 |
| 状态管理 | Pinia 2 |
| HTTP | Axios（拦截器：自动 Token + 401 无感刷新队列） |

### 基础设施

MySQL · Redis · RabbitMQ · Nacos · Elasticsearch · MongoDB · MinIO · XXL-JOB · Canal · Kibana · Logstash — **全部 Docker Compose 一键启动**。

---

## 🏗️ 微服务模块

```
campus-cloud/
├── campus-gateway      # ⚡ API 网关（全局鉴权 · 白名单 · JWT 校验 · 限流 · CORS）
├── campus-auth         # 🔐 认证服务（密码/验证码登录 · 注册 · 双Token · 黑名单 · 限流）
├── campus-user         # 👤 用户中心（地址管理 · 师傅实名认证审核）
├── campus-product      # 📦 商品服务（类目树 · SPU/SKU · 上下架）
├── campus-order        # 📋 订单中心 ★（状态机 7 状态 · 分库分表 64 片 · 订单快照 · 延迟取消）
├── campus-payment      # 💰 支付中心（创建支付单 · 回调幂等 · 退款 · 微信 V3 对接）
├── campus-coupon       # 🎫 优惠券 ★（Redis+Lua 原子抢券 · 模板管理 · 批量发放）
├── campus-settlement   # 💳 结算分账（师傅钱包 · 提现申请审核 · T+1 对账）
├── campus-tenant       # 🏫 租户管理（多校区 SaaS · 套餐 · 自助入驻 · 用量统计）
├── campus-search       # 🔍 搜索中心（ES 全文检索 + 拼音 + 高亮 · Canal 同步）
├── campus-im           # 💬 即时通讯（Netty WebSocket · MongoDB 存储 · 离线消息）
├── campus-notify       # 🔔 通知中心（短信/App推送/站内信 · 多通道 · MQ 异步）
├── campus-review       # ⭐ 评价反馈（评分 · 图片上传 · 内容审核）
├── campus-file         # 📁 文件中心（MinIO 上传/下载 · 前端直传预签名 URL）
├── campus-data         # 📊 数据分析（Dashboard 运营看板 · Feign 聚合多服务数据）
└── campus-common       # 🧩 公共模块（统一返回体 · 异常处理 · JWT · MyBatis 插件 · MQ 声明 · ES 配置）
```

---

## 🚀 快速开始

### 前置要求

- JDK 21+ · Maven 3.9+ · Docker Desktop · pnpm 8+

### 1. 配置环境变量

```bash
cp .env.example .env
# 编辑 .env 填入真实密码（.env 已在 .gitignore 中排除，不会提交到 Git）
```

### 2. 启动中间件

```bash
docker-compose up -d
# 等待所有容器 healthy: docker-compose ps
```

### 3. 启动后端

```bash
# 编译公共模块
mvn install -pl campus-common -DskipTests

# 按顺序启动（每个在新终端窗口）
mvn spring-boot:run -pl campus-auth -DskipTests      # :8081
mvn spring-boot:run -pl campus-gateway -DskipTests   # :8080
# 其他服务按需启动
```

### 4. 启动前端

```bash
cd campus-frontend && pnpm install
pnpm dev:admin    # 管理后台 → http://localhost:3000
pnpm dev:h5       # 移动端   → http://localhost:3001
```

### 5. 访问

| 端口 | 服务 | 地址 |
|------|------|------|
| 3000 | Admin 管理后台 | http://localhost:3000/login |
| 3001 | H5 移动端 | http://localhost:3001 |
| 8080 | API 网关 | — |
| 8848 | Nacos 控制台 | http://localhost:8848/nacos |

---

## 🔒 安全设计

```
第1层 Gateway AuthGlobalFilter     <- 白名单放行 + JWT 校验 + 注入 X-Gateway-Key
        |
        v
第2层 下游服务 GatewayAuthFilter    <- 校验 X-Gateway-Key 秘钥，拒绝非 Gateway 直连
        |
        v
第3层 UserContext ThreadLocal       <- 解析 Header -> 当前用户上下文
     + MyBatis-Plus TenantInterceptor <- 自动追加 WHERE tenant_id = ?
```

- **JWT 双 Token**：AccessToken 2h + RefreshToken 7d，前端 Axios 拦截器实现 401 排队无感刷新
- **BCrypt 密码加密**：不可逆，盐值内嵌
- **Token 黑名单**：登出时 jti 入 Redis，TTL = 剩余有效期
- **登录限流**：同一手机号 5 分钟内 5 次失败 -> 锁定 30 分钟
- **配置加密**：Jasypt 加密敏感配置，密钥通过环境变量注入

---

## 📚 项目文档

| 文档 | 说明 |
|------|------|
| [全功能代码流程测试文档](docs/全功能代码流程测试文档.md) | 76 个 API 逐功能追踪（输入->调用链->输出） |
| [企业级上线差距分析](docs/企业级上线差距分析.md) | 6 维度 20 项差距，上线 Checklist |
| [架构设计说明书](docs/架构设计说明书.md) | 系统架构全景 |
| [前端架构设计说明书](docs/前端架构设计说明书.md) | 前端 Monorepo · 路由 · 状态管理 |
| [迭代优化路线](docs/迭代优化路线.md) | 26 项代码优化计划 |
| [调试问题记录](docs/调试问题记录.md) | 历史踩坑记录 |
| [架构差异对照表](docs/架构差异对照表.md) | 设计 vs 实现对照 |
| [开发实施计划](docs/开发实施计划.md) | 开发排期与任务分解 |

---

## 📄 许可证

MIT License © 2026 CoderDongHuang
