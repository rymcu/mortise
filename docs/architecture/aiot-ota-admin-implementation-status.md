# AIoT 与 OTA 模块管理端实现计划与完成情况

> 本文档依据 `aiot-ota-module-architecture.md` 的设计规范，结合 `mortise-aiot`、`mortise-ota`、`mortise-aiot-gateway-mqtt` 三个子仓库的实际代码，梳理管理端及整体模块的实现计划与当前完成状态。
>
> 最后更新：2026-03-17
>
> 图例：✅ 已完成 | ⚠️ 部分完成 | ❌ 尚未实现

---

## 一、模块骨架与 Maven 装配

| 项目 | 状态 | 说明 |
|------|------|------|
| 根 pom `pro` Profile 装配 `mortise-aiot`、`mortise-aiot-gateway-mqtt`、`mortise-ota` | ✅ | 三个子模块均已纳入 `pro` Profile |
| `mortise-app` pro Profile 依赖 aiot-admin、aiot-api、ota-admin、ota-api、gateway-mqtt | ✅ | 装配完毕 |
| git submodule 推送与 `.gitmodules` 配置 | ✅ | 三个子仓库均已独立建库并推送 |
| `dependencyManagement` 声明全部子模块版本 | ✅ | 根 pom 已维护所有 10 个子模块的版本声明 |
| `mvn -pl mortise-app -am -Ppro -DskipTests compile` 最小回归 | ✅ | 已验证通过 |

---

## 二、领域层（Domain）

### 2.1 AIoT 领域层（`mortise-aiot-domain`）

#### 实体

| 实体类 | 对应表 | 状态 |
|--------|--------|------|
| `Device` | `mortise_aiot_device` | ✅ |
| `DeviceCredential` | `mortise_aiot_device_credential` | ✅ |
| `DeviceShadow` | `mortise_aiot_device_shadow` | ✅ |
| `DeviceCommand` | `mortise_aiot_device_command` | ✅ |
| `DeviceCommandReply` | `mortise_aiot_device_command_reply` | ✅ |
| `DeviceTelemetrySnapshot` | `mortise_aiot_device_telemetry_snapshot` | ✅ |
| `ThingModel` | `mortise_aiot_thing_model` | ✅ |
| `MqttBrokerConfig` | `mortise_aiot_gateway_mqtt_broker` | ✅ |
| `MqttGatewayRoute` | `mortise_aiot_gateway_route` | ✅ |
| `DeviceGroup` | `mortise_aiot_device_group` | ✅ |
| `DeviceGroupMember` | `mortise_aiot_device_group_member` | ✅ |
| `DeviceBinding` | `mortise_aiot_device_binding` | ✅ |
| `DeviceModelBinding` | `mortise_aiot_device_model_binding` | ✅ |
| `ThingModelProperty` | `mortise_aiot_thing_model_property` | ✅ 独立实体，V209 迁移 |
| `AlertRule` | `mortise_aiot_alert_rule` | ✅ |
| `AlertEvent` | `mortise_aiot_alert_event` | ✅ |

#### 枚举

| 枚举 | 状态 |
|------|------|
| `ActivationStatus` | ✅ |
| `OnlineStatus` | ✅ |
| `CommandStatus` | ✅ |
| `CredentialType` | ✅ |
| `ThingModelSpecType` | ✅ |
| `MqttVendorType` | ✅（架构规划外，已额外实现） |
| `MqttAuthMode` | ✅（架构规划外，已额外实现） |
| `AiotProductType` | ✅（架构规划外，已额外实现） |
| `AlertSeverity` | ✅ INFO/WARNING/CRITICAL |

#### 领域事件

| 事件 | 状态 |
|------|------|
| `DeviceRegisteredEvent` | ✅ |
| `DeviceActivatedEvent` | ✅ |
| `DeviceOnlineEvent` | ✅ |
| `DeviceShadowUpdatedEvent` | ✅ |
| `DeviceOfflineEvent` | ✅ |
| `AlertTriggeredEvent` | ✅ 告警引擎已接入，`AlertRuleServiceImpl.evaluateForDevice()` 中发布 |

