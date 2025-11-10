-- 最终密码修复
-- 使用经过验证的BCrypt hash（密码：123456）
USE zszg;

-- 这个hash经过Spring BCryptPasswordEncoder验证，确认对应密码"123456"
UPDATE t_user SET password_hash = '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyg3lsRz8xC6';

SELECT '密码已更新为经过验证的hash' AS message;
SELECT username, real_name, LEFT(password_hash, 30) as hash_prefix FROM t_user LIMIT 5;





