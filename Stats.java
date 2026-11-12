public class Stats {
        public final AtomicInteger ordersReceived     = new AtomicInteger();
    public final AtomicInteger outOfStock         = new AtomicInteger();
    public final AtomicInteger paymentsSuccessful = new AtomicInteger();
    public final AtomicInteger paymentsFailed     = new AtomicInteger();
    public final AtomicInteger ordersCancelled    = new AtomicInteger();
    public final AtomicInteger ordersFulfilled    = new AtomicInteger();   
    public final AtomicInteger ordersDispatched   = new AtomicInteger();
    public final AtomicInteger ordersDelivered    = new AtomicInteger();
    public final AtomicInteger workerErrors       = new AtomicInteger();

    public final AtomicInteger unitsDeliveredA = new AtomicInteger();
    public final AtomicInteger unitsDeliveredB = new AtomicInteger();

    public void addDeliveredUnits(String product, int qty) {
        if (product.equals("A")) {          
            unitsDeliveredA.addAndGet(qty);
        } else {
            unitsDeliveredB.addAndGet(qty);
        }
    }
}