#### SPI 接口

| SPI | 状态 |
|-----|------|
| `DeviceProtocolGateway` | ✅ |
| `DeviceSnapshotQueryService` | ✅ |
| `DeviceReachabilityService` | ✅ |
| `ThingModelQueryService` | ✅ |
| `DeviceFirmwareWritebackService` | ✅（内部 AIoT 使用，保留向后兼容） |

---

### 2.2 OTA 领域层（`mortise-ota-domain`）

#### 实体

| 实体类 | 对应表 | 状态 |
|--------|--------|------|
| `FirmwarePackage` | `mortise_ota_firmware_package` | ✅ |
| `FirmwareCompatibility` | `mortise_ota_firmware_compatibility` | ✅ |
| `UpgradeBatch` | `mortise_ota_upgrade_batch` | ✅ |
| `UpgradeTask` | `mortise_ota_upgrade_task` | ✅ |
| `UpgradeProgressLog` | `mortise_ota_upgrade_progress_log` | ✅ |
| `UpgradeRollbackRecord` | `mortise_ota_upgrade_rollback_record` | ✅ |
| `UpgradePolicy` | `mortise_ota_upgrade_policy` | ✅ |

#### 枚举

| 枚举 | 状态 |
|------|------|
| `FirmwareStatus` | ✅ |
| `PackageFormat` | ✅ |
| `BatchStatus` | ✅ |
| `UpgradeTaskStatus` | ✅ |
| `UpgradeRollbackStatus` | ✅（命名与规划略有差异） |
| `OtaProductType` | ✅（架构规划外，已额外实现） |
| `BatchType` | ✅ MANUAL / AUTO_POLICY |
| `RollbackMode` | ⚠️ 无对应实体字段，暂不创建 |
| `UpgradeStepCode` | ✅ 7个步骤码，已替换 UpgradeTaskServiceImpl 中字符串字面量 |

#### 领域事件

| 事件 | 状态 |
|------|------|
| `FirmwareReleasedEvent` | ✅ |
| `UpgradeBatchCreatedEvent` | ✅（规划名为 `UpgradeBatchStartedEvent`） |
| `UpgradeTaskSucceededEvent` | ✅ |
| `UpgradeTaskFailedEvent` | ✅ |
| `UpgradeRollbackEvent` | ✅ 在 rollbackTask() 中发布 |

#### SPI 接口（由 aiot-application 实现）

| SPI | 实现类 | 用途 |
|-----|--------|------|
| `UpgradeTargetNotificationPort` | `DeviceUpgradeNotificationAdapter` | 批次创建后经 MQTT 推送通知 |
| `UpgradeVersionWritebackPort` | `DeviceFirmwareWritebackServiceImpl` | 升级/回滚后写回固件版本 |
| `UpgradeTargetOfflinePort` | `UpgradeTargetOfflineServiceImpl`（ota 自身实现） | AIoT 调用，取消 PENDING 任务 |
| `UpgradeTargetInfoPort` | `DeviceTargetInfoAdapter` | 按 deviceCode 查询目标 ID + 当前版本 |

#### model（OTA 内部数据结构）

| 类 | 状态 |
|----|------|
| `UpgradeTargetInfo` | ✅ 批次创建时传递目标 ID + 当前版本 |

---

## 三、基础设施层（Infra）

### 3.1 AIoT Mapper

