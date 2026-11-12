import java.util.concurrent.atomic.AtomicInteger;

public class NotificationService {
   private final AtomicInteger messagesSent = new AtomicInteger();

   public NotificationService() {
   }

   public void notifyDispatched(CustomerOrder var1, DeliveryVehicle var2) {
      int var10002 = var1.getOrderId();
      this.send(var1, "your order #" + var10002 + " is on its way in " + var2.getName());
   }

   public void notifyDelivered(CustomerOrder var1, DeliveryVehicle var2) {
      int var10002 = var1.getOrderId();
      this.send(var1, "order #" + var10002 + " delivered by " + var2.getName());
   }

   public void notifyProblem(CustomerOrder var1, String var2) {
      int var10002 = var1.getOrderId();
      this.send(var1, "order #" + var10002 + " not completed: " + var2);
   }

   private void send(CustomerOrder var1, String var2) {
      this.messagesSent.incrementAndGet();
      String var10000 = var1.getCustomerName();
      Log.event("SMS to " + var10000 + ": " + var2);
   }

   public int getMessagesSent() {
      return this.messagesSent.get();
   }
}
