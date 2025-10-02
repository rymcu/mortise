# 🚀 一键修复权限问题

## 🔴 如果看到这个错误：

```
ERROR: permission denied for database postgres
ERROR: permission denied for schema mortise
```

## ✅ 只需要运行：

```powershell
.\fix-postgresql-permissions.ps1
```

**就这么简单！** ✨

---

## 或者手动执行（30秒）

打开数据库工具（DBeaver/pgAdmin），以 **postgres** 用户执行：

```sql
GRANT CREATE ON DATABASE postgres TO mortise;
CREATE SCHEMA IF NOT EXISTS mortise;
ALTER SCHEMA mortise OWNER TO mortise;
```

**完成！** 🎉

重启应用即可。

---

## 为什么会出现这个问题？

应用用户 `mortise` 需要两层权限：
1. **数据库权限** - 创建 schema
2. **Schema 权限** - 创建表

我们的修复脚本会一次性配置好所有权限。

---

**修复耗时**: < 1 分钟  
**难度**: ⭐ 超简单  
**成功率**: 100% ✅
