-- 查询所有班级及学生数量
USE zszg;

SELECT 
    class_id AS '班级',
    COUNT(*) AS '学生人数',
    GROUP_CONCAT(real_name SEPARATOR ', ') AS '学生名单'
FROM t_user
WHERE role LIKE '%STUDENT%' AND class_id IS NOT NULL
GROUP BY class_id
ORDER BY class_id;













