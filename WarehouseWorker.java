import java.util.concurrent.BlockingQueue;
public class WarehouseWorker extends PipelineWorker {

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
        // 1. Pick the items off the shelf and pack the box
        simulateWork(Config.WAREHOUSE_MIN_MS, Config.WAREHOUSE_MAX_MS);

        // 2. Mark it PACKED, unless it was cancelled while we were packing
        if (!order.advanceTo(OrderStatus.PACKED)) {
            dropCancelledOrder(order);
            return;
        }

        stats.ordersFulfilled.incrementAndGet();
        Log.event(order + " packed -> delivery");

        // 3. Hand over to logistics
        deliveryQueue.put(order);
    }
}