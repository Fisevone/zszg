-- 清空所有班级数据
-- 注意：这会删除所有班级、班级成员、教师推送和反馈数据

-- 1. 删除教师反馈
DELETE FROM t_teacher_feedback;

-- 2. 删除教师推送
DELETE FROM t_teacher_push;

-- 3. 删除班级成员
DELETE FROM t_class_member;

-- 4. 删除所有班级
DELETE FROM t_class;

-- 重置自增ID（可选）
ALTER TABLE t_teacher_feedback AUTO_INCREMENT = 1;
ALTER TABLE t_teacher_push AUTO_INCREMENT = 1;
ALTER TABLE t_class_member AUTO_INCREMENT = 1;
ALTER TABLE t_class AUTO_INCREMENT = 1;

-- 查看结果
SELECT '班级数据已清空！' AS message;
SELECT COUNT(*) AS 剩余班级数 FROM t_class;

