import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePasswordHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 生成密码 "123456" 的hash
        String password = "123456";
        String hash = encoder.encode(password);
        
        System.out.println("密码: " + password);
        System.out.println("Hash: " + hash);
        
        // 验证hash是否正确
        boolean matches = encoder.matches(password, hash);
        System.out.println("验证结果: " + matches);
        
        // 验证数据库中的hash
        String dbHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        boolean dbMatches = encoder.matches(password, dbHash);
        System.out.println("数据库Hash验证: " + dbMatches);
    }
}





