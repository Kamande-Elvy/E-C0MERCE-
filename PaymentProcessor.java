import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

public class PaymentProcessor extends PipelineWorker {

    private final BlockingQueue<CustomerOrder> warehouseQueue;

    public PaymentProcessor(BlockingQueue<CustomerOrder> paymentQueue,
            BlockingQueue<CustomerOrder> warehouseQueue,
            InventoryManager inventory,
            Stats stats,
            NotificationService notifications) {
        super(paymentQueue, inventory, stats, notifications);
        this.warehouseQueue = warehouseQueue;
    }

    @Override
    protected void process(CustomerOrder order) throws InterruptedException {

        simulateWork(Config.PAYMENT_MIN_MS, Config.PAYMENT_MAX_MS);

        boolean approved = ThreadLocalRandom.current().nextDouble() >= Config.PAYMENT_FAILURE_RATE;

        if (approved) {

            if (order.advanceTo(OrderStatus.PAID)) {
                stats.paymentsSuccessful.incrementAndGet();
                Log.event(order + " payment OK -> warehouse");
                warehouseQueue.put(order);
            } else {
                dropCancelledOrder(order);
            }
        } else {

            if (order.advanceTo(OrderStatus.PAYMENT_FAILED)) {
                inventory.release(order.getProduct(), order.getQuantity());
                stats.paymentsFailed.incrementAndGet();
                notifications.notifyProblem(order, "payment declined, stock released");
                Log.event(order + " payment DECLINED, stock released");
            } else {
                dropCancelledOrder(order);
            }
        }
    }
}
