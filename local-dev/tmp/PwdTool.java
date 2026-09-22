import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.Security;
public class PwdTool {
  public static SecretKey getDESSercretKey(String key) {
    byte[] result = new byte[8];
    byte[] keys = key.getBytes(StandardCharsets.UTF_8);
    for(int i=0;i<8;i++){ result[i] = i < keys.length ? keys[i] : (byte)0x01; }
    return new SecretKeySpec(result, "DES");
  }
  public static String encrypt(String pwd, String key) throws Exception {
    Security.addProvider(new com.sun.crypto.provider.SunJCE());
    Cipher cipher = Cipher.getInstance("DES");
    cipher.init(Cipher.ENCRYPT_MODE, getDESSercretKey(key));
    return java.util.Base64.getEncoder().encodeToString(cipher.doFinal(pwd.getBytes(StandardCharsets.UTF_8)));
  }
  public static String decrypt(String pwd, String key) throws Exception {
    Security.addProvider(new com.sun.crypto.provider.SunJCE());
    Cipher cipher = Cipher.getInstance("DES");
    cipher.init(Cipher.DECRYPT_MODE, getDESSercretKey(key));
    return new String(cipher.doFinal(java.util.Base64.getDecoder().decode(pwd)), StandardCharsets.UTF_8);
  }
  public static void main(String[] a) throws Exception {
    System.out.println("enc="+encrypt("123456","admin"));
    try { System.out.println("dec="+decrypt("7GljHQZJm2TlgEMdBnZiuQ==","admin")); } catch(Exception e){ System.out.println("decFail="+e.getMessage()); }
  }
}
