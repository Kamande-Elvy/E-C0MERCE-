import java.util.concurrent.atomic.AtomicInteger;

public class Stats {
   public final AtomicInteger ordersReceived = new AtomicInteger();
   public final AtomicInteger outOfStock = new AtomicInteger();
   public final AtomicInteger paymentsSuccessful = new AtomicInteger();
   public final AtomicInteger paymentsFailed = new AtomicInteger();
   public final AtomicInteger ordersCancelled = new AtomicInteger();
   public final AtomicInteger ordersFulfilled = new AtomicInteger();
   public final AtomicInteger ordersDispatched = new AtomicInteger();
   public final AtomicInteger ordersDelivered = new AtomicInteger();
   public final AtomicInteger workerErrors = new AtomicInteger();
   public final AtomicInteger unitsDeliveredA = new AtomicInteger();
   public final AtomicInteger unitsDeliveredB = new AtomicInteger();

   public Stats() {
   }

   public void addDeliveredUnits(String var1, int var2) {
      if (var1.equals("A")) {
         this.unitsDeliveredA.addAndGet(var2);
      } else {
         this.unitsDeliveredB.addAndGet(var2);
      }

   }
}