-- =============================================
-- 修复数据库表结构
-- 添加缺失的 type 字段
-- =============================================

USE zszg;

-- 添加 type 字段（如果已存在会报错，可以忽略）
-- 使用存储过程安全添加列
SET @dbname = DATABASE();
SET @tablename = "t_question";
SET @columnname = "type";
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  "SELECT 'Column type already exists, skipping...' AS msg;",
  "ALTER TABLE t_question ADD COLUMN `type` VARCHAR(20) NULL COMMENT '题目类型: choice/fill_blank/solve' AFTER subject;"
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 确保 content、answer、analysis 字段类型正确（支持大文本）
ALTER TABLE t_question 
MODIFY COLUMN content LONGTEXT NOT NULL;

ALTER TABLE t_question 
MODIFY COLUMN answer LONGTEXT NULL;

ALTER TABLE t_question 
MODIFY COLUMN analysis LONGTEXT NULL;

-- 显示当前表结构确认
SELECT '✅ 数据库表结构修复完成!' AS result;
DESCRIBE t_question;

