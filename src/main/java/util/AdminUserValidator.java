package util;

import dal.DoctorDAO;
import dal.UserDAO;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import model.Role;
import model.User;

public class AdminUserValidator {

    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_EMAIL_LENGTH = 100;
    private static final int MIN_EXPERIENCE = 0;
    private static final int MAX_EXPERIENCE = 50;
    private static final int MIN_PRICE = 0;
    private static final int MAX_PRICE = 10_000_000;
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern HAS_LETTER_PATTERN = Pattern.compile(".*\\p{L}+.*");
    private static final Pattern ONLY_NUMBER_OR_SYMBOL_PATTERN = Pattern.compile("^[\\d\\p{Punct}\\s]+$");

    private static final List<String> SPECIALIZATION_OPTIONS = Arrays.asList(
            "Da liễu dị ứng",
            "Da liễu nhiễm trùng",
            "Da liễu tổng quát",
            "Điều trị mụn"
    );

    private static final List<String> QUALIFICATION_OPTIONS = Arrays.asList(
            "Giáo sư / Phó Giáo sư",
            "Tiến sĩ / Bác sĩ CK II",
            "Thạc sĩ / Bác sĩ CK I / BS nội trú"
    );

    public ValidationResult validateAddUser(String fullName, String phone, String email, String roleStr,
            UserDAO userDAO) throws SQLException {

        ValidationResult result = new ValidationResult();

        validateUserCommonFields(result, "add", fullName, phone, email);

        Role targetRole = parseRole(roleStr);
        result.targetRole = targetRole;

        if (targetRole == null || targetRole == Role.admin) {
            result.addFieldError("addRoleError", "Vai trò không hợp lệ");
        }

        if (!result.hasAnyError() && userDAO.isPhoneExist(phone)) {
            result.addFieldError("addPhoneError", "Số điện thoại này đã tồn tại");
        }

        if (!result.hasAnyError() && userDAO.isEmailExist(email)) {
            result.addFieldError("addEmailError", "Email này đã tồn tại");
        }

        if (result.hasAnyError() && result.formError == null) {
            result.formError = "Dữ liệu tạo tài khoản không hợp lệ";
        }

        return result;
    }

    public ValidationResult validateAddUser(String fullName, String phone, String email, String roleStr,
            String specialization, String qualification, String experienceRaw, String priceRaw,
            UserDAO userDAO) throws SQLException {
        return validateAddUser(fullName, phone, email, roleStr, userDAO);
    }


    public ValidationResult validateEditUser(User existingUser, int userId, String fullName, String phone,
            String email, String roleStr, UserDAO userDAO, DoctorDAO doctorDAO) throws SQLException {

        ValidationResult result = new ValidationResult();

        validateUserCommonFields(result, "edit", fullName, phone, email);

        Role targetRole = parseRole(roleStr);
        result.targetRole = targetRole;

        if (targetRole == null) {
            result.addFieldError("editRoleError", "Vai trò không hợp lệ");
        } else if (!isAllowedEditableRoleTransition(existingUser.getRole(), targetRole)) {
            result.addFieldError("editRoleError", "Vai trò không hợp lệ");
        }

        if (!result.hasAnyError()) {
            User phoneOwner = userDAO.getUserByPhone(phone);
            if (phoneOwner != null && phoneOwner.getUserId() != userId) {
                result.addFieldError("editPhoneError", "Số điện thoại này đã tồn tại");
            }
        }

        if (!result.hasAnyError()) {
            User emailOwner = userDAO.getUserByEmail(email);
            if (emailOwner != null && emailOwner.getUserId() != userId) {
                result.addFieldError("editEmailError", "Email này đã tồn tại");
            }
        }

        if (existingUser.getRole() == Role.doctor && targetRole != Role.doctor
                && doctorDAO.hasFutureUnfinishedAppointmentsByUserId(userId)) {
            result.formError = "Không thể đổi vai trò bác sĩ khi vẫn còn lịch khám tương lai chưa hoàn tất";
        }

        if (result.hasAnyError() && result.formError == null) {
            result.formError = "Dữ liệu cập nhật không hợp lệ";
        }

        return result;
    }

    public ValidationResult validateEditUser(User existingUser, int userId, String fullName, String phone,
            String email, String roleStr, String specialization, String qualification,
            String experienceRaw, String priceRaw, UserDAO userDAO, DoctorDAO doctorDAO) throws SQLException {
        return validateEditUser(existingUser, userId, fullName, phone, email, roleStr, userDAO, doctorDAO);
    }


    private void validateUserCommonFields(ValidationResult result, String prefix,
            String fullName, String phone, String email) {

        if (fullName.isEmpty()) {
            result.addFieldError(prefix + "FullNameError", "Họ tên không được để trống");
        } else if (fullName.length() < MIN_NAME_LENGTH || fullName.length() > MAX_NAME_LENGTH) {
            result.addFieldError(prefix + "FullNameError", "Họ tên phải từ 2 đến 100 ký tự");
        } else if (!isMeaningfulFullName(fullName)) {
            result.addFieldError(prefix + "FullNameError", "Họ tên không hợp lệ");
        }

        if (phone.isEmpty()) {
            result.addFieldError(prefix + "PhoneError", "Số điện thoại không được để trống");
        } else if (!isValidPhone(phone)) {
            result.addFieldError(prefix + "PhoneError", "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0");
        }

        if (email.isEmpty()) {
            result.addFieldError(prefix + "EmailError", "Email không được để trống");
        } else if (email.length() > MAX_EMAIL_LENGTH) {
            result.addFieldError(prefix + "EmailError", "Email tối đa 100 ký tự");
        } else if (!isValidEmail(email)) {
            result.addFieldError(prefix + "EmailError", "Email không đúng định dạng");
        }
    }

