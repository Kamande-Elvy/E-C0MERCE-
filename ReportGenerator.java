import java.util.EnumMap;
import java.util.List;
import java.util.Map;



public class ReportGenerator {

    public static void print(Stats stats,
                             InventoryManager inventory,
                             List<DeliveryVehicle> vehicles,
                             List<CustomerOrder> allOrders,
                             NotificationService notifications,
                             long elapsedMillis) {

        int remainingA = inventory.getStock("A");
        int remainingB = inventory.getStock("B");

        System.out.println();
        System.out.println("================== FINAL REPORT ==================");
        row("Total orders received",        stats.ordersReceived.get());
        row("Rejected (out of stock)",      stats.outOfStock.get());
        row("Successful payments",          stats.paymentsSuccessful.get());
        row("Failed payments",              stats.paymentsFailed.get());
        row("Orders fulfilled (packed)",    stats.ordersFulfilled.get());
        row("Orders dispatched",            stats.ordersDispatched.get());
        row("Orders delivered",             stats.ordersDelivered.get());
        row("Orders cancelled",             stats.ordersCancelled.get());
        row("Worker errors",                stats.workerErrors.get());
        row("Notifications sent",           notifications.getMessagesSent());
        System.out.println();

        System.out.println("Remaining inventory:");
        System.out.printf("  Product A : %3d of %d%n", remainingA, Config.INITIAL_STOCK_A);
        System.out.printf("  Product B : %3d of %d%n", remainingB, Config.INITIAL_STOCK_B);
        System.out.println();

        System.out.println("Vehicle utilization (share of total run time spent driving):");
        for (DeliveryVehicle v : vehicles) {
            double percent = 100.0 * v.getBusyMillis() / elapsedMillis;
            System.out.printf("  %-10s : %3d deliveries, busy %5d ms (%4.1f%%)%n",
                    v.getName(), v.getTripsCompleted(), v.getBusyMillis(), percent);
        }
        System.out.println();

        System.out.println("Consistency checks:");

        // 1. Count the final status of every order (EnumMap = a Map keyed by enum values)
        Map<OrderStatus, Integer> finalStates = new EnumMap<>(OrderStatus.class);
        int stuck = 0;
        for (CustomerOrder o : allOrders) {
            OrderStatus s = o.getStatus();
            if (!s.isFinal()) {
                stuck++;
            }
            finalStates.merge(s, 1, Integer::sum);   
        }
        check("No order lost (every order reached a final state)", stuck == 0);

        
        int accounted = stats.outOfStock.get() + stats.paymentsFailed.get()
                      + stats.ordersCancelled.get() + stats.ordersDelivered.get();
        check("Every order counted once: received ("
                + stats.ordersReceived.get() + ") = rejected + failed + cancelled + delivered ("
                + accounted + ")", accounted == stats.ordersReceived.get());

        // 3. The counters agree with the orders' actual statuses
        boolean countersMatch =
                count(finalStates, OrderStatus.OUT_OF_STOCK)   == stats.outOfStock.get()
             && count(finalStates, OrderStatus.PAYMENT_FAILED) == stats.paymentsFailed.get()
             && count(finalStates, OrderStatus.CANCELLED)      == stats.ordersCancelled.get()
             && count(finalStates, OrderStatus.DELIVERED)      == stats.ordersDelivered.get();
        check("Counters match the orders' real statuses", countersMatch);

        
        check("No overselling (stock never negative)", remainingA >= 0 && remainingB >= 0);

        
        check("Product A: remaining " + remainingA + " + delivered " + stats.unitsDeliveredA.get()
                + " = " + Config.INITIAL_STOCK_A,
                remainingA + stats.unitsDeliveredA.get() == Config.INITIAL_STOCK_A);
        check("Product B: remaining " + remainingB + " + delivered " + stats.unitsDeliveredB.get()
                + " = " + Config.INITIAL_STOCK_B,
                remainingB + stats.unitsDeliveredB.get() == Config.INITIAL_STOCK_B);

        System.out.println();
        System.out.printf("Finished in %d ms%n", elapsedMillis);
        System.out.println("==================================================");
    }

    

    private static void row(String label, int value) {
        System.out.printf("%-28s : %5d%n", label, value);
    }

    private static void check(String description, boolean passed) {
        System.out.println("  [" + (passed ? "PASS" : "FAIL") + "] " + description);
    }

    private static int count(Map<OrderStatus, Integer> map, OrderStatus status) {
        return map.getOrDefault(status, 0);
    }
}

