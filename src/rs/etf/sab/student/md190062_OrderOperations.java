package rs.etf.sab.student;

import rs.etf.sab.operations.BuyerOperations;
import rs.etf.sab.operations.GeneralOperations;
import rs.etf.sab.operations.OrderOperations;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class md190062_OrderOperations implements OrderOperations {

    private final GeneralOperations generalOperations;
    private final BuyerOperations buyerOperations;
    private static final Connection connection = DB.getInstance().getConnection();

    public md190062_OrderOperations(
            GeneralOperations generalOperations, BuyerOperations buyerOperations) {
        this.generalOperations = generalOperations;
        this.buyerOperations = buyerOperations;
    }

    @Override
    public int addArticle(int orderId, int articleId, int count) {
        if (!shopHasItem(articleId, count)) {
            return -1;
        }

        removeItemsFromShop(articleId, count);
        int itemId = articleInOrder(orderId, articleId);
        if (itemId != -1) {
            String updateQuery = "UPDATE ArticleInOrder SET count = count + ? WHERE itemId = ?";
            try (PreparedStatement ps = connection.prepareStatement(updateQuery)) {

                ps.setInt(1, count);
                ps.setInt(2, itemId);
                ps.executeUpdate();
                return itemId;
            } catch (SQLException ex) {
                return -1;
            }
        } else {
            String insertQuery =
                    "INSERT INTO ArticleInOrder(orderId, articleId, count) VALUES(?, ?, ?)";
            try (PreparedStatement ps =
                    connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, orderId);
                ps.setInt(2, articleId);
                ps.setInt(3, count);
                ps.executeUpdate();
                ResultSet generatedKeys = ps.getGeneratedKeys();
                generatedKeys.next();
                return generatedKeys.getInt(1);
            } catch (SQLException ex) {
                return -1;
            }
        }
    }

    @Override
    public int removeArticle(int orderId, int articleId) {
        int amountRemoved;

        String selectQuery = "SELECT count FROM ArticleInOrder WHERE orderId = ? AND articleId = ?";
        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {

            ps.setInt(1, orderId);
            ps.setInt(2, articleId);
            ResultSet rs = ps.executeQuery();
            rs.next();
            amountRemoved = rs.getInt(1);
        } catch (SQLException ex) {
            return -1;
        }

        String deleteQuery = "DELETE FROM ArticleInOrder WHERE orderId = ? AND articleId = ?";
        try (PreparedStatement ps = connection.prepareStatement(deleteQuery)) {

            ps.setInt(1, orderId);
            ps.setInt(2, articleId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            return -1;
        }

        String updateQuery = "UPDATE Article SET amount = amount + ? WHERE articleId = ?";
        try (PreparedStatement ps = connection.prepareStatement(updateQuery)) {

            ps.setInt(1, amountRemoved);
            ps.setInt(2, articleId);
            ps.executeUpdate();
            return 1;
        } catch (SQLException ex) {
            return -1;
        }
    }

    @Override
    public List<Integer> getItems(int orderId) {
        String selectQuery = "SELECT itemId FROM ArticleInOrder WHERE orderId = ?";
        List<Integer> items = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {

            ps.setInt(1, orderId);
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                items.add(resultSet.getInt(1));
            }

            return items;
        } catch (SQLException ex) {
            return items;
        }
    }

    @Override
    public int completeOrder(int orderId) {
        String procedureQuery = "{ call dbo.SP_FINAL_PRICE (?, ?) }";
        try (CallableStatement cs = connection.prepareCall(procedureQuery)) {

            cs.setInt(1, orderId);
            cs.setDate(2, new Date(generalOperations.getCurrentTime().getTimeInMillis()));
            cs.execute();
        } catch (SQLException ex) {
            return -1;
        }

        String finalPriceQuery = "SELECT finalPrice FROM OrderT WHERE orderId = ?";
        BigDecimal finalPrice;
        try (PreparedStatement ps = connection.prepareStatement(finalPriceQuery)) {

            ps.setInt(1, orderId);
            ResultSet resultSet = ps.executeQuery();
            resultSet.next();
            finalPrice = resultSet.getBigDecimal(1);
        } catch (SQLException ex) {
            return -1;
        }

        int buyerId = getBuyer(orderId);
        BigDecimal buyerCredit = buyerOperations.getCredit(buyerId);
        if (finalPrice.compareTo(buyerCredit) > 0) {
            revertOrderCompletion(orderId);
            return -1;
        }

        buyerOperations.increaseCredit(buyerId, finalPrice.negate());
        sendOrder(orderId, buyerId);
        makeTransaction(orderId, buyerId, finalPrice);
        return 1;
    }

    @Override
    public BigDecimal getFinalPrice(int orderId) {
        String selectQuery =
                "SELECT finalPrice FROM OrderT WHERE orderId = ? AND state != 'created'";
        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {

            ps.setInt(1, orderId);
            ResultSet resultSet = ps.executeQuery();
            resultSet.next();
            return resultSet.getBigDecimal(1);
        } catch (SQLException ex) {
            return BigDecimal.valueOf(-1);
        }
    }

    @Override
    public BigDecimal getDiscountSum(int orderId) {
        String selectQuery =
                "SELECT discountSum FROM OrderT WHERE orderId = ? AND state != 'created'";
        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {

            ps.setInt(1, orderId);
            ResultSet resultSet = ps.executeQuery();
            resultSet.next();
            return resultSet.getBigDecimal(1);
        } catch (SQLException ex) {
            return BigDecimal.valueOf(-1);
        }
    }

    @Override
    public String getState(int orderId) {
        String selectQuery = "SELECT state FROM OrderT WHERE orderId = ?";
        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {

            ps.setInt(1, orderId);
            ResultSet resultSet = ps.executeQuery();
            resultSet.next();
            return resultSet.getString(1);
        } catch (SQLException ex) {
            return null;
        }
    }

    @Override
    public Calendar getSentTime(int orderId) {
        String selectQuery = "SELECT sentTime FROM OrderT WHERE orderId = ?";
        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {

            ps.setInt(1, orderId);
            ResultSet resultSet = ps.executeQuery();
            resultSet.next();

            Date sentDate = resultSet.getDate(1);
            if (sentDate == null) {
                return null;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.clear();
            calendar.setTime(sentDate);
            return calendar;
        } catch (SQLException ex) {
            return null;
        }
    }

    @Override
    public Calendar getRecievedTime(int orderId) {
        String selectQuery = "SELECT receivedTime FROM OrderT WHERE orderId = ?";
        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {

            ps.setInt(1, orderId);
            ResultSet resultSet = ps.executeQuery();
            resultSet.next();

            Date receivedDate = resultSet.getDate(1);
            if (receivedDate == null) {
                return null;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.clear();
            calendar.setTime(receivedDate);
            return calendar;
        } catch (SQLException ex) {
            return null;
        }
    }

    @Override
    public int getBuyer(int orderId) {
        String selectQuery = "SELECT buyerId FROM OrderT WHERE orderId = ?";
        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {

            ps.setInt(1, orderId);
            ResultSet resultSet = ps.executeQuery();
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException ex) {
            return -1;
        }
    }

    @Override
    public int getLocation(int orderId) {
        String selectQuery = "SELECT location FROM OrderT WHERE orderId = ? AND state != 'created'";
        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {

            ps.setInt(1, orderId);
            ResultSet resultSet = ps.executeQuery();
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException ex) {
            return -1;
        }
    }

    private int articleInOrder(int orderId, int articleId) {
        String selectQuery =
                "SELECT itemId FROM ArticleInOrder WHERE orderId = ? AND articleId = ?";
        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {

            ps.setInt(1, orderId);
            ps.setInt(2, articleId);
            ResultSet resultSet = ps.executeQuery();
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException ex) {
            return -1;
        }
    }

    private boolean shopHasItem(int articleId, int count) {
        String selectQuery = "SELECT amount FROM Article WHERE articleId = ?";
        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {

            ps.setInt(1, articleId);
            ResultSet resultSet = ps.executeQuery();
            resultSet.next();
            return resultSet.getInt(1) >= count;
        } catch (SQLException ex) {
            return false;
        }
    }

    private void removeItemsFromShop(int articleId, int count) {
        String updateQuery = "UPDATE Article SET amount = amount - ? WHERE articleId = ?";
        try (PreparedStatement ps = connection.prepareStatement(updateQuery)) {

            ps.setInt(1, count);
            ps.setInt(2, articleId);
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    private void revertOrderCompletion(int orderId) {
        String updateQuery =
                "UPDATE OrderT SET finalPrice = null, discountSum = null, systemProfit = null WHERE orderId = ?";
        try (PreparedStatement ps = connection.prepareStatement(updateQuery)) {

            ps.setInt(1, orderId);
            ps.executeUpdate(updateQuery);
        } catch (SQLException ignored) {
        }
    }

    private void sendOrder(int orderId, int buyerId) {
        int destination = buyerOperations.getCity(buyerId);
        int closestCity = RoutingUtils.getClosestCityWithShop(getCitiesWithShops(), destination);
        int timeToNext = RoutingUtils.getMaxTravelTime(getCitiesFromOrder(orderId), closestCity);

        String updateQuery =
                "UPDATE OrderT SET state = 'sent', sentTime = ?, location = ?, timeToNext = ? WHERE orderId = ?";
        try (PreparedStatement ps = connection.prepareStatement(updateQuery)) {

            ps.setDate(1, new Date(generalOperations.getCurrentTime().getTimeInMillis()));
            ps.setInt(2, closestCity);
            ps.setInt(3, timeToNext);
            ps.setInt(4, orderId);
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    private List<Integer> getCitiesWithShops() {
        String selectQuery = "SELECT DISTINCT(cityId) FROM Shop";
        List<Integer> cities = new ArrayList<>();

        try (Statement st = connection.createStatement()) {

            ResultSet resultSet = st.executeQuery(selectQuery);
            while (resultSet.next()) {
                cities.add(resultSet.getInt(1));
            }
            return cities;
        } catch (SQLException ex) {
            return cities;
        }
    }

    private List<Integer> getCitiesFromOrder(int orderId) {
        String selectQuery =
                "SELECT DISTINCT(cityId) FROM ArticleInOrder AiO JOIN Article A ON(Aio.articleId = A.articleId) JOIN Shop S ON(A.shopId = S.shopId) WHERE orderId = ?";
        List<Integer> cities = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {

            ps.setInt(1, orderId);
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                cities.add(resultSet.getInt(1));
            }
            return cities;
        } catch (SQLException ex) {
            return cities;
        }
    }

    private void makeTransaction(int orderId, int buyerId, BigDecimal finalPrice) {
        String insertQuery =
                "INSERT INTO TransactionT(buyerId, orderId, type, executionTime, amount) VALUES(?, ?, 'buyer', ?, ?)";
        try (PreparedStatement ps =
                connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, buyerId);
            ps.setInt(2, orderId);
            ps.setDate(3, new Date(generalOperations.getCurrentTime().getTimeInMillis()));
            ps.setBigDecimal(4, finalPrice);
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
    }
}
