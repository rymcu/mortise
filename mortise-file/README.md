# Mortise File 模块

文件管理模块，基于 [x-file-storage](https://x-file-storage.xuyanwu.cn/) 实现统一的文件存储服务。

## 功能特性

- ✅ 统一文件上传接口
- ✅ 支持多种存储平台（本地、阿里云OSS、腾讯云COS、MinIO等）
- ✅ 自动生成缩略图
- ✅ 文件信息持久化
- ✅ 文件上传/删除监听
- ✅ 完善的异常处理
- ✅ 参数校验
- ✅ 与业务模块解耦，通过 `objectId` 和 `objectType` 关联

## 快速开始

### 1. 引入依赖

在 `pom.xml` 中添加模块依赖：

```xml
<dependency>
    <groupId>com.rymcu</groupId>
    <artifactId>mortise-file</artifactId>
</dependency>
```

### 2. 配置文件

在 `mortise-app/src/main/resources/application-dev.yml` 中已配置好文件存储相关配置。

关键配置项：

```yaml
# x-file-storage 配置
dromara:
  x-file-storage:
    default-platform: local-plus-1
    thumbnail-suffix: .min
    local-plus:
      - platform: local-plus-1
        enable-storage: true
        domain: /files/
        storage-path: /app/files/

# Mortise 文件模块配置
mortise:
  file:
    enabled: true
    max-file-size: 10485760  # 10MB
```

### 3. 数据库迁移

文件模块的数据库表由 Flyway 自动创建：
- `V7__Create_File_Tables.sql` - 创建文件记录表和分片表

### 4. 使用示例

#### 直接使用 FileStorageService

```java
@RestController
public class MyController {
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @PostMapping("/upload")
    public FileInfo upload(MultipartFile file) {
        return fileStorageService.of(file)
            .setPath("mypath/")  // 设置路径
            .upload();
    }
}
```

#### 使用 REST API

**上传文件**

```bash
curl -X POST http://localhost:9999/mortise/api/v1/files \
  -F "file=@/path/to/file.jpg"
```

**上传文件到指定路径**

```bash
curl -X POST http://localhost:9999/mortise/api/v1/files/path \
  -F "file=@/path/to/file.jpg" \
  -F "path=upload/images/"
```

**上传图片并生成缩略图**

```bash
curl -X POST http://localhost:9999/mortise/api/v1/files/image \
  -F "file=@/path/to/image.jpg" \
  -F "width=300" \
  -F "height=300"
```

#### 在业务模块中集成

参考 `MediaAssetServiceImpl` 的实现：

```java
@Service
public class MediaAssetServiceImpl {
    
    @Resource
    private FileStorageService fileStorageService;
    
    public FileInfo uploadFile(MultipartFile file) {
        // 上传文件并保存记录
        FileInfo fileInfo = fileStorageService.of(file)
                .setPath("media/") // 设置存储路径
                .setSaveFilename(file.getOriginalFilename())
                .upload();
        
        // fileInfo.getId() 是文件记录ID，可以保存到业务表的 file_detail_id 字段
        return fileInfo;
    }
}
```

## 核心组件

### 1. FileDetailService

实现了 `FileRecorder` 接口，用于将文件信息持久化到数据库。

**核心方法：**
- `save(FileInfo)` - 保存文件信息
- `update(FileInfo)` - 更新文件信息
- `getByUrl(String)` - 根据 URL 查询文件
- `delete(String)` - 删除文件信息

### 2. FileController

提供 REST API 接口：
- `POST /api/v1/files` - 上传文件
- `POST /api/v1/files/path` - 上传文件到指定路径
- `POST /api/v1/files/image` - 上传图片并生成缩略图
- `GET /api/v1/files/info` - 获取文件信息
- `DELETE /api/v1/files` - 删除文件
- `GET /api/v1/files/exists` - 检查文件是否存在

### 3. 监听器

- **FileUploadListener**: 监听文件上传事件
- **FileDeleteListener**: 监听文件删除事件

## 业务集成指南

### 与 MediaAsset 集成示例

MediaAsset 通过 `fileDetailId` 字段关联到文件记录表：

```java
@Data
@Table(value = "mortise_media_asset")
public class MediaAsset {
    @Id
    private Long id;
    
    /**
     * 文件记录ID（关联 mortise_file_detail 表）
     */
    private Long fileDetailId;
    
    // 其他字段...
}
```

上传文件并创建媒体资产：

```java
@Service
public class MediaAssetServiceImpl {
    
    @Resource
    private FileStorageService fileStorageService;
    
    @Transactional(rollbackFor = Exception.class)
    public MediaAsset createAssetFromFileInfo(FileInfo fileInfo, Long uploaderId) {
        MediaAsset asset = new MediaAsset();
        
        // 关联文件记录
        if (StrUtil.isNotBlank(fileInfo.getId())) {
            asset.setFileDetailId(Long.parseLong(fileInfo.getId()));
        }
        
        // 设置文件信息
        asset.setFilename(fileInfo.getFilename());
        asset.setOriginalFilename(fileInfo.getOriginalFilename());
        asset.setFileSize(fileInfo.getSize());
        asset.setPublicUrl(fileInfo.getUrl());
        
        // 保存到数据库
        mapper.insert(asset);
        
        return asset;
    }
}
```

### 通用的 objectId/objectType 模式

`mortise_file_detail` 表提供了 `object_id` 和 `object_type` 字段，可以灵活关联到任何业务对象：

```java
// 上传文件并关联到业务对象
FileInfo fileInfo = fileStorageService.of(file)
    .setObjectId("123456")           // 业务对象ID
    .setObjectType("Course")         // 业务对象类型
    .upload();

// 查询某个业务对象的所有文件
List<FileDetail> files = fileDetailMapper.selectListByQuery(
    QueryWrapper.create()
        .where(FILE_DETAIL.OBJECT_ID.eq("123456"))
        .and(FILE_DETAIL.OBJECT_TYPE.eq("Course"))
);
```

## 配置说明

### x-file-storage 配置

```yaml
dromara:
  x-file-storage:
    # 默认存储平台
    default-platform: local-1
    # 缩略图后缀
    thumbnail-suffix: .min
    
    # 本地存储
    local:
      - platform: local-1
        enable-storage: true
        enable-access: true
        base-path: files/
        domain: http://localhost:8080
    
    # 阿里云 OSS
    aliyun-oss:
      - platform: aliyun-oss-1
        enable-storage: false
        access-key: your-access-key
        secret-key: your-secret-key
        end-point: oss-cn-hangzhou.aliyuncs.com
        bucket-name: your-bucket
        domain: https://your-domain.com
```

### Mortise 文件模块配置

```yaml
mortise:
  file:
    # 启用文件上传
    enabled: true
    # 最大文件大小（10MB）
    max-file-size: 10485760
    # 允许的文件类型
    allowed-content-types:
      - image/jpeg
      - image/png
      - application/pdf
    # 启用缩略图
    thumbnail-enabled: true
    # 缩略图尺寸
    thumbnail-width: 200
    thumbnail-height: 200
```

## 数据库表结构

### mortise_file_detail

存储文件的详细信息，包括：
- 基本信息：文件名、大小、类型、扩展名
- 存储信息：存储平台、路径、URL
- 缩略图信息：缩略图URL、大小等
- 关联信息：`object_id`、`object_type`（用于关联业务对象）
- 元数据：文件元数据、用户元数据、哈希信息等

### mortise_file_part_detail

存储文件分片上传信息（仅在手动分片上传时使用）。

### 业务表关联方式

**方式一：通过 file_detail_id 关联**（推荐）

```sql
-- 在业务表中添加字段
ALTER TABLE your_business_table 
    ADD COLUMN file_detail_id BIGINT;
```

**方式二：通过 object_id/object_type 关联**

直接在上传时设置：
```java
fileStorageService.of(file)
    .setObjectId(businessId)
    .setObjectType("BusinessType")
    .upload();
```

## 扩展功能

### 自定义存储平台

x-file-storage 支持多种存储平台，可根据需要配置：

- 本地存储 (local)
- 阿里云 OSS (aliyun-oss)
- 腾讯云 COS (tencent-cos)
- MinIO (minio)
- AWS S3 (aws-s3)
- 更多...

### 文件监听器

可以自定义监听器实现 `FileStorageAspect` 接口：

```java
@Component
public class MyFileListener implements FileStorageAspect {
    
    @Override
    public boolean uploadAround(UploadAspectChain chain) {
        // 上传前后的处理逻辑
        return chain.next();
    }
}
```

## 注意事项

1. 确保配置的存储路径有读写权限
2. 生产环境建议使用云存储（OSS、COS等）
3. 注意文件大小限制配置
4. 建议对敏感文件做访问权限控制

## 参考文档

- [x-file-storage 官方文档](https://x-file-storage.xuyanwu.cn/)
