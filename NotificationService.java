public class NotificationService {

    private final AtomicInteger messagesSent = new AtomicInteger();

    public void notifyDispatched(CustomerOrder order, DeliveryVehicle vehicle) {
        send(order, "your order #" + order.getOrderId() + " is on its way in " + vehicle.getName());
    }

    public void notifyDelivered(CustomerOrder order, DeliveryVehicle vehicle) {
        send(order, "order #" + order.getOrderId() + " delivered by " + vehicle.getName());
    }

    public void notifyProblem(CustomerOrder order, String reason) {
        send(order, "order #" + order.getOrderId() + " not completed: " + reason);
    }

    // private = internal helper, other classes can't call it directly
    private void send(CustomerOrder order, String message) {
        messagesSent.incrementAndGet();
        Log.event("SMS to " + order.getCustomerName() + ": " + message);
    }

    public int getMessagesSent() {
        return messagesSent.get();
    }
}
