package rs.etf.sab.student;

import rs.etf.sab.operations.CityOperations;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CityOperationsImpl implements CityOperations {

    private static final Connection connection = DB.getInstance().getConnection();

    @Override
    public int createCity(String name) {
        String insertQuery = "INSERT INTO City(name) VALUES(?)";
        try (PreparedStatement ps =
                connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.executeUpdate();
            ResultSet generatedKeys = ps.getGeneratedKeys();

            if (!generatedKeys.next()) {
                return -1;
            }
            int cityId = generatedKeys.getInt(1);
            RoutingUtils.addCity(cityId);
            return cityId;
        } catch (SQLException ex) {
            return -1;
        }
    }

    @Override
    public List<Integer> getCities() {
        String selectQuery = "SELECT cityId FROM City";
        List<Integer> cities = new ArrayList<>();

        try (Statement st = connection.createStatement()) {
            ResultSet rs = st.executeQuery(selectQuery);
            while (rs.next()) {
                cities.add(rs.getInt(1));
            }
            return cities;
        } catch (SQLException ex) {
            return null;
        }
    }

    @Override
    public int connectCities(int cityId1, int cityId2, int distance) {
        String selectQuery = "SELECT lineId FROM Line WHERE cityId1 = ? AND cityId2 = ?";
        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {
            ps.setInt(1, cityId2);
            ps.setInt(2, cityId1);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return -1;
            }
        } catch (SQLException ex) {
            return -1;
        }

        String insertQuery = "INSERT INTO Line(cityId1, cityId2, distance) VALUES(?, ?, ?)";
        try (PreparedStatement ps =
                connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, cityId1);
            ps.setInt(2, cityId2);
            ps.setInt(3, distance);
            ps.executeUpdate();

            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (!generatedKeys.next()) {
                return -1;
            }
            RoutingUtils.connectCities(cityId1, cityId2, distance);
            return generatedKeys.getInt(1);
        } catch (SQLException ex) {
            return -1;
        }
    }

    @Override
    public List<Integer> getConnectedCities(int cityId) {
        List<Integer> cities = new ArrayList<>();
        String selectQuery =
                "SELECT cityId2 FROM Line WHERE cityId1 = ? UNION SELECT cityId1 FROM Line WHERE cityId2 = ?";

        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {
            ps.setInt(1, cityId);
            ps.setInt(2, cityId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                cities.add(rs.getInt(1));
            }
            return cities;
        } catch (SQLException ex) {
            return cities;
        }
    }

    @Override
    public List<Integer> getShops(int cityId) {
        String selectQuery = "SELECT shopId FROM Shop WHERE cityId = ?";
        List<Integer> shops = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {
            ps.setInt(1, cityId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                shops.add(rs.getInt(1));
            }
            return shops;
        } catch (SQLException ex) {
            return null;
        }
    }
}
