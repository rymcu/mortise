package com.rymcu.mortise.common.constant;

/**
 * 项目基础常量
 *
 * @author ronger
 */
public final class ProjectConstant {

    public static final String OPENAI_API_KEY = "OPENAI_API_KEY";

    public static final String OPENAI_BASE_URL = "OPENAI_BASE_URL";

    public static final String ENCRYPTION_KEY = "ENCRYPTION_KEY";

    /**
     * 私有构造函数，防止实例化
     */
    private ProjectConstant() {
        throw new AssertionError("常量类不应该被实例化");
    }
}
