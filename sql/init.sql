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
    file_path    VARCHAR(500)          COMMENT '模板文件存储路径',
    file_name    VARCHAR(200)          COMMENT '模板文件名',
    file_size    BIGINT                COMMENT '模板文件大小(字节)',
    pdf_path     VARCHAR(500)          COMMENT 'PDF预览文件路径',
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
    ledger_id       BIGINT                 COMMENT '关联台账ID',
    split_seq       INT                    COMMENT '拆单序号',
    review_status   VARCHAR(20)   DEFAULT 'DRAFT' COMMENT '审核状态: DRAFT/SUBMITTED/REVIEWING/APPROVED/REJECTED/RELEASED',
    submit_time     DATETIME               COMMENT '提交时间',
    submit_by       BIGINT                 COMMENT '提交人ID',
    review_time     DATETIME               COMMENT '审核时间',
    review_by       BIGINT                 COMMENT '审核人ID',
    deleted         TINYINT       DEFAULT 0,
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_customs_tenant_id (tenant_id),
    KEY idx_customs_declaration_no (declaration_no),
    KEY idx_customs_status (status),
    KEY idx_customs_ie_type (ie_type),
    KEY idx_customs_create_time (create_time),
    KEY idx_customs_ledger_id (ledger_id),
    KEY idx_customs_review_status (review_status)
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
-- 15. biz_import_ledger - 进口台账主表
-- ============================================================
CREATE TABLE IF NOT EXISTS biz_import_ledger (
    id                BIGINT        AUTO_INCREMENT PRIMARY KEY,
    tenant_id         BIGINT        NOT NULL COMMENT '租户ID',
    ledger_no         VARCHAR(50)   NOT NULL COMMENT '台账编号',
    split_status      VARCHAR(20)            COMMENT '拆单状态: UNSPLIT/SPLITTING/SPLIT',
    doc_count         INT           DEFAULT 0 COMMENT '关联单据数',
    goods_count       INT           DEFAULT 0 COMMENT '商品数量',
    case_no           VARCHAR(100)           COMMENT '箱号/集装箱号',
    master_bl_no      VARCHAR(100)           COMMENT '主提单号',
    sub_bl_no         VARCHAR(100)           COMMENT '分提单号',
    supplier_id       BIGINT                 COMMENT '供应商ID',
    supplier_name     VARCHAR(200)           COMMENT '供应商名称',
    supervision_mode  VARCHAR(50)            COMMENT '监管方式',
    contract_no       VARCHAR(100)           COMMENT '合同号',
    contract_id       BIGINT                 COMMENT '合同ID',
    declare_customs   VARCHAR(50)            COMMENT '申报海关',
    entry_customs     VARCHAR(50)            COMMENT '入境海关',
    origin_country    VARCHAR(50)            COMMENT '原产国',
    transit_port      VARCHAR(100)           COMMENT '中转港',
    entry_port        VARCHAR(100)           COMMENT '入境口岸',
    transport_mode    VARCHAR(50)            COMMENT '运输方式',
    trade_mode        VARCHAR(50)            COMMENT '贸易方式',
    total_amount      DECIMAL(18,2)          COMMENT '总金额',
    currency          VARCHAR(10)   DEFAULT 'USD' COMMENT '币种',
    ie_date           DATE                   COMMENT '进出境日期',
    consignee         VARCHAR(200)           COMMENT '收货人',
    order_id          BIGINT                 COMMENT '关联订单ID',
    status            VARCHAR(20)   DEFAULT 'DRAFT' COMMENT '状态: DRAFT/SUBMITTED/COMPLETED',
    remark            VARCHAR(500)           COMMENT '备注',
    create_by         BIGINT                 COMMENT '创建人ID',
    deleted           TINYINT       DEFAULT 0,
    create_time       DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time       DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_ledger_tenant_id (tenant_id),
    KEY idx_ledger_no (ledger_no),
    KEY idx_ledger_status (status),
    KEY idx_ledger_supplier (supplier_id),
    KEY idx_ledger_contract (contract_id),
    KEY idx_ledger_order (order_id),
    KEY idx_ledger_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='进口台账主表';

-- ============================================================
-- 16. biz_import_ledger_goods - 进口台账商品明细
-- ============================================================
CREATE TABLE IF NOT EXISTS biz_import_ledger_goods (
    id              BIGINT        AUTO_INCREMENT PRIMARY KEY,
    ledger_id       BIGINT        NOT NULL COMMENT '台账ID',
    goods_no        VARCHAR(50)            COMMENT '商品序号',
    goods_id        BIGINT                 COMMENT '商品库ID',
    name            VARCHAR(200)           COMMENT '商品名称',
    hs_code         VARCHAR(20)            COMMENT 'HS编码',
    spec            VARCHAR(200)           COMMENT '规格型号',
    quantity        DECIMAL(18,4)          COMMENT '数量',
    unit            VARCHAR(20)            COMMENT '计量单位',
    price           DECIMAL(18,4)          COMMENT '单价',
    amount          DECIMAL(18,2)          COMMENT '金额',
    origin_country  VARCHAR(50)            COMMENT '原产国',
    currency        VARCHAR(10)            COMMENT '币种',
    assigned_qty    DECIMAL(18,4) DEFAULT 0 COMMENT '已分配数量(拆单用)',
    KEY idx_ledger_goods_ledger_id (ledger_id),
    KEY idx_ledger_goods_id (goods_id),
    KEY idx_ledger_goods_hs_code (hs_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='进口台账商品明细';

-- ============================================================
-- 17. biz_import_ledger_file - 进口台账-文件关联
-- ============================================================
CREATE TABLE IF NOT EXISTS biz_import_ledger_file (
    id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    ledger_id    BIGINT       NOT NULL COMMENT '台账ID',
    file_id      BIGINT       NOT NULL COMMENT '文件ID',
    file_type    VARCHAR(50)           COMMENT '文件分类: 合同/发票/箱单/提单/报关单 等',
    create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    KEY idx_ledger_file_ledger_id (ledger_id),
    KEY idx_ledger_file_file_id (file_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='进口台账-文件关联表';

-- ============================================================
-- 18. biz_customs_review - 报关单审核记录
-- ============================================================
CREATE TABLE IF NOT EXISTS biz_customs_review (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    declaration_id  BIGINT       NOT NULL COMMENT '报关单ID',
    action          VARCHAR(20)           COMMENT '操作类型: SUBMIT/APPROVE/REJECT/RELEASE/REVOKE',
    from_status     VARCHAR(20)           COMMENT '原状态',
    to_status       VARCHAR(20)           COMMENT '目标状态',
    operator_id     BIGINT                COMMENT '操作人ID',
    operator_name   VARCHAR(50)           COMMENT '操作人姓名',
    comment         VARCHAR(500)          COMMENT '审批意见',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    KEY idx_review_declaration_id (declaration_id),
    KEY idx_review_action (action),
    KEY idx_review_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报关单审核记录';

-- ============================================================
-- 初始化数据
-- ============================================================

-- 默认租户：杨凌国合跨境贸易有限公司
INSERT INTO sys_tenant (tenant_code, name, credit_code, status) VALUES ('GUOHE', '杨凌国合跨境贸易有限公司', '91610000XXXXXXXXXX', 1);

-- 默认管理员用户 (密码: admin123)
INSERT INTO sys_user (username, password, real_name, role, tenant_id, status) VALUES ('admin', '$2a$10$vRuGcfYdzolQjrqCBxbSAei3oCVY5UtAp50CfFtie260O/ohtOOey', '系统管理员', 'ADMIN', 1, 1);
-- default password: admin123

-- ============================================================
-- 基础配置数据
-- ============================================================

-- 国别/地区
INSERT INTO sys_config_item (code, name, status) VALUES ('COUNTRY', '国别/地区', 1);
INSERT INTO sys_config_value (config_item_id, code, name, status) VALUES
((SELECT id FROM sys_config_item WHERE code='COUNTRY'), 'CN', '中国', 1),
((SELECT id FROM sys_config_item WHERE code='COUNTRY'), 'US', '美国', 1),
((SELECT id FROM sys_config_item WHERE code='COUNTRY'), 'JP', '日本', 1),
((SELECT id FROM sys_config_item WHERE code='COUNTRY'), 'KR', '韩国', 1),
((SELECT id FROM sys_config_item WHERE code='COUNTRY'), 'DE', '德国', 1),
((SELECT id FROM sys_config_item WHERE code='COUNTRY'), 'GB', '英国', 1),
((SELECT id FROM sys_config_item WHERE code='COUNTRY'), 'FR', '法国', 1),
((SELECT id FROM sys_config_item WHERE code='COUNTRY'), 'AU', '澳大利亚', 1),
((SELECT id FROM sys_config_item WHERE code='COUNTRY'), 'CA', '加拿大', 1),
((SELECT id FROM sys_config_item WHERE code='COUNTRY'), 'RU', '俄罗斯', 1),
((SELECT id FROM sys_config_item WHERE code='COUNTRY'), 'TH', '泰国', 1),
((SELECT id FROM sys_config_item WHERE code='COUNTRY'), 'VN', '越南', 1),
((SELECT id FROM sys_config_item WHERE code='COUNTRY'), 'MY', '马来西亚', 1),
((SELECT id FROM sys_config_item WHERE code='COUNTRY'), 'SG', '新加坡', 1),
((SELECT id FROM sys_config_item WHERE code='COUNTRY'), 'ID', '印度尼西亚', 1),
((SELECT id FROM sys_config_item WHERE code='COUNTRY'), 'IN', '印度', 1),
((SELECT id FROM sys_config_item WHERE code='COUNTRY'), 'AE', '阿联酋', 1),
((SELECT id FROM sys_config_item WHERE code='COUNTRY'), 'SA', '沙特阿拉伯', 1),
((SELECT id FROM sys_config_item WHERE code='COUNTRY'), 'BR', '巴西', 1),
((SELECT id FROM sys_config_item WHERE code='COUNTRY'), 'ZA', '南非', 1);

-- 币种
INSERT INTO sys_config_item (code, name, status) VALUES ('CURRENCY', '币种', 1);
INSERT INTO sys_config_value (config_item_id, code, name, status) VALUES
((SELECT id FROM sys_config_item WHERE code='CURRENCY'), 'CNY', '人民币', 1),
((SELECT id FROM sys_config_item WHERE code='CURRENCY'), 'USD', '美元', 1),
((SELECT id FROM sys_config_item WHERE code='CURRENCY'), 'EUR', '欧元', 1),
((SELECT id FROM sys_config_item WHERE code='CURRENCY'), 'GBP', '英镑', 1),
((SELECT id FROM sys_config_item WHERE code='CURRENCY'), 'JPY', '日元', 1),
((SELECT id FROM sys_config_item WHERE code='CURRENCY'), 'KRW', '韩元', 1),
((SELECT id FROM sys_config_item WHERE code='CURRENCY'), 'HKD', '港币', 1),
((SELECT id FROM sys_config_item WHERE code='CURRENCY'), 'AUD', '澳元', 1),
((SELECT id FROM sys_config_item WHERE code='CURRENCY'), 'CAD', '加元', 1),
((SELECT id FROM sys_config_item WHERE code='CURRENCY'), 'RUB', '卢布', 1);

-- 运输方式
INSERT INTO sys_config_item (code, name, status) VALUES ('TRANSPORT_MODE', '运输方式', 1);
INSERT INTO sys_config_value (config_item_id, code, name, status) VALUES
((SELECT id FROM sys_config_item WHERE code='TRANSPORT_MODE'), 'SEA', '海运', 1),
((SELECT id FROM sys_config_item WHERE code='TRANSPORT_MODE'), 'AIR', '空运', 1),
((SELECT id FROM sys_config_item WHERE code='TRANSPORT_MODE'), 'RAIL', '铁路运输', 1),
((SELECT id FROM sys_config_item WHERE code='TRANSPORT_MODE'), 'ROAD', '公路运输', 1),
((SELECT id FROM sys_config_item WHERE code='TRANSPORT_MODE'), 'MULTI', '多式联运', 1),
((SELECT id FROM sys_config_item WHERE code='TRANSPORT_MODE'), 'POST', '邮政快递', 1);

-- 贸易方式
INSERT INTO sys_config_item (code, name, status) VALUES ('TRADE_MODE', '贸易方式', 1);
INSERT INTO sys_config_value (config_item_id, code, name, status) VALUES
((SELECT id FROM sys_config_item WHERE code='TRADE_MODE'), 'GENERAL', '一般贸易', 1),
((SELECT id FROM sys_config_item WHERE code='TRADE_MODE'), 'PROCESS', '来料加工', 1),
((SELECT id FROM sys_config_item WHERE code='TRADE_MODE'), 'PROCESS_SUPPLY', '进料加工', 1),
((SELECT id FROM sys_config_item WHERE code='TRADE_MODE'), 'BORDER', '边境小额贸易', 1),
((SELECT id FROM sys_config_item WHERE code='TRADE_MODE'), 'EQUIPMENT', '外商投资设备', 1),
((SELECT id FROM sys_config_item WHERE code='TRADE_MODE'), 'BONDED', '保税贸易', 1),
((SELECT id FROM sys_config_item WHERE code='TRADE_MODE'), 'OTHER', '其他贸易方式', 1);

-- 计量单位
INSERT INTO sys_config_item (code, name, status) VALUES ('UNIT', '计量单位', 1);
INSERT INTO sys_config_value (config_item_id, code, name, status) VALUES
((SELECT id FROM sys_config_item WHERE code='UNIT'), 'KG', '千克', 1),
((SELECT id FROM sys_config_item WHERE code='UNIT'), 'TON', '吨', 1),
((SELECT id FROM sys_config_item WHERE code='UNIT'), 'PCS', '个/件', 1),
((SELECT id FROM sys_config_item WHERE code='UNIT'), 'SET', '套', 1),
((SELECT id FROM sys_config_item WHERE code='UNIT'), 'BOX', '箱', 1),
((SELECT id FROM sys_config_item WHERE code='UNIT'), 'BAG', '袋', 1),
((SELECT id FROM sys_config_item WHERE code='UNIT'), 'PALLET', '托盘', 1),
((SELECT id FROM sys_config_item WHERE code='UNIT'), 'CONTAINER', '集装箱', 1),
((SELECT id FROM sys_config_item WHERE code='UNIT'), 'M', '米', 1),
((SELECT id FROM sys_config_item WHERE code='UNIT'), 'SQM', '平方米', 1),
((SELECT id FROM sys_config_item WHERE code='UNIT'), 'L', '升', 1);

-- 常用海关
INSERT INTO sys_config_item (code, name, status) VALUES ('CUSTOMS', '常用海关', 1);
INSERT INTO sys_config_value (config_item_id, code, name, status) VALUES
((SELECT id FROM sys_config_item WHERE code='CUSTOMS'), '7200', '西安海关', 1),
((SELECT id FROM sys_config_item WHERE code='CUSTOMS'), '7201', '咸阳海关', 1),
((SELECT id FROM sys_config_item WHERE code='CUSTOMS'), '2200', '青岛海关', 1),
((SELECT id FROM sys_config_item WHERE code='CUSTOMS'), '2300', '上海海关', 1),
((SELECT id FROM sys_config_item WHERE code='CUSTOMS'), '5100', '深圳海关', 1),
((SELECT id FROM sys_config_item WHERE code='CUSTOMS'), '4200', '宁波海关', 1),
((SELECT id FROM sys_config_item WHERE code='CUSTOMS'), '5000', '广州海关', 1),
((SELECT id FROM sys_config_item WHERE code='CUSTOMS'), '3100', '天津海关', 1),
((SELECT id FROM sys_config_item WHERE code='CUSTOMS'), '2900', '连云港海关', 1);

-- 业务文件类型
INSERT INTO sys_config_item (code, name, status) VALUES ('FILE_TYPE', '业务文件类型', 1);
INSERT INTO sys_config_value (config_item_id, code, name, status) VALUES
((SELECT id FROM sys_config_item WHERE code='FILE_TYPE'), 'CONTRACT', '合同', 1),
((SELECT id FROM sys_config_item WHERE code='FILE_TYPE'), 'INVOICE', '发票', 1),
((SELECT id FROM sys_config_item WHERE code='FILE_TYPE'), 'PACKING_LIST', '装箱单', 1),
((SELECT id FROM sys_config_item WHERE code='FILE_TYPE'), 'BL', '提单', 1),
((SELECT id FROM sys_config_item WHERE code='FILE_TYPE'), 'CUSTOMS_DECL', '报关单', 1),
((SELECT id FROM sys_config_item WHERE code='FILE_TYPE'), 'CERTIFICATE', '证书/证明', 1),
((SELECT id FROM sys_config_item WHERE code='FILE_TYPE'), 'OTHER', '其他', 1);

-- ============================================================
-- 15. biz_service_enterprise - 服务企业档案表
-- ============================================================
CREATE TABLE IF NOT EXISTS biz_service_enterprise (
    id                  BIGINT       AUTO_INCREMENT PRIMARY KEY,
    tenant_id           BIGINT       NOT NULL COMMENT '所属租户',
    enterprise_name     VARCHAR(200) NOT NULL COMMENT '企业名称',
    credit_code         VARCHAR(50)  NOT NULL COMMENT '统一社会信用代码',
    region              VARCHAR(100)          COMMENT '所属地区',
    industry            VARCHAR(100)          COMMENT '行业',
    product_type        VARCHAR(200)          COMMENT '主要产品类型',
    is_first_trade      TINYINT      DEFAULT 0 COMMENT '是否首次外贸企业 0=否 1=是',
    agreement_status    VARCHAR(20)  DEFAULT 'UNSIGNED' COMMENT '服务协议状态: UNSIGNED/SIGNED/EXPIRED',
    agreement_no        VARCHAR(100)          COMMENT '服务协议编号',
    service_start_date  DATE                  COMMENT '服务起始日期',
    service_end_date    DATE                  COMMENT '服务结束日期',
    service_scope       VARCHAR(500)          COMMENT '服务内容范围: 通关,物流,退税,结算,信保,融资',
    biz_contact_name    VARCHAR(50)           COMMENT '业务联系人',
    biz_contact_phone   VARCHAR(20)           COMMENT '业务联系电话',
    fin_contact_name    VARCHAR(50)           COMMENT '财务联系人',
    fin_contact_phone   VARCHAR(20)           COMMENT '财务联系电话',
    doc_contact_name    VARCHAR(50)           COMMENT '单证联系人',
    doc_contact_phone   VARCHAR(20)           COMMENT '单证联系电话',
    risk_level          VARCHAR(20)  DEFAULT 'NORMAL' COMMENT '风险等级: LOW/NORMAL/HIGH/BLACKLIST',
    annual_service_count INT         DEFAULT 0 COMMENT '年度服务次数',
    annual_export_amount DECIMAL(18,2) DEFAULT 0 COMMENT '年度出口额(万元)',
    annual_import_amount DECIMAL(18,2) DEFAULT 0 COMMENT '年度进口额(万元)',
    status              TINYINT      DEFAULT 1 COMMENT '1=正常 0=禁用',
    remark              TEXT                  COMMENT '备注',
    deleted             TINYINT      DEFAULT 0,
    create_time         DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time         DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tenant (tenant_id),
    INDEX idx_credit_code (credit_code),
    INDEX idx_agreement_status (agreement_status),
    INDEX idx_risk_level (risk_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务企业档案表';

-- ============================================================
-- 16. biz_logistics - 物流服务表
-- ============================================================
CREATE TABLE IF NOT EXISTS biz_logistics (
    id                  BIGINT       AUTO_INCREMENT PRIMARY KEY,
    tenant_id           BIGINT       NOT NULL COMMENT '所属租户',
    logistics_no        VARCHAR(100) NOT NULL COMMENT '物流单号',
    order_id            BIGINT                COMMENT '关联订单ID',
    contract_id         BIGINT                COMMENT '关联合同ID',
    transport_mode      VARCHAR(30)           COMMENT '运输方式: SEA/AIR/RAIL/ROAD/MULTIMODAL',
    departure_port      VARCHAR(100)          COMMENT '起运港/站',
    destination_port    VARCHAR(100)          COMMENT '目的港/站',
    vessel_voyage       VARCHAR(200)          COMMENT '船名航次/航班/车次',
    bl_no               VARCHAR(100)          COMMENT '提单号',
    waybill_no          VARCHAR(100)          COMMENT '运单号',
    logistics_provider  VARCHAR(200)          COMMENT '物流供应商',
    freight_amount      DECIMAL(18,2)         COMMENT '运费金额',
    freight_currency    VARCHAR(10)           COMMENT '运费币种',
    insurance_amount    DECIMAL(18,2)         COMMENT '保险金额',
    etd                 DATE                  COMMENT '预计离港日',
    eta                 DATE                  COMMENT '预计到港日',
    actual_departure    DATE                  COMMENT '实际离港日',
    actual_arrival      DATE                  COMMENT '实际到港日',
    is_cold_chain       TINYINT      DEFAULT 0 COMMENT '是否冷链 0=否 1=是',
    temperature_range   VARCHAR(50)           COMMENT '温控范围(℃)',
    status              VARCHAR(20)  DEFAULT 'PENDING' COMMENT '状态: PENDING/IN_TRANSIT/ARRIVED/DELIVERED/ABNORMAL',
    remark              TEXT                  COMMENT '备注',
    deleted             TINYINT      DEFAULT 0,
    create_time         DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time         DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tenant (tenant_id),
    INDEX idx_order (order_id),
    INDEX idx_logistics_no (logistics_no),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物流服务表';

-- ============================================================
-- 17. biz_tax_refund - 退税管理表
-- ============================================================
CREATE TABLE IF NOT EXISTS biz_tax_refund (
    id                  BIGINT       AUTO_INCREMENT PRIMARY KEY,
    tenant_id           BIGINT       NOT NULL COMMENT '所属租户',
    refund_no           VARCHAR(100) NOT NULL COMMENT '退税申报编号',
    enterprise_id       BIGINT                COMMENT '关联服务企业ID',
    order_id            BIGINT                COMMENT '关联订单ID',
    customs_decl_id     BIGINT                COMMENT '关联报关单ID',
    contract_id         BIGINT                COMMENT '关联合同ID',
    invoice_no          VARCHAR(100)          COMMENT '增值税发票号',
    invoice_amount      DECIMAL(18,2)         COMMENT '发票金额',
    refund_rate         DECIMAL(5,2)          COMMENT '退税率(%)',
    refund_amount       DECIMAL(18,2)         COMMENT '应退税额',
    actual_refund       DECIMAL(18,2)         COMMENT '实退税额',
    apply_date          DATE                  COMMENT '申报日期',
    approve_date        DATE                  COMMENT '审批日期',
    refund_date         DATE                  COMMENT '到账日期',
    status              VARCHAR(20)  DEFAULT 'DRAFT' COMMENT '状态: DRAFT/SUBMITTED/REVIEWING/APPROVED/REJECTED/REFUNDED/SUSPENDED',
    risk_flag           VARCHAR(20)  DEFAULT 'NORMAL' COMMENT '风险标志: NORMAL/SUSPICIOUS/REJECTED',
    risk_remark         VARCHAR(500)          COMMENT '风险备注',
    remark              TEXT                  COMMENT '备注',
    deleted             TINYINT      DEFAULT 0,
    create_time         DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time         DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tenant (tenant_id),
    INDEX idx_enterprise (enterprise_id),
    INDEX idx_refund_no (refund_no),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退税管理表';

-- ============================================================
-- 18. biz_settlement - 结算收汇表
-- ============================================================
CREATE TABLE IF NOT EXISTS biz_settlement (
    id                  BIGINT       AUTO_INCREMENT PRIMARY KEY,
    tenant_id           BIGINT       NOT NULL COMMENT '所属租户',
    settlement_no       VARCHAR(100) NOT NULL COMMENT '结算单号',
    settlement_type     VARCHAR(20)  NOT NULL COMMENT '类型: RECEIVABLE/PAYABLE/EXCHANGE',
    enterprise_id       BIGINT                COMMENT '关联服务企业ID',
    order_id            BIGINT                COMMENT '关联订单ID',
    customs_decl_id     BIGINT                COMMENT '关联报关单ID',
    currency            VARCHAR(10)           COMMENT '币种',
    amount              DECIMAL(18,2)         COMMENT '金额(外币)',
    exchange_rate       DECIMAL(10,6)         COMMENT '汇率',
    rmb_amount          DECIMAL(18,2)         COMMENT '折合人民币',
    bank_name           VARCHAR(200)          COMMENT '银行名称',
    bank_account        VARCHAR(100)          COMMENT '银行账号',
    payment_date        DATE                  COMMENT '收付汇日期',
    status              VARCHAR(20)  DEFAULT 'PENDING' COMMENT '状态: PENDING/COMPLETED/ABNORMAL',
    remark              TEXT                  COMMENT '备注',
    deleted             TINYINT      DEFAULT 0,
    create_time         DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time         DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tenant (tenant_id),
    INDEX idx_enterprise (enterprise_id),
    INDEX idx_settlement_no (settlement_no),
    INDEX idx_type (settlement_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='结算收汇表';

-- ============================================================
-- 19. biz_insurance - 信保服务表
-- ============================================================
CREATE TABLE IF NOT EXISTS biz_insurance (
    id                  BIGINT       AUTO_INCREMENT PRIMARY KEY,
    tenant_id           BIGINT       NOT NULL COMMENT '所属租户',
    policy_no           VARCHAR(100)          COMMENT '保单号',
    enterprise_id       BIGINT                COMMENT '关联服务企业ID',
    buyer_name          VARCHAR(200)          COMMENT '买方名称',
    buyer_country       VARCHAR(100)          COMMENT '买方国家',
    credit_limit        DECIMAL(18,2)         COMMENT '信用限额',
    insured_amount      DECIMAL(18,2)         COMMENT '投保金额',
    premium             DECIMAL(18,2)         COMMENT '保费',
    coverage_start      DATE                  COMMENT '承保起始日',
    coverage_end        DATE                  COMMENT '承保终止日',
    shipment_date       DATE                  COMMENT '出运日期',
    shipment_amount     DECIMAL(18,2)         COMMENT '出运金额',
    overdue_amount      DECIMAL(18,2)         COMMENT '逾期应收金额',
    claim_amount        DECIMAL(18,2)         COMMENT '理赔金额',
    status              VARCHAR(20)  DEFAULT 'DRAFT' COMMENT '状态: DRAFT/APPLIED/APPROVED/ACTIVE/CLAIMED/CLOSED',
    remark              TEXT                  COMMENT '备注',
    deleted             TINYINT      DEFAULT 0,
    create_time         DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time         DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tenant (tenant_id),
    INDEX idx_enterprise (enterprise_id),
    INDEX idx_policy_no (policy_no),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='信保服务表';

-- ============================================================
-- 20. biz_financing - 融资协助表
-- ============================================================
CREATE TABLE IF NOT EXISTS biz_financing (
    id                  BIGINT       AUTO_INCREMENT PRIMARY KEY,
    tenant_id           BIGINT       NOT NULL COMMENT '所属租户',
    financing_no        VARCHAR(100) NOT NULL COMMENT '融资编号',
    enterprise_id       BIGINT                COMMENT '关联服务企业ID',
    financing_type      VARCHAR(30)           COMMENT '融资类型: ORDER_LOAN/CREDIT_LOAN/TAX_LOAN/POLICY_LOAN/OTHER',
    bank_name           VARCHAR(200)          COMMENT '合作银行/机构',
    apply_amount        DECIMAL(18,2)         COMMENT '申请金额',
    approved_amount     DECIMAL(18,2)         COMMENT '批准金额',
    interest_rate       DECIMAL(6,4)          COMMENT '利率(%)',
    apply_date          DATE                  COMMENT '申请日期',
    approve_date        DATE                  COMMENT '批准日期',
    disbursement_date   DATE                  COMMENT '放款日期',
    maturity_date       DATE                  COMMENT '到期日期',
    repaid_amount       DECIMAL(18,2)         COMMENT '已还金额',
    status              VARCHAR(20)  DEFAULT 'DRAFT' COMMENT '状态: DRAFT/APPLIED/APPROVED/DISBURSED/REPAID/OVERDUE/REJECTED',
    remark              TEXT                  COMMENT '备注',
    deleted             TINYINT      DEFAULT 0,
    create_time         DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time         DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tenant (tenant_id),
    INDEX idx_enterprise (enterprise_id),
    INDEX idx_financing_no (financing_no),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='融资协助表';

-- ============================================================
-- 21. sys_operation_log - 操作日志表
-- ============================================================
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    tenant_id       BIGINT                COMMENT '所属租户',
    user_id         BIGINT                COMMENT '操作用户ID',
    username        VARCHAR(50)           COMMENT '操作用户名',
    module          VARCHAR(50)           COMMENT '操作模块',
    action          VARCHAR(50)           COMMENT '操作类型: CREATE/UPDATE/DELETE/EXPORT/IMPORT/LOGIN/LOGOUT',
    target_type     VARCHAR(50)           COMMENT '操作对象类型',
    target_id       BIGINT                COMMENT '操作对象ID',
    description     VARCHAR(500)          COMMENT '操作描述',
    request_method  VARCHAR(10)           COMMENT '请求方法',
    request_url     VARCHAR(500)          COMMENT '请求URL',
    request_ip      VARCHAR(50)           COMMENT '请求IP',
    status          TINYINT      DEFAULT 1 COMMENT '1=成功 0=失败',
    error_msg       VARCHAR(500)          COMMENT '错误信息',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_tenant (tenant_id),
    INDEX idx_user (user_id),
    INDEX idx_module (module),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ============================================================
-- 数据链路补全：为合同表和报关单表添加关联字段
-- ============================================================
ALTER TABLE biz_contract
    ADD COLUMN enterprise_id  BIGINT       NULL COMMENT '关联服务企业ID' AFTER remark,
    ADD COLUMN contract_type  VARCHAR(30)  NULL COMMENT '合同类型: PURCHASE/SALES/SERVICE/AGENCY/OTHER' AFTER enterprise_id,
    ADD INDEX idx_contract_enterprise_id (enterprise_id);

ALTER TABLE biz_order
    ADD COLUMN enterprise_id  BIGINT       NULL COMMENT '关联服务企业ID' AFTER remark,
    ADD INDEX idx_order_enterprise_id (enterprise_id);

ALTER TABLE biz_customs_declaration
    ADD COLUMN contract_id    BIGINT       NULL COMMENT '关联合同ID' AFTER remark,
    ADD COLUMN order_id       BIGINT       NULL COMMENT '关联订单ID' AFTER contract_id,
    ADD COLUMN enterprise_id  BIGINT       NULL COMMENT '关联服务企业ID' AFTER order_id,
    ADD INDEX idx_customs_contract_id (contract_id),
    ADD INDEX idx_customs_order_id (order_id),
    ADD INDEX idx_customs_enterprise_id (enterprise_id);
