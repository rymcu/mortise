## 项目架构优化

本项目采用“多模块单体 + 业务域分层”架构，保证模块边界清晰、可扩展。

### 分层说明

- L6 应用层：`mortise-app` 聚合启动与装配。
- L5 业务域 API：`*-admin` / `*-api` 对外提供接口。
- L4 业务域应用与基础设施：`*-application` / `*-infra` / `*-domain`。
- L3 应用基础层：`mortise-auth` / `mortise-web-support` / `mortise-monitor`。
- L2 基础设施层：`mortise-log` / `mortise-cache` / `mortise-notification` / `mortise-persistence`。
- L1 核心层：`mortise-common` / `mortise-core`。

### 依赖原则

- 同层不互相依赖。
- 依赖方向由上到下，禁止反向依赖。
- 业务域接口层只依赖本域 application 与基础模块。

### 架构图

```mermaid
flowchart TB
	subgraph L6[应用层]
		app[mortise-app]
	end

	subgraph L5[业务域 API]
		sysAdmin[mortise-system-admin]
		sysApi[mortise-system-api]
		memAdmin[mortise-member-admin]
		memApi[mortise-member-api]
	end

	subgraph L4[业务域应用与基础设施]
		sysApp[mortise-system-application]
		sysInfra[mortise-system-infra]
		memApp[mortise-member-application]
		memInfra[mortise-member-infra]
		sysDomain[mortise-system-domain]
		memDomain[mortise-member-domain]
	end

	subgraph L3[应用基础层]
		auth[mortise-auth]
		webSupport[mortise-web-support]
		monitor[mortise-monitor]
	end

	subgraph L2[基础设施层]
		log[mortise-log]
		cache[mortise-cache]
		notify[mortise-notification]
		persistence[mortise-persistence]
	end

	subgraph L1[核心层]
		common[mortise-common]
		core[mortise-core]
	end

	app --> sysAdmin
	app --> sysApi
	app --> memAdmin
	app --> memApi
	app --> auth
	app --> webSupport
	app --> monitor

	sysAdmin --> sysApp
	sysApi --> sysApp
	memAdmin --> memApp
	memApi --> memApp

	sysApp --> sysDomain
	sysApp --> sysInfra
	memApp --> memDomain
	memApp --> memInfra

	sysInfra --> persistence
	memInfra --> persistence
	sysInfra --> log
	memInfra --> log

	auth --> cache
	monitor --> common
	webSupport --> core

	log --> core
	cache --> core
	notify --> core
	persistence --> core

	core --> common
```
