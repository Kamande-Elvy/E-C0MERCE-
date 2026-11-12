import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Semaphore;


public class DeliveryManager extends PipelineWorker {

    private final Semaphore vanPermits = new Semaphore(Config.DELIVERY_VEHICLES, true);
    private final ConcurrentLinkedQueue<DeliveryVehicle> freeVans = new ConcurrentLinkedQueue<>();
    private final ExecutorService deliveryPool;

    public DeliveryManager(BlockingQueue<CustomerOrder> deliveryQueue,
                           List<DeliveryVehicle> vehicles,
                           ExecutorService deliveryPool,
                           InventoryManager inventory,
                           Stats stats,
                           NotificationService notifications) {
        super(deliveryQueue, inventory, stats, notifications);
        this.freeVans.addAll(vehicles);
        this.deliveryPool = deliveryPool;
    }


    @Override
    protected void process(CustomerOrder order) {
        deliveryPool.submit(() -> deliver(order));
    }

    private void deliver(CustomerOrder order) {
        try {
            vanPermits.acquire();                     
            DeliveryVehicle van = freeVans.poll();     
            try {
                if (!order.advanceTo(OrderStatus.DISPATCHED)) {
                    dropCancelledOrder(order);
                    return;   
                }
                stats.ordersDispatched.incrementAndGet();
                notifications.notifyDispatched(order, van);

                long tripMillis = ThreadLocalRandom.current()
                        .nextInt(Config.DELIVERY_MIN_MS, Config.DELIVERY_MAX_MS + 1);
                Thread.sleep(tripMillis);

                order.advanceTo(OrderStatus.DELIVERED);
                van.recordTrip(tripMillis);
                stats.ordersDelivered.incrementAndGet();
                stats.addDeliveredUnits(order.getProduct(), order.getQuantity());
                notifications.notifyDelivered(order, van);

            } finally {
            
                freeVans.offer(van);
                vanPermits.release();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (RuntimeException e) {
            handleCrash(order, e);
        }
    }

    public void waitForDeliveriesToFinish() throws InterruptedException {
        deliveryPool.shutdown();
        deliveryPool.awaitTermination(1, TimeUnit.MINUTES);
    }
}

