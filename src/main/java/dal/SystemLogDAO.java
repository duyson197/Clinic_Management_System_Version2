package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import model.SystemLog;

public class SystemLogDAO extends DBContext {

    public void addLog(Integer userId, String action, String description) {
        String sql = "INSERT INTO system_logs (user_id, action, description, created_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            if (userId == null) {
                st.setNull(1, java.sql.Types.INTEGER);
            } else {
                st.setInt(1, userId);
            }
            st.setString(2, action);
            st.setString(3, description);
            Timestamp vnNow = Timestamp.valueOf(
                ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDateTime()
            );
            st.setTimestamp(4, vnNow);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int countLogs(String actionFilter, String keyword, Timestamp from, Timestamp to) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS total FROM system_logs WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (actionFilter != null && !actionFilter.isEmpty()) {
            sql.append(" AND action = ?");
            params.add(actionFilter);
        }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (description LIKE ? OR action LIKE ?)");
            String k = "%" + keyword + "%";
            params.add(k);
            params.add(k);
        }
        if (from != null) {
            sql.append(" AND created_at >= ?");
            params.add(from);
        }
        if (to != null) {
            sql.append(" AND created_at <= ?");
            params.add(to);
        }

        try (PreparedStatement st = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                st.setObject(i + 1, params.get(i));
            }
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<SystemLog> getLogs(String actionFilter, String keyword, Timestamp from, Timestamp to, int page, int pageSize) {
        List<SystemLog> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT l.log_id, l.user_id, l.action, l.description, l.created_at,
                   u.full_name, u.role
            FROM system_logs l
            LEFT JOIN users u ON l.user_id = u.user_id
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        if (actionFilter != null && !actionFilter.isEmpty()) {
            sql.append(" AND l.action = ?");
            params.add(actionFilter);
        }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (l.description LIKE ? OR l.action LIKE ? OR u.full_name LIKE ?)");
            String k = "%" + keyword + "%";
            params.add(k);
            params.add(k);
            params.add(k);
        }
        if (from != null) {
            sql.append(" AND l.created_at >= ?");
            params.add(from);
        }
        if (to != null) {
            sql.append(" AND l.created_at <= ?");
            params.add(to);
        }

        sql.append(" ORDER BY l.created_at DESC, l.log_id DESC");
        sql.append(" LIMIT ? OFFSET ?");

        int offset = (page - 1) * pageSize;
        params.add(pageSize);
        params.add(offset);

        try (PreparedStatement st = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                st.setObject(i + 1, params.get(i));
            }
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                SystemLog log = new SystemLog();
                log.setLogId(rs.getInt("log_id"));
                int uid = rs.getInt("user_id");
                if (rs.wasNull()) {
                    log.setUserId(null);
                } else {
                    log.setUserId(uid);
                }
                log.setAction(rs.getString("action"));
                log.setDescription(rs.getString("description"));
                log.setCreatedAt(rs.getTimestamp("created_at"));
                log.setUserFullName(rs.getString("full_name"));
                log.setUserRole(rs.getString("role"));
                list.add(log);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}

