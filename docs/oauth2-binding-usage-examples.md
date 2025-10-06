# OAuth2 绑定使用示例

## 1. OAuth2 登录流程

### 控制器示例

```java
@RestController
@RequestMapping("/api/v1/auth")
public class OAuth2AuthController {
    
    @Resource
    private AuthService authService;
    
    /**
     * OAuth2 登录回调处理
     */
    @GetMapping("/oauth2/callback/{provider}")
    public GlobalResult<TokenUser> oauth2Callback(
            @PathVariable String provider,
            @AuthenticationPrincipal OidcUser oidcUser) {
        
        // 构建标准化的 OAuth2 用户信息
        StandardOAuth2UserInfo userInfo = StandardOAuth2UserInfo.builder()
                .provider(provider)
                .openId(oidcUser.getSubject())
                .nickname(oidcUser.getName())
                .email(oidcUser.getEmail())
                .avatar(oidcUser.getPicture())
                .build();
        
        // 查找或创建用户（自动处理绑定）
        User user = authService.findOrCreateUserFromOAuth2(userInfo);
        
        // 生成 Token
        TokenUser tokenUser = authService.generateTokens(user);
        
        return GlobalResult.success(tokenUser);
    }
}
```

## 2. 查询用户的 OAuth2 绑定

### Service 示例

```java
@Service
public class UserProfileService {
    
    @Resource
    private UserOAuth2BindingService bindingService;
    
    /**
     * 获取用户的所有 OAuth2 绑定
     */
    public List<UserOAuth2Binding> getUserBindings(Long userId) {
        return bindingService.list(
            QueryWrapper.create()
                .where(UserOAuth2Binding::getUserId).eq(userId)
        );
    }
    
    /**
     * 检查用户是否绑定了某个提供商
     */
    public boolean hasBinding(Long userId, String provider) {
        UserOAuth2Binding binding = bindingService.findByUserIdAndProvider(userId, provider);
        return binding != null;
    }
    
    /**
     * 获取用户在某个提供商的绑定信息
     */
    public UserOAuth2Binding getBinding(Long userId, String provider) {
        return bindingService.findByUserIdAndProvider(userId, provider);
    }
}
```

## 3. 绑定新的 OAuth2 账号

### Service 示例

```java
@Service
public class OAuth2BindingManagementService {
    
    @Resource
    private UserOAuth2BindingService bindingService;
    @Resource
    private UserService userService;
    
    /**
     * 为已登录用户绑定新的 OAuth2 账号
     */
    @Transactional(rollbackFor = Exception.class)
    public void bindOAuth2Account(Long userId, StandardOAuth2UserInfo userInfo) {
        // 检查用户是否存在
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 检查该 OAuth2 账号是否已被其他用户绑定
        UserOAuth2Binding existingBinding = bindingService.findByProviderAndOpenId(
            userInfo.getProvider(), userInfo.getOpenId()
        );
        
        if (existingBinding != null) {
            if (!existingBinding.getUserId().equals(userId)) {
                throw new BusinessException("该 OAuth2 账号已被其他用户绑定");
            }
            // 已绑定到当前用户，无需重复绑定
            return;
        }
        
        // 创建新绑定
        UserOAuth2Binding binding = new UserOAuth2Binding();
        binding.setUserId(userId);
        binding.setProvider(userInfo.getProvider());
        binding.setOpenId(userInfo.getOpenId());
        binding.setNickname(userInfo.getNickname());
        binding.setAvatar(userInfo.getAvatar());
        binding.setEmail(userInfo.getEmail());
        binding.setCreatedTime(LocalDateTime.now());
        binding.setUpdatedTime(LocalDateTime.now());
        
        bindingService.save(binding);
    }
    
    /**
     * 解绑 OAuth2 账号
     */
    @Transactional(rollbackFor = Exception.class)
    public void unbindOAuth2Account(Long userId, String provider) {
        // 检查用户是否存在
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 检查是否还有其他登录方式
        long bindingCount = bindingService.count(
            QueryWrapper.create()
                .where(UserOAuth2Binding::getUserId).eq(userId)
        );
        
        if (bindingCount <= 1 && StringUtils.isBlank(user.getPassword())) {
            throw new BusinessException("至少保留一种登录方式");
        }
        
        // 删除绑定
        UserOAuth2Binding binding = bindingService.findByUserIdAndProvider(userId, provider);
        if (binding != null) {
            bindingService.removeById(binding.getId());
        }
    }
}
```

## 4. 控制器示例

### 用户绑定管理接口

