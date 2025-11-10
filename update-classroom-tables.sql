-- 创建班级相关表

USE zszg;

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

-- 教师推送表
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

SELECT 'Tables created successfully!' AS message;


USE zszg;

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

-- 教师推送表
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

SELECT 'Tables created successfully!' AS message;




