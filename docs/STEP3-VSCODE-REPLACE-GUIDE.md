# 🔧 第三步：VS Code 批量替换详细指南

## 📋 **前提条件**
- ✅ 已运行 `migrate-system-from-github.ps1` 脚本
- ✅ mortise-system 目录已创建，文件已复制
- ✅ 包名已自动替换完成

---

## 🎯 **目标**
替换所有 Java 文件中的 `import` 语句，将旧包名更新为新模块化包名。

---

## 📝 **操作步骤**

### **步骤 1：打开 VS Code 全局搜索替换**

1. 在 VS Code 中确保打开了 `D:\rymcu2024\mortise` 项目
2. 按快捷键：**`Ctrl + Shift + H`** （打开全局搜索替换）
3. 界面说明：
   ```
   ┌─────────────────────────────────────────┐
   │ 🔍 Search (搜索框)                       │
   │ ↓                                        │
   │ 📝 Replace (替换框)                      │
   │                                          │
   │ files to include (包含的文件)            │
   │ files to exclude (排除的文件)            │
   └─────────────────────────────────────────┘
   ```

### **步骤 2：启用正则表达式模式**

在搜索框右侧找到并点击 **`.*`** 图标（Use Regular Expression）
- 图标位置：搜索框右侧的工具栏
- 启用后图标会高亮显示

### **步骤 3：设置文件过滤**

在 **`files to include`** 输入框中输入：
```
mortise-system/**/*.java
```
这样只会在 mortise-system 模块的 Java 文件中进行替换。

### **步骤 4：逐条执行替换**

按照下表顺序，逐条执行 9 个替换操作：

---

## 🔄 **替换模式清单**

### **✅ 替换 1/9：实体类导入**
- **搜索框输入**：
  ```regex
  import com\.rymcu\.mortise\.entity\.
  ```
- **替换框输入**：
  ```
  import com.rymcu.mortise.system.entity.
  ```
- **说明**：替换 Entity 实体类的导入语句
- **点击**：`Replace All` (全部替换)
- **预期匹配数**：约 20-30 处

---

### **✅ 替换 2/9：Mapper 导入**
- **搜索框输入**：
  ```regex
  import com\.rymcu\.mortise\.mapper\.
  ```
- **替换框输入**：
  ```
  import com.rymcu.mortise.system.mapper.
  ```
- **说明**：替换 Mapper 接口的导入语句
- **点击**：`Replace All`
- **预期匹配数**：约 10-15 处

---

### **✅ 替换 3/9：Model 导入**
- **搜索框输入**：
  ```regex
  import com\.rymcu\.mortise\.model\.
  ```
- **替换框输入**：
  ```
  import com.rymcu.mortise.system.model.
  ```
- **说明**：替换 Model（DTO、VO）的导入语句
- **点击**：`Replace All`
- **预期匹配数**：约 15-25 处

---

### **✅ 替换 4/9：Service 导入**
- **搜索框输入**：
  ```regex
  import com\.rymcu\.mortise\.service\.
  ```
- **替换框输入**：
  ```
  import com.rymcu.mortise.system.service.
  ```
- **说明**：替换 Service 服务类的导入语句
- **点击**：`Replace All`
- **预期匹配数**：约 15-20 处

---

### **✅ 替换 5/9：Controller 导入**
- **搜索框输入**：
  ```regex
  import com\.rymcu\.mortise\.web\.admin\.
  ```
- **替换框输入**：
  ```
  import com.rymcu.mortise.system.controller.
  ```
- **说明**：替换 Controller 控制器的导入语句（注意：web.admin → controller）
- **点击**：`Replace All`
- **预期匹配数**：约 5-10 处

---

### **✅ 替换 6/9：工具类导入**
- **搜索框输入**：
  ```regex
  import com\.rymcu\.mortise\.util\.
  ```
- **替换框输入**：
  ```
  import com.rymcu.mortise.common.util.
  ```
- **说明**：替换工具类的导入语句（迁移到 common 模块）
- **点击**：`Replace All`
- **预期匹配数**：约 5-10 处

---

### **✅ 替换 7/9：结果类导入**
- **搜索框输入**：
  ```regex
  import com\.rymcu\.mortise\.result\.
  ```
- **替换框输入**：
  ```
  import com.rymcu.mortise.core.result.
  ```
- **说明**：替换 Result、GlobalResult 的导入语句（迁移到 core 模块）
- **点击**：`Replace All`
- **预期匹配数**：约 10-15 处

---

