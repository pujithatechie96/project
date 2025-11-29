import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Encoders;

public class GenerateJwtKey {
    public static void main(String[] args) {
        var key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        System.out.println("Secure Key: " + Encoders.BASE64.encode(key.getEncoded()));
    }
}
