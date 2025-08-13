-- 智慧旅游SaaS平台数据库表结构
-- 创建数据库
CREATE DATABASE IF NOT EXISTS tourism_saas DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE tourism_saas;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    nickname VARCHAR(100) DEFAULT NULL COMMENT '昵称',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    avatar VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    gender TINYINT DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
    birthday DATETIME DEFAULT NULL COMMENT '生日',
    status TINYINT DEFAULT 1 COMMENT '账户状态：0-禁用，1-正常',
    user_type TINYINT DEFAULT 1 COMMENT '用户类型：1-普通用户，2-VIP用户，3-企业用户',
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 用户表索引优化（解决登录SQL性能问题）
CREATE INDEX idx_username ON t_user(username);
CREATE INDEX idx_email ON t_user(email);
CREATE INDEX idx_phone ON t_user(phone);
CREATE INDEX idx_status_deleted ON t_user(status, deleted);
CREATE INDEX idx_create_time ON t_user(create_time);

-- 2. 旅游产品分类表
CREATE TABLE IF NOT EXISTS t_product_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID，0表示顶级分类',
    level TINYINT DEFAULT 1 COMMENT '分类层级',
    sort_order INT DEFAULT 0 COMMENT '排序序号',
    icon VARCHAR(500) DEFAULT NULL COMMENT '分类图标',
    description TEXT DEFAULT NULL COMMENT '分类描述',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='旅游产品分类表';

CREATE INDEX idx_parent_id ON t_product_category(parent_id);
CREATE INDEX idx_status_deleted ON t_product_category(status, deleted);

-- 3. 旅游产品表
CREATE TABLE IF NOT EXISTS t_tourism_product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '产品ID',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    name VARCHAR(200) NOT NULL COMMENT '产品名称',
    subtitle VARCHAR(500) DEFAULT NULL COMMENT '产品副标题',
    description TEXT DEFAULT NULL COMMENT '产品描述',
    detail_html LONGTEXT DEFAULT NULL COMMENT '产品详情HTML',
    main_image VARCHAR(500) DEFAULT NULL COMMENT '主图片',
    sub_images TEXT DEFAULT NULL COMMENT '子图片，JSON格式',
    original_price DECIMAL(10,2) DEFAULT 0.00 COMMENT '原价',
    current_price DECIMAL(10,2) NOT NULL COMMENT '现价',
    stock INT DEFAULT 0 COMMENT '库存数量',
    sales_count INT DEFAULT 0 COMMENT '销量',
    view_count INT DEFAULT 0 COMMENT '浏览量',
    destination VARCHAR(200) DEFAULT NULL COMMENT '目的地',
    duration_days INT DEFAULT 1 COMMENT '行程天数',
    departure_city VARCHAR(100) DEFAULT NULL COMMENT '出发城市',
    travel_type TINYINT DEFAULT 1 COMMENT '旅游类型：1-跟团游，2-自由行，3-当地游，4-邮轮游',
    difficulty_level TINYINT DEFAULT 1 COMMENT '难度等级：1-轻松，2-一般，3-较难，4-困难',
    age_range VARCHAR(50) DEFAULT NULL COMMENT '适合年龄范围',
    tags VARCHAR(500) DEFAULT NULL COMMENT '标签，逗号分隔',
    features TEXT DEFAULT NULL COMMENT '产品特色',
    included_services TEXT DEFAULT NULL COMMENT '包含服务',
    excluded_services TEXT DEFAULT NULL COMMENT '不包含服务',
    booking_notice TEXT DEFAULT NULL COMMENT '预订须知',
    refund_policy TEXT DEFAULT NULL COMMENT '退改政策',
    status TINYINT DEFAULT 1 COMMENT '状态：0-下架，1-上架，2-待审核',
    sort_order INT DEFAULT 0 COMMENT '排序序号',
    seo_title VARCHAR(200) DEFAULT NULL COMMENT 'SEO标题',
    seo_keywords VARCHAR(500) DEFAULT NULL COMMENT 'SEO关键词',
    seo_description VARCHAR(1000) DEFAULT NULL COMMENT 'SEO描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='旅游产品表';

