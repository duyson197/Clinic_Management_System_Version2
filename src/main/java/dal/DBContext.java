package dal;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author FPT University - PRJ30X
 */
public class DBContext {

    public Connection connection;

    public DBContext() {
        try {
            Properties properties = new Properties();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("ConnectDB.properties");
            if (inputStream == null) {
                throw new RuntimeException("Không tìm thấy ConnectDB.properties");
            }
            try {
                properties.load(inputStream);
            } catch (IOException ex) {
                Logger.getLogger(DBContext.class.getName()).log(Level.SEVERE, null, ex);
            }
            String user = properties.getProperty("user");
            String pass = properties.getProperty("password");
            String url = properties.getProperty("url");
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, user, pass);
            System.out.println("DB CONNECTED SUCCESS");
            System.out.println("DB connected: " + connection);
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(DBContext.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static void main(String[] args) {
    System.out.println("=== TEST DB CONNECTION ===");

    DBContext db = new DBContext();

    if (db.connection != null) {
        System.out.println("✅ Kết nối thành công!");
    } else {
        System.out.println("❌ Kết nối thất bại!");
    }
}
}