```java
@RestController
@RequestMapping("/api/v1/user/oauth2")
public class UserOAuth2BindingController {
    
    @Resource
    private OAuth2BindingManagementService bindingManagementService;
    @Resource
    private UserOAuth2BindingService bindingService;
    
    /**
     * 获取当前用户的所有 OAuth2 绑定
     */
    @GetMapping("/bindings")
    public GlobalResult<List<UserOAuth2BindingDTO>> getBindings(
            @AuthenticationPrincipal UserDetailInfo userDetails) {
        
        Long userId = userDetails.getUser().getId();
        List<UserOAuth2Binding> bindings = bindingService.list(
            QueryWrapper.create()
                .where(UserOAuth2Binding::getUserId).eq(userId)
        );
        
        // 转换为 DTO（隐藏敏感信息）
        List<UserOAuth2BindingDTO> dtos = bindings.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
        
        return GlobalResult.success(dtos);
    }
    
    /**
     * 解绑 OAuth2 账号
     */
    @DeleteMapping("/bindings/{provider}")
    public GlobalResult<Void> unbind(
            @PathVariable String provider,
            @AuthenticationPrincipal UserDetailInfo userDetails) {
        
        Long userId = userDetails.getUser().getId();
        bindingManagementService.unbindOAuth2Account(userId, provider);
        
        return GlobalResult.success();
    }
    
    private UserOAuth2BindingDTO toDTO(UserOAuth2Binding binding) {
        UserOAuth2BindingDTO dto = new UserOAuth2BindingDTO();
        dto.setId(binding.getId());
        dto.setProvider(binding.getProvider());
        dto.setNickname(binding.getNickname());
        dto.setAvatar(binding.getAvatar());
        dto.setEmail(binding.getEmail());
        dto.setCreatedTime(binding.getCreatedTime());
        // 不包含 accessToken, refreshToken 等敏感信息
        return dto;
    }
}
```

### DTO 定义

```java
@Data
public class UserOAuth2BindingDTO {
    private Long id;
    private String provider;
    private String nickname;
    private String avatar;
    private String email;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;
}
```

## 5. 前端调用示例

### 获取绑定列表

```javascript
// 获取用户的 OAuth2 绑定
async function getOAuth2Bindings() {
    const response = await fetch('/api/v1/user/oauth2/bindings', {
        headers: {
            'Authorization': `Bearer ${accessToken}`
        }
    });
    
    const result = await response.json();
    if (result.success) {
        return result.data; // 返回绑定列表
    }
}

// 显示绑定状态
const bindings = await getOAuth2Bindings();
console.log('已绑定账号:', bindings);
// 输出示例:
// [
//   { provider: 'github', nickname: 'octocat', avatar: '...', ... },
//   { provider: 'google', nickname: 'John Doe', avatar: '...', ... }
// ]
```

### 解绑 OAuth2 账号

```javascript
// 解绑 GitHub 账号
async function unbindGitHub() {
    const response = await fetch('/api/v1/user/oauth2/bindings/github', {
        method: 'DELETE',
        headers: {
            'Authorization': `Bearer ${accessToken}`
        }
    });
    
    const result = await response.json();
    if (result.success) {
        console.log('解绑成功');
    } else {
        console.error('解绑失败:', result.message);
    }
}
```

### OAuth2 登录

```javascript
// 跳转到 OAuth2 登录页面
function loginWithGitHub() {
    window.location.href = '/oauth2/authorization/github';
}

function loginWithGoogle() {
    window.location.href = '/oauth2/authorization/google';
}
```

## 6. 测试示例

### 单元测试

```java
@SpringBootTest
class AuthServiceImplTest {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private UserOAuth2BindingService bindingService;
    
    @Autowired
    private UserService userService;
    
    @Test
    @Transactional
    void testFindOrCreateUserFromOAuth2_NewUser() {
        // 准备测试数据
        StandardOAuth2UserInfo userInfo = StandardOAuth2UserInfo.builder()
                .provider("github")
                .openId("12345678")
                .nickname("testuser")
                .email("test@example.com")
                .avatar("https://example.com/avatar.jpg")
                .build();
        
        // 执行
        User user = authService.findOrCreateUserFromOAuth2(userInfo);
        
        // 验证用户创建
        assertNotNull(user);
        assertNotNull(user.getId());
        assertEquals("test@example.com", user.getEmail());
        
        // 验证绑定创建
        UserOAuth2Binding binding = bindingService.findByProviderAndOpenId("github", "12345678");
        assertNotNull(binding);
        assertEquals(user.getId(), binding.getUserId());
        assertEquals("testuser", binding.getNickname());
    }
    
    @Test
    @Transactional
    void testFindOrCreateUserFromOAuth2_ExistingUser() {
        // 准备已存在的用户和绑定
        User existingUser = createTestUser();
        createTestBinding(existingUser.getId(), "github", "87654321");
        
        StandardOAuth2UserInfo userInfo = StandardOAuth2UserInfo.builder()
                .provider("github")
                .openId("87654321")
                .nickname("updatedname")
                .email("updated@example.com")
                .avatar("https://example.com/new-avatar.jpg")
                .build();
        
        // 执行
        User user = authService.findOrCreateUserFromOAuth2(userInfo);
        
        // 验证返回的是同一个用户
        assertEquals(existingUser.getId(), user.getId());
        
        // 验证绑定信息已更新
        UserOAuth2Binding binding = bindingService.findByProviderAndOpenId("github", "87654321");
        assertEquals("updatedname", binding.getNickname());
        assertEquals("updated@example.com", binding.getEmail());
    }
}
```

## 总结

新的 OAuth2 绑定机制提供了：

1. **灵活的多账号绑定**: 用户可以绑定多个 OAuth2 账号
2. **清晰的数据结构**: 绑定信息与用户信息分离
3. **完整的生命周期管理**: 支持创建、查询、更新、删除绑定
4. **良好的扩展性**: 易于添加新的 OAuth2 提供商
5. **安全性保障**: Token 等敏感信息独立存储

通过这个新设计，系统可以更好地支持多种社交账号登录和账号合并等高级功能。