-- 产品表索引优化（支持毫秒级查询）
CREATE INDEX idx_category_id ON t_tourism_product(category_id);
CREATE INDEX idx_status_deleted ON t_tourism_product(status, deleted);
CREATE INDEX idx_destination ON t_tourism_product(destination);
CREATE INDEX idx_travel_type ON t_tourism_product(travel_type);
CREATE INDEX idx_price ON t_tourism_product(current_price);
CREATE INDEX idx_sales_count ON t_tourism_product(sales_count);
CREATE INDEX idx_create_time ON t_tourism_product(create_time);
CREATE INDEX idx_departure_city ON t_tourism_product(departure_city);
-- 复合索引用于多条件查询
CREATE INDEX idx_category_status_price ON t_tourism_product(category_id, status, current_price);
CREATE INDEX idx_destination_type_status ON t_tourism_product(destination, travel_type, status);

-- 4. 订单表
CREATE TABLE IF NOT EXISTS t_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '产品ID',
    product_name VARCHAR(200) NOT NULL COMMENT '产品名称',
    product_image VARCHAR(500) DEFAULT NULL COMMENT '产品图片',
    unit_price DECIMAL(10,2) NOT NULL COMMENT '单价',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    discount_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '优惠金额',
    actual_amount DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    order_status TINYINT DEFAULT 1 COMMENT '订单状态：1-待支付，2-已支付，3-已确认，4-进行中，5-已完成，6-已取消，7-已退款',
    payment_status TINYINT DEFAULT 0 COMMENT '支付状态：0-未支付，1-已支付，2-部分退款，3-全额退款',
    payment_method TINYINT DEFAULT 0 COMMENT '支付方式：1-微信，2-支付宝，3-银行卡',
    payment_time DATETIME DEFAULT NULL COMMENT '支付时间',
    travel_date DATE DEFAULT NULL COMMENT '出行日期',
    traveler_count INT DEFAULT 1 COMMENT '出行人数',
    contact_name VARCHAR(50) DEFAULT NULL COMMENT '联系人姓名',
    contact_phone VARCHAR(20) DEFAULT NULL COMMENT '联系人电话',
    contact_email VARCHAR(100) DEFAULT NULL COMMENT '联系人邮箱',
    special_requirements TEXT DEFAULT NULL COMMENT '特殊要求',
    remark TEXT DEFAULT NULL COMMENT '订单备注',
    cancel_reason VARCHAR(500) DEFAULT NULL COMMENT '取消原因',
    refund_reason VARCHAR(500) DEFAULT NULL COMMENT '退款原因',
    refund_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '退款金额',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 订单表索引优化（支持多表联合查询）
CREATE INDEX idx_order_no ON t_order(order_no);
CREATE INDEX idx_user_id ON t_order(user_id);
CREATE INDEX idx_product_id ON t_order(product_id);
CREATE INDEX idx_order_status ON t_order(order_status);
CREATE INDEX idx_payment_status ON t_order(payment_status);
CREATE INDEX idx_create_time ON t_order(create_time);
CREATE INDEX idx_travel_date ON t_order(travel_date);
-- 复合索引用于订单-用户-产品联合查询
CREATE INDEX idx_user_order_status ON t_order(user_id, order_status);
CREATE INDEX idx_user_create_time ON t_order(user_id, create_time);

-- 5. 订单项表（用于支持多产品订单）
CREATE TABLE IF NOT EXISTS t_order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单项ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_id BIGINT NOT NULL COMMENT '产品ID',
    product_name VARCHAR(200) NOT NULL COMMENT '产品名称',
    product_image VARCHAR(500) DEFAULT NULL COMMENT '产品图片',
    unit_price DECIMAL(10,2) NOT NULL COMMENT '单价',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '小计金额',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单项表';

CREATE INDEX idx_order_id ON t_order_item(order_id);
CREATE INDEX idx_product_id ON t_order_item(product_id);

