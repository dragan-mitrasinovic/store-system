package rs.etf.sab.student;

import rs.etf.sab.operations.ShopOperations;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShopOperationsImpl implements ShopOperations {

    private static final Connection connection = DB.getInstance().getConnection();

    @Override
    public int createShop(String shopName, String cityName) {
        int cityId = getCityId(cityName);
        if (cityId == -1) {
            return -1;
        }

        String insertQuery = "INSERT INTO Shop(name, cityId) VALUES(?, ?)";
        try (PreparedStatement ps =
                connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, shopName);
            ps.setInt(2, cityId);
            ps.executeUpdate();
            ResultSet generatedKeys = ps.getGeneratedKeys();
            return generatedKeys.next() ? generatedKeys.getInt(1) : -1;
        } catch (SQLException ex) {
            return -1;
        }
    }

    @Override
    public int setCity(int shopId, String cityName) {
        int cityId = getCityId(cityName);
        if (cityId == -1) {
            return -1;
        }

        String updateQuery = "UPDATE Shop SET cityId = ? WHERE shopId = ?";
        try (PreparedStatement ps = connection.prepareStatement(updateQuery)) {
            ps.setInt(1, cityId);
            ps.setInt(2, shopId);
            int updatedRows = ps.executeUpdate();
            return updatedRows == 1 ? 1 : -1;
        } catch (SQLException ex) {
            return -1;
        }
    }

    @Override
    public int getCity(int shopId) {
        String selectQuery = "SELECT cityId FROM Shop WHERE shopId = ?";
        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {
            ps.setInt(1, shopId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (SQLException ex) {
            return -1;
        }
    }

    @Override
    public int setDiscount(int shopId, int discount) {
        String updateQuery = "UPDATE Shop SET discount = ? WHERE shopId = ?";
        try (PreparedStatement ps = connection.prepareStatement(updateQuery)) {
            ps.setInt(1, discount);
            ps.setInt(2, shopId);
            int updatedRows = ps.executeUpdate();
            return updatedRows == 1 ? 1 : -1;
        } catch (SQLException ex) {
            return -1;
        }
    }

    @Override
    public int increaseArticleCount(int articleId, int increment) {
        String updateQuery = "UPDATE Article SET amount = amount + ? WHERE articleId = ?";
        try (PreparedStatement ps = connection.prepareStatement(updateQuery)) {
            ps.setInt(1, increment);
            ps.setInt(2, articleId);
            int updatedRows = ps.executeUpdate();
            if (updatedRows == 0) {
                return -1;
            }
        } catch (SQLException ex) {
            return -1;
        }

        String selectQuery = "SELECT amount FROM Article WHERE articleId = ?";
        try (PreparedStatement ps =
                connection.prepareStatement(selectQuery, new String[] {"amount"})) {
            ps.setInt(1, articleId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (SQLException ex) {
            return -1;
        }
    }

    @Override
    public int getArticleCount(int articleId) {
        String selectQuery = "SELECT amount FROM Article WHERE articleId = ?";
        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {
            ps.setInt(1, articleId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (SQLException ex) {
            return -1;
        }
    }

    @Override
    public List<Integer> getArticles(int shopId) {
        String selectQuery = "SELECT articleId FROM Article WHERE shopId = ?";
        List<Integer> articles = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {
            ps.setInt(1, shopId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                articles.add(rs.getInt(1));
            }
            return articles;
        } catch (SQLException ex) {
            return articles;
        }
    }

    @Override
    public int getDiscount(int shopId) {
        String selectQuery = "SELECT discount FROM Shop WHERE shopId = ?";
        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {
            ps.setInt(1, shopId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (SQLException ex) {
            return -1;
        }
    }

    private int getCityId(String cityName) {
        String selectQuery = "SELECT cityId FROM City WHERE name = ?";
        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {
            ps.setString(1, cityName);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (SQLException ex) {
            return -1;
        }
    }
}
