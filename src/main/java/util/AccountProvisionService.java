package util;

import dal.UserDAO;
import java.security.SecureRandom;
import java.sql.SQLException;
import model.EmailOtpService;
import model.User;

public class AccountProvisionService {

    private static final String TEMP_PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789@#$%";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public ProvisionResult createAccountWithTemporaryPassword(User newUser, UserDAO userDAO) throws SQLException {
        ProvisionResult result = new ProvisionResult();

        String temporaryPassword = generateRandomPassword(10);
        newUser.setPasswordHash(temporaryPassword);
        userDAO.createUser(newUser);

        result.user = userDAO.getUserByEmail(newUser.getEmail());
        result.temporaryPassword = temporaryPassword;
        result.passwordUpdated = true;
        return result;
    }

    public ProvisionResult sendTemporaryPassword(User targetUser, String temporaryPassword) {
        ProvisionResult result = new ProvisionResult();
        result.user = targetUser;
        result.temporaryPassword = temporaryPassword;

        try {
            EmailOtpService.sendNewAccountPassword(
                    targetUser.getEmail(),
                    targetUser.getFullName(),
                    temporaryPassword
            );
            result.mailSent = true;
        } catch (Exception mailEx) {
            result.mailSent = false;
            result.errorMessage = mailEx.getMessage();
            mailEx.printStackTrace();
        }

        return result;
    }

    public ProvisionResult resetTemporaryPassword(User targetUser, UserDAO userDAO) throws SQLException {
        ProvisionResult result = new ProvisionResult();
        result.user = targetUser;

        String temporaryPassword = generateRandomPassword(10);
        boolean updated = userDAO.updatePasswordByEmail(targetUser.getEmail(), temporaryPassword);
        result.temporaryPassword = temporaryPassword;
        result.passwordUpdated = updated;

        ProvisionResult deliveryResult = sendTemporaryPassword(targetUser, temporaryPassword);
        result.mailSent = deliveryResult.isMailSent();
        if (!deliveryResult.isMailSent()) {
            result.errorMessage = deliveryResult.getErrorMessage();
        }

        return result;
    }

    private String generateRandomPassword(int length) {
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = SECURE_RANDOM.nextInt(TEMP_PASSWORD_CHARS.length());
            password.append(TEMP_PASSWORD_CHARS.charAt(index));
        }
        return password.toString();
    }

    public static class ProvisionResult {

        private User user;
        private String temporaryPassword;
        private boolean passwordUpdated;
        private boolean mailSent;
        private String errorMessage;

        public User getUser() {
            return user;
        }

        public String getTemporaryPassword() {
            return temporaryPassword;
        }

        public boolean isPasswordUpdated() {
            return passwordUpdated;
        }

        public boolean isMailSent() {
            return mailSent;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
