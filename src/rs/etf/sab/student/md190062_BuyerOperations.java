package rs.etf.sab.student;

import rs.etf.sab.operations.BuyerOperations;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class md190062_BuyerOperations implements BuyerOperations {

    private static final Connection connection = DB.getInstance().getConnection();

    @Override
    public int createBuyer(String name, int cityId) {
        String insertQuery = "INSERT INTO Buyer(name, cityId) VALUES(?, ?)";
        try (PreparedStatement ps =
                connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setInt(2, cityId);
            ps.executeUpdate();
            ResultSet generatedKeys = ps.getGeneratedKeys();
            generatedKeys.next();
            return generatedKeys.getInt(1);
        } catch (SQLException ex) {
            return -1;
        }
    }

    @Override
    public int setCity(int buyerId, int cityId) {
        String updateQuery = "UPDATE Buyer SET cityId = ? WHERE buyerId = ?";
        try (PreparedStatement ps = connection.prepareStatement(updateQuery)) {

            ps.setInt(1, cityId);
            ps.setInt(2, buyerId);
            int updatedRows = ps.executeUpdate();
            return updatedRows == 1 ? 1 : -1;
        } catch (SQLException ex) {
            return -1;
        }
    }

    @Override
    public int getCity(int buyerId) {
        String selectQuery = "SELECT cityId FROM Buyer WHERE buyerId = ?";
        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {

            ps.setInt(1, buyerId);
            ResultSet resultSet = ps.executeQuery();
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException ex) {
            return -1;
        }
    }

    @Override
    public BigDecimal increaseCredit(int buyerId, BigDecimal credit) {
        String updateQuery = "UPDATE Buyer SET credit = credit + ? WHERE buyerId = ?";
        try (PreparedStatement ps = connection.prepareStatement(updateQuery)) {

            ps.setBigDecimal(1, credit);
            ps.setInt(2, buyerId);
            int updatedRows = ps.executeUpdate();
            if (updatedRows == 0) {
                return null;
            }
        } catch (SQLException ex) {
            return null;
        }

        String selectQuery = "SELECT credit FROM Buyer WHERE buyerId = ?";
        try (PreparedStatement ps =
                connection.prepareStatement(selectQuery, new String[] {"amount"})) {

            ps.setInt(1, buyerId);
            ResultSet resultSet = ps.executeQuery();
            resultSet.next();
            return resultSet.getBigDecimal(1);
        } catch (SQLException ex) {
            return null;
        }
    }

    @Override
    public int createOrder(int buyerId) {
        String insertQuery = "INSERT INTO OrderT(buyerId) VALUES(?)";
        try (PreparedStatement ps =
                connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, buyerId);
            ps.executeUpdate();
            ResultSet generatedKeys = ps.getGeneratedKeys();
            generatedKeys.next();
            return generatedKeys.getInt(1);
        } catch (SQLException ex) {
            return -1;
        }
    }

    @Override
    public List<Integer> getOrders(int buyerId) {
        String selectQuery = "SELECT orderId FROM OrderT WHERE buyerId = ?";
        List<Integer> orders = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {

            ps.setInt(1, buyerId);
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                orders.add(resultSet.getInt(1));
            }

            return orders;
        } catch (SQLException ex) {
            return orders;
        }
    }

    @Override
    public BigDecimal getCredit(int buyerId) {
        String selectQuery = "SELECT credit FROM Buyer WHERE buyerId = ?";
        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {

            ps.setInt(1, buyerId);
            ResultSet resultSet = ps.executeQuery();
            resultSet.next();
            return resultSet.getBigDecimal(1);
        } catch (SQLException ex) {
            return null;
        }
    }
}
