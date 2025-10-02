# UserContextService 使用示例

## 快速开始

### 1. 在 Service 层使用

```java
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {
    
    // ✅ 注入 UserContextService
    private final UserContextService userContextService;
    private final ArticleMapper articleMapper;
    
    @Override
    public Article createArticle(ArticleDTO dto) {
        // 获取当前用户
        User currentUser = userContextService.getCurrentUser();
        
        Article article = new Article();
        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        article.setAuthorId(currentUser.getId());
        article.setAuthorName(currentUser.getNickname());
        
        articleMapper.insert(article);
        return article;
    }
    
    @Override
    public void deleteArticle(Long articleId) {
        // Optional 方式处理可能未登录的情况
        userContextService.getCurrentUserOptional().ifPresent(user -> {
            Article article = articleMapper.selectById(articleId);
            if (article != null && article.getAuthorId().equals(user.getId())) {
                articleMapper.deleteById(articleId);
            } else {
                throw new BusinessException("无权删除该文章");
            }
        });
    }
}
```

### 2. 在 Controller 层使用（方式1：注入服务）

```java
@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {
    
    private final ArticleService articleService;
    private final UserContextService userContextService;
    
    @GetMapping("/my")
    public GlobalResult<List<Article>> getMyArticles() {
        // 获取当前用户 ID
        Long userId = userContextService.getCurrentUserId();
        if (userId == null) {
            return GlobalResult.failure("请先登录");
        }
        
        List<Article> articles = articleService.getArticlesByUserId(userId);
        return GlobalResult.success(articles);
    }
    
    @GetMapping("/profile")
    public GlobalResult<UserProfile> getProfile() {
        // Optional 方式
        return userContextService.getCurrentUserOptional()
                .map(user -> {
                    UserProfile profile = new UserProfile();
                    profile.setUsername(user.getAccount());
                    profile.setNickname(user.getNickname());
                    return GlobalResult.success(profile);
                })
                .orElse(GlobalResult.failure("未登录"));
    }
}
```

### 3. 在 Controller 层使用（方式2：推荐 - 使用注解）

```java
@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {
    
    private final ArticleService articleService;
    
    /**
     * ✨ 推荐方式：使用 @AuthenticationPrincipal 注解
     * Spring Security 自动注入当前用户信息
     */
    @PostMapping
    public GlobalResult<Article> createArticle(
            @RequestBody ArticleDTO dto,
            @AuthenticationPrincipal UserDetailInfo userDetails) {
        
        // 直接从参数获取用户信息
        User currentUser = userDetails.getUser();
        
        Article article = articleService.createArticle(dto, currentUser);
        return GlobalResult.success(article);
    }
    
    @DeleteMapping("/{id}")
    public GlobalResult<Void> deleteArticle(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailInfo userDetails) {
        
        articleService.deleteArticle(id, userDetails.getUser());
        return GlobalResult.success();
    }
}
```

### 4. 权限检查

```java
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    
    private final UserContextService userContextService;
    
    public void performAdminAction() {
        // 检查是否有管理员权限
        if (!userContextService.hasAuthority("ROLE_ADMIN")) {
            throw new BusinessException("需要管理员权限");
        }
        
        // 执行管理员操作
        // ...
    }
    
    public void performSensitiveAction() {
        // 检查是否有多个权限中的任意一个
        if (!userContextService.hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPER_USER")) {
            throw new BusinessException("权限不足");
        }
        
        // 执行敏感操作
        // ...
    }
    
    public void performCriticalAction() {
        // 检查是否同时拥有多个权限
        if (!userContextService.hasAllAuthorities("ROLE_ADMIN", "ROLE_AUDITOR")) {
            throw new BusinessException("需要同时具备管理员和审计员权限");
        }
        
        // 执行关键操作
        // ...
    }
}
```

### 5. 在拦截器中使用

```java
@Component
@RequiredArgsConstructor
public class UserActivityInterceptor implements HandlerInterceptor {
    
    private final UserContextService userContextService;
    private final UserActivityService userActivityService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) {
        // 记录用户活动
        userContextService.getCurrentUserOptional().ifPresent(user -> {
            userActivityService.recordActivity(
                user.getId(),
                request.getRequestURI(),
                LocalDateTime.now()
            );
        });
        
        return true;
    }
}
```

### 6. 在 AOP 切面中使用

