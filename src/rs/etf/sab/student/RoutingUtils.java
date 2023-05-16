package rs.etf.sab.student;

import java.util.*;

class Path {
    private final int cost;
    private final List<City> cityList = new ArrayList<>();

    public Path(int cost) {
        this.cost = cost;
    }

    public int getCost() {
        return cost;
    }

    public void addCityToPath(City city) {
        cityList.add(city);
    }

    public void copyPath(Path otherPath) {
        cityList.addAll(otherPath.cityList);
    }

    public City getLastCity() {
        if (cityList.size() == 0) {
            return null;
        }
        return cityList.get(cityList.size() - 1);
    }

    public City getFirstCity() {
        if (cityList.size() == 0) {
            return null;
        }
        return cityList.get(0);
    }
}

class City {
    private final int id;
    private final Map<City, Integer> connectedCities = new HashMap<>();

    City(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Map<City, Integer> getConnectedCities() {
        return connectedCities;
    }

    public void connect(City city, int distance) {
        if (connectedCities.containsKey(city)) {
            return;
        }
        connectedCities.put(city, distance);
    }
}

public class RoutingUtils {
    private static final Map<Integer, City> cities = new HashMap<>();

    public static void addCity(int cityId) {
        cities.put(cityId, new City(cityId));
    }

    public static void connectCities(int cityId1, int cityId2, int distance) {
        City city1 = cities.get(cityId1);
        City city2 = cities.get(cityId2);

        city1.connect(city2, distance);
        city2.connect(city1, distance);
    }

    public static int getNextCityToDestination(int sourceId, int destinationId) {
        City source = cities.get(sourceId);
        City destination = cities.get(destinationId);
        PriorityQueue<Path> queue = new PriorityQueue<>(Comparator.comparing(Path::getCost));

        for (Map.Entry<City, Integer> entry : source.getConnectedCities().entrySet()) {
            int cost = entry.getValue();
            City nextCity = entry.getKey();

            Path newPath = new Path(cost);
            newPath.addCityToPath(nextCity);
            queue.add(newPath);
        }

        Path currentPath;
        while (true) {
            currentPath = queue.poll();
            if (currentPath == null) {
                return -1;
            }

            City lastCity = currentPath.getLastCity();
            if (lastCity == null) {
                return -1;
            }
            if (lastCity.equals(destination)) {
                return currentPath.getFirstCity().getId();
            }

            for (Map.Entry<City, Integer> entry : lastCity.getConnectedCities().entrySet()) {
                int cost = entry.getValue();
                City nextCity = entry.getKey();

                Path newPath = new Path(currentPath.getCost() + cost);
                newPath.copyPath(currentPath);
                newPath.addCityToPath(nextCity);
                queue.add(newPath);
            }
        }
    }

    public static int getDistanceToCity(int sourceId, int destinationId) {
        if (sourceId == destinationId) {
            return 0;
        }

        City source = cities.get(sourceId);
        City destination = cities.get(destinationId);
        PriorityQueue<Path> queue = new PriorityQueue<>(Comparator.comparing(Path::getCost));

        for (Map.Entry<City, Integer> entry : source.getConnectedCities().entrySet()) {
            City nextCity = entry.getKey();
            int cost = entry.getValue();

            Path newPath = new Path(cost);
            newPath.addCityToPath(nextCity);
            queue.add(newPath);
        }

        Path currentPath;
        while (true) {
            currentPath = queue.poll();
            if (currentPath == null) {
                return -1;
            }

            City lastCity = currentPath.getLastCity();
            if (lastCity == null) {
                return -1;
            }
            if (lastCity.equals(destination)) {
                return currentPath.getCost();
            }

            for (Map.Entry<City, Integer> entry : lastCity.getConnectedCities().entrySet()) {
                City nextCity = entry.getKey();
                int cost = entry.getValue();

                Path newPath = new Path(currentPath.getCost() + cost);
                newPath.copyPath(currentPath);
                newPath.addCityToPath(nextCity);
                queue.add(newPath);
            }
        }
    }

    public static int getClosestCityWithShop(List<Integer> citiesWithShops, int destinationId) {
        int min = Integer.MAX_VALUE;
        int closestCity = -1;

        for (int city : citiesWithShops) {
            int currentDistance = getDistanceToCity(city, destinationId);
            if (currentDistance < min) {
                min = currentDistance;
                closestCity = city;
            }
        }
        return closestCity;
    }

    public static int getMaxTravelTime(List<Integer> cities, int destinationId) {
        int max = Integer.MIN_VALUE;
        for (int city : cities) {
            int currentDistance = getDistanceToCity(city, destinationId);
            max = Math.max(max, currentDistance);
        }
        return max;
    }
}
