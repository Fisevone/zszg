-- 创建任务表
CREATE TABLE IF NOT EXISTS t_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    teacher_id BIGINT NOT NULL COMMENT '教师ID',
    teacher_name VARCHAR(100) COMMENT '教师姓名',
    class_id VARCHAR(50) NOT NULL COMMENT '班级ID',
    class_name VARCHAR(100) COMMENT '班级名称',
    title VARCHAR(255) NOT NULL COMMENT '任务标题',
    content TEXT NOT NULL COMMENT '任务内容',
    task_type VARCHAR(50) COMMENT '任务类型（学习任务、活动、考试等）',
    priority VARCHAR(20) COMMENT '优先级（普通、重要、紧急）',
    deadline DATETIME COMMENT '截止时间',
    location VARCHAR(200) COMMENT '地点',
    participants VARCHAR(200) COMMENT '参与人群',
    quantity_requirement VARCHAR(100) COMMENT '数量要求',
    important_reminders TEXT COMMENT '重要提醒（JSON数组）',
    smart_tags TEXT COMMENT '智能标签（JSON数组）',
    parsed_tasks TEXT COMMENT 'AI拆解的子任务（JSON数组）',
    ai_notes TEXT COMMENT 'AI备注',
    status VARCHAR(20) DEFAULT '待下发' COMMENT '状态（待下发、已下发、已完成）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_class_id (class_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师任务推送表';

-- 创建学生任务表
CREATE TABLE IF NOT EXISTS t_student_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL COMMENT '任务ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    student_name VARCHAR(100) COMMENT '学生姓名',
    is_completed BOOLEAN DEFAULT FALSE COMMENT '是否完成',
    completed_at DATETIME COMMENT '完成时间',
    notes TEXT COMMENT '学生备注',
    received_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '接收时间',
    INDEX idx_task_id (task_id),
    INDEX idx_student_id (student_id),
    INDEX idx_is_completed (is_completed),
    FOREIGN KEY (task_id) REFERENCES t_task(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生任务接收记录表';

-- 插入一些测试数据（可选）
INSERT INTO t_task (teacher_id, teacher_name, class_id, class_name, title, content, task_type, priority, deadline, status) 
VALUES 
(2, '王老师', '高一1班', '高一1班', '第三章函数与导数错题整理', 
 '同学们，本周五前完成第三章函数与导数的错题整理，每人至少录入5道错题，重点关注求导和极值问题。完成后记得标注知识点。', 
 '学习任务', '普通', DATE_ADD(NOW(), INTERVAL 2 DAY), '待下发');

-- 查看创建结果
SELECT 'Task tables created successfully!' AS message;





















