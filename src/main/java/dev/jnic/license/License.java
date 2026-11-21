package dev.jnic.license;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import dev.jnic.Jnic;
import dev.jnic.logging.JnicLogger;
import dev.jnic.utils.Base64Codec;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Objects;
import java.util.function.Function;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

public class License {
   private final String holder;
   private final String expiration;
   private final String edition;

   private License(String holder, String expiration, String edition) {
      this.holder = holder;
      this.expiration = expiration;
      this.edition = edition;
   }

   public static License defaultLicense() {
      return new License(configuredLicenseValue(System.getenv("JNIC_HOLDER"), "JNIC"), "Never", "DEV");
   }

   public String getHolder() {
      return this.holder;
   }

   public String getExpiration() {
      return this.expiration;
   }

   private static String configuredLicenseValue(String text, String text2) {
      return text != null && !text.isEmpty() ? text : text2;
   }

   public String licenseType() {
      String var10000;
      switch (this.edition) {
         case "DEV":
            var10000 = "Dev";
            break;
         case "1":
            var10000 = "standard";
            break;
         case "2":
            var10000 = "small developer";
            break;
         case "3":
            var10000 = "evaluation";
            break;
         default:
            var10000 = null;
      }

      return var10000;
   }

   public static String hardwareId() {
      try {
         byte[] var1 = Objects.requireNonNull(NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress());
         return Base64Codec.encodeToString(PinnedTrustManager.SHA256.digest(var1));
      } catch (SocketException | UnknownHostException | NullPointerException var7) {
         try {
            ArrayList var4 = new ArrayList();
            Enumeration var2 = NetworkInterface.getNetworkInterfaces();

            while (var2.hasMoreElements()) {
               NetworkInterface var3 = (NetworkInterface)var2.nextElement();
               if (var3.getHardwareAddress() != null) {
                  var4.add(var3);
               }
            }

            Function<NetworkInterface, String> function5 = NetworkInterface::getName;
            var4.sort(Comparator.comparing(function5));
            return Base64Codec.encodeToString(PinnedTrustManager.SHA256.digest(((NetworkInterface)var4.get(0)).getHardwareAddress()));
         } catch (SocketException var6) {
            return null;
         }
      }
   }

   private static String activationParameters(String text, String text2) {
      String value2;
      try {
         value2 = InetAddress.getLocalHost().getHostName();
      } catch (UnknownHostException var6) {
         value2 = null;
      }

      String value3 = System.getProperty("user.name");
      String value4 = hardwareId();
      String value5 = Jnic.class.getPackage().getImplementationVersion();
      return "token="
         + URLEncoder.encode(text, StandardCharsets.UTF_8)
         + "&app_id="
         + URLEncoder.encode(text2, StandardCharsets.UTF_8)
         + "&machine="
         + encodeParameter(value2)
         + "&user="
         + encodeParameter(value3)
         + "&hwid="
         + encodeParameter(value4)
         + "&version="
         + encodeParameter(value5);
   }

   private static String encodeParameter(String text) {
      return URLEncoder.encode(Objects.toString(text), StandardCharsets.UTF_8);
   }

   public static boolean activate(String text) {
      try {
         SSLContext var1 = SSLContext.getInstance("TLS");
         var1.init(null, new TrustManager[]{new PinnedTrustManager()}, new SecureRandom());
         HttpsURLConnection.setDefaultSSLSocketFactory(var1.getSocketFactory());
         HttpsURLConnection var4 = (HttpsURLConnection)new URL("https://activation.jnic.dev/api/activate").openConnection();
         var4.setRequestMethod("POST");
         var4.setDoOutput(true);
         String[] var2 = text.split("/");
         if (var2.length != 2) {
            throw new IOException("Invalid key format");
         }

         byte[] var3 = activationParameters(var2[0], var2[1]).getBytes(StandardCharsets.UTF_8);
         var4.setRequestProperty("Accept", "application/json");
         var4.setFixedLengthStreamingMode(var3.length);
         var4.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
         var4.connect();

         try (OutputStream var8 = var4.getOutputStream()) {
            var8.write(var3);
         }

         if (var4.getResponseCode() == 201) {
            JsonObject var9 = Json.parse(new InputStreamReader(var4.getInputStream())).asObject();
            String value13 = var9.getString("result", "failure");
            if (value13 == null) {
               throw new NullPointerException("");
            }

            if (value13.equals("ok")) {
               try (PrintWriter var6 = new PrintWriter(new FileOutputStream("jnic.licence", false))) {
                  var6.print(text);
               }

               JnicLogger.INSTANCE.info("Successfully activated");
               JnicLogger.INSTANCE.info("Remaining activations: " + var9.getString("remainingActivations", "N/A"));
               return true;
            }
         }

         JsonObject var11 = Json.parse(new InputStreamReader(var4.getErrorStream())).asObject();
         JnicLogger.INSTANCE.error("Activation failed: " + var11.getString("error", "unknown error"));
         JsonValue var14 = var11.get("support_message");
         if (var14 != null && !var14.isNull() && var14.isString()) {
            JnicLogger.INSTANCE.error(var14.asString());
         }

         return false;
      } catch (GeneralSecurityException var19) {
         JnicLogger.INSTANCE.error("Activation failed: security error");
         return false;
      } catch (IOException var20) {
         JnicLogger.INSTANCE.error("Activation failed: could not connect to licencing server");
         return false;
      }
   }
}
