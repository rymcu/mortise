package com.rymcu.mortise.service.impl;

import com.rymcu.mortise.core.constant.ProjectConstant;
import com.rymcu.mortise.service.JavaMailService;
import com.rymcu.mortise.util.Utils;
import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2024/4/17 22:58.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.service.impl
 */
@Service
public class JavaMailServiceImpl implements JavaMailService {

    /**
     * Java邮件发送器
     */
    @Resource
    private JavaMailSenderImpl mailSender;
    @Autowired
    private StringRedisTemplate redisTemplate;
    /**
     * thymeleaf模板引擎
     */
    @Resource
    private TemplateEngine templateEngine;

    @Value("${spring.mail.host}")
    private String SERVER_HOST;
    @Value("${spring.mail.port}")
    private String SERVER_PORT;
    @Value("${spring.mail.username}")
    private String USERNAME;
    @Value("${spring.mail.password}")
    private String PASSWORD;
    @Value("${baseUrl}")
    private String BASE_URL;

    @Override
    public int sendEmailCode(String email) throws MessagingException {
        return sendCode(email, 0, String.valueOf(Utils.genCode()));
    }

    @Override
    public int sendForgetPasswordEmail(String email) throws MessagingException {
        String code = Utils.encryptPassword(email);
        return sendCode(email, 1, code);
    }

    /**
     * 发送用户初始密码邮件
     *
     * @param email 收件人邮箱
     * @param code
     * @return 执行结果 0：失败1：成功
     * @throws MessagingException
     */
    @Override
    public int sendInitialPassword(String email, String code) throws MessagingException {
        return sendCode(email, 2, code);
    }

    private int sendCode(String to, int type, String code) throws MessagingException {
        Properties props = getProps();
        mailSender.setJavaMailProperties(props);
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(USERNAME);
        simpleMailMessage.setTo(to);
        if (type == 0) {
            redisTemplate.boundValueOps(ProjectConstant.REDIS_REGISTER + to).set(code, 5, TimeUnit.MINUTES);
            simpleMailMessage.setSubject("新用户注册邮箱验证");
            simpleMailMessage.setText("【RYMCU】您的校验码是 " + code + ",有效时间 5 分钟，请不要泄露验证码给其他人。如非本人操作,请忽略！");
            mailSender.send(simpleMailMessage);
            return 1;
        } else if (type == 1) {
            String url = BASE_URL + "/forget-password?code=" + code;
            redisTemplate.boundValueOps(code).set(ProjectConstant.REDIS_FORGET_PASSWORD + to, 5, TimeUnit.MINUTES);

            String thymeleafTemplatePath = "mail/forgetPasswordTemplate";
            Map<String, Object> thymeleafTemplateVariable = new HashMap<String, Object>(1);
            thymeleafTemplateVariable.put("url", url);

            sendTemplateEmail(USERNAME,
                    new String[]{to},
                    new String[]{},
                    "【RYMCU】 找回密码",
                    thymeleafTemplatePath,
                    thymeleafTemplateVariable);
            return 1;
        } else if (type == 2) {
            simpleMailMessage.setSubject("新用户初始密码");
            simpleMailMessage.setText("【RYMCU】感谢您选择 RYMCU, 您的初始密码是 " + code + ", 请及时修改初始密码。");
            mailSender.send(simpleMailMessage);
            return 1;
        }
        return 0;
    }

    private @NotNull Properties getProps() {
        Properties props = new Properties();
        // 表示SMTP发送邮件，需要进行身份验证
        props.put("mail.smtp.auth", Optional.of(true));
        props.put("mail.smtp.ssl.enable", Optional.of(true));
        props.put("mail.smtp.host", SERVER_HOST);
        props.put("mail.smtp.port", SERVER_PORT);
        // 如果使用ssl，则去掉使用25端口的配置，进行如下配置,
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.port", SERVER_PORT);
        // 发件人的账号，填写控制台配置的发信地址,比如xxx@xxx.com
        props.put("mail.user", USERNAME);
        // 访问SMTP服务时需要提供的密码(在控制台选择发信地址进行设置)
        props.put("mail.password", PASSWORD);
        return props;
    }

    /**
     * 发送thymeleaf模板邮件
     *
     * @param deliver                   发送人邮箱名 如： javalsj@163.com
     * @param receivers                 收件人，可多个收件人 如：11111@qq.com,2222@163.com
     * @param carbonCopys               抄送人，可多个抄送人 如：33333@sohu.com
     * @param subject                   邮件主题 如：您收到一封高大上的邮件，请查收。
     * @param thymeleafTemplatePath     邮件模板 如：mail\mailTemplate.html。
     * @param thymeleafTemplateVariable 邮件模板变量集
     */
    public void sendTemplateEmail(String deliver, String[] receivers, String[] carbonCopys, String subject, String thymeleafTemplatePath,
                                  Map<String, Object> thymeleafTemplateVariable) throws MessagingException {
        String text = null;
        if (thymeleafTemplateVariable != null && !thymeleafTemplateVariable.isEmpty()) {
            Context context = new Context();
            thymeleafTemplateVariable.forEach(context::setVariable);
            text = templateEngine.process(thymeleafTemplatePath, context);
        }
        sendMimeMail(deliver, receivers, carbonCopys, subject, text, true, null);
    }

    /**
     * 发送的邮件(支持带附件/html类型的邮件)
     *
     * @param deliver             发送人邮箱名 如： javalsj@163.com
     * @param receivers           收件人，可多个收件人 如：11111@qq.com,2222@163.com
     * @param carbonCopys         抄送人，可多个抄送人 如：3333@sohu.com
     * @param subject             邮件主题 如：您收到一封高大上的邮件，请查收。
     * @param text                邮件内容 如：测试邮件逗你玩的。 <html><body><img
     *                            src=\"cid:attchmentFileName\"></body></html>
     * @param attachmentFilePaths 附件文件路径 如：
     *                            需要注意的是addInline函数中资源名称attchmentFileName需要与正文中cid:attchmentFileName对应起来
     * @throws Exception 邮件发送过程中的异常信息
     */
    private void sendMimeMail(String deliver, String[] receivers, String[] carbonCopys, String subject, String text,
                              boolean isHtml, String[] attachmentFilePaths) throws MessagingException {
        StopWatch stopWatch = new StopWatch();

        stopWatch.start();
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom(deliver);
        helper.setTo(receivers);
        helper.setCc(carbonCopys);
        helper.setSubject(subject);
        helper.setText(text, isHtml);
        // 添加邮件附件
        if (attachmentFilePaths != null) {
            for (String attachmentFilePath : attachmentFilePaths) {
                File file = new File(attachmentFilePath);
                if (file.exists()) {
                    String attachmentFile = attachmentFilePath
                            .substring(attachmentFilePath.lastIndexOf(File.separator));
                    long size = file.length();
                    if (size > 1024 * 1024) {
                        String msg = String.format("邮件单个附件大小不允许超过1MB，[%s]文件大小[%s]。", attachmentFilePath,
                                Optional.of(file.length()));
                        throw new RuntimeException(msg);
                    } else {
                        FileSystemResource fileSystemResource = new FileSystemResource(file);
                        helper.addInline(attachmentFile, fileSystemResource);
                    }
                }
            }
        }
        mailSender.send(mimeMessage);
        stopWatch.stop();

    }
}
