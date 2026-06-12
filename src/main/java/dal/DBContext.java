package dal;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enterprise DBContext structure with HikariCP
 */
public class DBContext {

    private static HikariDataSource dataSource;

    static {
        try {
            Properties properties = new Properties();
            InputStream inputStream = DBContext.class.getClassLoader().getResourceAsStream("ConnectDB.properties");
            if (inputStream == null) {
                throw new RuntimeException("Không tìm thấy ConnectDB.properties");
            }
            properties.load(inputStream);
            
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(properties.getProperty("url"));
            config.setUsername(properties.getProperty("user"));
            config.setPassword(properties.getProperty("password"));
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            
            // Standard Enterprise Connection Pool Settings
            config.setMaximumPoolSize(20);
            config.setMinimumIdle(5);
            config.setIdleTimeout(30000);
            config.setConnectionTimeout(30000);
            config.setMaxLifetime(1800000);
            
            dataSource = new HikariDataSource(config);
            System.out.println("HikariCP DataSource initialized successfully!");
        } catch (Exception ex) {
            Logger.getLogger(DBContext.class.getName()).log(Level.SEVERE, "Error initializing HikariCP", ex);
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    // Keep legacy connection for backward compatibility with old DAOs
    public Connection connection;

    public DBContext() {
        try {
            Properties properties = new Properties();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("ConnectDB.properties");
            if (inputStream == null) {
                throw new RuntimeException("Không tìm thấy ConnectDB.properties");
            }
            properties.load(inputStream);
            String user = properties.getProperty("user");
            String pass = properties.getProperty("password");
            String url = properties.getProperty("url");
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, user, pass);
        } catch (IOException | ClassNotFoundException | SQLException ex) {
            Logger.getLogger(DBContext.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== TEST DB CONNECTION ===");
        try (Connection conn = DBContext.getConnection()) {
            if (conn != null) {
                System.out.println("✅ Kết nối HikariCP thành công!");
            } else {
                System.out.println("❌ Kết nối HikariCP thất bại!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
