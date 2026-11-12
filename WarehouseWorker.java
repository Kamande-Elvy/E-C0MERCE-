import java.util.concurrent.BlockingQueue;

public class WarehouseWorker {
     private final BlockingQueue<CustomerOrder> deliveryQueue;

    public WarehouseWorker(BlockingQueue<CustomerOrder> warehouseQueue,
                           BlockingQueue<CustomerOrder> deliveryQueue,
                           InventoryManager inventory,
                           Stats stats,
                           NotificationService notifications) {
        super(warehouseQueue, inventory, stats, notifications);
        this.deliveryQueue = deliveryQueue;
    }

    @Override
    protected void process(CustomerOrder order) throws InterruptedException {
        simulateWork(Config.WAREHOUSE_MIN_MS, Config.WAREHOUSE_MAX_MS);

        if (!order.advanceTo(OrderStatus.PACKED)) {
            dropCancelledOrder(order);
            return;
        }

        stats.ordersFulfilled.incrementAndGet();
        Log.event(order + " packed -> delivery");
        
        deliveryQueue.put(order);
    }
}