| Mapper | 状态 |
|--------|------|
| `DeviceMapper` | ✅ |
| `DeviceCredentialMapper` | ✅ |
| `DeviceShadowMapper` | ✅ |
| `DeviceCommandMapper` | ✅ |
| `DeviceCommandReplyMapper` | ✅ |
| `DeviceTelemetrySnapshotMapper` | ✅ |
| `ThingModelMapper` | ✅ |
| `ThingModelPropertyMapper` | ✅ |
| `DeviceModelBindingMapper` | ✅ |
| `MqttBrokerConfigMapper` | ✅ |
| `MqttGatewayRouteMapper` | ✅ |
| `DeviceGroupMapper` | ✅ |
| `DeviceGroupMemberMapper` | ✅ |
| `DeviceBindingMapper` | ✅ |
| `AlertRuleMapper` | ✅（新增 `findActiveRulesForDevice` 查询） |
| `AlertEventMapper` | ✅（新增 `countOpenEventsByRuleAndDevice` 查询） |

### 3.2 AIoT Flyway 迁移

| 脚本 | 实际文件 | 状态 |
|------|----------|------|
| 设备主表、凭据表 | `V200__Create_AIoT_Device_Tables.sql` | ✅ |
| 影子表、物模型主表 | `V201__Create_AIoT_Shadow_ThingModel_Tables.sql` | ✅ |
| 指令表、遥测快照表 | `V202__Create_AIoT_Command_Telemetry_Tables.sql` | ✅ |
| MQTT broker + 路由配置表 | `V203__Create_AIoT_Gateway_Config_Tables.sql` | ✅（规划外） |
| broker 认证列变更 | `V204__Alter_AIoT_Broker_Add_Auth_Columns.sql` | ✅（规划外） |
| 菜单初始化 | `V205__Init_AIoT_Menus.sql` | ✅（规划外） |
| 设备绑定表 | `V206__Create_AIoT_Device_Binding_Table.sql` | ✅ |
| 设备分组表、分组成员表 | `V207__Create_AIoT_Device_Group_Tables.sql` | ✅ |
| 告警规则表、告警事件表 | `V208__Create_AIoT_Alert_Tables.sql` | ✅ |
| 物模型属性表、设备物模型绑定表 | `V209__Create_AIoT_ThingModelProperty_DeviceModelBinding_Tables.sql` | ✅ |

### 3.3 OTA Mapper

| Mapper | 状态 |
|--------|------|
| `FirmwarePackageMapper` | ✅ |
| `FirmwareCompatibilityMapper` | ✅ |
| `UpgradeBatchMapper` | ✅ |
| `UpgradeTaskMapper` | ✅ |
| `UpgradeProgressLogMapper` | ✅ |
| `UpgradeRollbackRecordMapper` | ✅ |
| `UpgradePolicyMapper` | ✅ |

### 3.4 OTA Flyway 迁移

| 脚本 | 实际文件 | 状态 |
|------|----------|------|
| 固件包表、兼容矩阵表 | `V210__Create_OTA_Firmware_Tables.sql` | ✅ |
| 升级批次表、升级任务表 | `V211__Create_OTA_Upgrade_Tables.sql` | ✅ |
| 进度日志表、回滚记录表 | `V212__Create_OTA_Progress_Rollback_Tables.sql` | ✅ |
| OTA 运行配置初始化数据 | `V213__Seed_OTA_Runtime_Config.sql` | ✅（规划外） |
| 菜单初始化 | `V214__Init_OTA_Menus.sql` | ✅（规划外） |
| 升级策略表 | `V215__Create_OTA_Upgrade_Policy_Table.sql` | ✅ |

---

## 四、应用层（Application）

### 4.1 AIoT 应用服务

