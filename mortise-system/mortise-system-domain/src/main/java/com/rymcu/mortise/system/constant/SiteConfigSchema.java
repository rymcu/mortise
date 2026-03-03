package com.rymcu.mortise.system.constant;

import com.rymcu.mortise.common.enumerate.FormFieldType;
import com.rymcu.mortise.system.model.SiteConfigFieldDef;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 网站配置各分组的字段 Schema 集中定义
 * <p>
 * Schema（字段定义）属于程序逻辑，不入库；
 * 实际配置值（values）才存储于 mortise_system_config 表。
 * 新增配置组只需在此枚举追加一个条目，前端无需改动。
 *
 * @author ronger
 */
@Getter
public enum SiteConfigSchema {

    SITE("site", "基本信息", List.of(
            new SiteConfigFieldDef("site.name",        "系统名称",   FormFieldType.TEXT,   true,  "请输入系统名称",        "Mortise", null),
            new SiteConfigFieldDef("site.logo",        "系统 Logo",  FormFieldType.IMAGE,  false, null,                  null,      null),
            new SiteConfigFieldDef("site.favicon",     "Favicon",   FormFieldType.IMAGE,  false, null,                  null,      null),
            new SiteConfigFieldDef("site.description", "网站描述",   FormFieldType.TEXT,   false, "请输入网站描述",        null,      null)
    )),

    SEO("seo", "SEO 设置", List.of(
            new SiteConfigFieldDef("seo.title_template", "页面标题模板", FormFieldType.TEXT, false,
                    "如：{page} - {site}，{page} 为当前页标题，{site} 为系统名称", "{page} - {site}", null),
            new SiteConfigFieldDef("seo.keywords", "全局关键词", FormFieldType.TEXT, false,
                    "多个关键词以英文逗号分隔", null, null)
    )),

    FOOTER("footer", "页脚配置", List.of(
            new SiteConfigFieldDef("footer.copyright",  "版权信息",           FormFieldType.TEXT, false, "© 2025 Mortise",             null, null),
            new SiteConfigFieldDef("footer.icp",        "ICP 备案号",         FormFieldType.TEXT, false, "如：粤ICP备XXXXXXXX号",        null, null),
            new SiteConfigFieldDef("footer.icp_link",   "ICP 备案链接",       FormFieldType.TEXT, false, "https://beian.miit.gov.cn/",  null, null),
            new SiteConfigFieldDef("footer.gov_beian",  "网安备案号",         FormFieldType.TEXT, false, "如：粤公网安备XXXXXXXXXXXXXXXX号", null, null),
            new SiteConfigFieldDef("footer.gov_link",   "网安备案链接",       FormFieldType.TEXT, false, "https://www.beian.gov.cn/",   null, null),
            new SiteConfigFieldDef("footer.telecom",    "电信业务经营许可证", FormFieldType.TEXT, false, "如：粤B2-XXXXXXXX",           null, null),
            new SiteConfigFieldDef("footer.telecom_link",    "电信业务经营许可证链接", FormFieldType.TEXT, false, "如：https://dxzhgl.miit.gov.cn/",           null, null)
    ));

    private final String group;
    private final String label;
    private final List<SiteConfigFieldDef> fields;

    SiteConfigSchema(String group, String label, List<SiteConfigFieldDef> fields) {
        this.group = group;
        this.label = label;
        this.fields = fields;
    }

    /**
     * 根据 group 标识查找对应枚举值。
     *
     * @param group 分组标识
     * @return 匹配的 SiteConfigSchema
     * @throws IllegalArgumentException 如果不存在对应分组
     */
    public static SiteConfigSchema ofGroup(String group) {
        return Arrays.stream(values())
                .filter(s -> s.group.equals(group))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未知的配置分组: " + group));
    }
}
