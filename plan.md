# PRD-1：事件库 + 服务库 + 自动化建模

## 问题陈述

当前 Mortise AIoT 平台存在两个核心缺口：

1. **事件/服务定义缺乏复用机制**：属性库（PropertyTemplate）已实现独立可复用的属性模板，但事件和服务定义只能在物模型或物模型模板中逐个手工创建。当多个物模型需要相同的事件（如"温度告警"）或服务（如"设备重启"）时，管理员必须重复填写，效率低且容易不一致。

2. **缺少自动化/场景联动能力**：平台仅有基于阈值的告警规则（AlertRule），无法实现"当温度超过 40℃ 时自动关闭加热器"这类跨设备联动场景。用户需要自行编写外部脚本或人工干预，无法在平台内完成设备间的自动化编排。

## 解决方案

### 事件库 + 服务库

参照属性库（PropertyTemplate）模式，新增独立的事件模板库和服务模板库：

- 管理员在事件库/服务库中定义可复用的事件和服务模板，按设备类别组织
- 创建物模型或物模型模板的事件/服务时，可从库中选取模板快速填充（复制模式，填充后独立演进）
- 三个库（属性库、事件库、服务库）体验一致，统一管理入口

### 自动化模块（mortise-automation）

新建独立商业子模块 `mortise-automation`，提供场景自动化的数据建模与管理能力：

- 定义场景（Scene）、触发器（Trigger）、条件（Condition）、动作（Action）核心实体
- 提供 SPI 扩展契约（TriggerSourceProvider、ActionExecutor），支持不同业务模块接入
- 场景 CRUD 与管理后台 UI
- 本 PRD 聚焦建模与管理，执行引擎在 PRD-2 中实现

## 用户故事

### 事件库

1. 作为后台管理员，我希望在事件库中创建可复用的事件模板（如"温度告警事件"），以便在多个物模型中快速引用
2. 作为后台管理员，我希望按设备类别筛选事件模板，以便快速找到特定类别的常用事件
3. 作为后台管理员，我希望在创建物模型事件时从事件库选取模板自动填充表单，以便减少重复输入
4. 作为后台管理员，我希望在创建物模型模板事件时同样能从事件库选取，以便模板级别也能复用
5. 作为后台管理员，我希望从事件库选取后仍可修改填充的字段值，以便针对具体物模型做微调

### 服务库

6. 作为后台管理员，我希望在服务库中创建可复用的服务模板（如"设备重启"、"固件升级"），以便跨物模型复用
7. 作为后台管理员，我希望按设备类别筛选服务模板
8. 作为后台管理员，我希望在创建物模型服务时从服务库选取模板自动填充
9. 作为后台管理员，我希望在创建物模型模板服务时同样能从服务库选取
10. 作为后台管理员，我希望从服务库选取后仍可修改填充的字段值

### 自动化场景

11. 作为后台管理员，我希望创建自动化场景并配置触发器、条件和动作，以便定义设备联动规则
12. 作为后台管理员，我希望为场景配置多种触发器类型（设备属性阈值、设备事件、定时、设备上下线），以便覆盖常见触发场景
13. 作为后台管理员，我希望为场景配置 AND/OR 组合条件，以便精确控制触发后是否执行动作
14. 作为后台管理员，我希望为场景配置多种动作类型（设置设备属性、调用设备服务、发送通知、触发另一场景、延时），以便实现丰富的联动效果
15. 作为后台管理员，我希望启用/禁用场景，以便灵活控制自动化规则的生效状态
16. 作为后台管理员，我希望查看场景列表并按状态、触发器类型筛选，以便管理大量场景

## 实现决策

### 一、事件库（EventTemplate）

#### 数据模型

`mortise_aiot_event_template` 表，字段对齐 `ThingModelEvent` + `PropertyTemplate` 的归属模式：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | FlexId |
| event_key | VARCHAR(100) | 事件标识，同类别内唯一 |
| event_name | VARCHAR(200) | 事件名称 |
| event_level | VARCHAR(20) | 事件级别：info/warn/error |
| output_params | JSONB | 输出参数定义 |
| description | TEXT | 描述 |
| category_id | BIGINT | 所属设备类别 ID（0=通用） |
| sort_order | INTEGER | 排序 |
| created_time / updated_time | TIMESTAMP | 时间戳 |
| del_flag | INTEGER | 逻辑删除 |

唯一约束：`(category_id, event_key) WHERE del_flag = 0`

#### 代码分层

- **domain**：`EventTemplate` 实体（mortise-aiot-domain）
- **infra**：`EventTemplateMapper`、Flyway 迁移脚本（mortise-aiot-infra）
- **application**：`EventTemplateService`（mortise-aiot-application）
- **admin**：`AdminEventTemplateController`（mortise-aiot-admin）

#### API 契约

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /admin/aiot/event-templates | aiot:event-template:list | 分页查询，支持 categoryId 筛选 |
| GET | /admin/aiot/event-templates/{id} | aiot:event-template:query | 查询详情 |
| POST | /admin/aiot/event-templates | aiot:event-template:add | 新增 |
| PUT | /admin/aiot/event-templates/{id} | aiot:event-template:edit | 编辑 |
| DELETE | /admin/aiot/event-templates/{id} | aiot:event-template:delete | 删除 |

