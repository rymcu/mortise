create table mortise_user
(
    id               bigserial
        constraint mortise_user_pk
            primary key,
    account          varchar(32),
    password         varchar(64),
    nickname         varchar(128),
    avatar           varchar(512),
    email            varchar(64),
    phone            varchar(11),
    status           smallint  default 1,
    created_time     timestamp default now(),
    last_login_time  timestamp,
    real_name        varchar(128),
    last_online_time timestamp,
    del_flag         smallint  default 1
);

comment on table mortise_user is '用户表';

comment on constraint mortise_user_pk on mortise_user is '用户表主键';

comment on column mortise_user.account is '账号';

comment on column mortise_user.password is '密码';

comment on column mortise_user.nickname is '昵称';

comment on column mortise_user.avatar is '头像';

comment on column mortise_user.email is '邮箱';

comment on column mortise_user.phone is '手机号';

comment on column mortise_user.status is '状态';

comment on column mortise_user.created_time is '注册时间';

comment on column mortise_user.last_login_time is '最后登录时间';

comment on column mortise_user.real_name is '真实姓名';

comment on column mortise_user.last_online_time is '最后在线时间';

comment on column mortise_user.del_flag is '删除标记';


create unique index mortise_user_account_uindex
    on mortise_user (account);

comment on index mortise_user_account_uindex is '用户表-账号唯一索引';

create unique index mortise_user_email_uindex
    on mortise_user (email);

comment on index mortise_user_email_uindex is '用户表-邮箱唯一索引';

create unique index mortise_user_phone_uindex
    on mortise_user (phone);

comment on index mortise_user_phone_uindex is '用户表-手机号唯一索引';

create table mortise_role
(
    id           bigserial
        constraint mortise_role_pk
            primary key,
    label        varchar(128) not null,
    permission   varchar(128) not null,
    status       smallint  default 1,
    created_time timestamp default now(),
    updated_time timestamp default now(),
    del_flag     smallint  default 1
);

comment on table mortise_role is '角色表';

comment on column mortise_role.id is '主键';

comment on constraint mortise_role_pk on mortise_role is '角色表主键';

comment on column mortise_role.label is '名称';

comment on column mortise_role.permission is '权限';

comment on column mortise_role.status is '状态';

comment on column mortise_role.created_time is '创建时间';

comment on column mortise_role.updated_time is '更新时间';

comment on column mortise_role.del_flag is '删除标记';

create unique index mortise_role_permission_uindex
    on mortise_role (permission);

comment on index mortise_role_permission_uindex is '角色表权限索引';

create table mortise_user_role
(
    id_mortise_user bigint not null,
    id_mortise_role bigint not null
);

comment on table mortise_user_role is '用户权限表';

comment on column mortise_user_role.id_mortise_user is '用户表主键';

comment on column mortise_user_role.id_mortise_role is '角色表主键';


create unique index mortise_user_role_id_mortise_user_id_mortise_role_uindex
    on mortise_user_role (id_mortise_user, id_mortise_role);

comment on index mortise_user_role_id_mortise_user_id_mortise_role_uindex is '用户权限表主键索引';

create table mortise_menu
(
    id           bigserial
        constraint mortise_menu_pk
            primary key,
    label        varchar(128) not null,
    permission   varchar(128),
    icon         varchar(128),
    href         varchar(128),
    menu_type    smallint default 0,
    parent_id    bigint   default 0,
    sort_no      smallint default 50,
    status       smallint default 1,
    created_time timestamp,
    updated_time timestamp,
    del_flag     smallint default 1,
    created_by   bigint,
    updated_by   bigint
);

comment on table mortise_menu is '菜单表';

comment on column mortise_menu.id is '主键';

comment on constraint mortise_menu_pk on mortise_menu is '菜单表主键';

comment on column mortise_menu.label is '名称';

comment on column mortise_menu.permission is '权限';

comment on column mortise_menu.icon is '图标';

comment on column mortise_menu.href is '链接';

comment on column mortise_menu.menu_type is '菜单类型（0: 菜单，1: 按钮）';

comment on column mortise_menu.parent_id is '父 ID';

comment on column mortise_menu.sort_no is '排序';

comment on column mortise_menu.status is '状态';

comment on column mortise_menu.created_time is '创建时间';

comment on column mortise_menu.updated_time is '修改时间';

