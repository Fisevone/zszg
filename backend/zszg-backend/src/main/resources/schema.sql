CREATE TABLE IF NOT EXISTS t_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) UNIQUE NOT NULL,
  password_hash VARCHAR(100) NOT NULL,
  real_name VARCHAR(50),
  phone VARCHAR(20),
  email VARCHAR(100),
  role VARCHAR(20) NOT NULL,
  status VARCHAR(20) DEFAULT 'ACTIVE',
  class_id VARCHAR(50),
  grade VARCHAR(20),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS t_question (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  subject VARCHAR(50) NOT NULL,
  type VARCHAR(20) COMMENT '题目类型: choice/fill_blank/solve',
  difficulty VARCHAR(20),
  content LONGTEXT NOT NULL,
  options VARCHAR(500),
  answer LONGTEXT,
  analysis LONGTEXT,
  source VARCHAR(100),
  created_by BIGINT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_q_user FOREIGN KEY (created_by) REFERENCES t_user(id)
);

CREATE TABLE IF NOT EXISTS t_knowledge (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  subject VARCHAR(50) NOT NULL,
  title VARCHAR(100) NOT NULL,
  code VARCHAR(50),
  parent_id BIGINT,
  textbook_ref VARCHAR(255),
  teacher_notes_url VARCHAR(255),
  video_url VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_k_parent FOREIGN KEY (parent_id) REFERENCES t_knowledge(id)
);

CREATE TABLE IF NOT EXISTS t_question_knowledge (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  question_id BIGINT NOT NULL,
  knowledge_id BIGINT NOT NULL,
  CONSTRAINT fk_qk_q FOREIGN KEY (question_id) REFERENCES t_question(id) ON DELETE CASCADE,
  CONSTRAINT fk_qk_k FOREIGN KEY (knowledge_id) REFERENCES t_knowledge(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS t_error_book (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  question_id BIGINT NOT NULL,
  error_reason TEXT,
  correction TEXT,
  images JSON NULL,
  tags VARCHAR(255),
  status VARCHAR(20) DEFAULT 'PRIVATE',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_eb_user FOREIGN KEY (user_id) REFERENCES t_user(id),
  CONSTRAINT fk_eb_question FOREIGN KEY (question_id) REFERENCES t_question(id)
);

CREATE TABLE IF NOT EXISTS t_share_pool (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  error_book_id BIGINT NOT NULL,
  scope VARCHAR(20) DEFAULT 'CLASS',
  approved TINYINT(1) DEFAULT 0,
  approved_by BIGINT,
  approved_at TIMESTAMP NULL,
  likes INT DEFAULT 0,
  favorites INT DEFAULT 0,
  views INT DEFAULT 0,
  tags VARCHAR(128),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL,
  CONSTRAINT fk_sp_eb FOREIGN KEY (error_book_id) REFERENCES t_error_book(id) ON DELETE CASCADE,
  CONSTRAINT fk_sp_admin FOREIGN KEY (approved_by) REFERENCES t_user(id)
);

CREATE TABLE IF NOT EXISTS t_resource (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(100) NOT NULL,
  type VARCHAR(20) NOT NULL,
  subject VARCHAR(50),
  file_url VARCHAR(255) NOT NULL,
  uploaded_by BIGINT NOT NULL,
  approved TINYINT(1) DEFAULT 0,
  approved_by BIGINT,
  approved_at TIMESTAMP NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_res_user FOREIGN KEY (uploaded_by) REFERENCES t_user(id),
  CONSTRAINT fk_res_admin FOREIGN KEY (approved_by) REFERENCES t_user(id)
);

-- 班级表
CREATE TABLE IF NOT EXISTS t_class (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  grade_level VARCHAR(20),
  teacher_id BIGINT NOT NULL,
  invite_code VARCHAR(20) UNIQUE NOT NULL,
  description TEXT,
  status VARCHAR(20) DEFAULT 'ACTIVE',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_class_teacher FOREIGN KEY (teacher_id) REFERENCES t_user(id)
);

-- 班级成员表
CREATE TABLE IF NOT EXISTS t_class_member (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  class_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  status VARCHAR(20) DEFAULT 'ACTIVE',
  CONSTRAINT fk_cm_class FOREIGN KEY (class_id) REFERENCES t_class(id) ON DELETE CASCADE,
  CONSTRAINT fk_cm_student FOREIGN KEY (student_id) REFERENCES t_user(id) ON DELETE CASCADE,
  UNIQUE KEY uk_class_student (class_id, student_id)
);

-- 教师推送资源表
CREATE TABLE IF NOT EXISTS t_teacher_push (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  teacher_id BIGINT NOT NULL,
  class_id BIGINT NOT NULL,
  title VARCHAR(100) NOT NULL,
  content TEXT,
  resource_type VARCHAR(20) NOT NULL COMMENT 'KNOWLEDGE, QUESTION, NOTICE',
  resource_id BIGINT COMMENT '关联的资源ID（知识点/题目）',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_tp_teacher FOREIGN KEY (teacher_id) REFERENCES t_user(id),
  CONSTRAINT fk_tp_class FOREIGN KEY (class_id) REFERENCES t_class(id) ON DELETE CASCADE
);

-- 教师反馈表
CREATE TABLE IF NOT EXISTS t_teacher_feedback (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  teacher_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  error_book_id BIGINT NOT NULL,
  feedback TEXT NOT NULL,
  rating INT COMMENT '评分1-5',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_tf_teacher FOREIGN KEY (teacher_id) REFERENCES t_user(id),
  CONSTRAINT fk_tf_student FOREIGN KEY (student_id) REFERENCES t_user(id),
  CONSTRAINT fk_tf_errorbook FOREIGN KEY (error_book_id) REFERENCES t_error_book(id) ON DELETE CASCADE
);

-- 任务表
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
  CONSTRAINT fk_task_teacher FOREIGN KEY (teacher_id) REFERENCES t_user(id)
);

-- 学生任务表
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

-- 任务子任务表
CREATE TABLE IF NOT EXISTS t_task_subtask (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_id BIGINT NOT NULL,
  name VARCHAR(200) NOT NULL,
  description TEXT,
  order_num INT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_ts_task FOREIGN KEY (task_id) REFERENCES t_task(id) ON DELETE CASCADE
);

-- 任务关键词表
CREATE TABLE IF NOT EXISTS t_task_keyword (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_id BIGINT NOT NULL,
  keyword VARCHAR(100) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_tk_task FOREIGN KEY (task_id) REFERENCES t_task(id) ON DELETE CASCADE
);

-- 任务学习目标表
CREATE TABLE IF NOT EXISTS t_task_objective (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_id BIGINT NOT NULL,
  objective TEXT NOT NULL,
  order_num INT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_to_task FOREIGN KEY (task_id) REFERENCES t_task(id) ON DELETE CASCADE
);

-- 点赞收藏记录表
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

