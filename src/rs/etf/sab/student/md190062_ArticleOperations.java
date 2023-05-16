package rs.etf.sab.student;

import rs.etf.sab.operations.ArticleOperations;

import java.sql.*;

public class md190062_ArticleOperations implements ArticleOperations {

    private static final Connection connection = DB.getInstance().getConnection();

    @Override
    public int createArticle(int shopId, String articleName, int articlePrice) {
        String insertQuery = "INSERT INTO Article(shopId, name, price) VALUES(?, ?, ?)";
        try (PreparedStatement ps =
                connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, shopId);
            ps.setString(2, articleName);
            ps.setInt(3, articlePrice);
            ps.executeUpdate();
            ResultSet generatedKeys = ps.getGeneratedKeys();
            generatedKeys.next();
            return generatedKeys.getInt(1);
        } catch (SQLException ex) {
            return -1;
        }
    }
}