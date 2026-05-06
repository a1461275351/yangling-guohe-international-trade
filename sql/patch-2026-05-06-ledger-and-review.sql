-- ============================================================
-- 补丁脚本：补齐进口台账 + 报关审核工作流相关表/字段
-- 适用：已经初始化过 init.sql 但缺失这些对象的现有数据库
-- 使用：mysql -u root -p trade_platform < patch-2026-05-06-ledger-and-review.sql
-- ============================================================

USE trade_platform;

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
-- 补齐 biz_customs_declaration 缺失字段
-- ------------------------------------------------------------
-- MySQL 没有 ADD COLUMN IF NOT EXISTS（5.7/8.0.28-）
-- 用存储过程检测后再加，避免重复执行报错
-- ============================================================

DROP PROCEDURE IF EXISTS pf_add_col;
DELIMITER $$
CREATE PROCEDURE pf_add_col(IN tbl VARCHAR(64), IN col VARCHAR(64), IN coldef TEXT)
BEGIN
    IF NOT EXISTS(
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tbl AND COLUMN_NAME = col
    ) THEN
        SET @sql = CONCAT('ALTER TABLE `', tbl, '` ADD COLUMN `', col, '` ', coldef);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

CALL pf_add_col('biz_customs_declaration', 'ledger_id',     'BIGINT NULL COMMENT ''关联台账ID''');
CALL pf_add_col('biz_customs_declaration', 'split_seq',     'INT NULL COMMENT ''拆单序号''');
CALL pf_add_col('biz_customs_declaration', 'review_status', 'VARCHAR(20) DEFAULT ''DRAFT'' COMMENT ''审核状态: DRAFT/SUBMITTED/REVIEWING/APPROVED/REJECTED/RELEASED''');
CALL pf_add_col('biz_customs_declaration', 'submit_time',   'DATETIME NULL COMMENT ''提交时间''');
CALL pf_add_col('biz_customs_declaration', 'submit_by',     'BIGINT NULL COMMENT ''提交人ID''');
CALL pf_add_col('biz_customs_declaration', 'review_time',   'DATETIME NULL COMMENT ''审核时间''');
CALL pf_add_col('biz_customs_declaration', 'review_by',     'BIGINT NULL COMMENT ''审核人ID''');
CALL pf_add_col('biz_customs_declaration', 'update_time',   'DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL pf_add_col('biz_customs_declaration', 'deleted',       'TINYINT DEFAULT 0');

-- 补索引
DROP PROCEDURE IF EXISTS pf_add_idx;
DELIMITER $$
CREATE PROCEDURE pf_add_idx(IN tbl VARCHAR(64), IN idx VARCHAR(64), IN col VARCHAR(64))
BEGIN
    IF NOT EXISTS(
        SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tbl AND INDEX_NAME = idx
    ) THEN
        SET @sql = CONCAT('ALTER TABLE `', tbl, '` ADD INDEX `', idx, '` (`', col, '`)');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

CALL pf_add_idx('biz_customs_declaration', 'idx_customs_ledger_id', 'ledger_id');
CALL pf_add_idx('biz_customs_declaration', 'idx_customs_review_status', 'review_status');

DROP PROCEDURE IF EXISTS pf_add_col;
DROP PROCEDURE IF EXISTS pf_add_idx;

-- ============================================================
-- 验证
-- ============================================================
SELECT 'biz_import_ledger'        AS table_name, COUNT(*) AS exists_flag FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'biz_import_ledger'
UNION ALL SELECT 'biz_import_ledger_goods', COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'biz_import_ledger_goods'
UNION ALL SELECT 'biz_import_ledger_file',  COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'biz_import_ledger_file'
UNION ALL SELECT 'biz_customs_review',      COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'biz_customs_review';