#### 前端

- 新增事件模板库管理页面（列表 + 表单弹窗），放在 admin-aiot layer
- 物模型事件面板（EventFormModal）增加"从事件库导入"按钮
- 物模型模板事件面板同样增加"从事件库导入"按钮

### 二、服务库（ServiceTemplate）

#### 数据模型

`mortise_aiot_service_template` 表：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | FlexId |
| service_key | VARCHAR(100) | 服务标识，同类别内唯一 |
| service_name | VARCHAR(200) | 服务名称 |
| input_params | JSONB | 输入参数定义 |
| output_params | JSONB | 输出参数定义 |
| description | TEXT | 描述 |
| category_id | BIGINT | 所属设备类别 ID（0=通用） |
| sort_order | INTEGER | 排序 |
| created_time / updated_time | TIMESTAMP | 时间戳 |
| del_flag | INTEGER | 逻辑删除 |

唯一约束：`(category_id, service_key) WHERE del_flag = 0`

#### 代码分层、API 契约、前端

与事件库完全对称，权限前缀为 `aiot:service-template`，路径为 `/admin/aiot/service-templates`。

### 三、自动化模块（mortise-automation）

#### 模块结构

作为独立商业子模块（git submodule），遵循标准分层：

```
mortise-automation/
├── mortise-automation-domain/      # 实体、枚举、SPI 契约
├── mortise-automation-application/ # 场景编排服务
├── mortise-automation-infra/       # MyBatis 实现、Flyway、AIoT 适配器
├── mortise-automation-admin/       # 后台管理 API
└── mortise-automation-api/         # 开放 API（本期可为空壳）
```

Maven 装配：在 `mortise-app/pom.xml` 的 `pro` profile 中添加 `mortise-automation-admin` 和 `mortise-automation-api` 依赖，通过 `<exists>${basedir}/../mortise-automation/pom.xml</exists>` 激活。

#### 核心实体模型

**Scene（场景）**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | FlexId |
| name | VARCHAR(200) | 场景名称 |
| description | TEXT | 场景描述 |
| status | SMALLINT | 状态：0=禁用，1=启用 |
| owner_type | VARCHAR(20) | 所有权类型：PLATFORM / PERSONAL / SHARED |
| owner_id | BIGINT | 所有者 ID（PLATFORM 时为 0） |
| trigger_logic | VARCHAR(8) | 多触发器间逻辑：ANY（任一触发）/ ALL（全部满足） |
| condition_logic | VARCHAR(8) | 多条件间逻辑：AND / OR |
| created_time / updated_time | TIMESTAMP | 时间戳 |
| del_flag | INTEGER | 逻辑删除 |

**SceneTrigger（场景触发器）**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | FlexId |
| scene_id | BIGINT | 所属场景 |
| trigger_type | VARCHAR(32) | 类型：DEVICE_PROPERTY / DEVICE_EVENT / TIMER / DEVICE_STATUS |
| config | JSONB | 触发器配置 |
| sort_order | INTEGER | 排序 |

**SceneCondition（场景条件）**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | FlexId |
| scene_id | BIGINT | 所属场景 |
| condition_type | VARCHAR(32) | 类型：DEVICE_PROPERTY / TIME_RANGE |
| config | JSONB | 条件配置 |
| sort_order | INTEGER | 排序 |

**SceneAction（场景动作）**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | FlexId |
| scene_id | BIGINT | 所属场景 |
| action_type | VARCHAR(32) | 类型：SET_PROPERTY / INVOKE_SERVICE / SEND_NOTIFICATION / TRIGGER_SCENE / DELAY |
| config | JSONB | 动作配置 |
| sort_order | INTEGER | 执行顺序 |

#### JSONB config 示例

- DEVICE_PROPERTY 触发器：`{ "deviceId": 123, "propertyKey": "temperature", "operator": "GT", "threshold": 40.0, "duration": 60 }`
- TIMER 触发器：`{ "cron": "0 0 8 * * ?", "timezone": "Asia/Shanghai" }`
- DEVICE_STATUS 触发器：`{ "deviceId": 123, "statusChange": "ONLINE" }`
- SET_PROPERTY 动作：`{ "deviceId": 456, "propertyKey": "power_switch", "value": false }`
- INVOKE_SERVICE 动作：`{ "deviceId": 456, "serviceKey": "reboot", "inputParams": {} }`
- DELAY 动作：`{ "delaySeconds": 30 }`

#### SPI 契约（定义在 mortise-automation-domain）

TriggerSourceProvider：`triggerType()` + `validateConfig(Map)`
ActionExecutor：`actionType()` + `validateConfig(Map)`

本 PRD 仅定义 SPI 接口和校验能力。实际触发监听与动作执行在 PRD-2 中实现。

#### 跨模块集成

- 依赖方向：automation-infra → aiot-domain（optional）
- 触发器：`@EventListener` 监听 AIoT Spring Event + `@ConditionalOnClass` 按需激活
- 动作：适配器调用 AIoT application service
- AIoT 零改动

