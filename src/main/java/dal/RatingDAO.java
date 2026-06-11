package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.Rating_note;
import model.Rating_review;
import model.ReviewAnswer;

public class RatingDAO extends DBContext {

    public List<Rating_review> getQuestions() {
        List<Rating_review> list = new ArrayList<>();
        String sql = "SELECT id, question_text FROM rating_questions";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Rating_review r = new Rating_review();
                r.setId(rs.getInt("id"));
                r.setQuestion_text(rs.getString("question_text"));

                list.add(r);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public void insertReviewAnswer(int questionId, Integer rating, int userId, int doctorId, int appointmentId, String note) {

        String sql = "INSERT INTO review_answers (question_id, rating, users_id, doctor_id, appointment_id, note) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, questionId);

            if (rating != null) {
                ps.setInt(2, rating);
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }

            ps.setInt(3, userId);
            ps.setInt(4, doctorId);
            ps.setInt(5, appointmentId);

            if (note != null && !note.trim().isEmpty()) {
                ps.setString(6, note);
            } else {
                ps.setNull(6, java.sql.Types.VARCHAR);
            }

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Double getAverageRating(int doctorID) {
        String sql = "SELECT AVG(rating) FROM review_answers WHERE doctor_id = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, doctorID);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getDouble(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void updateDoctorRating(int doctorID, Double avg) {
        String sql = "UPDATE doctors SET rating = ? WHERE doctor_id = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);

            ps.setDouble(1, avg);
            ps.setInt(2, doctorID);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getTotalReview(int id, int doctorId) {

        String sql = "SELECT COUNT(*) FROM review_answers "
                + "WHERE question_id = ? AND doctor_id = ? AND rating IS NOT NULL";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setInt(2, doctorId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public double getAverageRating(int id, int doctorId) {
        String sql = "SELECT AVG(rating) FROM review_answers "
                + "WHERE question_id = ? AND doctor_id = ? AND rating IS NOT NULL";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setInt(2, doctorId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getDouble(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public List<Rating_note> getNotesByDoctor(int doctorId) {
        List<Rating_note> list = new ArrayList<>();
        String sql = "SELECT u.full_name, r.note, r.appointment_id, r.users_id "
                + "FROM review_answers r "
                + "JOIN users u ON r.users_id = u.user_id "
                + "WHERE r.doctor_id = ? "
                + "AND r.question_id = 5 "
                + "AND r.note IS NOT NULL AND r.note <> '' "
                + "ORDER BY r.id DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Rating_note n = new Rating_note();
                n.setUserName(rs.getString("full_name"));
                n.setNote(rs.getString("note"));
                n.setAppointment_id(rs.getInt("appointment_id"));
                n.setUser_id(rs.getInt("users_id")); // ← THÊM DÒNG NÀY
                list.add(n);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void deleteRatingByAppointment(int appointmentID) {
        String sql = "DELETE FROM review_answers WHERE appointment_id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, appointmentID);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<ReviewAnswer> getAnswersByAppointment(int appId) {
        List<ReviewAnswer> list = new ArrayList<>();

        String sql = "SELECT id, question_id, rating_value, note "
                + "FROM review_answers "
                + "WHERE appointment_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, appId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ReviewAnswer a = new ReviewAnswer();

                a.setId(rs.getInt("id"));
                a.setQuestionid(rs.getInt("question_id"));
                a.setRating(rs.getInt("rating_value"));
                a.setNote(rs.getString("note"));

                list.add(a);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

}
