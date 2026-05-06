-- ============================================================
-- 外综服平台 (Foreign Trade Comprehensive Service Platform)
-- Database Initialization Script
-- ============================================================

CREATE DATABASE IF NOT EXISTS trade_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE trade_platform;

-- ============================================================
-- 1. sys_tenant - 租户/企业表
-- ============================================================
CREATE TABLE IF NOT EXISTS sys_tenant (
    id             BIGINT       AUTO_INCREMENT PRIMARY KEY,
    tenant_code    VARCHAR(50)  NOT NULL COMMENT '企业号，用于登录路由',
    name           VARCHAR(200) NOT NULL COMMENT '企业名称',
    credit_code    VARCHAR(50)           COMMENT '统一社会信用代码',
    status         TINYINT      DEFAULT 1 COMMENT '1=正常, 0=禁用',
    contact_person VARCHAR(50)           COMMENT '联系人',
    contact_phone  VARCHAR(20)           COMMENT '联系电话',
    address        VARCHAR(500)          COMMENT '地址',
    deleted        TINYINT      DEFAULT 0,
    create_time    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tenant_code (tenant_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户/企业表';

-- ============================================================
-- 2. sys_user - 用户表
-- ============================================================
CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL COMMENT '用户名',
    password    VARCHAR(200) NOT NULL COMMENT '密码（BCrypt加密）',
    real_name   VARCHAR(50)           COMMENT '真实姓名',
    phone       VARCHAR(20)           COMMENT '手机号',
    email       VARCHAR(100)          COMMENT '邮箱',
    role        VARCHAR(20)  NOT NULL COMMENT '角色: ADMIN, GUOHE, ENTERPRISE',
    tenant_id   BIGINT                COMMENT '租户ID，ADMIN可为空',
    status      TINYINT      DEFAULT 1 COMMENT '1=启用, 0=禁用',
    deleted     TINYINT      DEFAULT 0,
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username),
    KEY idx_user_tenant_id (tenant_id),
    KEY idx_user_status (status),
    KEY idx_user_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================================
-- 3. sys_user_apply - 用户注册申请表
-- ============================================================
CREATE TABLE IF NOT EXISTS sys_user_apply (
    id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL COMMENT '用户名',
    password      VARCHAR(200) NOT NULL COMMENT '密码（BCrypt加密）',
    real_name     VARCHAR(50)           COMMENT '真实姓名',
    phone         VARCHAR(20)           COMMENT '手机号',
    email         VARCHAR(100)          COMMENT '邮箱',
    role          VARCHAR(20)  NOT NULL COMMENT '角色',
    tenant_id     BIGINT                COMMENT '租户ID',
    status        VARCHAR(20)  DEFAULT 'PENDING' COMMENT 'PENDING, APPROVED, REJECTED',
    reject_reason VARCHAR(500)          COMMENT '拒绝原因',
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_apply_status (status),
    KEY idx_apply_tenant_id (tenant_id),
    KEY idx_apply_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户注册申请表';

-- ============================================================
-- 4. sys_config_item - 配置项表
-- ============================================================
CREATE TABLE IF NOT EXISTS sys_config_item (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(50)  NOT NULL COMMENT '配置项编码',
    name        VARCHAR(100) NOT NULL COMMENT '配置项名称',
    status      TINYINT      DEFAULT 1,
    deleted     TINYINT      DEFAULT 0,
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_config_item_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配置项表';

-- ============================================================
-- 5. sys_config_value - 配置值表
-- ============================================================
CREATE TABLE IF NOT EXISTS sys_config_value (
    id             BIGINT       AUTO_INCREMENT PRIMARY KEY,
    config_item_id BIGINT       NOT NULL COMMENT '配置项ID',
    code           VARCHAR(50)  NOT NULL COMMENT '配置值编码',
    name           VARCHAR(200) NOT NULL COMMENT '配置值名称',
    status         TINYINT      DEFAULT 1,
    deleted        TINYINT      DEFAULT 0,
    create_time    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_config_value_item_id (config_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配置值表';

-- ============================================================
-- 6. biz_template - 单据模板表
-- ============================================================
CREATE TABLE IF NOT EXISTS biz_template (
    id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    type         VARCHAR(20)  NOT NULL COMMENT '模板类型: CONTRACT, ORDER, INVOICE, PACKING, CUSTOMS',
    name         VARCHAR(200) NOT NULL COMMENT '模板名称',
    description  VARCHAR(500)          COMMENT '模板描述',
    html_content LONGTEXT              COMMENT 'HTML模板内容',
    deleted      TINYINT      DEFAULT 0,
    create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_template_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='单据模板表';

-- ============================================================
-- 7. biz_contract - 合同表
-- ============================================================
CREATE TABLE IF NOT EXISTS biz_contract (
    id           BIGINT        AUTO_INCREMENT PRIMARY KEY,
    tenant_id    BIGINT        NOT NULL COMMENT '租户ID',
    contract_no  VARCHAR(50)   NOT NULL COMMENT '合同编号',
    title        VARCHAR(200)           COMMENT '合同标题',
    our_company  VARCHAR(200)           COMMENT '本方企业名称',
    partner_id   BIGINT                 COMMENT '关联供应商/客户ID',
    partner_type VARCHAR(20)            COMMENT 'SUPPLIER, CUSTOMER',
    partner_name VARCHAR(200)           COMMENT '对方企业名称',
    status       VARCHAR(20)   DEFAULT 'INIT' COMMENT 'INIT, SIGNING, EFFECTIVE, EXECUTING, COMPLETED, EXPIRED, DESTROYED',
    sign_date    DATE                   COMMENT '签订日期',
    expire_date  DATE                   COMMENT '到期日期',
    amount       DECIMAL(18,2)          COMMENT '合同金额',
    currency     VARCHAR(10)   DEFAULT 'CNY' COMMENT '币种',
    terms        TEXT                   COMMENT '合同条款',
    remark       VARCHAR(500)           COMMENT '备注',
    deleted      TINYINT       DEFAULT 0,
    create_time  DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_contract_tenant_id (tenant_id),
    KEY idx_contract_status (status),
    KEY idx_contract_contract_no (contract_no),
    KEY idx_contract_create_time (create_time),
    KEY idx_contract_partner_id (partner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='合同表';

-- ============================================================
-- 8. biz_goods - 商品表
-- ============================================================
CREATE TABLE IF NOT EXISTS biz_goods (
    id          BIGINT        AUTO_INCREMENT PRIMARY KEY,
    tenant_id   BIGINT        NOT NULL COMMENT '租户ID',
    goods_no    VARCHAR(50)            COMMENT '商品编号',
    name        VARCHAR(200)  NOT NULL COMMENT '商品名称',
    hs_code     VARCHAR(20)            COMMENT 'HS编码',
    spec        VARCHAR(200)           COMMENT '规格',
    model       VARCHAR(200)           COMMENT '型号',
    unit        VARCHAR(20)            COMMENT '计量单位',
    price       DECIMAL(18,4)          COMMENT '单价',
    currency    VARCHAR(10)   DEFAULT 'CNY' COMMENT '币种',
    image_url   VARCHAR(500)           COMMENT '图片地址',
    category    VARCHAR(50)            COMMENT '分类',
    deleted     TINYINT       DEFAULT 0,
    create_time DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_goods_tenant_id (tenant_id),
    KEY idx_goods_hs_code (hs_code),
    KEY idx_goods_goods_no (goods_no),
    KEY idx_goods_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- ============================================================
-- 9. biz_order - 订单表
-- ============================================================
CREATE TABLE IF NOT EXISTS biz_order (
    id             BIGINT        AUTO_INCREMENT PRIMARY KEY,
    tenant_id      BIGINT        NOT NULL COMMENT '租户ID',
    order_no       VARCHAR(50)   NOT NULL COMMENT '订单编号',
    contract_id    BIGINT        NOT NULL COMMENT '关联合同ID',
    trade_terms    VARCHAR(50)            COMMENT '贸易条款，如FOB, CIF',
    payment_method VARCHAR(50)            COMMENT '付款方式',
    status         VARCHAR(20)   DEFAULT 'DRAFT' COMMENT 'DRAFT, SUBMITTED, PROCESSING, COMPLETED, CANCELLED',
    total_amount   DECIMAL(18,2)          COMMENT '总金额',
    currency       VARCHAR(10)   DEFAULT 'CNY' COMMENT '币种',
    remark         VARCHAR(500)           COMMENT '备注',
    deleted        TINYINT       DEFAULT 0,
    create_time    DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_order_tenant_id (tenant_id),
    KEY idx_order_order_no (order_no),
    KEY idx_order_contract_id (contract_id),
    KEY idx_order_status (status),
    KEY idx_order_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- ============================================================
-- 10. biz_order_goods - 订单商品关联表
-- ============================================================
CREATE TABLE IF NOT EXISTS biz_order_goods (
    id         BIGINT        AUTO_INCREMENT PRIMARY KEY,
    order_id   BIGINT        NOT NULL COMMENT '订单ID',
    goods_id   BIGINT        NOT NULL COMMENT '商品ID',
    goods_name VARCHAR(200)           COMMENT '商品名称',
    goods_no   VARCHAR(50)            COMMENT '商品编号',
    hs_code    VARCHAR(20)            COMMENT 'HS编码',
    quantity   DECIMAL(18,4) NOT NULL COMMENT '数量',
    unit       VARCHAR(20)            COMMENT '计量单位',
    price      DECIMAL(18,4) NOT NULL COMMENT '单价',
    amount     DECIMAL(18,2)          COMMENT '金额',
    KEY idx_order_goods_order_id (order_id),
    KEY idx_order_goods_goods_id (goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单商品关联表';

-- ============================================================
-- 11. biz_customs_declaration - 报关单表
-- ============================================================
CREATE TABLE IF NOT EXISTS biz_customs_declaration (
    id              BIGINT        AUTO_INCREMENT PRIMARY KEY,
    tenant_id       BIGINT        NOT NULL COMMENT '租户ID',
    declaration_no  VARCHAR(50)            COMMENT '报关单号',
    ie_type         VARCHAR(10)            COMMENT '进出口类型: I=进口, E=出口',
    ie_date         DATE                   COMMENT '进出境日期',
    transport_mode  VARCHAR(50)            COMMENT '运输方式',
    trade_mode      VARCHAR(50)            COMMENT '贸易方式',
    customs_code    VARCHAR(50)            COMMENT '海关编码',
    consignee       VARCHAR(200)           COMMENT '收货人',
    consigner       VARCHAR(200)           COMMENT '发货人',
    status          VARCHAR(20)            COMMENT '申报状态',
    total_amount    DECIMAL(18,2)          COMMENT '总金额',
    currency        VARCHAR(10)            COMMENT '币种',
    remark          VARCHAR(500)           COMMENT '备注',
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    KEY idx_customs_tenant_id (tenant_id),
    KEY idx_customs_declaration_no (declaration_no),
    KEY idx_customs_status (status),
    KEY idx_customs_ie_type (ie_type),
    KEY idx_customs_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报关单表';

-- ============================================================
-- 12. biz_customs_goods - 报关单商品表
-- ============================================================
CREATE TABLE IF NOT EXISTS biz_customs_goods (
    id              BIGINT        AUTO_INCREMENT PRIMARY KEY,
    declaration_id  BIGINT        NOT NULL COMMENT '报关单ID',
    goods_no        VARCHAR(50)            COMMENT '商品编号',
    name            VARCHAR(200)           COMMENT '商品名称',
    hs_code         VARCHAR(20)            COMMENT 'HS编码',
    spec            VARCHAR(200)           COMMENT '规格',
    quantity        DECIMAL(18,4)          COMMENT '数量',
    unit            VARCHAR(20)            COMMENT '计量单位',
    price           DECIMAL(18,4)          COMMENT '单价',
    amount          DECIMAL(18,2)          COMMENT '金额',
    origin_country  VARCHAR(50)            COMMENT '原产国',
    KEY idx_customs_goods_declaration_id (declaration_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报关单商品表';

-- ============================================================
-- 13. biz_file - 文件管理表
-- ============================================================
CREATE TABLE IF NOT EXISTS biz_file (
    id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
    tenant_id     BIGINT       NOT NULL COMMENT '租户ID',
    file_name     VARCHAR(200) NOT NULL COMMENT '存储文件名',
    original_name VARCHAR(200) NOT NULL COMMENT '原始文件名',
    file_path     VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_size     BIGINT                COMMENT '文件大小（字节）',
    file_type     VARCHAR(50)           COMMENT '文件类型',
    business_type VARCHAR(50)           COMMENT '业务类型: 企业资质/合同附件/报关单据',
    deleted       TINYINT      DEFAULT 0,
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    KEY idx_file_tenant_id (tenant_id),
    KEY idx_file_business_type (business_type),
    KEY idx_file_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件管理表';

-- ============================================================
-- 14. biz_partner - 供应商/客户表
-- ============================================================
CREATE TABLE IF NOT EXISTS biz_partner (
    id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
    tenant_id     BIGINT       NOT NULL COMMENT '租户ID',
    type          VARCHAR(20)  NOT NULL COMMENT 'SUPPLIER=供应商, CUSTOMER=客户',
    name          VARCHAR(200) NOT NULL COMMENT '企业名称',
    credit_code   VARCHAR(50)           COMMENT '统一社会信用代码',
    address       VARCHAR(500)          COMMENT '地址',
    province      VARCHAR(50)           COMMENT '省',
    city          VARCHAR(50)           COMMENT '市',
    district      VARCHAR(50)           COMMENT '区',
    contact_name  VARCHAR(50)           COMMENT '联系人',
    contact_phone VARCHAR(20)           COMMENT '联系电话',
    contact_email VARCHAR(100)          COMMENT '联系邮箱',
    status        TINYINT      DEFAULT 1 COMMENT '1=正常, 0=禁用',
    deleted       TINYINT      DEFAULT 0,
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_partner_tenant_id (tenant_id),
    KEY idx_partner_type (type),
    KEY idx_partner_status (status),
    KEY idx_partner_name (name(50))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='供应商/客户表';

-- ============================================================
-- 初始化数据
-- ============================================================

-- 默认租户：杨凌国合跨境贸易有限公司
INSERT INTO sys_tenant (tenant_code, name, credit_code, status) VALUES ('GUOHE', '杨凌国合跨境贸易有限公司', '91610000XXXXXXXXXX', 1);

-- 默认管理员用户 (密码: admin123)
INSERT INTO sys_user (username, password, real_name, role, tenant_id, status) VALUES ('admin', '$2a$10$vRuGcfYdzolQjrqCBxbSAei3oCVY5UtAp50CfFtie260O/ohtOOey', '系统管理员', 'ADMIN', 1, 1);
-- default password: admin123
