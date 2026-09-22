import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Security;
public class PwdTool2 {
  public static SecretKey key(String k) {
    byte[] result = new byte[8];
    byte[] keys = k.getBytes(StandardCharsets.UTF_8);
    for(int i=0;i<8;i++){ result[i] = i < keys.length ? keys[i] : (byte)0x01; }
    return new SecretKeySpec(result, "DES");
  }
  public static void main(String[] a) throws Exception {
    String account = a[0];
    String pwd = a[1];
    Security.addProvider(new com.sun.crypto.provider.SunJCE());
    Cipher c = Cipher.getInstance("DES");
    c.init(Cipher.ENCRYPT_MODE, key(account));
    String enc = java.util.Base64.getEncoder().encodeToString(c.doFinal(pwd.getBytes(StandardCharsets.UTF_8)));
    System.out.println(enc);
  }
}
