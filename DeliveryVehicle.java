public class DeliveryVehicle {

    private final String name;
    private int tripsCompleted = 0;
    private long busyMillis = 0;

    public DeliveryVehicle(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public synchronized void recordTrip(long millis) {
        tripsCompleted++;
        busyMillis += millis;
    }

    public synchronized int getTripsCompleted() {
        return tripsCompleted;
    }

    public synchronized long getBusyMillis() {
        return busyMillis;
    }
}