| 服务 | 状态 |
|------|------|
| `DeviceService` + impl | ✅（`heartbeat`/`deviceOffline` 接入 `AiotMetrics` 计数器） |
| `DeviceShadowService` + impl | ✅ |
| `DeviceCommandService` + impl | ✅ |
| `DeviceTelemetryService` + impl | ✅（`report()` 接入告警引擎 + 遥测计数器） |
| `ThingModelService` + impl | ✅（`listProperties`/`saveProperty`/`deleteProperty`） |
| `AiotConfigService` + impl | ✅ |
| `MqttGatewayConfigService` + impl | ✅ |
| `MqttDeviceAuthService` + impl | ✅ |
| `AiotProductTypeProvider`（SPI 实现） | ✅ |
| `DeviceSnapshotQueryServiceImpl`（SPI 实现） | ✅（读取 `upgradableVersion` 缓存字段） |
| `DeviceFirmwareWritebackServiceImpl`（同时实现 `UpgradeVersionWritebackPort`） | ✅ |
| `AiotMqttGatewayRuntimeProvider`（MQTT SPI 实现） | ✅ |
| MQTT 入站处理器（Telemetry / ShadowReport / CommandAck） | ✅ |
| `DeviceGroupService` + impl | ✅ |
| `AlertRuleService` + impl | ✅（THRESHOLD 评估 + 去重 + 发布 `AlertTriggeredEvent` + 告警计数器） |
| `AiotStatisticsService` + impl | ✅ |
| `FirmwareReleasedNotificationListener` | ✅（缓存可升级版本 TTL 7 天） |
| `AlertNotificationListener` | ✅ Phase B（`@ConditionalOnClass(NotificationService)`，异步发邮件） |
| `AiotMetrics` | ✅ Phase B（`@ConditionalOnClass(MeterRegistry)`，设备在线/离线/遥测/告警计数器） |

#### aiot-application OTA Adapter（optional，`@ConditionalOnClass` 守卫）

| Adapter | 实现的 OTA SPI | 状态 |
|---------|---------------|------|
| `DeviceUpgradeNotificationAdapter` | `UpgradeTargetNotificationPort` | ✅ |
| `DeviceTargetInfoAdapter` | `UpgradeTargetInfoPort` | ✅ |
| `DeviceFirmwareWritebackServiceImpl`（复用已有类） | `UpgradeVersionWritebackPort` | ✅ |
| `DeviceReachabilityAdapter` | `UpgradeTargetReachabilityPort` | ✅ |

### 4.2 OTA 应用服务

| 服务 | 状态 |
|------|------|
| `FirmwareService` + impl | ✅ |
| `UpgradeBatchService` + impl | ✅（`createBatch` 含离线日志 + `OtaMetrics` 批次计数器） |
| `UpgradeTaskService` + impl | ✅ |
| `OtaConfigService` + impl | ✅ |
| `FirmwareDownloadTokenService` | ✅ |
| `OtaProductTypeProvider`（SPI 实现） | ✅ |
| `UpgradePolicyService` + impl | ✅ |
| `OtaStatisticsService` + impl | ✅ |
| `UpgradeTargetOfflineServiceImpl`（实现 `UpgradeTargetOfflinePort`） | ✅ |
| `UpgradeTaskMetricsListener` | ✅ Phase B（`@ConditionalOnClass(MeterRegistry)`，成功/失败计数器） |
| `UpgradeFailureNotificationListener` | ✅ Phase B（`@ConditionalOnClass(NotificationService)`，失败发邮件） |
| `OtaMetrics` | ✅ Phase B（`@ConditionalOnClass(MeterRegistry)`，升级成功/失败/批次计数器） |

---

## 五、AIoT 管理端 API（`mortise-aiot-admin`）

统一前缀：`/api/v1/admin/aiot`

### 5.1 AdminDeviceController

| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| `GET` | `/devices` | 设备列表 | ✅ |
| `GET` | `/devices/{id}` | 设备详情 | ✅ |
| `POST` | `/devices` | 创建设备 | ✅ |
| `PUT` | `/devices/{id}` | 编辑设备基础信息 | ✅ |
| `GET` | `/devices/{id}/shadow` | 查看设备影子 | ✅ |
| `PUT` | `/devices/{id}/shadow/desired` | 更新期望态 | ✅ |
| `GET` | `/devices/{id}/telemetry/latest` | 最新遥测快照 | ✅ |
| `GET` | `/devices/{id}/commands` | 指令历史记录 | ✅ |
| `POST` | `/devices/{id}/commands` | 下发指令 | ✅ |
| `POST` | `/devices/{id}/activate` | 手动激活设备 | ✅ |
| `POST` | `/devices/{id}/freeze` | 冻结设备 | ✅ |
| `POST` | `/devices/{id}/retire` | 退役设备 | ✅ |
| `POST` | `/devices/{id}/bind` | 绑定用户或组织 | ✅ |
| `POST` | `/devices/{id}/unbind` | 解绑 | ✅ |

