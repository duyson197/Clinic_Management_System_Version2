package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.InAppNotification;

public class NotificationDAO extends DBContext {

    private static final Logger LOGGER = Logger.getLogger(NotificationDAO.class.getName());

    public NotificationDAO() {
        ensureNotificationTable();
    }

    private void ensureNotificationTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS in_app_notifications (
                notification_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                user_id INT NOT NULL,
                title VARCHAR(255) NOT NULL,
                message TEXT NOT NULL,
                notification_type VARCHAR(50) NOT NULL,
                event_ref VARCHAR(120),
                is_read TINYINT(1) NOT NULL DEFAULT 0,
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                INDEX idx_notification_user_created (user_id, created_at),
                INDEX idx_notification_user_read (user_id, is_read)
            )
            """;

        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.execute();
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Không thể đảm bảo bảng in_app_notifications tồn tại", ex);
        }
    }

    public boolean createNotification(int userId, String title, String message, String type, String eventRef) {
        String sql = "INSERT INTO in_app_notifications (user_id, title, message, notification_type, event_ref) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, userId);
            st.setString(2, title);
            st.setString(3, message);
            st.setString(4, type);
            st.setString(5, eventRef);
            return st.executeUpdate() > 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Không thể tạo thông báo in-app", ex);
            return false;
        }
    }

    public boolean createNotificationForAppointment(long appointmentId, String title, String message, String type, String eventRef) {
        Integer userId = getPatientUserIdByAppointment(appointmentId);
        if (userId == null) {
            return false;
        }
        return createNotification(userId, title, message, type, eventRef);
    }

    public Integer getPatientUserIdByAppointment(long appointmentId) {
        String sql = """
            SELECT p.user_id
            FROM appointments a
            JOIN patients p ON p.patient_id = a.patient_id
            WHERE a.appointment_id = ?
            LIMIT 1
            """;

        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setLong(1, appointmentId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Không thể lấy user_id của bệnh nhân từ appointment", ex);
        }

        return null;
    }

    public int countUnreadNotifications(int userId) {
        String sql = "SELECT COUNT(*) AS total FROM in_app_notifications WHERE user_id = ? AND is_read = 0";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, userId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Không thể đếm thông báo chưa đọc", ex);
        }
        return 0;
    }

    public List<InAppNotification> getLatestNotifications(int userId, int limit) {
        List<InAppNotification> notifications = new ArrayList<>();
        String sql = """
            SELECT notification_id, user_id, title, message, notification_type, event_ref, is_read, created_at
            FROM in_app_notifications
            WHERE user_id = ?
            ORDER BY created_at DESC, notification_id DESC
            LIMIT ?
            """;

        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, userId);
            st.setInt(2, limit);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    InAppNotification item = new InAppNotification();
                    item.setNotificationId(rs.getLong("notification_id"));
                    item.setUserId(rs.getInt("user_id"));
                    item.setTitle(rs.getString("title"));
                    item.setMessage(rs.getString("message"));
                    item.setNotificationType(rs.getString("notification_type"));
                    item.setEventRef(rs.getString("event_ref"));
                    item.setRead(rs.getBoolean("is_read"));
                    item.setCreatedAt(rs.getTimestamp("created_at"));
                    notifications.add(item);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Không thể tải danh sách thông báo", ex);
        }

        return notifications;
    }
    public int markAllAsRead(int userId) {
        String sql = "UPDATE in_app_notifications SET is_read = 1 WHERE user_id = ? AND is_read = 0";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, userId);
            return st.executeUpdate();
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Không thể đánh dấu đã đọc toàn bộ thông báo", ex);
        }
        return 0;
    }
    public boolean markAsRead(long notificationId, int userId) {
        String sql = "UPDATE in_app_notifications SET is_read = 1 WHERE notification_id = ? AND user_id = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setLong(1, notificationId);
            st.setInt(2, userId);
            return st.executeUpdate() > 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Không thể đánh dấu đã đọc thông báo", ex);
        }
        return false;
    }
public boolean deleteNotification(long notificationId, int userId) {
        String sql = "DELETE FROM in_app_notifications WHERE notification_id = ? AND user_id = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setLong(1, notificationId);
            st.setInt(2, userId);
            return st.executeUpdate() > 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Không thể xóa thông báo", ex);
        }
        return false;
    }

}
