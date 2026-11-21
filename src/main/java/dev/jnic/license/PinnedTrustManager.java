package dev.jnic.license;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import javax.net.ssl.X509TrustManager;

public class PinnedTrustManager implements X509TrustManager {
   public static final MessageDigest SHA256;

   @Override
   public void checkClientTrusted(X509Certificate[] values, String text) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void checkServerTrusted(X509Certificate[] values, String text) throws CertificateException {
      byte[] var9 = values[0].getPublicKey().getEncoded();
      if (!Arrays.equals(Base64.getDecoder().decode("o8A6OI58AOvUceqqKfs+TkfmCEubXzhYtXLi1ZWm3zI="), SHA256.digest(var9))) {
         throw new CertificateException("bad certificate");
      }
   }

   @Override
   public X509Certificate[] getAcceptedIssuers() {
      return new X509Certificate[0];
   }

   static {
      try {
         SHA256 = MessageDigest.getInstance("SHA-256");
      } catch (NoSuchAlgorithmException var1) {
         throw new ExceptionInInitializerError(var1);
      }
   }
}