comment on column mortise_menu.del_flag is '删除标记';

comment on column mortise_menu.created_by is '创建人';

comment on column mortise_menu.updated_by is '更新人';

create table mortise_role_menu
(
    id_mortise_role bigint not null,
    id_mortise_menu bigint not null
);

comment on table mortise_role_menu is '角色菜单表';

comment on column mortise_role_menu.id_mortise_role is '角色表主键';

comment on column mortise_role_menu.id_mortise_menu is '菜单表主键';

create unique index mortise_role_menu_uindex
    on mortise_role_menu (id_mortise_role, id_mortise_menu);

comment on index mortise_role_menu_uindex is '角色菜单表主键';

create table mortise_dict_type
(
    id           bigserial
        constraint mortise_dict_type_pk
            primary key,
    label        varchar(128) not null,
    type_code    varchar(64),
    sort_no      smallint  default 50,
    status       smallint  default 1,
    created_by   bigint,
    created_time timestamp default now(),
    updated_by   bigint,
    updated_time timestamp default now(),
    del_flag     smallint  default 1
);

comment on table mortise_dict_type is '字典类别表';

comment on column mortise_dict_type.id is '主键';

comment on constraint mortise_dict_type_pk on mortise_dict_type is '字典类别表主键';

comment on column mortise_dict_type.label is '名称';

comment on column mortise_dict_type.type_code is '代码';

comment on column mortise_dict_type.sort_no is '排序';

comment on column mortise_dict_type.status is '状态';

comment on column mortise_dict_type.created_by is '创建人';

comment on column mortise_dict_type.created_time is '创建时间';

comment on column mortise_dict_type.updated_by is '更新人';

comment on column mortise_dict_type.updated_time is '更新时间';

comment on column mortise_dict_type.del_flag is '删除标记';

create index mortise_dict_type_code_uindex
    on mortise_dict_type (type_code);

comment on index mortise_dict_type_code_uindex is '字典类别表类别代码索引';

create table mortise_dict
(
    id             bigserial
        constraint mortise_dict_pk
            primary key,
    label          varchar(128) not null,
    value          varchar(256),
    dict_type_code varchar(128) not null,
    sort_no        smallint  default 50,
    status         smallint  default 1,
    created_by     bigint,
    created_time   timestamp default now(),
    updated_by     bigint,
    updated_time   timestamp default now(),
    del_flag       smallint  default 1
);

comment on table mortise_dict is '字典数据表';

comment on column mortise_dict.id is '主键';

comment on constraint mortise_dict_pk on mortise_dict is '字典数据表主键';

comment on column mortise_dict.label is '名称';

comment on column mortise_dict.value is '数据';

comment on column mortise_dict.dict_type_code is '类别代码';

comment on column mortise_dict.sort_no is '排序';

comment on column mortise_dict.status is '状态';

comment on column mortise_dict.created_by is '创建人';

comment on column mortise_dict.created_time is '创建时间';

comment on column mortise_dict.updated_by is '更新人';

comment on column mortise_dict.updated_time is '更新时间';

comment on column mortise_dict.del_flag is '删除标记';

create table mortise_operate_log
(
    id             bigserial
        constraint mortise_operate_log_pk
            primary key,
    trace_id       varchar(64),
    operator       varchar(64),
    type           varchar(64),
    sub_type       varchar(64),
    content        text,
    extra          varchar(512),
    request_method varchar(16),
    request_url    varchar(512),
    java_method    varchar(512),
    user_agent     varchar(256),
    created_time   timestamp default now(),
    tenant         varchar(512),
    biz_no         varchar(512),
    user_ip        inet,
    fail           smallint  default 0,
    del_flag       smallint  default 1
);

comment on table mortise_operate_log is '操作日志表';

comment on column mortise_operate_log.id is '主键';

comment on constraint mortise_operate_log_pk on mortise_operate_log is '操作日志表主键';

comment on column mortise_operate_log.trace_id is '链路追踪 ID';

comment on column mortise_operate_log.operator is '操作用户';

comment on column mortise_operate_log.type is '模块';

comment on column mortise_operate_log.sub_type is '操作类型';

comment on column mortise_operate_log.content is '操作内容';

comment on column mortise_operate_log.extra is '扩展字段';

