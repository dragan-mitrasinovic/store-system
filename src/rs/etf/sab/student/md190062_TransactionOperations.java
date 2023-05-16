package rs.etf.sab.student;

import rs.etf.sab.operations.TransactionOperations;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class md190062_TransactionOperations implements TransactionOperations {

    private static final Connection connection = DB.getInstance().getConnection();

    @Override
    public BigDecimal getBuyerTransactionsAmmount(int buyerId) {
        String selectQuery =
                "SELECT COALESCE(SUM(amount), 0) FROM TransactionT WHERE buyerId = ? AND type = 'buyer'";
        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {
            ps.setInt(1, buyerId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getBigDecimal(1) : BigDecimal.valueOf(-1);
        } catch (SQLException ex) {
            return BigDecimal.valueOf(-1);
        }
    }

    @Override
    public BigDecimal getShopTransactionsAmmount(int shopId) {
        String selectQuery =
                "SELECT COALESCE(SUM(amount), 0) FROM TransactionT WHERE shopId = ? AND type = 'shop'";
        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {
            ps.setInt(1, shopId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getBigDecimal(1) : BigDecimal.valueOf(-1);
        } catch (SQLException ex) {
            return BigDecimal.valueOf(-1);
        }
    }

    @Override
    public List<Integer> getTransationsForBuyer(int buyerId) {
        String selectQuery =
                "SELECT transactionId FROM TransactionT WHERE buyerId = ? AND type = 'buyer'";
        List<Integer> transactions = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {
            ps.setInt(1, buyerId);
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                transactions.add(resultSet.getInt(1));
            }
            return transactions.isEmpty() ? null : transactions;
        } catch (SQLException ex) {
            return null;
        }
    }

    @Override
    public int getTransactionForBuyersOrder(int orderId) {
        String selectQuery =
                "SELECT transactionId FROM TransactionT WHERE orderId = ? AND type = 'buyer'";
        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (SQLException ex) {
            return -1;
        }
    }

    @Override
    public int getTransactionForShopAndOrder(int orderId, int shopId) {
        String selectQuery =
                "SELECT transactionId FROM TransactionT WHERE orderId = ? AND shopId = ? AND type = 'shop'";
        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {
            ps.setInt(1, orderId);
            ps.setInt(2, shopId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (SQLException ex) {
            return -1;
        }
    }

    @Override
    public List<Integer> getTransationsForShop(int shopId) {
        String selectQuery =
                "SELECT transactionId FROM TransactionT WHERE shopId = ? AND type = 'shop'";
        List<Integer> transactions = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {
            ps.setInt(1, shopId);
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                transactions.add(resultSet.getInt(1));
            }
            return transactions.isEmpty() ? null : transactions;
        } catch (SQLException ex) {
            return null;
        }
    }

    @Override
    public Calendar getTimeOfExecution(int transactionId) {
        String selectQuery = "SELECT executionTime FROM TransactionT WHERE transactionId = ?";
        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {
            ps.setInt(1, transactionId);
            ResultSet rs = ps.executeQuery();
            rs.next();

            Date executionTime = rs.getDate(1);
            if (executionTime == null) {
                return null;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.clear();
            calendar.setTime(executionTime);
            return calendar;
        } catch (SQLException ex) {
            return null;
        }
    }

    @Override
    public BigDecimal getAmmountThatBuyerPayedForOrder(int orderId) {
        String selectQuery = "SELECT amount FROM TransactionT WHERE orderId = ? AND type = 'buyer'";
        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getBigDecimal(1) : null;
        } catch (SQLException ex) {
            return null;
        }
    }

    @Override
    public BigDecimal getAmmountThatShopRecievedForOrder(int shopId, int orderId) {
        String selectQuery =
                "SELECT amount FROM TransactionT WHERE shopId = ? AND orderId = ? AND type = 'shop'";
        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getBigDecimal(1) : null;
        } catch (SQLException ex) {
            return null;
        }
    }

    @Override
    public BigDecimal getTransactionAmount(int transactionId) {
        String selectQuery = "SELECT amount FROM TransactionT WHERE transactionId = ?";
        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {
            ps.setInt(1, transactionId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getBigDecimal(1) : null;
        } catch (SQLException ex) {
            return null;
        }
    }

    @Override
    public BigDecimal getSystemProfit() {
        String selectQuery =
                "SELECT COALESCE(SUM(amount), 0) FROM TransactionT WHERE type = 'system'";
        try (Statement stm = connection.createStatement()) {
            ResultSet rs = stm.executeQuery(selectQuery);
            return rs.next() ? rs.getBigDecimal(1) : null;
        } catch (SQLException ex) {
            return null;
        }
    }
}