```java
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class OperationLogAspect {
    
    private final UserContextService userContextService;
    private final OperationLogService operationLogService;
    
    @Around("@annotation(operationLog)")
    public Object logOperation(ProceedingJoinPoint pjp, OperationLog operationLog) throws Throwable {
        // 获取当前操作用户
        String operator = userContextService.getCurrentUsername();
        
        // 记录操作日志
        log.info("用户 {} 执行操作: {}", operator, operationLog.value());
        
        try {
            Object result = pjp.proceed();
            
            // 保存操作日志到数据库
            operationLogService.saveLog(
                operator,
                operationLog.value(),
                "SUCCESS",
                LocalDateTime.now()
            );
            
            return result;
        } catch (Exception e) {
            operationLogService.saveLog(
                operator,
                operationLog.value(),
                "FAILURE",
                LocalDateTime.now()
            );
            throw e;
        }
    }
}
```

### 7. 在定时任务中使用（注意事项）

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {
    
    private final UserContextService userContextService;
    
    /**
     * ⚠️ 注意：定时任务中没有 HTTP 请求上下文
     * SecurityContext 可能为空
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledTask() {
        // ❌ 错误：定时任务中直接获取当前用户会失败
        // User user = userContextService.getCurrentUser();
        
        // ✅ 正确：检查是否有认证上下文
        if (userContextService.isAuthenticated()) {
            User user = userContextService.getCurrentUser();
            // 处理逻辑
        } else {
            log.info("定时任务执行，无用户上下文");
            // 使用系统账户或跳过需要用户信息的操作
        }
    }
}
```

### 8. 单元测试

```java
@SpringBootTest
class ArticleServiceTest {
    
    @MockBean
    private UserContextService userContextService;
    
    @Autowired
    private ArticleService articleService;
    
    @Test
    void testCreateArticle() {
        // 准备测试数据
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setAccount("test@example.com");
        mockUser.setNickname("测试用户");
        
        // Mock UserContextService
        when(userContextService.getCurrentUser()).thenReturn(mockUser);
        when(userContextService.getCurrentUserId()).thenReturn(1L);
        
        // 执行测试
        ArticleDTO dto = new ArticleDTO();
        dto.setTitle("测试文章");
        dto.setContent("测试内容");
        
        Article article = articleService.createArticle(dto);
        
        // 验证结果
        assertNotNull(article);
        assertEquals("测试文章", article.getTitle());
        assertEquals(1L, article.getAuthorId());
        
        // 验证 Mock 被调用
        verify(userContextService, times(1)).getCurrentUser();
    }
    
    @Test
    void testCreateArticleWhenNotAuthenticated() {
        // Mock 未认证状态
        when(userContextService.getCurrentUser())
                .thenThrow(new UsernameNotFoundException("未认证"));
        
        // 验证抛出异常
        ArticleDTO dto = new ArticleDTO();
        assertThrows(UsernameNotFoundException.class, () -> {
            articleService.createArticle(dto);
        });
    }
}
```

## API 速查表

| 方法 | 返回类型 | 说明 | 未登录时行为 |
|------|---------|------|-------------|
| `getCurrentUser()` | `User` | 获取当前用户 | 抛出异常 |
| `getCurrentUserOptional()` | `Optional<User>` | 获取当前用户（Optional） | 返回 `Optional.empty()` |
| `getCurrentUserId()` | `Long` | 获取当前用户 ID | 返回 `null` |
| `getCurrentUsername()` | `String` | 获取当前用户名 | 返回 `null` |
| `getCurrentAccount()` | `String` | 获取当前用户账号 | 返回 `null` |
| `isAuthenticated()` | `boolean` | 检查是否已认证 | 返回 `false` |
| `getCurrentUserDetails()` | `UserDetailInfo` | 获取用户详情 | 抛出异常 |
| `hasAuthority(String)` | `boolean` | 检查是否有权限 | 返回 `false` |
| `hasAnyAuthority(String...)` | `boolean` | 检查是否有任一权限 | 返回 `false` |
| `hasAllAuthorities(String...)` | `boolean` | 检查是否有所有权限 | 返回 `false` |

## 最佳实践建议

### ✅ 推荐做法

1. **Controller 层优先使用 `@AuthenticationPrincipal` 注解**
2. **Service 层注入 `UserContextService`**
3. **使用 Optional 方式处理可能未登录的场景**
4. **在需要用户信息的地方明确声明依赖**

### ❌ 避免的做法

1. **不要使用静态工具类 `UserUtils`**
2. **不要在定时任务中直接获取当前用户（无上下文）**
3. **不要在没有 HTTP 请求的场景使用（如消息监听器）**
4. **不要忘记处理未认证的情况**

## 迁移清单

- [ ] 创建 `UserContextService` 接口和实现
- [ ] 在需要的类中注入 `UserContextService`
- [ ] 替换所有 `UserUtils.getCurrentUser()` 调用
- [ ] 更新单元测试
- [ ] 标记 `UserUtils` 为 `@Deprecated`
- [ ] 验证所有功能正常
- [ ] 删除 `UserUtils` 类