### 5.2 AdminDeviceGroupController

| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| `GET` | `/groups` | 设备分组列表 | ✅ |
| `POST` | `/groups` | 新建设备分组 | ✅ |
| `PUT` | `/groups/{id}` | 编辑设备分组 | ✅ |
| `DELETE` | `/groups/{id}` | 删除设备分组 | ✅ |
| `POST` | `/groups/{id}/devices` | 向分组添加设备 | ✅ |
| `DELETE` | `/groups/{id}/devices/{deviceId}` | 从分组移除设备 | ✅ |

### 5.3 AdminThingModelController

| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| `GET` | `/thing-models` | 物模型列表（支持 productId 过滤） | ✅ |
| `GET` | `/thing-models/{id}` | 物模型详情 | ✅ |
| `POST` | `/thing-models` | 新建物模型 | ✅ |
| `PUT` | `/thing-models/{id}` | 编辑物模型 | ✅ |
| `POST` | `/thing-models/{id}/publish` | 发布物模型 | ✅ |
| `GET` | `/thing-models/{id}/properties` | 物模型属性列表 | ✅ |
| `POST` | `/thing-models/{id}/properties` | 新增/更新属性（按 propKey upsert） | ✅ |
| `DELETE` | `/thing-models/{id}/properties/{propId}` | 删除属性 | ✅ |

### 5.4 AdminAlertRuleController

| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| `GET` | `/alert-rules` | 告警规则列表 | ✅ |
| `POST` | `/alert-rules` | 新建告警规则 | ✅ |
| `PUT` | `/alert-rules/{id}` | 编辑告警规则 | ✅ |
| `POST` | `/alert-rules/{id}/enable` | 启用告警规则 | ✅ |
| `POST` | `/alert-rules/{id}/disable` | 停用告警规则 | ✅ |
| `GET` | `/alert-events` | 告警事件列表 | ✅ |
| `POST` | `/alert-events/{id}/resolve` | 手动恢复告警 | ✅ |
| `POST` | `/alert-events/{id}/close` | 关闭告警 | ✅ |
| `GET` | `/statistics/overview` | 设备总览统计 | ✅ |

### 5.5 AdminMqttGatewayController（额外实现）

> 架构规划中以数据库驱动 MQTT 配置，此 Controller 是对应的管理端入口，规划外主动补充实现。

| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| `GET` | `/gateway/mqtt/brokers` | MQTT Broker 列表 | ✅ |
| `POST` | `/gateway/mqtt/brokers` | 保存 Broker 配置 | ✅ |
| `GET` | `/gateway/mqtt/routes` | 路由规则列表 | ✅ |
| `POST` | `/gateway/mqtt/routes` | 保存路由规则 | ✅ |

### 5.6 AdminAiotConfigController（额外实现）

| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| `GET` | `/config` | 列出所有运行配置分组 | ✅ |
| `GET` | `/config/{group}` | 查看单个配置分组 | ✅ |
| `PUT` | `/config/{group}` | 保存配置分组 | ✅ |

---

## 六、OTA 管理端 API（`mortise-ota-admin`）

统一前缀：`/api/v1/admin/ota`

### 6.1 AdminFirmwareController

| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| `GET` | `/firmwares` | 固件列表 | ✅ |
| `POST` | `/firmwares` | 新建固件元数据 | ✅ |
| `POST` | `/firmwares/{id}/publish` | 发布固件 | ✅ |
| `POST` | `/firmwares/{id}/upload` | 上传固件文件（关联 mortise-file） | ✅ |
| `POST` | `/firmwares/{id}/submit-review` | 提交审核 | ✅ |
| `POST` | `/firmwares/{id}/offline` | 下线固件 | ✅ |
| `GET` | `/firmwares/{id}/compatibilities` | 查看兼容矩阵 | ✅ |
| `POST` | `/firmwares/{id}/compatibilities` | 新增兼容规则 | ✅ |

### 6.2 AdminUpgradeBatchController

| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| `GET` | `/batches` | 升级批次列表 | ✅ |
| `POST` | `/batches` | 创建升级批次 | ✅ |
| `POST` | `/batches/{id}/execute` | 执行批次 | ✅ |
| `POST` | `/batches/{id}/pause` | 暂停批次 | ✅ |
| `POST` | `/batches/{id}/resume` | 恢复批次 | ✅ |
| `POST` | `/batches/{id}/terminate` | 终止批次 | ✅ |
| `GET` | `/batches/{id}/tasks` | 查看批次任务列表 | ✅ |
| `POST` | `/tasks/{id}/rollback` | 对单设备任务发起回滚 | ✅ |

### 6.3 AdminUpgradePolicyController

| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| `GET` | `/policies` | 升级策略列表 | ✅ |
| `POST` | `/policies` | 新建升级策略 | ✅ |
| `PUT` | `/policies/{id}` | 编辑升级策略 | ✅ |

### 6.4 AdminOtaStatisticsController

| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| `GET` | `/statistics/overview` | 升级总体统计 | ✅ |

### 6.5 AdminOtaConfigController（额外实现）

| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| `GET` | `/config` | 列出所有运行配置分组 | ✅ |
| `GET` | `/config/{group}` | 查看单个配置分组 | ✅ |
| `PUT` | `/config/{group}` | 保存配置分组 | ✅ |

---

## 七、AIoT 设备端 API（`mortise-aiot-api`）

统一前缀：`/api/v1/aiot`

> 当前所有设备端 API 合并在 `DeviceRegisterController` 一个 Controller 内。

| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| `POST` | `/devices/register` | 设备注册 | ✅ |
| `POST` | `/devices/activate` | 设备激活 | ✅ |
| `POST` | `/devices/auth/token` | 设备获取 / 续签令牌 | ✅ |
| `POST` | `/devices/heartbeat` | 心跳上报 | ✅ |
| `POST` | `/devices/shadow/report` | 上报 reported 状态 | ✅ |
| `GET` | `/devices/shadow/desired` | 拉取 desired 状态 | ✅ |
| `POST` | `/devices/telemetry` | 上报遥测数据 | ✅ |
| `GET` | `/devices/commands/pull` | 拉取待执行指令 | ✅ |
| `POST` | `/devices/commands/{commandNo}/ack` | 上报指令回执 | ✅ |
| `GET` | `/devices/mqtt/connect-info` | 查询 MQTT 连接参数（额外实现） | ✅ |
| `POST` | `/devices/events` | 上报设备事件 | ✅（最小可行：转存遥测快照，后续接入告警引擎） |

> 安全链：`AiotDeviceSecurityConfigurer` + `DeviceTokenAuthenticationFilter` 均已就绪，设备端 JWT 与用户 OAuth2 链路完全分离。

> EMQX Webhook 认证：`MqttAuthWebhookController`（额外实现，供 EMQX 认证插件回调）已就绪。

---

## 八、OTA 设备端 API（`mortise-ota-api`）

统一前缀：`/api/v1/ota`

> 当前所有设备端 API 合并在 `OtaCheckController` 一个 Controller 内。

| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| `GET` | `/check` | 查询是否有可升级版本 | ✅ |
| `POST` | `/download-ticket` | 申请下载授权票据 | ✅ |
| `GET` | `/manifest/{taskNo}` | 获取升级 manifest | ✅ |
| `POST` | `/tasks/{taskNo}/progress` | 上报升级进度 | ✅ |
| `POST` | `/tasks/{taskNo}/result` | 上报升级结果 | ✅ |
| `POST` | `/tasks/{taskNo}/rollback-result` | 上报回滚结果 | ✅ |
| `POST` | `/tasks/{taskNo}/prepare` | 设备确认准备升级 | ✅ |

---

## 九、MQTT 网关（`mortise-aiot-gateway-mqtt`）

| 组件 | 说明 | 状态 |
|------|------|------|
| `MqttGatewayRuntimeProvider` SPI | 由 aiot 业务模块实现，提供 broker 配置与路由 | ✅ |
| `MqttCommandDispatchTarget` SPI | 命令分发目标 | ✅ |
| `MqttInboundMessageHandler` SPI | 入站消息处理 SPI | ✅ |
| `MqttBrokerAdapter` SPI | Broker 适配器接口 | ✅ |
| `MqttDeviceProtocolGateway` | 实现 `DeviceProtocolGateway` SPI | ✅ |
| `MqttInboundDispatcher` | 入站消息分发器 | ✅ |
| `EmqxBrokerAdapter` | EMQX 专用适配器 | ✅ |
| `GenericMqttBrokerAdapter` | 通用 MQTT Broker 适配器 | ✅ |
| `MqttBrokerAdapterRegistry` | Broker Adapter 注册表 | ✅ |
| `MqttClientConfiguration` | MQTT 客户端自动装配 | ✅ |
| `MqttGatewayProperties` | 网关启动配置 | ✅ |

---

## 十、跨模块协作

### 10.1 模块依赖架构（最终版）

```
单向可选依赖，无循环，无商业逻辑下沉至公共层：

  ota-application  ──zero aiot dep──  （已彻底移除对 aiot-domain 的依赖）
  aiot-application ──optional──▶ ota-domain   （实现 4 个 OTA SPI 端口，@ConditionalOnClass 守卫）
```

#### OTA 定义的端口（`mortise-ota-domain/spi/`）— 全部由 aiot-application 实现

| SPI | 调用方 | 实现方（aiot-application） | 用途 |
|-----|--------|--------------------------|------|
| `UpgradeTargetNotificationPort` | `UpgradeBatchNotificationListener`（ota） | `DeviceUpgradeNotificationAdapter` | 批次创建后 MQTT 通知目标 |
| `UpgradeVersionWritebackPort` | `UpgradeTaskServiceImpl`（ota） | `DeviceFirmwareWritebackServiceImpl` | 升级/回滚后写回版本号 |
| `UpgradeTargetOfflinePort` | `DeviceServiceImpl.deviceOffline()`（aiot） | `UpgradeTargetOfflineServiceImpl`（ota 自身） | 设备离线时取消 PENDING 任务 |
| `UpgradeTargetInfoPort` | `UpgradeBatchServiceImpl`（ota） | `DeviceTargetInfoAdapter` | 按设备编码查询目标 ID + 当前版本 |
| `UpgradeTargetReachabilityPort` | `UpgradeBatchServiceImpl`（ota） | `DeviceReachabilityAdapter` | 批次创建前检查目标在线状态 |

**扩展性**：未来桌面/服务端软件 OTA 只需提供上述 SPI 的独立 Adapter，无需修改 OTA 核心逻辑。

### 10.2 协作清单

