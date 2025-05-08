package com.awesomeshot5051.separatedFiles.systemConfiguration.passwordManagement;

import com.awesomeshot5051.*;
import com.awesomeshot5051.separatedFiles.*;
import com.awesomeshot5051.separatedFiles.group.*;
import com.awesomeshot5051.separatedFiles.session.*;
import com.awesomeshot5051.separatedFiles.userManagement.*;

import java.sql.*;
import java.time.*;

public class GetPasswordExpiration {
    private final LocalDate today = LocalDate.now();
    private final boolean isExpired;
    private final Connection connection;

    // Constructor now accepts Connection from SessionManager
    public GetPasswordExpiration(User user) throws SQLException {
        this.connection = SessionManager.getConnection();
        isExpired = getIsExpired(user.getUsername(), new PasswordHasher().getPassword());
    }

    public GetPasswordExpiration(String username, String password) throws SQLException {
        this.connection = Main.getConnection();
        isExpired = getIsExpired(username, password);
    }

    public GetPasswordExpiration() {
        this.connection = Main.getConnection();
        isExpired = false;
    }

    public boolean isExpired() {
        return isExpired;
    }

    private boolean getIsExpired(String username, String password) throws SQLException {
        boolean expired = false;

        String query = "CALL getPasswordExpiration(?, ?)";
        if (new UserValues(username, password).getGroupType() instanceof SuperAdminIGroup) {
            return false;
        }
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            // Set the username and password parameters
            stmt.setString(1, username);
            stmt.setString(2, password);

            // Execute the query
            ResultSet rs = stmt.executeQuery();

            // Get the nextChangedDate from the result set
            if (rs.next()) {
                if (rs.getDate("nextChangeDate") == null) {
                    return false;
                }
                LocalDate nextChangedDate = rs.getDate("nextChangeDate").toLocalDate();

                // Compare today's date with nextChangedDate
                expired = today.isAfter(nextChangedDate) || today.equals(nextChangedDate);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return expired;
    }

    public int getPasswordExpirationDays(String username, String password) throws SQLException {
        String query = "CALL getPasswordExpiration(?,?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("days");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String getPasswordExpirationDays(String username) {
        try (CallableStatement cs = connection.prepareCall("{ call getPasswordExpirationDays(?)}")) {
            cs.setString(1, username);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return "0";
    }
}
