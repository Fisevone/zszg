-- 插入测试数据
USE zszg;

-- 1. 插入几个学生用户（如果不存在）
INSERT IGNORE INTO t_user (username, password_hash, real_name, email, role, status) VALUES
('张三', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhka', '张三', 'zhangsan@test.com', 'ROLE_STUDENT', 'ACTIVE'),
('李四', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhka', '李四', 'lisi@test.com', 'ROLE_STUDENT', 'ACTIVE'),
('王五', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhka', '王五', 'wangwu@test.com', 'ROLE_STUDENT', 'ACTIVE');

-- 密码都是: password123

-- 2. 获取学生ID（用于后续插入）
SET @student1_id = (SELECT id FROM t_user WHERE username = '张三' LIMIT 1);
SET @student2_id = (SELECT id FROM t_user WHERE username = '李四' LIMIT 1);
SET @student3_id = (SELECT id FROM t_user WHERE username = '王五' LIMIT 1);

-- 3. 插入测试题目
INSERT IGNORE INTO t_question (content, answer, difficulty, subject, tags, source, created_at) VALUES
('求函数f(x)=x²-2x+1的最小值', '最小值为0，当x=1时取得', '中等', '数学', '函数,二次函数', '期中考试', NOW()),
('计算物体从10米高处自由落体所需时间（g=10m/s²）', 't=√(2h/g)=√2≈1.4秒', '简单', '物理', '运动学,自由落体', '课后练习', NOW()),
('英语中现在完成时的基本结构是什么？', 'have/has + 过去分词', '简单', '英语', '语法,时态', '语法测试', NOW()),
('解方程组：x+y=5, x-y=1', 'x=3, y=2', '中等', '数学', '方程组', '月考', NOW()),
('化学式NaCl中Na的化合价是多少？', '+1价', '简单', '化学', '化合价', '随堂测验', NOW());

-- 4. 获取题目ID
SET @question1_id = (SELECT id FROM t_question WHERE content LIKE '求函数f(x)=x²-2x+1%' LIMIT 1);
SET @question2_id = (SELECT id FROM t_question WHERE content LIKE '计算物体从10米高处%' LIMIT 1);
SET @question3_id = (SELECT id FROM t_question WHERE content LIKE '英语中现在完成时%' LIMIT 1);
SET @question4_id = (SELECT id FROM t_question WHERE content LIKE '解方程组%' LIMIT 1);
SET @question5_id = (SELECT id FROM t_question WHERE content LIKE '化学式NaCl%' LIMIT 1);

-- 5. 插入错题记录
INSERT INTO t_error_book (user_id, question_id, error_reason, correction, my_answer, status, created_at) VALUES
(@student1_id, @question1_id, '忘记配方，计算错误', '应该配方：f(x)=(x-1)²，最小值为0', 'f(x)最小值为1', 'PRIVATE', NOW()),
(@student1_id, @question2_id, '公式记错了', '正确公式是t=√(2h/g)', '直接用t=h/g计算', 'SHARED', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(@student2_id, @question3_id, '时态混淆', '现在完成时表示过去发生对现在有影响的动作', '写成了have+动词原形', 'SHARED', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(@student2_id, @question4_id, '计算粗心', '将两式相加得2x=6，所以x=3，代入得y=2', '算成x=2, y=3了', 'PRIVATE', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(@student3_id, @question5_id, '概念不清', 'NaCl中Na失去一个电子，显+1价', '记成了-1价', 'PRIVATE', DATE_SUB(NOW(), INTERVAL 1 HOUR));

-- 6. 插入共享池记录（用于教师审核）
INSERT INTO t_share_pool (error_book_id, user_id, scope, tags, status, shared_at)
SELECT 
    eb.id,
    eb.user_id,
    'PUBLIC',
    '常见错误,值得分享',
    'PENDING',
    eb.created_at
FROM t_error_book eb
WHERE eb.status = 'SHARED'
AND NOT EXISTS (SELECT 1 FROM t_share_pool sp WHERE sp.error_book_id = eb.id);

-- 7. 插入知识点
INSERT IGNORE INTO t_knowledge (name, subject, category, description, created_at) VALUES
('二次函数', '数学', '函数', '二次函数的性质和图像', NOW()),
('自由落体运动', '物理', '力学', '物体只在重力作用下的运动', NOW()),
('现在完成时', '英语', '语法', '表示过去发生对现在有影响的动作', NOW()),
('二元一次方程组', '数学', '方程', '含有两个未知数的方程组', NOW()),
('化合价', '化学', '基础概念', '元素在化合物中的化合价', NOW());

SELECT '✅ 测试数据插入完成！' AS 'Status';
SELECT '学生用户数:', COUNT(*) FROM t_user WHERE role = 'ROLE_STUDENT';
SELECT '题目数:', COUNT(*) FROM t_question;
SELECT '错题数:', COUNT(*) FROM t_error_book;
SELECT '待审核共享:', COUNT(*) FROM t_share_pool WHERE status = 'PENDING';