comment on column mortise_operate_log.request_method is '请求方法名';

comment on column mortise_operate_log.request_url is '请求地址';

comment on column mortise_operate_log.java_method is 'java 方法名';

comment on column mortise_operate_log.user_agent is '浏览器 ua';

comment on column mortise_operate_log.created_time is '创建时间';

comment on column mortise_operate_log.tenant is '租户编号';

comment on column mortise_operate_log.biz_no is '业务编号';

comment on column mortise_operate_log.user_ip is '用户 IP';

comment on column mortise_operate_log.fail is '成功 or 失败';

comment on column mortise_operate_log.del_flag is '删除标记';

create index mortise_operate_log_search_index
    on mortise_operate_log (type, sub_type, created_time);

comment on index mortise_operate_log_search_index is '操作日志表搜索索引';

create table mortise_file_detail
(
    id               bigserial
        primary key,
    url               varchar(512),
    size              bigint,
    filename          varchar(256),
    original_filename varchar(256),
    base_path         varchar(256),
    path              varchar(256),
    ext               varchar(32),
    content_type      varchar(128),
    platform          varchar(32),
    th_url            varchar(512),
    th_filename       varchar(256),
    th_size           bigint,
    th_content_type   varchar(128),
    object_id         varchar(32),
    object_type       varchar(32),
    metadata          text,
    user_metadata     text,
    th_metadata       text,
    th_user_metadata  text,
    attr              text,
    file_acl          varchar(32),
    th_file_acl       varchar(32),
    upload_id         varchar(128),
    upload_status     smallint,
    created_time      timestamp,
    hash_info         text
);

comment on column mortise_file_detail.url is '文件访问地址';

comment on column mortise_file_detail.size is '文件大小,单位字节';

comment on column mortise_file_detail.filename is '文件名称';

comment on column mortise_file_detail.original_filename is '原始文件名';

comment on column mortise_file_detail.base_path is '基础存储路径';

comment on column mortise_file_detail.path is '存储路径';

comment on column mortise_file_detail.ext is '文件扩展名';

comment on column mortise_file_detail.content_type is 'MIME类型';

comment on column mortise_file_detail.platform is '存储平台';

comment on column mortise_file_detail.th_url is '缩略图访问路径';

comment on column mortise_file_detail.th_filename is '缩略图名称';

comment on column mortise_file_detail.th_size is '缩略图大小，单位字节';

comment on column mortise_file_detail.th_content_type is '缩略图MIME类型';

comment on column mortise_file_detail.object_id is '文件所属对象id';

comment on column mortise_file_detail.object_type is '文件所属对象类型，例如用户头像，评价图片';

comment on column mortise_file_detail.metadata is '文件元数据';

comment on column mortise_file_detail.user_metadata is '文件用户元数据';

comment on column mortise_file_detail.th_metadata is '缩略图元数据';

comment on column mortise_file_detail.th_user_metadata is '缩略图用户元数据';

comment on column mortise_file_detail.attr is '附加属性';

comment on column mortise_file_detail.file_acl is '文件ACL';

comment on column mortise_file_detail.th_file_acl is '缩略图文件ACL';

comment on column mortise_file_detail.upload_id is '上传ID，仅在手动分片上传时使用';

comment on column mortise_file_detail.upload_status is '上传状态，仅在手动分片上传时使用，1：初始化完成，2：上传完成';

comment on column mortise_file_detail.created_time is '时间';

comment on column mortise_file_detail.hash_info is '哈希信息';

create table mortise_file_part_detail
(
    id           bigserial
        primary key,
    platform     varchar(32),
    upload_id    bigint,
    e_tag        varchar(256),
    part_number  integer,
    part_size    bigint,
    hash_info    text,
    created_time timestamp default now()
);

comment on table mortise_file_part_detail is '文件分片上传记录表';

comment on column mortise_file_part_detail.id is '主键';

comment on column mortise_file_part_detail.platform is '平台';

comment on column mortise_file_part_detail.upload_id is '上传 ID';

comment on column mortise_file_part_detail.e_tag is '标签';

comment on column mortise_file_part_detail.part_number is '分片数量';

comment on column mortise_file_part_detail.part_size is '分片大小';

comment on column mortise_file_part_detail.hash_info is '哈希信息';

comment on column mortise_file_part_detail.created_time is '创建时间';


