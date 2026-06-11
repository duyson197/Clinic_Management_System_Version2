/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;

import java.util.ArrayList;
import java.util.List;
import java.sql.*;
import model.ServicePrice;

/**
 *
 * @author admin
 */
public class ServiceDAO extends DBContext {

    // lấy toàn bộ dịch vụ
    public List<ServicePrice> getAllServices() {
        List<ServicePrice> list = new ArrayList<>();
        String sql = """
            SELECT service_id, name, service_type, price
            FROM service_prices
        """;

        try (PreparedStatement st = connection.prepareStatement(sql); ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                ServicePrice s = new ServicePrice();
                s.setServiceId(rs.getInt("service_id"));
                s.setName(rs.getString("name"));
                s.setServiceType(rs.getString("service_type"));
                s.setPrice(rs.getBigDecimal("price"));
                list.add(s);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không thể tải danh sách dịch vụ", e);
        }
        return list;
    }

    // lấy 1 dịch vụ theo id
    public ServicePrice getServiceById(int id) {
        String sql = """
            SELECT service_id, name, service_type, price
            FROM service_prices
            WHERE service_id = ?
        """;

        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, id);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                ServicePrice s = new ServicePrice();
                s.setServiceId(rs.getInt("service_id"));
                s.setName(rs.getString("name"));
                s.setServiceType(rs.getString("service_type"));
                s.setPrice(rs.getBigDecimal("price"));
                return s;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không thể tìm dịch vụ theo ID", e);
        }
        return null;
    }

    public boolean isServiceExist(String name, String serviceType) {
        return isServiceExistNormalized(name, serviceType, null);
    }

    public boolean isServiceExistForOtherId(String name, String serviceType, int excludedServiceId) {
        return isServiceExistNormalized(name, serviceType, excludedServiceId);
    }

    public boolean isServiceExistNormalized(String normalizedName, String serviceType, Integer excludedServiceId) {
        String sql = """
            SELECT service_id, name
            FROM service_prices
            WHERE service_type = ?
        """;

        if (excludedServiceId != null) {
            sql += " AND service_id <> ?";
        }

        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, serviceType);
            if (excludedServiceId != null) {
                st.setInt(2, excludedServiceId);
            }

            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                String dbName = normalizeName(rs.getString("name"));
                if (dbName.equalsIgnoreCase(normalizedName)) {
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("Không thể kiểm tra trùng tên dịch vụ", e);
        }
    }

    // update dịch vụ
    public int updateService(ServicePrice s) {
        String sql = """
            UPDATE service_prices
            SET name = ?, service_type = ?, price = ?
            WHERE service_id = ?
        """;

        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, s.getName());
            st.setString(2, s.getServiceType());
            st.setBigDecimal(3, s.getPrice());
            st.setInt(4, s.getServiceId());
            return st.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Không thể cập nhật dịch vụ", e);
        }
    }

    // thêm dịch vụ
    public int addService(ServicePrice s) {
        String sql = """
        INSERT INTO service_prices (name, service_type, price)
        VALUES (?, ?, ?)
    """;

        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, s.getName());
            st.setString(2, s.getServiceType());
            st.setBigDecimal(3, s.getPrice());
            return st.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Không thể thêm dịch vụ", e);
        }
    }

    // xóa dịch vụ
    public int deleteService(int serviceId) {
        String sql = "DELETE FROM service_prices WHERE service_id = ?";

        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, serviceId);
            return st.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Không thể xóa dịch vụ", e);
        }
    }

    private String normalizeName(String name) {
        if (name == null) {
            return "";
        }
        return name.trim().replaceAll("\\s+", " ");
    }
}