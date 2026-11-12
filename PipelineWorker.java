import java.util.concurrent.BlockingQueue;


public abstract class PipelineWorker implements Runnable {

    protected final BlockingQueue<CustomerOrder> inbox;
    protected final InventoryManager inventory;
    protected final Stats stats;
    protected final NotificationService notifications;

    protected PipelineWorker(BlockingQueue<CustomerOrder> inbox,
                             InventoryManager inventory,
                             Stats stats,
                             NotificationService notifications) {
        this.inbox = inbox;
        this.inventory = inventory;
        this.stats = stats;
        this.notifications = notifications;
    }

    @Override
    public final void run() {
        try {
            while (true) {
                CustomerOrder order = inbox.take();   

                if (order.isPoisonPill()) {
                    Log.event("received poison pill, shutting down");
                    break;                             
                }

                try {
                    process(order);                    
                } catch (RuntimeException e) {
                    
                    handleCrash(order, e);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    protected abstract void process(CustomerOrder order) throws InterruptedException;

    protected void dropCancelledOrder(CustomerOrder order) {
        inventory.release(order.getProduct(), order.getQuantity());
        stats.ordersCancelled.incrementAndGet();
        notifications.notifyProblem(order, "cancelled by customer, stock released");
        Log.event(order + " dropped (cancelled), stock released");
    }


    protected void handleCrash(CustomerOrder order, RuntimeException e) {
        stats.workerErrors.incrementAndGet();
        System.err.println("[" + Thread.currentThread().getName() + "] error on " + order + ": " + e);
        order.cancel();
        if (order.getStatus() == OrderStatus.CANCELLED) {
            dropCancelledOrder(order);
        }
    }

    protected static void simulateWork(int minMs, int maxMs) throws InterruptedException {
        Thread.sleep(java.util.concurrent.ThreadLocalRandom.current().nextInt(minMs, maxMs + 1));
    }
}