    private DoctorTransitionData validateDoctorTransitionFields(String prefix,
            String specialization, String qualification, String experienceRaw, String priceRaw) {

        DoctorTransitionData data = new DoctorTransitionData();
        data.specialization = specialization;
        data.qualification = qualification;

        if (specialization.isEmpty()) {
            data.addFieldError(prefix + "DoctorSpecializationError", "Chuyên môn là bắt buộc");
        } else if (!SPECIALIZATION_OPTIONS.contains(specialization)) {
            data.addFieldError(prefix + "DoctorSpecializationError", "Chuyên môn không hợp lệ");
        }

        if (qualification.isEmpty()) {
            data.addFieldError(prefix + "DoctorQualificationError", "Bằng cấp là bắt buộc");
        } else if (!QUALIFICATION_OPTIONS.contains(qualification)) {
            data.addFieldError(prefix + "DoctorQualificationError", "Bằng cấp không hợp lệ");
        }

        if (experienceRaw.isEmpty()) {
            data.addFieldError(prefix + "DoctorExperienceError", "Kinh nghiệm là bắt buộc");
        } else if (!experienceRaw.matches("\\d+")) {
            data.addFieldError(prefix + "DoctorExperienceError", "Kinh nghiệm phải là số nguyên");
        } else {
            int exp = Integer.parseInt(experienceRaw);
            if (exp < MIN_EXPERIENCE || exp > MAX_EXPERIENCE) {
                data.addFieldError(prefix + "DoctorExperienceError", "Kinh nghiệm phải từ 0 đến 50");
            } else {
                data.experienceYears = exp;
            }
        }

        if (priceRaw.isEmpty()) {
            data.addFieldError(prefix + "DoctorPriceError", "Giá khám là bắt buộc");
        } else if (!priceRaw.matches("\\d+")) {
            data.addFieldError(prefix + "DoctorPriceError", "Giá khám phải là số nguyên không âm");
        } else {
            int price = Integer.parseInt(priceRaw);
            if (price < MIN_PRICE || price > MAX_PRICE) {
                data.addFieldError(prefix + "DoctorPriceError", "Giá khám phải từ 0 đến 10000000");
            } else {
                data.priceBooking = price;
            }
        }

        return data;
    }

    private Role parseRole(String roleStr) {
        String safeRole = safeTrim(roleStr).toLowerCase();

        if ("doctor".equals(safeRole)) {
            return Role.doctor;
        }
        if ("receptionist".equals(safeRole)) {
            return Role.receptionist;
        }
        if ("technician".equals(safeRole)) {
            return Role.technician;
        }
        if ("patient_manager".equals(safeRole)) {
            return Role.patient_manager;
        }
        if ("patient".equals(safeRole)) {
            return Role.patient;
        }
        if ("admin".equals(safeRole)) {
            return Role.admin;
        }

        return null;
    }

    private boolean isAllowedEditableRoleTransition(Role currentRole, Role targetRole) {
        if (currentRole == null || targetRole == null) {
            return false;
        }
        if (currentRole == targetRole) {
            return true;
        }
        if (targetRole == Role.admin) {
            return currentRole != Role.admin;
        }
        return isStaffRole(currentRole) && isStaffRole(targetRole);
    }

    private boolean isStaffRole(Role role) {
        return role == Role.doctor
                || role == Role.receptionist
                || role == Role.technician
                || role == Role.patient_manager;
    }

    private boolean isValidPhone(String phone) {
        return PHONE_PATTERN.matcher(phone).matches();
    }

    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    private boolean isMeaningfulFullName(String fullName) {
        if (!HAS_LETTER_PATTERN.matcher(fullName).matches()) {
            return false;
        }
        return !ONLY_NUMBER_OR_SYMBOL_PATTERN.matcher(fullName).matches();
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    public static class ValidationResult {

        private final Map<String, String> fieldErrors = new LinkedHashMap<>();
        private String formError;
        private Role targetRole;
        private DoctorTransitionData doctorData;

        public boolean isValid() {
            return formError == null && fieldErrors.isEmpty();
        }

        public boolean hasAnyError() {
            return formError != null || !fieldErrors.isEmpty();
        }

        public void addFieldError(String key, String message) {
            fieldErrors.put(key, message);
        }

        public void merge(Map<String, String> errors) {
            if (errors == null || errors.isEmpty()) {
                return;
            }
            fieldErrors.putAll(errors);
        }

        public Map<String, String> getFieldErrors() {
            return fieldErrors;
        }

        public String getFormError() {
            return formError;
        }

        public Role getTargetRole() {
            return targetRole;
        }

        public DoctorTransitionData getDoctorData() {
            return doctorData;
        }
    }

    public static class DoctorTransitionData {

        private final Map<String, String> fieldErrors = new LinkedHashMap<>();
        private String specialization;
        private String qualification;
        private int experienceYears;
        private int priceBooking;

        private void addFieldError(String key, String message) {
            fieldErrors.put(key, message);
        }

        public boolean isValid() {
            return fieldErrors.isEmpty();
        }

        public String getSpecialization() {
            return specialization;
        }

        public String getQualification() {
            return qualification;
        }

        public int getExperienceYears() {
            return experienceYears;
        }

        public int getPriceBooking() {
            return priceBooking;
        }
    }
}