### **✅ 替换 8/9：异常类导入**
- **搜索框输入**：
  ```regex
  import com\.rymcu\.mortise\.exception\.
  ```
- **替换框输入**：
  ```
  import com.rymcu.mortise.common.exception.
  ```
- **说明**：替换异常类的导入语句（迁移到 common 模块）
- **点击**：`Replace All`
- **预期匹配数**：约 5-10 处

---

### **✅ 替换 9/9：枚举类导入**
- **搜索框输入**：
  ```regex
  import com\.rymcu\.mortise\.enumerate\.
  ```
- **替换框输入**：
  ```
  import com.rymcu.mortise.common.enumerate.
  ```
- **说明**：替换枚举类的导入语句（迁移到 common 模块）
- **点击**：`Replace All`
- **预期匹配数**：约 3-5 处

---

## ✅ **替换完成检查**

### **验证步骤**：

1. **检查替换总数**：
   - VS Code 会显示每次替换的匹配数
   - 总替换数应在 **100-150** 处左右

2. **搜索残留的旧包名**：
   - 在搜索框输入（启用正则）：
     ```regex
     import com\.rymcu\.mortise\.(?!system\.|common\.|core\.)
     ```
   - 应该 **0 匹配**，如果有匹配说明还有遗漏

3. **检查 package 声明**：
   - 搜索（禁用正则）：
     ```
     package com.rymcu.mortise.
     ```
   - 应该只匹配到 `com.rymcu.mortise.system.`、`com.rymcu.mortise.common.`、`com.rymcu.mortise.core.` 等新包名

---

## 📊 **替换完成记录**

使用此清单跟踪进度：

- [ ] ✅ 替换 1/9：实体类导入 (entity)
- [ ] ✅ 替换 2/9：Mapper 导入 (mapper)
- [ ] ✅ 替换 3/9：Model 导入 (model)
- [ ] ✅ 替换 4/9：Service 导入 (service)
- [ ] ✅ 替换 5/9：Controller 导入 (web.admin → controller)
- [ ] ✅ 替换 6/9：工具类导入 (util → common.util)
- [ ] ✅ 替换 7/9：结果类导入 (result → core.result)
- [ ] ✅ 替换 8/9：异常类导入 (exception → common.exception)
- [ ] ✅ 替换 9/9：枚举类导入 (enumerate → common.enumerate)

---

## 🎯 **下一步**

替换完成后，执行：

```powershell
# 1. 运行验证脚本
.\verify-system.ps1

# 2. 编译 mortise-system 模块
mvn clean compile -pl mortise-system -am

# 3. 检查编译错误并修复
```

---

## 💡 **常见问题**

### **Q1: 替换后出现编译错误？**
**A**: 可能是某些类还未迁移，需要：
- 检查错误提示的类名
- 确认该类应该在哪个模块
- 手动添加正确的依赖或迁移该类

### **Q2: 某些导入语句没有被替换？**
**A**: 检查：
- 是否启用了正则表达式模式（`.*` 图标）
- 搜索框中的点号是否转义（`\.`）
- 是否在 `files to include` 中正确设置了过滤

### **Q3: 替换后 UserUtils 找不到？**
**A**: UserUtils 需要单独处理：
- 从 GitHub 复制 `util/UserUtils.java`
- 放到 `mortise-common/src/main/java/com/rymcu/mortise/common/util/`
- 或者临时注释掉相关代码

### **Q4: @DictFormat 注解找不到？**
**A**: 该注解需要单独迁移：
- 从 GitHub 查找 `annotation/DictFormat.java`
- 迁移到 `mortise-common/annotation/` 或 `mortise-system/annotation/`

---

## 🔍 **快捷参考**

### **正则表达式语法说明**：
- `\.` - 匹配点号（需要转义）
- `import com\.rymcu\.mortise\.entity\.` - 匹配 "import com.rymcu.mortise.entity." 开头的行

### **VS Code 快捷键**：
- `Ctrl + Shift + H` - 全局搜索替换
- `Ctrl + Alt + Enter` - Replace All（全部替换）
- `F4` - 跳到下一个匹配
- `Shift + F4` - 跳到上一个匹配

---

## ⏱️ **预计耗时**

- 熟悉界面：2 分钟
- 执行 9 次替换：10 分钟
- 验证检查：3 分钟
- **总计：约 15 分钟**

---

**祝您替换顺利！** 🚀

如有问题，请检查：
1. 是否启用了正则表达式模式
2. 是否正确设置了文件过滤
3. 是否逐条执行（不要一次性多条）
