public class CustomerOrder {

   
     
    public static final CustomerOrder POISON_PILL = new CustomerOrder(-1, "POISON_PILL", "-", 0);

   
    private final int orderId;
    private final String customerName;
    private final String product;     
    private final int quantity;

    private OrderStatus status = OrderStatus.PENDING;   // the only thing that changes

    public CustomerOrder(int orderId, String customerName, String product, int quantity) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.product = product;
        this.quantity = quantity;
    }

    
    public int getOrderId()         { return orderId; }
    public String getCustomerName() { return customerName; }
    public String getProduct()      { return product; }
    public int getQuantity()        { return quantity; }

    public boolean isPoisonPill() {
        return this == POISON_PILL;   
    }

    

    public synchronized OrderStatus getStatus() {
        return status;
    }

    
     
    public synchronized void setStatus(OrderStatus newStatus) {
        this.status = newStatus;
    }


      
     
    public synchronized boolean advanceTo(OrderStatus next) {
        if (status == OrderStatus.CANCELLED) {
            return false;
        }
        status = next;
        return true;
    }

    
     
     
     
     
    public synchronized boolean cancel() {
        if (status == OrderStatus.RESERVED
                || status == OrderStatus.PAID
                || status == OrderStatus.PACKED) {
            status = OrderStatus.CANCELLED;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Order#" + orderId + " (" + customerName + ", " + quantity + "x Product " + product + ")";
    }
}
