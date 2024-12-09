package com.rymcu.mortise.core.constant;

/**
 * 项目常量
 * @author ronger
 */
public final class ProjectConstant {
    /**当前环境*/
    public static final String ENV = "dev";
    /**项目基础包名称，根据自己公司的项目修改*/
    public static final String BASE_PACKAGE = "com.rymcu.mortise";
    /**DTO所在包*/
    public static final String DTO_PACKAGE = BASE_PACKAGE + ".model";
    /**Model所在包*/
    public static final String MODEL_PACKAGE = BASE_PACKAGE + ".entity";
    /**Mapper所在包*/
    public static final String MAPPER_PACKAGE = BASE_PACKAGE + ".mapper";
    /**Service所在包*/
    public static final String SERVICE_PACKAGE = BASE_PACKAGE + ".service";
    /**ServiceImpl所在包*/
    public static final String SERVICE_IMPL_PACKAGE = SERVICE_PACKAGE + ".impl";
    /**Controller所在包*/
    public static final String CONTROLLER_PACKAGE = BASE_PACKAGE + ".web";
    /**Mapper插件基础接口的完全限定名*/
    public static final String MAPPER_INTERFACE_REFERENCE = BASE_PACKAGE + ".core.mapper.Mapper";

    public static final String OPENAI_API_KEY = "OPENAI_API_KEY";

    public static final String OPENAI_BASE_URL = "OPENAI_BASE_URL";

    public static final String ENCRYPTION_KEY = "ENCRYPTION_KEY";

    public static final String REDIS_REGISTER = "register:";

    public static final String REDIS_FORGET_PASSWORD = "forget:";
}
