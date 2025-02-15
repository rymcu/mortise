create table if not exists mortise_dict
(
    id             bigint auto_increment comment '主键'
        primary key,
    dict_type_code varchar(64)           not null comment '代码',
    label          varchar(64)           not null comment '名称',
    value          varchar(255)          null comment '数据',
    sort_no        decimal(4) default 50 null comment '排序',
    status         int        default 1  null comment '状态',
    created_by     bigint                null comment '创建人',
    created_time   datetime              null comment '创建时间',
    updated_by     bigint                null comment '更新人',
    updated_time   datetime              null comment '更新时间',
    del_flag       decimal(4) default 1  null comment '删除标记'
)
    comment '字典数据表';

create table if not exists mortise_dict_type
(
    id           bigint auto_increment comment '主键'
        primary key,
    type_name    varchar(64)           not null comment '名称',
    type_code    varchar(64)           not null comment '代码',
    sort_no      decimal(4) default 50 null comment '排序',
    status       int        default 1  null comment '状态',
    created_by   bigint                null comment '创建人',
    created_time datetime              null comment '创建时间',
    updated_by   bigint                null comment '更新人',
    updated_time datetime              null comment '更新时间',
    del_flag     decimal(4) default 1  null comment '删除标记'
)
    comment '字典类别表';

create table if not exists mortise_menu
(
    id           bigint auto_increment comment '主键'
        primary key,
    label        varchar(64)                          not null comment '名称',
    permission   varchar(128)                         null comment '权限',
    icon         varchar(128)                         null comment '图标',
    href         varchar(128)                         not null comment '链接',
    status       decimal(1) default 1                 not null comment '状态',
    created_time datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    updated_time datetime   default CURRENT_TIMESTAMP null comment '更新时间',
    parent_id    bigint                               null comment '父 ID',
    menu_type    decimal(1) default 0                 not null comment '菜单类型(0: 菜单, 1: 按钮)',
    sort_no      decimal(4) default 50                not null comment '排序',
    del_flag     decimal(4) default 1                 null comment '删除标记'
)
    comment '菜单表';

create table if not exists mortise_operate_log
(
    id             bigint auto_increment comment '主键'
        primary key,
    trace_id       varchar(64)                null comment '链路追踪 ID',
    operator       varchar(64)                null comment '用户',
    type           varchar(50)                null comment '模块',
    sub_type       varchar(50)                null comment '操作分类',
    content        varchar(2000)              null comment '操作内容',
    extra          varchar(512)               null comment '扩展字段',
    request_method varchar(16)                null comment '请求方法名',
    request_url    varchar(512)               null comment '请求地址',
    java_method    varchar(512)               null comment 'java方法名',
    user_agent     varchar(200)               null comment '浏览器ua',
    created_time   datetime   default (now()) null comment '创建时间',
    tenant         varchar(512)               null comment '租户编号',
    biz_no         varchar(512)               null comment '业务编号',
    user_ip        varchar(128)               null comment '用户 IP',
    fail           tinyint                    null comment '成功 or 失败',
    del_flag       decimal(4) default 1       null comment '删除标记'
)
    comment '操作日志表';

create index mortise_operate_log_search_index
    on mortise_operate_log (type asc, sub_type asc, created_time desc);

create table if not exists mortise_role
(
    id           bigint auto_increment comment '主键'
        primary key,
    label        varchar(128)               not null comment '名称',
    permission   varchar(128)               not null comment '权限',
    status       int        default 1       null comment '状态',
    created_time datetime   default (now()) null comment '创建时间',
    updated_time datetime   default (now()) null comment '更新时间',
    del_flag     decimal(4) default 1       null comment '删除标记',
    constraint mortise_role_id_uindex
        unique (id),
    constraint mortise_role_permission_uindex
        unique (permission)
)
    comment '角色表';

create table if not exists mortise_role_menu
(
    id_mortise_role bigint not null comment '角色表主键',
    id_mortise_menu bigint not null comment '菜单表主键',
    constraint mortise_role_menu_uindex
        unique (id_mortise_role, id_mortise_menu)
)
    comment '角色菜单表';

create table if not exists mortise_user
(
    id               bigint auto_increment comment '主键'
        primary key,
    account          varchar(32)                not null comment '账号',
    password         varchar(64)                null comment '密码',
    nickname         varchar(128)               null comment '昵称',
    avatar           varchar(512)               null comment '头像',
    email            varchar(64)                null comment '邮箱',
    phone            varchar(11)                null comment '手机号',
    status           int        default 1       null comment '状态',
    created_time     datetime   default (now()) null comment '注册时间',
    last_login_time  datetime                   null comment '最后登录时间',
    real_name        varchar(128)               null comment '真实姓名',
    last_online_time datetime                   null comment '最后在线时间',
    del_flag         decimal(4) default 1       null comment '删除标记',
    constraint mortise_user_id_uindex
        unique (id)
)
    comment '系统用户表';

create table if not exists mortise_user_role
(
    id_mortise_user bigint not null comment '用户表主键',
    id_mortise_role bigint not null comment '角色表主键',
    constraint mortise_user_role_id_mortise_user_id_mortise_role_uindex
        unique (id_mortise_user, id_mortise_role)
)
    comment '用户权限表';


