package ro.uaic.asli.lab2;

public class Lab2Basic {
    public static class Location {
        private String name;
        private String type;
        private double x;
        private double y;

        public Location(String name, String type, double x, double y) {
            this.name = name;
            this.type = type;
            this.x = x;
            this.y = y;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }

        @Override
        public String toString() {
            return "Location{name='" + name + "', type='" + type + "', x=" + x + ", y=" + y + "}";
        }
    }

    public static class Road {
        private String type;
        private double length;
        private double speedLimit;

        public Road(String type, double length, double speedLimit) {
            this.type = type;
            this.length = length;
            this.speedLimit = speedLimit;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public double getLength() {
            return length;
        }

        public void setLength(double length) {
            this.length = length;
        }

        public double getSpeedLimit() {
            return speedLimit;
        }

        public void setSpeedLimit(double speedLimit) {
            this.speedLimit = speedLimit;
        }

        @Override
        public String toString() {
            return "Road{type='" + type + "', length=" + length + ", speedLimit=" + speedLimit + "}";
        }
    }

    public static void main(String[] args) {
        Location city = new Location("Iasi", "city", 10.2, 20.4);
        Location airport = new Location("IAS", "airport", 15.7, 25.9);
        Location gas = new Location("FuelStop", "gas station", 5.0, 12.5);

        Road highway = new Road("highway", 120.5, 100);
        Road express = new Road("express", 75.0, 80);
        Road country = new Road("country", 45.2, 60);

        System.out.println(city);
        System.out.println(airport);
        System.out.println(gas);
        System.out.println(highway);
        System.out.println(express);
        System.out.println(country);
    }
}
