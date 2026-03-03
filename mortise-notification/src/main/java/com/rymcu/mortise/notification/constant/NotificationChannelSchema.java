package com.rymcu.mortise.notification.constant;

import com.rymcu.mortise.common.enumerate.FormFieldType;
import com.rymcu.mortise.notification.enums.NotificationType;
import com.rymcu.mortise.notification.model.ChannelFieldDef;
import com.rymcu.mortise.notification.model.ChannelFieldDef.SelectOption;

import java.util.Arrays;
import java.util.List;

/**
 * 各通知渠道的字段 Schema 集中定义
 * <p>
 * Schema（字段定义）属于程序逻辑，不入库；
 * 实际配置值（values）才存储于 notification_channel_config 表。
 * 新增渠道只需在此枚举中追加一个条目，前端无需改动。
 *
 * @author ronger
 */
public enum NotificationChannelSchema {

    EMAIL(NotificationType.EMAIL, List.of(
            new ChannelFieldDef("host",      "SMTP 服务器",  FormFieldType.TEXT,     true,  "smtp.example.com",   null,   null),
            new ChannelFieldDef("port",      "端口",         FormFieldType.NUMBER,   true,  "465",                "465",  null),
            new ChannelFieldDef("username",  "发件邮箱",     FormFieldType.EMAIL,    true,  "no-reply@example.com", null, null),
            new ChannelFieldDef("password",  "授权码/密码",  FormFieldType.PASSWORD, true,  "",                   null,   null),
            new ChannelFieldDef("ssl",       "启用 SSL",     FormFieldType.BOOLEAN,  false, null,                 "true", null),
            new ChannelFieldDef("from_name", "发件人名称",   FormFieldType.TEXT,     false, "系统通知",            null,   null)
    )),

    SMS(NotificationType.SMS, List.of(
            new ChannelFieldDef("provider",   "短信供应商",  FormFieldType.SELECT,   true, null, "aliyun",
                    List.of(new SelectOption("阿里云", "aliyun"), new SelectOption("腾讯云", "tencent"))),
            new ChannelFieldDef("access_key", "AccessKey",  FormFieldType.TEXT,     true, null, null, null),
            new ChannelFieldDef("secret_key", "SecretKey",  FormFieldType.PASSWORD, true, null, null, null),
            new ChannelFieldDef("sign_name",  "短信签名",   FormFieldType.TEXT,     true, null, null, null)
    )),

    WECHAT(NotificationType.WECHAT, List.of(
            new ChannelFieldDef("app_id",     "AppID",     FormFieldType.TEXT,     true,  null, null, null),
            new ChannelFieldDef("app_secret", "AppSecret", FormFieldType.PASSWORD, true,  null, null, null),
            new ChannelFieldDef("template_id","默认模板ID", FormFieldType.TEXT,     false, null, null, null)
    ));

    private final NotificationType type;
    private final List<ChannelFieldDef> fields;

    NotificationChannelSchema(NotificationType type, List<ChannelFieldDef> fields) {
        this.type = type;
        this.fields = fields;
    }

    public NotificationType getType() {
        return type;
    }

    public List<ChannelFieldDef> getFields() {
        return fields;
    }

    /**
     * 根据 {@link NotificationType} 查找对应的 Schema 定义
     *
     * @param type 通知类型
     * @return 对应 Schema
     * @throws IllegalArgumentException 若该类型暂无 Schema 定义
     */
    public static NotificationChannelSchema fromType(NotificationType type) {
        return Arrays.stream(values())
                .filter(s -> s.type == type)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("暂无此渠道的 Schema 定义: " + type));
    }
}
