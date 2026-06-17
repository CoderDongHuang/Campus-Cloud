# 校享云 (ShareCampus Cloud)

> 高校智慧后勤与生活服务平台 — 多租户 SaaS 微服务系统

[![Java](https://img.shields.io/badge/Java-17-orange)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green)](https://spring.io/projects/spring-boot)
[![Spring Cloud Alibaba](https://img.shields.io/badge/SCA-2023.x-blue)](https://sca.aliyun.com)

## 项目简介

针对高校后勤服务（维修、保洁、搬家等）长期依赖微信群预约、人工派单、流程不透明的问题，从 0 到 1 开发了一套 **多租户 SaaS 平台**，将服务浏览 → 下单 → 支付 → 派单 → 服务 → 评价全流程线上化。

**架构亮点**：Spring Cloud Alibaba 微服务全家桶、订单状态机模式、ShardingSphere 分库分表、Redis + Lua 抢券、Elasticsearch + Canal 数据同步、Netty WebSocket 即时通讯。

## 技术栈

| 层级 | 技术 |
|------|------|
| 基础框架 | Java 17、Spring Boot 3.2、Spring Cloud Alibaba 2023.x |
| 服务治理 | Nacos（注册/配置）、Spring Cloud Gateway、OpenFeign、Sentinel（限流熔断） |
| 数据存储 | MySQL 8.0、ShardingSphere-JDBC（分库分表）、Redis 7.2、Elasticsearch 8.x、MongoDB 6.0 |
| 中间件 | RabbitMQ、XXL-JOB、Canal、Redisson（分布式锁） |
| 对象存储 | MinIO |
| ORM | MyBatis-Plus |
| 文档 | Knife4j（Swagger 增强） |
| 工具 | Hutool、MapStruct、Lombok |

## 微服务模块

```
campus-cloud/
├── campus-gateway      # API 网关（鉴权、限流、路由转发）
├── campus-auth         # 认证服务（登录/注册/Token/黑名单）
├── campus-user         # 用户中心（地址管理、师傅认证）
├── campus-product      # 商品服务（类目树、SPU/SKU）
├── campus-order        # 订单中心 ★（状态机 + 分库分表 + 快照缓存）
├── campus-payment      # 支付中心（支付/退款/回调幂等）
├── campus-coupon       # 营销中心 ★（Redis+Lua 抢券）
├── campus-settlement   # 结算分账（师傅钱包、T+1 结算）
├── campus-tenant       # 租户管理（SaaS 多租户入口）
├── campus-search       # 搜索中心（ES + Canal 数据同步）
├── campus-im           # 即时通讯（Netty WebSocket + MongoDB）
├── campus-notify       # 通知中心（短信/推送/站内信）
├── campus-review       # 评价反馈
├── campus-file         # 文件中心（MinIO）
├── campus-data         # 数据分析（运营看板）
└── campus-common       # 公共模块（异常/JWT/MyBatis/MQ/ES）
```

## 快速开始

```bash
# 1. 启动中间件
docker-compose up -d

# 2. 编译项目
mvn clean install -DskipTests

# 3. 按顺序启动服务
# Gateway(8080) → Auth(8081) → User(8082) → Product(8083) → Order(8084) → ...
```

## 核心设计

- **订单状态机** — `OrderState` 接口 + 7 个状态实现类，状态流转集中管控，新增状态不改旧代码
- **ShardingSphere 分库分表** — 按租户分 4 库、按订单 ID 分 16 表，共 64 张物理表
- **Redis + Lua 抢券** — Lua 脚本原子执行去重→验库存→扣减→标记，零超卖
- **ES + Canal 搜索** — Canal 监听 MySQL binlog → MQ → Elasticsearch，IK 中文分词 + 拼音搜索
- **订单快照 + 缓存** — 下单生成快照 JSON，读优先 Redis，Cache Aside 策略

## 许可证

MIT License
