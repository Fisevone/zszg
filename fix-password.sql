-- 修复用户密码
-- 将所有用户的密码更新为正确的BCrypt hash（密码：123456）
USE zszg;

-- 使用正确的BCrypt hash for "123456"
-- Hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

UPDATE t_user SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy';

SELECT '密码已更新！所有账号密码现在是：123456' AS message;
SELECT username, real_name, role FROM t_user;





