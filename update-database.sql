-- ========================================
-- 知错就改项目 - 数据库完整更新脚本
-- 执行方式：mysql -u root -p zszg < update-database.sql
-- ========================================

USE zszg;

-- 1. 添加任务相关表（如果不存在）
CREATE TABLE IF NOT EXISTS t_task (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  teacher_id BIGINT NOT NULL,
  teacher_name VARCHAR(50) NOT NULL,
  class_id VARCHAR(50) NOT NULL,
  class_name VARCHAR(100) NOT NULL,
  title VARCHAR(200) NOT NULL,
  content TEXT NOT NULL,
  task_type VARCHAR(50),
  deadline DATETIME,
  priority VARCHAR(20) DEFAULT '普通',
  difficulty VARCHAR(20),
  subject VARCHAR(50),
  quantity_requirement VARCHAR(100),
  time_requirement VARCHAR(100),
  location VARCHAR(200),
  participants VARCHAR(200),
  expected_outcome TEXT,
  ai_notes TEXT,
  status VARCHAR(20) DEFAULT 'PUBLISHED',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_task_teacher FOREIGN KEY (teacher_id) REFERENCES t_user(id) ON DELETE CASCADE
);

-- 2. 学生任务表
CREATE TABLE IF NOT EXISTS t_student_task (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  student_name VARCHAR(50),
  status VARCHAR(20) DEFAULT 'PENDING',
  completed_at DATETIME,
  notes TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_st_task FOREIGN KEY (task_id) REFERENCES t_task(id) ON DELETE CASCADE,
  CONSTRAINT fk_st_student FOREIGN KEY (student_id) REFERENCES t_user(id) ON DELETE CASCADE,
  UNIQUE KEY uk_task_student (task_id, student_id)
);

-- 3. 任务子任务表
CREATE TABLE IF NOT EXISTS t_task_subtask (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_id BIGINT NOT NULL,
  name VARCHAR(200) NOT NULL,
  description TEXT,
  order_num INT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_ts_task FOREIGN KEY (task_id) REFERENCES t_task(id) ON DELETE CASCADE
);

-- 4. 任务关键词表
CREATE TABLE IF NOT EXISTS t_task_keyword (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_id BIGINT NOT NULL,
  keyword VARCHAR(100) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_tk_task FOREIGN KEY (task_id) REFERENCES t_task(id) ON DELETE CASCADE
);

-- 5. 任务学习目标表
CREATE TABLE IF NOT EXISTS t_task_objective (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_id BIGINT NOT NULL,
  objective TEXT NOT NULL,
  order_num INT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_to_task FOREIGN KEY (task_id) REFERENCES t_task(id) ON DELETE CASCADE
);

-- 6. 用户交互记录表（点赞/收藏）
CREATE TABLE IF NOT EXISTS t_user_interaction (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  target_type VARCHAR(20) NOT NULL COMMENT 'SHARE_POOL, RESOURCE, ERROR_BOOK',
  target_id BIGINT NOT NULL,
  interaction_type VARCHAR(20) NOT NULL COMMENT 'LIKE, FAVORITE',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_ui_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE,
  UNIQUE KEY uk_user_target_interaction (user_id, target_type, target_id, interaction_type)
);

-- 7. 为共享池添加标签字段（如果不存在）
ALTER TABLE t_share_pool 
ADD COLUMN IF NOT EXISTS tags VARCHAR(255) COMMENT '标签';

-- 8. 为错题本添加图片URL字段（如果不存在）
ALTER TABLE t_error_book
ADD COLUMN IF NOT EXISTS image_url VARCHAR(500) COMMENT '原图URL';

-- 9. 创建索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_error_book_user_status ON t_error_book(user_id, status);
CREATE INDEX IF NOT EXISTS idx_share_pool_approved ON t_share_pool(approved, created_at);
CREATE INDEX IF NOT EXISTS idx_resource_type_subject ON t_resource(type, subject, approved);
CREATE INDEX IF NOT EXISTS idx_task_teacher_class ON t_task(teacher_id, class_id);
CREATE INDEX IF NOT EXISTS idx_student_task_student ON t_student_task(student_id, status);

-- 10. 插入示例数据（如果表为空）
INSERT IGNORE INTO t_user (id, username, password_hash, real_name, email, role, status, created_at) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lFhNR3SLcaLOLCGve', '系统管理员', 'admin@zszg.com', 'ROLE_ADMIN', 'ACTIVE', NOW()),
(2, 'teacher', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lFhNR3SLcaLOLCGve', '张老师', 'teacher@zszg.com', 'ROLE_TEACHER', 'ACTIVE', NOW()),
(3, 'student', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lFhNR3SLcaLOLCGve', '李同学', 'student@zszg.com', 'ROLE_STUDENT', 'ACTIVE', NOW());

-- 11. 插入示例知识点（如果表为空）
INSERT IGNORE INTO t_knowledge (id, subject, title, code, parent_id, created_at) VALUES
(1, '数学', '函数与导数', 'MATH_FUNC_001', NULL, NOW()),
(2, '数学', '三角函数', 'MATH_TRIG_001', NULL, NOW()),
(3, '数学', '立体几何', 'MATH_GEOM_001', NULL, NOW()),
(4, '物理', '力学', 'PHYS_MECH_001', NULL, NOW()),
(5, '物理', '电磁学', 'PHYS_ELEC_001', NULL, NOW());

-- 完成
SELECT '✅ 数据库更新完成！' AS status;
SELECT COUNT(*) AS user_count FROM t_user;
SELECT COUNT(*) AS knowledge_count FROM t_knowledge;
SELECT '所有表已创建并添加必要索引' AS note;

