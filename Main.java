import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class Main {

    public static void main(String[] args) throws InterruptedException {
        long startTime = System.currentTimeMillis();

        System.out.println("=== Flash sale: " + Config.NUM_CUSTOMERS + " customers, "
                + Config.INITIAL_STOCK_A + " units of A, " + Config.INITIAL_STOCK_B + " units of B ===");

    
        InventoryManager inventory = new InventoryManager(Config.INITIAL_STOCK_A, Config.INITIAL_STOCK_B);
        Stats stats = new Stats();
        NotificationService notifications = new NotificationService();

        BlockingQueue<CustomerOrder> paymentQueue   = new LinkedBlockingQueue<>();
        BlockingQueue<CustomerOrder> warehouseQueue = new LinkedBlockingQueue<>();
        BlockingQueue<CustomerOrder> deliveryQueue  = new LinkedBlockingQueue<>();

       
        List<DeliveryVehicle> vehicles = new ArrayList<>();
        for (int i = 1; i <= Config.DELIVERY_VEHICLES; i++) {
            vehicles.add(new DeliveryVehicle("Van-" + i));
        }

        
        ExecutorService paymentPool = namedThreadPool("payment", Config.PAYMENT_WORKERS);
        for (int i = 0; i < Config.PAYMENT_WORKERS; i++) {
            paymentPool.submit(new PaymentProcessor(paymentQueue, warehouseQueue, inventory, stats, notifications));
        }

        ExecutorService warehousePool = namedThreadPool("warehouse", Config.WAREHOUSE_WORKERS);
        for (int i = 0; i < Config.WAREHOUSE_WORKERS; i++) {
            warehousePool.submit(new WarehouseWorker(warehouseQueue, deliveryQueue, inventory, stats, notifications));
        }

        ExecutorService deliveryPool = namedThreadPool("delivery", Config.DELIVERY_THREADS);
        DeliveryManager deliveryManager = new DeliveryManager(
                deliveryQueue, vehicles, deliveryPool, inventory, stats, notifications);

       
        Thread deliveryManagerThread = new Thread(deliveryManager, "delivery-manager");
        deliveryManagerThread.start();


        CountDownLatch startSignal = new CountDownLatch(1);   
        ExecutorService customerPool = namedThreadPool("customer", Config.CUSTOMER_THREADS);
        List<CustomerOrder> allOrders = new ArrayList<>();    

        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 1; i <= Config.NUM_CUSTOMERS; i++) {
            String product = random.nextBoolean() ? "A" : "B";
            int qty = random.nextInt(1, Config.MAX_QTY_PER_ORDER + 1);
            CustomerOrder order = new CustomerOrder(i, "Customer-" + i, product, qty);
            allOrders.add(order);
            customerPool.submit(new CustomerTask(order, inventory, paymentQueue, stats, notifications, startSignal));
        }

        System.out.println("All customers ready. GO!");
        startSignal.countDown();    

      
       


        customerPool.shutdown();                          
        customerPool.awaitTermination(1, TimeUnit.MINUTES); 

        for (int i = 0; i < Config.PAYMENT_WORKERS; i++) {
            paymentQueue.put(CustomerOrder.POISON_PILL);
        }
        paymentPool.shutdown();
        paymentPool.awaitTermination(1, TimeUnit.MINUTES);


        for (int i = 0; i < Config.WAREHOUSE_WORKERS; i++) {
            warehouseQueue.put(CustomerOrder.POISON_PILL);
        }
        warehousePool.shutdown();
        warehousePool.awaitTermination(1, TimeUnit.MINUTES);

        
        deliveryQueue.put(CustomerOrder.POISON_PILL);
        deliveryManagerThread.join();                     
        deliveryManager.waitForDeliveriesToFinish();

        long elapsed = System.currentTimeMillis() - startTime;
        ReportGenerator.print(stats, inventory, vehicles, allOrders, notifications, elapsed);
    }

  
    private static ExecutorService namedThreadPool(String prefix, int size) {
        AtomicInteger counter = new AtomicInteger(1);
        return Executors.newFixedThreadPool(size,
                runnable -> new Thread(runnable, prefix + "-" + counter.getAndIncrement()));
    }
}
