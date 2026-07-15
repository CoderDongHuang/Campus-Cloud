import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class HashGen { 
  public static void main(String[] a) { 
    BCryptPasswordEncoder e = new BCryptPasswordEncoder();
    System.out.println("admin:" + e.encode("admin123"));
    System.out.println("student:" + e.encode("test123"));
    System.out.println("worker:" + e.encode("test123"));
  }
}
