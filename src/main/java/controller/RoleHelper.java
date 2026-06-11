package controller;

import model.User;
import java.lang.reflect.Method;

/**
 * Helper để lấy role từ User qua reflection, tương thích cả bản User có getRole() hoặc getRoleId().
 */
public final class RoleHelper {

    private RoleHelper() {}

    /**
     * Lấy tên role (lowercase): admin, patient, doctor, receptionist, technician, patient_manager.
     */
    public static String getRoleName(User u) {
        if (u == null) return "";
        try {
            Method m = u.getClass().getMethod("getRole");
            Object r = m.invoke(u);
            return r != null ? r.toString().toLowerCase() : "";
        } catch (NoSuchMethodException e) {
            try {
                Method m = u.getClass().getMethod("getRoleId");
                Object id = m.invoke(u);
                if (id == null) return "";
                int roleId = id instanceof Integer ? (Integer) id : ((Number) id).intValue();
                String[] names = { "admin", "patient", "doctor", "receptionist", "technician", "patient_manager" };
                return roleId >= 0 && roleId < names.length ? names[roleId] : "";
            } catch (Exception e2) {
                return "";
            }
        } catch (Exception e) {
            return "";
        }
    }

    public static boolean isTechnician(User u) {
        return "technician".equals(getRoleName(u));
    }

    public static boolean isReceptionist(User u) {
        return "receptionist".equals(getRoleName(u));
    }

    public static boolean isDoctor(User u) {
        return "doctor".equals(getRoleName(u));
    }

    public static boolean isAdmin(User u) {
        return "admin".equals(getRoleName(u));
    }

    public static boolean isPatientManager(User u) {
        return "patient_manager".equals(getRoleName(u));
    }
}