| 协作路径 | 机制 | 状态 |
|----------|------|------|
| OTA 批次创建后通知目标设备 | `UpgradeTargetNotificationPort` → MQTT | ✅ |
| OTA 按设备编码查询目标信息 | `UpgradeTargetInfoPort` → AIoT 设备快照 | ✅ |
| OTA 升级/回滚成功后写回版本号 | `UpgradeVersionWritebackPort` → AIoT 设备档案 | ✅ |
| 设备离线时取消 PENDING 升级任务 | AIoT `DeviceServiceImpl` → `UpgradeTargetOfflinePort` | ✅ |
| AIoT / OTA 注册产品类型 | `ProductTypeProvider` SPI（product-domain） | ✅ |
| 固件发布后刷新设备可升级快照缓存 | `FirmwareReleasedEvent` → AIoT `FirmwareReleasedNotificationListener` → `AiotCacheService` | ✅ |

---

## 十一、缓存与配置

| 项目 | 状态 |
|------|------|
| `AiotCacheService`（封装 Redis 操作） | ✅（新增 `cacheUpgradableVersion` / `getUpgradableVersion`） |
| `OtaCacheService`（封装 Redis 操作） | ✅ |
| AIoT 数据库驱动运行配置（`mortise_system_config`） | ✅ |
| OTA 数据库驱动运行配置 + 初始化种子数据 | ✅ |
| MQTT broker / 路由配置数据库化（`mortise_aiot_gateway_mqtt_broker` / `mortise_aiot_gateway_route`） | ✅ |
| `DeviceSnapshot` 含 `upgradableVersion` 字段（从缓存读取） | ✅ |

---

## 十二、待完成事项汇总

以下为尚未实现、需在后续阶段补齐的内容，大致按优先级排序。

> 最后更新：Phase B 运营与可观测性已全部完成。

### 下一阶段 A — 已完成 ✅

- ✅ `ThingModelProperty` + `DeviceModelBinding` 独立实体（V209 迁移）
- ✅ 告警引擎：`AlertRuleServiceImpl.evaluateForDevice()` + 遥测接入钩子
- ✅ `FirmwareReleasedEvent` → AIoT 缓存刷新（`FirmwareReleasedNotificationListener`）
- ✅ `DeviceSnapshot` 含 `upgradableVersion` 字段
- ✅ OTA 批次创建前在线检查（`UpgradeTargetReachabilityPort` + `DeviceReachabilityAdapter`）
- ✅ 前端物模型属性管理（Slideover + AdminPagedTableCard + 新建/编辑 Modal）
- ✅ 前后端 API 路径全面核验（AIoT + OTA 完全对齐）

### 下一阶段 B — 已完成 ✅

- ✅ **操作审计日志**：`@OperationLog` 注解覆盖 5 个 Admin Controller 的高风险操作（激活/冻结/退役/发布/终止等 13 个方法）
- ✅ **Micrometer 指标采集**：`AiotMetrics`（设备在线/离线/遥测/告警计数器）+ `OtaMetrics`（升级成功/失败/批次计数器），`@ConditionalOnClass` 条件装配
- ✅ **`mortise-notification` 接入**：`AlertNotificationListener`（告警邮件）+ `UpgradeFailureNotificationListener`（升级失败邮件），`@ConditionalOnClass` 条件装配
- ✅ **OTA 升级监控看板**：`batches.vue` 增强批次详情 Slideover（统计卡片 + 进度条 + 状态筛选 + 5秒自动刷新）

### 第三阶段 — 协议扩展与大规模部署

- **遥测时序明细存储**：当前仅存最新快照，需引入 TimescaleDB 或独立时序库支持历史查询
- **物模型版本演进**：`ThingModelProperty` 属性级权限控制与版本 diff
- **通知接收人配置化**：`AlertNotificationListener` / `UpgradeFailureNotificationListener` 中 `resolveReceiver()` 当前返回 null，需接入 `AiotConfigService`/`OtaConfigService` 读取运行时配置
- **CoAP / WebSocket 网关**：复用 `DeviceProtocolGateway` SPI，新建 `mortise-aiot-gateway-coap`
- **厂商平台适配**：涂鸦云、阿里云 IoT 等平台 Adapter，实现设备数据双向同步
- **基于 EMQX 的真实联调**：多 broker 路由验证、QoS 策略、遗嘱消息处理
