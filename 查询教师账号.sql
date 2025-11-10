-- 查询所有教师账号
USE zszg;

SELECT 
    id,
    username AS '用户名',
    real_name AS '真实姓名',
    role AS '角色',
    status AS '状态',
    email AS '邮箱'
FROM t_user
WHERE role LIKE '%TEACHER%'
ORDER BY id;

-- 查询所有用户（包括管理员、教师、学生）
SELECT 
    id,
    username AS '用户名',
    real_name AS '真实姓名',
    role AS '角色',
    status AS '状态',
    class_id AS '班级'
FROM t_user
ORDER BY 
    CASE 
        WHEN role LIKE '%ADMIN%' THEN 1
        WHEN role LIKE '%TEACHER%' THEN 2
        ELSE 3
    END,
    id;













