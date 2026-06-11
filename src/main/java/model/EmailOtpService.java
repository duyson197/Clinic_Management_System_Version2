package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class EmailOtpService {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT_SSL = 465;
    private static final String SENDER = "duongdamde2005@gmail.com";
    private static final String APP_PASSWORD = "xjqbdrsriadyiopm";

    private EmailOtpService() {
    }

    public static void sendOtp(String toEmail, String fullName, String otpCode, long ttlSeconds) throws IOException {
        String displayName = (fullName == null || fullName.isBlank()) ? "ban" : fullName;
        String subject = "[Phong kham ABC] Ma OTP xac thuc dang ky";
        String body = "Xin chao " + displayName + ",\r\n\r\n"
                + "Ma OTP cua ban la: " + otpCode + "\r\n"
                + "Ma co hieu luc trong " + ttlSeconds + " giay.\r\n\r\n"
                + "Neu ban khong yeu cau, vui long bo qua email nay.";
        sendPlainTextEmail(toEmail, subject, body);
    }

    public static void sendNewAccountPassword(String toEmail, String fullName, String tempPassword) throws IOException {
        String displayName = (fullName == null || fullName.isBlank()) ? "ban" : fullName;
        String subject = "[Phong kham ABC] Tai khoan moi da duoc tao";
        String body = "Xin chao " + displayName + ",\r\n\r\n"
                + "Tai khoan cua ban da duoc admin tao.\r\n"
                + "Email dang nhap: " + toEmail + "\r\n"
                + "Mat khau tam thoi: " + tempPassword + "\r\n\r\n"
                + "Vui long dang nhap va doi mat khau ngay.\r\n"
                + "Neu khong phai ban, vui long lien he phong kham.";
        sendPlainTextEmail(toEmail, subject, body);
    }

    private static void sendPlainTextEmail(String toEmail, String subject, String body) throws IOException {
        if (isBlank(SENDER) || isBlank(APP_PASSWORD)) {
            throw new IllegalStateException("Missing Gmail sender credentials");
        }

        try (SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(SMTP_HOST, SMTP_PORT_SSL);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

            expectOk(reader, "220");
            send(writer, "EHLO localhost");
            expectAny(reader, "250");

            send(writer, "AUTH LOGIN");
            expectOk(reader, "334");
            send(writer, base64(SENDER));
            expectOk(reader, "334");
            send(writer, base64(APP_PASSWORD));
            expectOk(reader, "235");

            send(writer, "MAIL FROM:<" + SENDER + ">");
            expectOk(reader, "250");
            send(writer, "RCPT TO:<" + toEmail + ">");
            expectOk(reader, "250");
            send(writer, "DATA");
            expectOk(reader, "354");

            sendRaw(writer,
                    "From: " + SENDER + "\r\n"
                    + "To: " + toEmail + "\r\n"
                    + "Subject: " + subject + "\r\n"
                    + "MIME-Version: 1.0\r\n"
                    + "Content-Type: text/plain; charset=UTF-8\r\n"
                    + "\r\n"
                    + body + "\r\n"
                    + ".\r\n");
            expectOk(reader, "250");

            send(writer, "QUIT");
        }
    }

    private static void send(BufferedWriter writer, String command) throws IOException {
        sendRaw(writer, command + "\r\n");
    }

    private static void sendRaw(BufferedWriter writer, String text) throws IOException {
        writer.write(text);
        writer.flush();
    }

    private static void expectOk(BufferedReader reader, String expectedPrefix) throws IOException {
        String line = reader.readLine();
        if (line == null || !line.startsWith(expectedPrefix)) {
            throw new IOException("SMTP error: " + line);
        }
    }

    private static void expectAny(BufferedReader reader, String expectedPrefix) throws IOException {
        String line;
        boolean ok = false;
        do {
            line = reader.readLine();
            if (line == null) {
                break;
            }
            if (line.startsWith(expectedPrefix)) {
                ok = true;
            }
        } while (line.length() > 3 && line.charAt(3) == '-');

        if (!ok) {
            throw new IOException("SMTP error: " + line);
        }
    }

    private static String base64(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
