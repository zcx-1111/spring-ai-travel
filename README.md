# AI 智能旅游规划助手

一个基于若依的旅游规划系统，包含 Spring Boot 后端、管理端和游客端。游客可以注册登录、生成并调整 AI 行程、查询历史方案；管理端维护景点等业务数据。

## 项目结构

- `RuoYi-Vue-master/`：Spring Boot 后端及管理接口
- `RuoYi-Vue3-master/`：若依 Vue 3 管理端
- `travel-frontend/`：面向游客的 Vue 3 前端
- `RuoYi-Vue-master/sql/`：若依系统表和 Quartz 表脚本

## 主要依赖

- Java 17、Maven
- Node.js 与 npm
- MySQL、Redis
- Elasticsearch、Qdrant（景点检索和向量知识库）
- 阿里云百炼兼容 OpenAI 的模型服务

后端的本地服务地址在 `RuoYi-Vue-master/ruoyi-admin/src/main/resources/application.yml` 中配置。部署时请使用环境变量提供凭据，不要将密钥或真实密码写入配置文件：

| 环境变量 | 用途 |
| --- | --- |
| `DB_USERNAME`、`DB_PASSWORD` | MySQL 账号 |
| `DASHSCOPE_API_KEY` | 百炼 API Key |
| `TOKEN_SECRET` | Token 签名密钥 |
| `DRUID_LOGIN_USERNAME`、`DRUID_LOGIN_PASSWORD` | Druid 监控登录账号 |

`TOKEN_SECRET` 应使用足够长的随机值。数据库、Redis、Elasticsearch 和 Qdrant 的连接地址也应按部署环境调整。

## 启动

先配置并启动 MySQL、Redis，以及需要使用的检索服务；业务表结构和初始景点数据需按实际数据库准备。然后分别启动：

```powershell
# 后端
cd RuoYi-Vue-master
mvn spring-boot:run -pl ruoyi-admin -am

# 游客端
cd travel-frontend
npm install
npm run dev

# 管理端
cd RuoYi-Vue3-master
npm install
npm run dev
```

游客端开发服务器默认使用 `http://localhost:8080` 作为后端代理目标，配置见 `travel-frontend/vite.config.js`。
