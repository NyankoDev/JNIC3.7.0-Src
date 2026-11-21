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
      String literalText;
      switch (this.edition) {
         case "DEV":
            literalText = "Dev";
            break;
         case "1":
            literalText = "standard";
            break;
         case "2":
            literalText = "small developer";
            break;
         case "3":
            literalText = "evaluation";
            break;
         default:
            literalText = null;
      }

      return literalText;
   }

   public static String hardwareId() {
      try {
         byte[] bytes = Objects.requireNonNull(NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress());
         return Base64Codec.encodeToString(PinnedTrustManager.SHA256.digest(bytes));
      } catch (SocketException | UnknownHostException | NullPointerException failure) {
         try {
            ArrayList interfacesWithAddresses = new ArrayList();
            Enumeration networkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (networkInterfaces.hasMoreElements()) {
               NetworkInterface networkInterface = (NetworkInterface)networkInterfaces.nextElement();
               if (networkInterface.getHardwareAddress() != null) {
                  interfacesWithAddresses.add(networkInterface);
               }
            }

            Function<NetworkInterface, String> nameMapper = NetworkInterface::getName;
            interfacesWithAddresses.sort(Comparator.comparing(nameMapper));
            return Base64Codec.encodeToString(PinnedTrustManager.SHA256.digest(((NetworkInterface)interfacesWithAddresses.get(0)).getHardwareAddress()));
         } catch (SocketException socketFailure) {
            return null;
         }
      }
   }

   private static String activationParameters(String text, String text2) {
      String hostName;
      try {
         hostName = InetAddress.getLocalHost().getHostName();
      } catch (UnknownHostException unknownHostFailure) {
         hostName = null;
      }

      String property = System.getProperty("user.name");
      String hardwareId = hardwareId();
      String implementationVersion = Jnic.class.getPackage().getImplementationVersion();
      return "token="
         + URLEncoder.encode(text, StandardCharsets.UTF_8)
         + "&app_id="
         + URLEncoder.encode(text2, StandardCharsets.UTF_8)
         + "&machine="
         + encodeParameter(hostName)
         + "&user="
         + encodeParameter(property)
         + "&hwid="
         + encodeParameter(hardwareId)
         + "&version="
         + encodeParameter(implementationVersion);
   }

   private static String encodeParameter(String text) {
      return URLEncoder.encode(Objects.toString(text), StandardCharsets.UTF_8);
   }

   public static boolean activate(String text) {
      try {
         SSLContext instance = SSLContext.getInstance("TLS");
         instance.init(null, new TrustManager[]{new PinnedTrustManager()}, new SecureRandom());
         HttpsURLConnection.setDefaultSSLSocketFactory(instance.getSocketFactory());
         HttpsURLConnection httpsURLConnection = (HttpsURLConnection)new URL("https://activation.jnic.dev/api/activate").openConnection();
         httpsURLConnection.setRequestMethod("POST");
         httpsURLConnection.setDoOutput(true);
         String[] strings = text.split("/");
         if (strings.length != 2) {
            throw new IOException("Invalid key format");
         }

         byte[] encodedBytes = activationParameters(strings[0], strings[1]).getBytes(StandardCharsets.UTF_8);
         httpsURLConnection.setRequestProperty("Accept", "application/json");
         httpsURLConnection.setFixedLengthStreamingMode(encodedBytes.length);
         httpsURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
         httpsURLConnection.connect();

         try (OutputStream outputStream = httpsURLConnection.getOutputStream()) {
            outputStream.write(encodedBytes);
         }

         if (httpsURLConnection.getResponseCode() == 201) {
            JsonObject jsonObject = Json.parse(new InputStreamReader(httpsURLConnection.getInputStream())).asObject();
            String string = jsonObject.getString("result", "failure");
            if (string == null) {
               throw new NullPointerException("");
            }

            if (string.equals("ok")) {
               try (PrintWriter printWriter = new PrintWriter(new FileOutputStream("jnic.licence", false))) {
                  printWriter.print(text);
               }

               JnicLogger.INSTANCE.info("Successfully activated");
               JnicLogger.INSTANCE.info("Remaining activations: " + jsonObject.getString("remainingActivations", "N/A"));
               return true;
            }
         }

         JsonObject jsonObject2 = Json.parse(new InputStreamReader(httpsURLConnection.getErrorStream())).asObject();
         JnicLogger.INSTANCE.error("Activation failed: " + jsonObject2.getString("error", "unknown error"));
         JsonValue jsonValue = jsonObject2.get("support_message");
         if (jsonValue != null && !jsonValue.isNull() && jsonValue.isString()) {
            JnicLogger.INSTANCE.error(jsonValue.asString());
         }

         return false;
      } catch (GeneralSecurityException generalSecurityFailure) {
         JnicLogger.INSTANCE.error("Activation failed: security error");
         return false;
      } catch (IOException ioFailure) {
         JnicLogger.INSTANCE.error("Activation failed: could not connect to licencing server");
         return false;
      }
   }
}