#### 场景 API

| 方法 | 路径 | 权限 |
|------|------|------|
| GET | /admin/automation/scenes | automation:scene:list |
| GET | /admin/automation/scenes/{id} | automation:scene:query |
| POST | /admin/automation/scenes | automation:scene:add |
| PUT | /admin/automation/scenes/{id} | automation:scene:edit |
| DELETE | /admin/automation/scenes/{id} | automation:scene:delete |
| PATCH | /admin/automation/scenes/{id}/status | automation:scene:edit |

#### 前端

- 新增 `frontend/layers/admin/automation/` 独立 admin layer（git submodule）
- 场景列表页 + 场景编辑页（分步配置触发器→条件→动作）

### 四、与 AlertRule 的关系

AlertRule 负责告警（检测异常→产生 AlertEvent），自动化场景负责联动（检测条件→执行动作）。两者互补共存。

## 测试决策

- 好测试的标准：只测外部行为，不测实现细节
- EventTemplateService / ServiceTemplateService：CRUD、categoryId 筛选、唯一约束校验
- SceneService：场景创建（含级联保存）、更新、删除、状态切换
- SPI 校验：TriggerSourceProvider / ActionExecutor 的 config 校验
- 测试参考：代码库中 PropertyTemplate 相关测试模式

## 垂直切片拆分

### 切片 1：事件库后端 + 前端管理页

**覆盖用户故事**：1、2
**验收标准**：
- [ ] EventTemplate 实体、Mapper、Service、AdminController 完整实现
- [ ] Flyway 迁移脚本创建表，含种子数据和菜单权限
- [ ] CRUD API 可用，支持 categoryId 筛选
- [ ] 前端事件模板库管理页面可正常 CRUD
**依赖关系**：无

### 切片 2：事件库 — 物模型/模板事件表单集成

**覆盖用户故事**：3、4、5
**验收标准**：
- [ ] 物模型事件表单增加"从事件库导入"按钮
- [ ] 物模型模板事件表单增加"从事件库导入"按钮
- [ ] 选取后自动填充所有字段，填充后可修改
**依赖关系**：依赖切片 1

### 切片 3：服务库后端 + 前端管理页

**覆盖用户故事**：6、7
**验收标准**：
- [ ] ServiceTemplate 实体、Mapper、Service、AdminController 完整实现
- [ ] Flyway 迁移脚本创建表，含种子数据和菜单权限
- [ ] CRUD API 可用，支持 categoryId 筛选
- [ ] 前端服务模板库管理页面可正常 CRUD
**依赖关系**：无（可与切片 1 并行）

### 切片 4：服务库 — 物模型/模板服务表单集成

**覆盖用户故事**：8、9、10
**验收标准**：
- [ ] 物模型服务表单增加"从服务库导入"按钮
- [ ] 物模型模板服务表单增加"从服务库导入"按钮
- [ ] 选取后自动填充所有字段，填充后可修改
**依赖关系**：依赖切片 3

### 切片 5：mortise-automation 脚手架 + 核心实体建模

**覆盖用户故事**：11
**验收标准**：
- [ ] 模块目录结构创建完成（domain/application/infra/admin/api）
- [ ] Maven pom.xml 配置完成，pro profile 添加依赖
- [ ] .gitmodules 注册子模块
- [ ] Scene/SceneTrigger/SceneCondition/SceneAction 实体定义完成
- [ ] Flyway 迁移脚本（V300 起）创建所有表
- [ ] SPI 接口定义完成
**依赖关系**：无（可与切片 1-4 并行）

### 切片 6：场景 CRUD API + 管理后台

**覆盖用户故事**：11-16
**验收标准**：
- [ ] SceneService 实现场景 CRUD（含级联触发器/条件/动作）
- [ ] AdminSceneController 实现所有 API
- [ ] 启用/禁用 API 可用
- [ ] Flyway 含菜单和权限数据
- [ ] 前端 admin-automation layer 创建完成
- [ ] 场景列表页和编辑页可正常使用
**依赖关系**：依赖切片 5

## 范围外

- **执行引擎**：触发器实时监听、条件求值、动作执行链 → PRD-2
- **AIoT 适配器实现**：设备属性触发源、设备服务动作执行器 → PRD-2
- **定时触发 Cron 调度**：与 Spring Scheduler 集成 → PRD-2
- **场景执行日志与审计** → PRD-2
- **Webhook 触发器/动作** → PRD-2 或后续
- **地理围栏触发器** → 后续
- **自定义脚本动作** → 后续
- **用户端场景管理 API**（mortise-automation-api）→ 后续
- **AlertRule 迁移到自动化模块** → 不迁移，两者互补共存

## 补充说明

- 种子数据应包含常见通用事件（故障告警、状态变更）和通用服务（设备重启、参数配置）
- 自动化模块 Flyway 版本号从 V300 开始，与 AIoT（V200 系列）区分
- 前端 admin-automation layer 需在 `.gitmodules` 中注册为独立子模块
- 场景编辑页的 config 字段使用动态表单，根据 type 切换不同表单 schema