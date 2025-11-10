@echo off
chcp 65001 >nul
echo ========================================
echo 快速插入测试数据
echo ========================================
echo.

echo [1/6] 插入测试学生用户...
mysql -u root -pg54571571 zszg -e "INSERT IGNORE INTO t_user (username, password_hash, real_name, email, role, status) VALUES ('张三', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhka', '张三', 'zhangsan@test.com', 'ROLE_STUDENT', 'ACTIVE');"
mysql -u root -pg54571571 zszg -e "INSERT IGNORE INTO t_user (username, password_hash, real_name, email, role, status) VALUES ('李四', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhka', '李四', 'lisi@test.com', 'ROLE_STUDENT', 'ACTIVE');"
mysql -u root -pg54571571 zszg -e "INSERT IGNORE INTO t_user (username, password_hash, real_name, email, role, status) VALUES ('王五', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhka', '王五', 'wangwu@test.com', 'ROLE_STUDENT', 'ACTIVE');"
echo ✓ 学生用户插入完成

echo.
echo [2/6] 插入测试题目...
mysql -u root -pg54571571 zszg -e "INSERT IGNORE INTO t_question (content, answer, difficulty, subject, tags, source, created_at) VALUES ('求函数f(x)=x²-2x+1的最小值', '最小值为0，当x=1时取得', '中等', '数学', '函数,二次函数', '期中考试', NOW());"
mysql -u root -pg54571571 zszg -e "INSERT IGNORE INTO t_question (content, answer, difficulty, subject, tags, source, created_at) VALUES ('计算物体从10米高处自由落体所需时间（g=10m/s²）', 't=√(2h/g)=√2≈1.4秒', '简单', '物理', '运动学,自由落体', '课后练习', NOW());"
mysql -u root -pg54571571 zszg -e "INSERT IGNORE INTO t_question (content, answer, difficulty, subject, tags, source, created_at) VALUES ('英语中现在完成时的基本结构是什么？', 'have/has + 过去分词', '简单', '英语', '语法,时态', '语法测试', NOW());"
mysql -u root -pg54571571 zszg -e "INSERT IGNORE INTO t_question (content, answer, difficulty, subject, tags, source, created_at) VALUES ('解方程组：x+y=5, x-y=1', 'x=3, y=2', '中等', '数学', '方程组', '月考', NOW());"
mysql -u root -pg54571571 zszg -e "INSERT IGNORE INTO t_question (content, answer, difficulty, subject, tags, source, created_at) VALUES ('化学式NaCl中Na的化合价是多少？', '+1价', '简单', '化学', '化合价', '随堂测验', NOW());"
echo ✓ 题目插入完成

echo.
echo [3/6] 查询用户和题目ID...
mysql -u root -pg54571571 zszg -e "SET @student1_id = (SELECT id FROM t_user WHERE username = '张三' LIMIT 1);"
echo ✓ ID查询完成

echo.
echo [4/6] 插入错题记录...
mysql -u root -pg54571571 zszg -e "INSERT INTO t_error_book (user_id, question_id, error_reason, correction, my_answer, status, created_at) SELECT (SELECT id FROM t_user WHERE username='张三' LIMIT 1), (SELECT id FROM t_question WHERE content LIKE '求函数f(x)=x²%%' LIMIT 1), '忘记配方，计算错误', '应该配方：f(x)=(x-1)²，最小值为0', 'f(x)最小值为1', 'PRIVATE', NOW() WHERE NOT EXISTS (SELECT 1 FROM t_error_book WHERE user_id=(SELECT id FROM t_user WHERE username='张三' LIMIT 1) AND question_id=(SELECT id FROM t_question WHERE content LIKE '求函数f(x)=x²%%' LIMIT 1));"
mysql -u root -pg54571571 zszg -e "INSERT INTO t_error_book (user_id, question_id, error_reason, correction, my_answer, status, created_at) SELECT (SELECT id FROM t_user WHERE username='张三' LIMIT 1), (SELECT id FROM t_question WHERE content LIKE '计算物体%%' LIMIT 1), '公式记错了', '正确公式是t=√(2h/g)', '直接用t=h/g计算', 'SHARED', DATE_SUB(NOW(), INTERVAL 1 DAY) WHERE NOT EXISTS (SELECT 1 FROM t_error_book WHERE user_id=(SELECT id FROM t_user WHERE username='张三' LIMIT 1) AND question_id=(SELECT id FROM t_question WHERE content LIKE '计算物体%%' LIMIT 1));"
mysql -u root -pg54571571 zszg -e "INSERT INTO t_error_book (user_id, question_id, error_reason, correction, my_answer, status, created_at) SELECT (SELECT id FROM t_user WHERE username='李四' LIMIT 1), (SELECT id FROM t_question WHERE content LIKE '英语中%%' LIMIT 1), '时态混淆', '现在完成时表示过去发生对现在有影响的动作', '写成了have+动词原形', 'SHARED', DATE_SUB(NOW(), INTERVAL 2 DAY) WHERE NOT EXISTS (SELECT 1 FROM t_error_book WHERE user_id=(SELECT id FROM t_user WHERE username='李四' LIMIT 1) AND question_id=(SELECT id FROM t_question WHERE content LIKE '英语中%%' LIMIT 1));"
echo ✓ 错题记录插入完成

echo.
echo [5/6] 插入共享池记录...
mysql -u root -pg54571571 zszg -e "INSERT INTO t_share_pool (error_book_id, user_id, scope, tags, status, shared_at) SELECT eb.id, eb.user_id, 'PUBLIC', '常见错误,值得分享', 'PENDING', eb.created_at FROM t_error_book eb WHERE eb.status = 'SHARED' AND NOT EXISTS (SELECT 1 FROM t_share_pool sp WHERE sp.error_book_id = eb.id);"
echo ✓ 共享池记录插入完成

echo.
echo [6/6] 验证数据...
mysql -u root -pg54571571 zszg -e "SELECT COUNT(*) AS '学生数' FROM t_user WHERE role='ROLE_STUDENT';"
mysql -u root -pg54571571 zszg -e "SELECT COUNT(*) AS '题目数' FROM t_question;"
mysql -u root -pg54571571 zszg -e "SELECT COUNT(*) AS '错题数' FROM t_error_book;"
mysql -u root -pg54571571 zszg -e "SELECT COUNT(*) AS '待审核共享' FROM t_share_pool WHERE status='PENDING';"

echo.
echo ========================================
echo ✅ 测试数据插入完成！
echo ========================================
echo.
echo 现在刷新浏览器页面，你将看到真实数据！
echo.
pause