# 菜单表初始数据
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, created_time, updated_time, parent_id, menu_type, sort_no) VALUES (1, 'Home', 'home', 'i-heroicons-home', '/', 1, '2024-05-04 13:22:48', '2024-05-04 05:22:48', 0, 0, 1);
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, created_time, updated_time, parent_id, menu_type, sort_no) VALUES (2, 'Inbox', 'inbox', 'i-heroicons-inbox', '/inbox', 1, '2024-05-04 13:22:48', '2024-05-04 05:22:48', 0, 0, 20);
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, created_time, updated_time, parent_id, menu_type, sort_no) VALUES (3, 'Users', 'users', 'i-heroicons-user-group', '/users', 1, '2024-05-04 13:22:48', '2024-05-04 05:22:48', 0, 0, 30);
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, created_time, updated_time, parent_id, menu_type, sort_no) VALUES (4, 'Roles', 'roles', 'i-heroicons-identification', '/roles', 1, '2024-05-04 13:22:48', '2024-05-04 05:22:48', 0, 0, 40);
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, created_time, updated_time, parent_id, menu_type, sort_no) VALUES (5, 'Menus', 'menus', 'i-heroicons-list-bullet', '/menus', 1, '2024-05-04 13:22:48', '2024-05-04 05:22:48', 0, 0, 50);
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, created_time, updated_time, parent_id, menu_type, sort_no) VALUES (6, 'Settings', 'settings', 'i-heroicons-cog-8-tooth', '/settings', 1, '2024-05-04 13:22:48', '2024-05-04 05:22:48', 0, 0, 60);
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, created_time, updated_time, parent_id, menu_type, sort_no) VALUES (7, 'General', 'general', null, '/settings', 1, '2024-05-04 13:26:45', '2024-05-04 05:26:46', 6, 0, 10);
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, created_time, updated_time, parent_id, menu_type, sort_no) VALUES (8, 'Members', 'members', null, '/settings/members', 1, '2024-05-04 13:26:46', '2024-05-04 05:26:46', 6, 0, 20);
INSERT INTO mortise.mortise_menu (id, label, permission, icon, href, status, created_time, updated_time, parent_id, menu_type, sort_no) VALUES (9, 'Notifications', 'notifications', null, '/settings/notifications', 1, '2024-05-04 13:26:46', '2024-05-04 05:26:46', 6, 0, 30);
# 角色表初始数据
INSERT INTO mortise.mortise_role (id, label, permission, status, created_time, updated_time) VALUES (1, '管理员', 'admin', 1, '2024-04-18 01:46:55', '2024-04-18 01:46:55');
INSERT INTO mortise.mortise_role (id, label, permission, status, created_time, updated_time) VALUES (2, '普通用户', 'user', 1, '2024-04-18 01:46:55', '2024-04-18 01:46:55');
INSERT INTO mortise.mortise_role (id, label, permission, status, created_time, updated_time) VALUES (3, '会员用户', 'member', 1, '2024-04-18 01:46:56', '2024-04-18 01:46:56');
# 角色菜单表初始数据
INSERT INTO mortise.mortise_role_menu (id_mortise_role, id_mortise_menu) VALUES (1, 1);
INSERT INTO mortise.mortise_role_menu (id_mortise_role, id_mortise_menu) VALUES (1, 2);
INSERT INTO mortise.mortise_role_menu (id_mortise_role, id_mortise_menu) VALUES (1, 3);
INSERT INTO mortise.mortise_role_menu (id_mortise_role, id_mortise_menu) VALUES (1, 4);
INSERT INTO mortise.mortise_role_menu (id_mortise_role, id_mortise_menu) VALUES (1, 5);
INSERT INTO mortise.mortise_role_menu (id_mortise_role, id_mortise_menu) VALUES (1, 6);
INSERT INTO mortise.mortise_role_menu (id_mortise_role, id_mortise_menu) VALUES (1, 7);
INSERT INTO mortise.mortise_role_menu (id_mortise_role, id_mortise_menu) VALUES (1, 8);
INSERT INTO mortise.mortise_role_menu (id_mortise_role, id_mortise_menu) VALUES (1, 9);
INSERT INTO mortise.mortise_role_menu (id_mortise_role, id_mortise_menu) VALUES (2, 1);
INSERT INTO mortise.mortise_role_menu (id_mortise_role, id_mortise_menu) VALUES (2, 2);
INSERT INTO mortise.mortise_role_menu (id_mortise_role, id_mortise_menu) VALUES (2, 6);
INSERT INTO mortise.mortise_role_menu (id_mortise_role, id_mortise_menu) VALUES (2, 7);
INSERT INTO mortise.mortise_role_menu (id_mortise_role, id_mortise_menu) VALUES (2, 8);
INSERT INTO mortise.mortise_role_menu (id_mortise_role, id_mortise_menu) VALUES (2, 9);
INSERT INTO mortise.mortise_role_menu (id_mortise_role, id_mortise_menu) VALUES (3, 1);
INSERT INTO mortise.mortise_role_menu (id_mortise_role, id_mortise_menu) VALUES (3, 2);
INSERT INTO mortise.mortise_role_menu (id_mortise_role, id_mortise_menu) VALUES (3, 6);
INSERT INTO mortise.mortise_role_menu (id_mortise_role, id_mortise_menu) VALUES (3, 7);
INSERT INTO mortise.mortise_role_menu (id_mortise_role, id_mortise_menu) VALUES (3, 8);
INSERT INTO mortise.mortise_role_menu (id_mortise_role, id_mortise_menu) VALUES (3, 9);
# 用户表初始数据
INSERT INTO mortise.mortise_user (id, account, password, nickname, avatar, email, phone, status, created_time, last_login_time, real_name, last_online_time) VALUES (1, '1411780001', '7d392ab4ab81547b860c4f87a4854503f6e2148a8376b0f3254e9473', 'admin', 'https://static.rymcu.com/article/1578475481946.png', 'admin@x.com', null, 1, '2024-04-18 01:48:08', '2024-05-06 06:50:37', null, '2024-05-06 07:20:16');
# 用户角色表初始数据
INSERT INTO mortise.mortise_user_role (id_mortise_user, id_mortise_role) VALUES (1, 1);

