-- ============================================
-- 知错就改系统 - 数据验证SQL
-- 用于检查测试数据是否正确导入
-- ============================================

USE zszg;

SELECT '========================================' AS '';
SELECT '         数据验证报告' AS '';
SELECT '========================================' AS '';
SELECT '' AS '';

-- 1. 用户统计
SELECT '【1. 用户统计】' AS '';
SELECT 
    role AS '角色',
    COUNT(*) AS '数量',
    GROUP_CONCAT(real_name SEPARATOR ', ') AS '姓名列表'
FROM t_user 
GROUP BY role;
SELECT '' AS '';

-- 2. 知识点统计
SELECT '【2. 知识点统计】' AS '';
SELECT 
    subject AS '学科',
    COUNT(*) AS '知识点数量'
FROM t_knowledge 
GROUP BY subject;
SELECT '' AS '';

-- 3. 题目统计
SELECT '【3. 题目统计】' AS '';
SELECT 
    subject AS '学科',
    difficulty AS '难度',
    COUNT(*) AS '题目数量'
FROM t_question 
GROUP BY subject, difficulty
ORDER BY subject, 
    CASE difficulty 
        WHEN '简单' THEN 1 
        WHEN '中等' THEN 2 
        WHEN '困难' THEN 3 
    END;
SELECT '' AS '';

-- 4. 错题本统计
SELECT '【4. 错题本统计】' AS '';
SELECT 
    u.real_name AS '学生姓名',
    u.class_id AS '班级',
    COUNT(e.id) AS '错题数量',
    SUM(CASE WHEN e.status = 'PUBLIC' THEN 1 ELSE 0 END) AS '公开数量',
    SUM(CASE WHEN e.status = 'PRIVATE' THEN 1 ELSE 0 END) AS '私密数量'
FROM t_user u
LEFT JOIN t_error_book e ON u.id = e.user_id
WHERE u.role = 'ROLE_STUDENT'
GROUP BY u.id, u.real_name, u.class_id
HAVING COUNT(e.id) > 0
ORDER BY u.class_id, u.real_name;
SELECT '' AS '';

-- 5. 分享池统计
SELECT '【5. 分享池统计】' AS '';
SELECT 
    CASE WHEN approved = 1 THEN '已审核' ELSE '待审核' END AS '状态',
    scope AS '范围',
    COUNT(*) AS '数量',
    ROUND(AVG(likes), 1) AS '平均点赞数',
    ROUND(AVG(favorites), 1) AS '平均收藏数'
FROM t_share_pool 
GROUP BY approved, scope;
SELECT '' AS '';

-- 6. 学习资源统计
SELECT '【6. 学习资源统计】' AS '';
SELECT 
    subject AS '学科',
    type AS '类型',
    CASE WHEN approved = 1 THEN '已审核' ELSE '待审核' END AS '状态',
    COUNT(*) AS '数量'
FROM t_resource 
GROUP BY subject, type, approved
ORDER BY subject, type;
SELECT '' AS '';

-- 7. 题目-知识点关联统计
SELECT '【7. 题目-知识点关联统计】' AS '';
SELECT 
    COUNT(DISTINCT question_id) AS '已关联知识点的题目数',
    COUNT(*) AS '关联关系总数',
    ROUND(COUNT(*) / COUNT(DISTINCT question_id), 2) AS '平均每题关联知识点数'
FROM t_question_knowledge;
SELECT '' AS '';

-- 8. 热门分享（点赞最多的前5条）
SELECT '【8. 热门分享TOP5】' AS '';
SELECT 
    sp.id AS 'ID',
    u.real_name AS '分享者',
    LEFT(q.content, 30) AS '题目摘要',
    sp.likes AS '点赞数',
    sp.favorites AS '收藏数',
    sp.scope AS '范围'
FROM t_share_pool sp
JOIN t_error_book eb ON sp.error_book_id = eb.id
JOIN t_user u ON eb.user_id = u.id
JOIN t_question q ON eb.question_id = q.id
WHERE sp.approved = 1
ORDER BY sp.likes DESC
LIMIT 5;
SELECT '' AS '';

-- 9. 各班级错题分布
SELECT '【9. 各班级错题统计】' AS '';
SELECT 
    u.class_id AS '班级',
    COUNT(DISTINCT u.id) AS '学生人数',
    COUNT(e.id) AS '总错题数',
    ROUND(COUNT(e.id) / COUNT(DISTINCT u.id), 2) AS '人均错题数'
FROM t_user u
LEFT JOIN t_error_book e ON u.id = e.user_id
WHERE u.role = 'ROLE_STUDENT' AND u.class_id IS NOT NULL
GROUP BY u.class_id
ORDER BY u.class_id;
SELECT '' AS '';

-- 10. 数据完整性检查
SELECT '【10. 数据完整性检查】' AS '';
SELECT 
    '✓ 用户数据' AS '检查项',
    IF(COUNT(*) >= 20, '通过', '失败') AS '状态',
    CONCAT(COUNT(*), ' 人') AS '详情'
FROM t_user
UNION ALL
SELECT 
    '✓ 知识点数据',
    IF(COUNT(*) >= 50, '通过', '失败'),
    CONCAT(COUNT(*), ' 个')
FROM t_knowledge
UNION ALL
SELECT 
    '✓ 题目数据',
    IF(COUNT(*) >= 25, '通过', '失败'),
    CONCAT(COUNT(*), ' 道')
FROM t_question
UNION ALL
SELECT 
    '✓ 错题本数据',
    IF(COUNT(*) >= 15, '通过', '失败'),
    CONCAT(COUNT(*), ' 条')
FROM t_error_book
UNION ALL
SELECT 
    '✓ 分享池数据',
    IF(COUNT(*) >= 10, '通过', '失败'),
    CONCAT(COUNT(*), ' 条')
FROM t_share_pool
UNION ALL
SELECT 
    '✓ 学习资源数据',
    IF(COUNT(*) >= 20, '通过', '失败'),
    CONCAT(COUNT(*), ' 个')
FROM t_resource;
SELECT '' AS '';

SELECT '========================================' AS '';
SELECT '         验证完成！' AS '';
SELECT '========================================' AS '';





