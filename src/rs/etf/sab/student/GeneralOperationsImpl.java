package rs.etf.sab.student;

import rs.etf.sab.operations.GeneralOperations;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Calendar;

public class GeneralOperationsImpl implements GeneralOperations {

    private static final Connection connection = DB.getInstance().getConnection();
    private Calendar systemTime;

    @Override
    public void setInitialTime(Calendar initialTime) {
        systemTime = (Calendar) initialTime.clone();
    }

    @Override
    public Calendar time(int days) {
        String selectQuery =
                "SELECT orderId, location, nextCity, timeToNext, systemProfit FROM OrderT WHERE state = 'sent'";
        try (Statement st = connection.createStatement()) {
            ResultSet rs = st.executeQuery(selectQuery);
            while (rs.next()) {
                int orderId = rs.getInt(1);
                int location = rs.getInt(2);
                int nextCity = rs.getInt(3);
                int timeToNext = rs.getInt(4);
                BigDecimal systemProfit = rs.getBigDecimal(5);
                updateOrder(orderId, location, nextCity, timeToNext, days, systemProfit);
            }
        } catch (SQLException ignored) {
        }

        systemTime.add(Calendar.DAY_OF_MONTH, days);
        return systemTime;
    }

    @Override
    public Calendar getCurrentTime() {
        return (Calendar) systemTime.clone();
    }

    @Override
    public void eraseAll() {
        String deleteQuery = "DELETE FROM TransactionT\n"
                        + "DELETE FROM ArticleInOrder\n"
                        + "DELETE FROM OrderT\n"
                        + "DELETE FROM Article\n"
                        + "DELETE FROM Buyer\n"
                        + "DELETE FROM Shop\n"
                        + "DELETE FROM Line\n"
                        + "DELETE FROM City";
        try (Statement st = connection.createStatement()) {
            st.executeQuery(deleteQuery);
        } catch (SQLException ignored) {
        }
    }

    private void updateOrder(
            int orderId,
            int location,
            int nextCity,
            int timeToNext,
            int days,
            BigDecimal systemProfit) {

        int destination;
        String selectQuery =
                "SELECT B.cityId FROM OrderT O JOIN Buyer B ON(O.buyerId = B.buyerId) WHERE orderId = ?";
        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            destination = rs.next() ? rs.getInt(1) : -1;
        } catch (SQLException ignored) {
            destination = -1;
        }

        if (destination == -1) {
            return;
        }

        int initialDays = days;
        if (nextCity == 0 && timeToNext > days) {
            String updateQuery = "UPDATE OrderT SET timeToNext = ? WHERE orderId = ?";
            try (PreparedStatement ps = connection.prepareStatement(updateQuery)) {
                ps.setInt(1, timeToNext - days);
                ps.setInt(2, orderId);
                ps.executeUpdate();
            } catch (SQLException ignored) {
            }
            return;
        }

        if (nextCity == 0) {
            days -= timeToNext;
            nextCity = RoutingUtils.getNextCityToDestination(location, destination);
            timeToNext = RoutingUtils.getDistanceToCity(location, nextCity);
        }

        while (days > 0) {
            if (timeToNext > days) {
                timeToNext -= days;
                break;
            }

            days -= timeToNext;
            if (destination == nextCity) {
                finishOrder(orderId, destination, systemProfit, initialDays - days);
                return;
            }
            location = nextCity;
            nextCity = RoutingUtils.getNextCityToDestination(nextCity, destination);
            timeToNext = RoutingUtils.getDistanceToCity(location, nextCity);
        }

        String updateQuery =
                "UPDATE OrderT SET timeToNext = ?, location = ?, nextCity = ? WHERE orderId = ?";
        try (PreparedStatement ps = connection.prepareStatement(updateQuery)) {
            ps.setInt(1, timeToNext);
            ps.setInt(2, location);
            ps.setInt(3, nextCity);
            ps.setInt(4, orderId);
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    private void finishOrder(int orderId, int destination, BigDecimal systemProfit, int days) {
        Calendar dateOfArrival = getCurrentTime();
        dateOfArrival.add(Calendar.DAY_OF_MONTH, days);
        String updateQuery =
                "UPDATE OrderT SET state = 'arrived', location = ?, receivedTime = ? WHERE orderId = ?";

        try (PreparedStatement ps = connection.prepareStatement(updateQuery)) {
            ps.setInt(1, destination);
            ps.setDate(2, new Date(dateOfArrival.getTimeInMillis()));
            ps.setInt(3, orderId);
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }

        String insertQuery =
                "INSERT INTO TransactionT(orderId, type, executionTime, amount) VALUES(?, 'system', ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(insertQuery)) {

            ps.setInt(1, orderId);
            ps.setDate(2, new Date(dateOfArrival.getTimeInMillis()));
            ps.setBigDecimal(3, systemProfit);
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
    }
}
