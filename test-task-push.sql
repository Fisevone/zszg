-- =============================================
-- 测试和修复教师任务推送功能
-- =============================================

USE zszg;

-- ==========================================
-- 第一步：检查数据现状
-- ==========================================

-- 1. 查看所有用户的班级信息
SELECT 
    id,
    username,
    real_name,
    role,
    class_id,
    grade
FROM t_user
ORDER BY role, id;

-- 2. 查看所有已创建的任务
SELECT 
    id,
    title,
    teacher_name,
    class_id,
    class_name,
    status,
    created_at
FROM t_task
ORDER BY created_at DESC;

-- 3. 查看所有学生任务记录
SELECT 
    st.id as student_task_id,
    st.task_id,
    st.student_id,
    st.student_name,
    st.is_completed,
    st.received_at,
    u.class_id as student_class_id
FROM t_student_task st
LEFT JOIN t_user u ON st.student_id = u.id
ORDER BY st.received_at DESC;

-- ==========================================
-- 第二步：修复学生班级信息（如果需要）
-- ==========================================

-- 如果学生的class_id为空，设置为默认班级
UPDATE t_user 
SET class_id = '1', 
    grade = '高一'
WHERE role = 'ROLE_STUDENT' 
  AND (class_id IS NULL OR class_id = '');

-- ==========================================
-- 第三步：创建测试任务
-- ==========================================

-- 删除旧的测试任务（如果存在）
DELETE FROM t_student_task WHERE task_id IN (
    SELECT id FROM t_task WHERE title LIKE '%测试任务%'
);
DELETE FROM t_task WHERE title LIKE '%测试任务%';

-- 插入测试任务
INSERT INTO t_task (
    teacher_id,
    teacher_name,
    class_id,
    class_name,
    title,
    content,
    task_type,
    deadline,
    priority,
    quantity_requirement,
    location,
    participants,
    ai_notes,
    status,
    created_at,
    updated_at
) VALUES (
    (SELECT id FROM t_user WHERE role = 'ROLE_TEACHER' LIMIT 1), -- 取第一个教师
    (SELECT real_name FROM t_user WHERE role = 'ROLE_TEACHER' LIMIT 1),
    '1', -- 班级ID
    '高一1班',
    '【测试任务】第三章错题整理',
    '同学们，本周五前完成第三章函数与导数的错题整理，每人至少录入5道错题，重点关注求导和极值问题。完成后记得标注知识点。',
    '错题整理',
    DATE_ADD(NOW(), INTERVAL 2 DAY), -- 2天后截止
    '普通',
    '至少5道题',
    NULL,
    NULL,
    'AI自动解析的任务信息',
    '已下发', -- 直接设置为已下发
    NOW(),
    NOW()
);

-- 获取刚创建的任务ID
SET @task_id = LAST_INSERT_ID();

-- 为所有class_id='1'的学生创建任务记录
INSERT INTO t_student_task (
    task_id,
    student_id,
    student_name,
    is_completed,
    received_at
)
SELECT 
    @task_id,
    id,
    real_name,
    FALSE,
    NOW()
FROM t_user
WHERE role = 'ROLE_STUDENT' 
  AND class_id = '1';

-- ==========================================
-- 第四步：验证结果
-- ==========================================

SELECT '=== 测试任务创建结果 ===' AS info;

SELECT 
    t.id,
    t.title,
    t.class_name,
    t.status,
    COUNT(st.id) as student_count
FROM t_task t
LEFT JOIN t_student_task st ON t.id = st.task_id
WHERE t.id = @task_id
GROUP BY t.id, t.title, t.class_name, t.status;

SELECT '=== 学生任务分配情况 ===' AS info;

SELECT 
    st.id,
    st.student_name,
    st.is_completed,
    u.username,
    u.class_id
FROM t_student_task st
JOIN t_user u ON st.student_id = u.id
WHERE st.task_id = @task_id;

SELECT '✅ 测试任务创建完成！' AS result;
SELECT '请刷新学生端"教师推送"页面查看效果' AS tip;

