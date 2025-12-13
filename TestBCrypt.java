import org.springframework.security.crypto.bcrypt.BCrypt;

public class TestBCrypt {
    public static void main(String[] args) {
        String adminHash = "$2a$10$xMYKVCWnL.7XEXVHx6rLheJuCr3e2MdDlDjWEq7D3WKpvUe.YIJpu";
        String userHash = "$2a$10$vDM9BQCO0rPTvKMb4PQg6u6TbZsVxCJGiMUhq9gT5F8z/GKmJNvvy";
        
        System.out.println("Testing admin/password:");
        System.out.println("Hash: " + adminHash);
        System.out.println("Match: " + BCrypt.checkpw("password", adminHash));
        
        System.out.println("\nTesting user/123456:");
        System.out.println("Hash: " + userHash);
        System.out.println("Match: " + BCrypt.checkpw("123456", userHash));
        
        // Generate new hashes
        System.out.println("\n--- NEW HASHES ---");
        String newAdminHash = BCrypt.hashpw("password", BCrypt.gensalt());
        String newUserHash = BCrypt.hashpw("123456", BCrypt.gensalt());
        
        System.out.println("New admin hash: " + newAdminHash);
        System.out.println("Verify: " + BCrypt.checkpw("password", newAdminHash));
        
        System.out.println("\nNew user hash: " + newUserHash);
        System.out.println("Verify: " + BCrypt.checkpw("123456", newUserHash));
    }
}