-- 6. 用户行为日志表（用于大数据分析）
CREATE TABLE IF NOT EXISTS t_user_behavior_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    user_id BIGINT DEFAULT NULL COMMENT '用户ID，未登录用户为NULL',
    session_id VARCHAR(100) DEFAULT NULL COMMENT '会话ID',
    action_type VARCHAR(50) NOT NULL COMMENT '行为类型：view、search、click、add_cart、order、pay等',
    target_type VARCHAR(50) DEFAULT NULL COMMENT '目标类型：product、category、order等',
    target_id BIGINT DEFAULT NULL COMMENT '目标ID',
    target_name VARCHAR(200) DEFAULT NULL COMMENT '目标名称',
    page_url VARCHAR(1000) DEFAULT NULL COMMENT '页面URL',
    referrer_url VARCHAR(1000) DEFAULT NULL COMMENT '来源URL',
    user_agent TEXT DEFAULT NULL COMMENT '用户代理',
    ip_address VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    device_type VARCHAR(50) DEFAULT NULL COMMENT '设备类型：pc、mobile、tablet',
    browser_type VARCHAR(50) DEFAULT NULL COMMENT '浏览器类型',
    os_type VARCHAR(50) DEFAULT NULL COMMENT '操作系统类型',
    extra_data JSON DEFAULT NULL COMMENT '额外数据，JSON格式',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户行为日志表';

-- 行为日志表索引
CREATE INDEX idx_user_id ON t_user_behavior_log(user_id);
CREATE INDEX idx_action_type ON t_user_behavior_log(action_type);
CREATE INDEX idx_target_type_id ON t_user_behavior_log(target_type, target_id);
CREATE INDEX idx_create_time ON t_user_behavior_log(create_time);
CREATE INDEX idx_session_id ON t_user_behavior_log(session_id);

