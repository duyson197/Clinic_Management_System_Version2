package util;

import dal.SystemLogDAO;
import jakarta.servlet.http.HttpSession;
import model.User;

public class SystemLogService {

    private static final SystemLogDAO dao = new SystemLogDAO();

    private static Integer getUserIdFromSession(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object account = session.getAttribute("account");
        if (account instanceof User) {
            return ((User) account).getUserId();
        }
        return null;
    }

    public static void logWithSession(HttpSession session, String action, String description) {
        Integer userId = getUserIdFromSession(session);
        dao.addLog(userId, action, description);
    }

    public static void log(Integer userId, String action, String description) {
        dao.addLog(userId, action, description);
    }
}

