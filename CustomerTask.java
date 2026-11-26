import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;


public class CustomerTask implements Runnable {

    private final CustomerOrder order;
    private final InventoryManager inventory;
    private final BlockingQueue<CustomerOrder> paymentQueue;
    private final Stats stats;
    private final NotificationService notifications;
    private final CountDownLatch startSignal;

    public CustomerTask(CustomerOrder order,
                        InventoryManager inventory,
                        BlockingQueue<CustomerOrder> paymentQueue,
                        Stats stats,
                        NotificationService notifications,
                        CountDownLatch startSignal) {
        this.order = order;
        this.inventory = inventory;
        this.paymentQueue = paymentQueue;
        this.stats = stats;
        this.notifications = notifications;
        this.startSignal = startSignal;
    }

    @Override
    public void run() {
        try {
            
            startSignal.await();

            stats.ordersReceived.incrementAndGet();

            
            if (!inventory.reserve(order.getProduct(), order.getQuantity())) {
                order.setStatus(OrderStatus.OUT_OF_STOCK);
                stats.outOfStock.incrementAndGet();
                notifications.notifyProblem(order, "out of stock");
                Log.event(order + " rejected: out of stock");
                return;
            }

           
            order.setStatus(OrderStatus.RESERVED);
            Log.event(order + " reserved stock -> payment");
            paymentQueue.put(order);

            if (ThreadLocalRandom.current().nextDouble() < Config.CANCEL_RATE) {
                Thread.sleep(ThreadLocalRandom.current().nextInt(0, 40));
                if (order.getStatus() == OrderStatus.RESERVED) {
                    order.setStatus(OrderStatus.CANCELLED);
                    Log.event(order + " customer requested cancellation");
                } else {
                    Log.event(order + " cancellation refused (already " + order.getStatus() + ")");
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}