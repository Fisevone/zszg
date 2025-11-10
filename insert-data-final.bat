@echo off
chcp 65001 >nul
cls
echo.
echo ========================================
echo    插入测试数据到数据库
echo ========================================
echo.

echo 正在连接数据库并插入测试数据...
echo.

echo [1/5] 插入测试用户...
mysql -u root -pg54571571 zszg -e "INSERT IGNORE INTO t_user (username, password_hash, real_name, email, role, status) VALUES ('zhangsan', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhka', '张三', 'zhangsan@test.com', 'ROLE_STUDENT', 'ACTIVE');" 2>nul
mysql -u root -pg54571571 zszg -e "INSERT IGNORE INTO t_user (username, password_hash, real_name, email, role, status) VALUES ('lisi', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhka', '李四', 'lisi@test.com', 'ROLE_STUDENT', 'ACTIVE');" 2>nul
mysql -u root -pg54571571 zszg -e "INSERT IGNORE INTO t_user (username, password_hash, real_name, email, role, status) VALUES ('wangwu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhka', '王五', 'wangwu@test.com', 'ROLE_STUDENT', 'ACTIVE');" 2>nul
echo ✓ 用户数据插入完成

echo.
echo [2/5] 插入测试题目...
mysql -u root -pg54571571 zszg -e "INSERT IGNORE INTO t_question (id, content, answer, difficulty, subject, tags, source, created_at) VALUES (1001, '求函数f(x)=x²-2x+1的最小值', '最小值为0，当x=1时取得', '中等', '数学', '函数,二次函数', '期中考试', NOW());" 2>nul
mysql -u root -pg54571571 zszg -e "INSERT IGNORE INTO t_question (id, content, answer, difficulty, subject, tags, source, created_at) VALUES (1002, '计算物体从10米高处自由落体所需时间（g=10m/s²）', 't=√(2h/g)=√2≈1.4秒', '简单', '物理', '运动学,自由落体', '课后练习', NOW());" 2>nul
mysql -u root -pg54571571 zszg -e "INSERT IGNORE INTO t_question (id, content, answer, difficulty, subject, tags, source, created_at) VALUES (1003, '英语中现在完成时的基本结构是什么？', 'have/has + 过去分词', '简单', '英语', '语法,时态', '语法测试', NOW());" 2>nul
mysql -u root -pg54571571 zszg -e "INSERT IGNORE INTO t_question (id, content, answer, difficulty, subject, tags, source, created_at) VALUES (1004, '解方程组：x+y=5, x-y=1', 'x=3, y=2', '中等', '数学', '方程组', '月考', NOW());" 2>nul
mysql -u root -pg54571571 zszg -e "INSERT IGNORE INTO t_question (id, content, answer, difficulty, subject, tags, source, created_at) VALUES (1005, '化学式NaCl中Na的化合价是多少？', '+1价', '简单', '化学', '化合价', '随堂测验', NOW());" 2>nul
echo ✓ 题目数据插入完成

echo.
echo [3/5] 插入错题记录...
mysql -u root -pg54571571 zszg -e "INSERT IGNORE INTO t_error_book (user_id, question_id, error_reason, correction, my_answer, status, created_at) SELECT u.id, 1001, '忘记配方，计算错误', '应该配方：f(x)=(x-1)²，最小值为0', 'f(x)最小值为1', 'PRIVATE', NOW() FROM t_user u WHERE u.username='zhangsan' LIMIT 1;" 2>nul
mysql -u root -pg54571571 zszg -e "INSERT IGNORE INTO t_error_book (user_id, question_id, error_reason, correction, my_answer, status, created_at) SELECT u.id, 1002, '公式记错了', '正确公式是t=√(2h/g)', '直接用t=h/g计算', 'SHARED', DATE_SUB(NOW(), INTERVAL 1 DAY) FROM t_user u WHERE u.username='zhangsan' LIMIT 1;" 2>nul
mysql -u root -pg54571571 zszg -e "INSERT IGNORE INTO t_error_book (user_id, question_id, error_reason, correction, my_answer, status, created_at) SELECT u.id, 1003, '时态混淆', '现在完成时表示过去发生对现在有影响的动作', '写成了have+动词原形', 'SHARED', DATE_SUB(NOW(), INTERVAL 2 DAY) FROM t_user u WHERE u.username='lisi' LIMIT 1;" 2>nul
mysql -u root -pg54571571 zszg -e "INSERT IGNORE INTO t_error_book (user_id, question_id, error_reason, correction, my_answer, status, created_at) SELECT u.id, 1004, '计算粗心', '将两式相加得2x=6，所以x=3，代入得y=2', '算成x=2, y=3了', 'PRIVATE', DATE_SUB(NOW(), INTERVAL 3 DAY) FROM t_user u WHERE u.username='lisi' LIMIT 1;" 2>nul
mysql -u root -pg54571571 zszg -e "INSERT IGNORE INTO t_error_book (user_id, question_id, error_reason, correction, my_answer, status, created_at) SELECT u.id, 1005, '概念不清', 'NaCl中Na失去一个电子，显+1价', '记成了-1价', 'PRIVATE', DATE_SUB(NOW(), INTERVAL 1 HOUR) FROM t_user u WHERE u.username='wangwu' LIMIT 1;" 2>nul
echo ✓ 错题记录插入完成

echo.
echo [4/5] 插入共享池记录...
mysql -u root -pg54571571 zszg -e "INSERT IGNORE INTO t_share_pool (error_book_id, user_id, scope, tags, status, shared_at) SELECT eb.id, eb.user_id, 'PUBLIC', '常见错误,值得分享', 'PENDING', eb.created_at FROM t_error_book eb WHERE eb.status = 'SHARED' AND NOT EXISTS (SELECT 1 FROM t_share_pool sp WHERE sp.error_book_id = eb.id);" 2>nul
echo ✓ 共享池记录插入完成

echo.
echo [5/5] 验证插入结果...
echo.
mysql -u root -pg54571571 zszg -e "SELECT COUNT(*) AS '学生总数' FROM t_user WHERE role='ROLE_STUDENT';" 2>nul
mysql -u root -pg54571571 zszg -e "SELECT COUNT(*) AS '题目总数' FROM t_question;" 2>nul
mysql -u root -pg54571571 zszg -e "SELECT COUNT(*) AS '错题总数' FROM t_error_book;" 2>nul
mysql -u root -pg54571571 zszg -e "SELECT COUNT(*) AS '待审核共享' FROM t_share_pool WHERE status='PENDING';" 2>nul

echo.
echo ========================================
echo ✅  测试数据插入完成！
echo ========================================
echo.
echo 📝 测试账号信息：
echo    用户名：zhangsan / lisi / wangwu
echo    密码：  password123
echo.
echo 💡 现在刷新浏览器，教师端将显示真实数据！
echo.
pause


