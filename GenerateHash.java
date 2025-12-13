import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerateHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String adminPassword = "password";
        String userPassword = "123456";
        
        String adminHash = encoder.encode(adminPassword);
        String userHash = encoder.encode(userPassword);
        
        System.out.println("Admin hash (password): " + adminHash);
        System.out.println("User hash (123456): " + userHash);
        
        // Verify
        System.out.println("\nVerifying admin: " + encoder.matches(adminPassword, adminHash));
        System.out.println("Verifying user: " + encoder.matches(userPassword, userHash));
    }
}