-- 7. 运营数据统计表
CREATE TABLE IF NOT EXISTS t_analytics_daily (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '统计ID',
    stat_date DATE NOT NULL COMMENT '统计日期',
    total_users INT DEFAULT 0 COMMENT '总用户数',
    new_users INT DEFAULT 0 COMMENT '新增用户数',
    active_users INT DEFAULT 0 COMMENT '活跃用户数',
    total_orders INT DEFAULT 0 COMMENT '总订单数',
    new_orders INT DEFAULT 0 COMMENT '新增订单数',
    paid_orders INT DEFAULT 0 COMMENT '已支付订单数',
    total_revenue DECIMAL(15,2) DEFAULT 0.00 COMMENT '总收入',
    page_views INT DEFAULT 0 COMMENT '页面浏览量',
    unique_visitors INT DEFAULT 0 COMMENT '独立访客数',
    bounce_rate DECIMAL(5,2) DEFAULT 0.00 COMMENT '跳出率',
    avg_session_duration INT DEFAULT 0 COMMENT '平均会话时长（秒）',
    conversion_rate DECIMAL(5,2) DEFAULT 0.00 COMMENT '转化率',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='运营数据日统计表';

CREATE UNIQUE INDEX idx_stat_date ON t_analytics_daily(stat_date);

-- 8. 产品推荐配置表
CREATE TABLE IF NOT EXISTS t_product_recommendation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '推荐ID',
    product_id BIGINT NOT NULL COMMENT '产品ID',
    recommendation_type TINYINT NOT NULL COMMENT '推荐类型：1-首页推荐，2-热门推荐，3-相关推荐',
    weight INT DEFAULT 0 COMMENT '推荐权重',
    position INT DEFAULT 0 COMMENT '推荐位置',
    start_time DATETIME DEFAULT NULL COMMENT '推荐开始时间',
    end_time DATETIME DEFAULT NULL COMMENT '推荐结束时间',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品推荐配置表';

CREATE INDEX idx_product_id ON t_product_recommendation(product_id);
CREATE INDEX idx_recommendation_type ON t_product_recommendation(recommendation_type);
CREATE INDEX idx_status_weight ON t_product_recommendation(status, weight);

-- 插入测试数据
-- 用户测试数据
INSERT INTO t_user (username, password, nickname, email, phone, status, user_type) VALUES
('admin', '$2a$10$7JB720yubVSQLVCIxU7WO.ZYfP2QJ6HPKwF4BgrKCVCpG6.N.4vOG', '管理员', 'admin@tourism.com', '13800138000', 1, 3),
('user001', '$2a$10$7JB720yubVSQLVCIxU7WO.ZYfP2QJ6HPKwF4BgrKCVCpG6.N.4vOG', '普通用户001', 'user001@example.com', '13800138001', 1, 1),
('vip001', '$2a$10$7JB720yubVSQLVCIxU7WO.ZYfP2QJ6HPKwF4BgrKCVCpG6.N.4vOG', 'VIP用户001', 'vip001@example.com', '13800138002', 1, 2);

-- 产品分类测试数据
INSERT INTO t_product_category (name, parent_id, level, sort_order, status) VALUES
('国内游', 0, 1, 1, 1),
('出境游', 0, 1, 2, 1),
('周边游', 0, 1, 3, 1),
('自由行', 0, 1, 4, 1),
('跟团游', 0, 1, 5, 1);

-- 旅游产品测试数据
INSERT INTO t_tourism_product (category_id, name, subtitle, description, current_price, stock, destination, duration_days, departure_city, travel_type, status) VALUES
(1, '北京故宫深度三日游', '探秘紫禁城，感受皇家文化', '专业导游带您深度游览故宫博物院，了解明清两代皇室历史文化', 1299.00, 50, '北京', 3, '上海', 1, 1),
(1, '杭州西湖精品二日游', '上有天堂，下有苏杭', '游览西湖十景，品味江南水乡文化，住宿豪华酒店', 899.00, 30, '杭州', 2, '上海', 1, 1),
(2, '日本东京樱花五日游', '春暖花开，樱花满城', '最佳樱花观赏季节，东京、京都、大阪经典路线', 4999.00, 20, '东京', 5, '上海', 1, 1),
(3, '苏州园林一日游', '园林甲天下，苏州甲江南', '拙政园、虎丘、平江路历史街区一日精华游', 299.00, 100, '苏州', 1, '上海', 1, 1),
(4, '三亚海滨自由行', '椰风海韵，度假天堂', '五星级海景酒店，自由安排行程，含往返机票', 2999.00, 25, '三亚', 4, '上海', 2, 1);

-- 创建视图用于优化查询
CREATE VIEW v_product_order_stats AS
SELECT 
    p.id as product_id,
    p.name as product_name,
    p.current_price,
    p.stock,
    p.sales_count,
    p.view_count,
    COUNT(o.id) as order_count,
    COALESCE(SUM(o.actual_amount), 0) as total_revenue
FROM t_tourism_product p
LEFT JOIN t_order o ON p.id = o.product_id AND o.deleted = 0
WHERE p.deleted = 0 AND p.status = 1
GROUP BY p.id;

-- 存储过程：更新产品销量
DELIMITER //
CREATE PROCEDURE UpdateProductSales(IN productId BIGINT, IN quantity INT)
BEGIN
    UPDATE t_tourism_product 
    SET sales_count = sales_count + quantity,
        stock = stock - quantity,
        update_time = NOW()
    WHERE id = productId AND deleted = 0;
END //
DELIMITER ;

-- 存储过程：生成订单号
DELIMITER //
CREATE FUNCTION GenerateOrderNo() RETURNS VARCHAR(50)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE orderNo VARCHAR(50);
    DECLARE seq_no INT;
    
    SELECT COALESCE(MAX(CAST(SUBSTRING(order_no, 9) AS UNSIGNED)), 0) + 1 
    INTO seq_no 
    FROM t_order 
    WHERE DATE(create_time) = CURDATE();
    
    SET orderNo = CONCAT(DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(seq_no, 6, '0'));
    
    RETURN orderNo;
END //
DELIMITER ;