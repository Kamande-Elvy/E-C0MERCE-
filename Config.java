public final class Config {

    private Config() { }   

    
    public static final int NUM_CUSTOMERS     = 1000;
    public static final int PAYMENT_WORKERS   = 3;
    public static final int WAREHOUSE_WORKERS = 5;
    public static final int DELIVERY_VEHICLES = 4;
    public static final int INITIAL_STOCK_A   = 100;
    public static final int INITIAL_STOCK_B   = 50;

    // ----- Our own simulation choices -----
    public static final int    MAX_QTY_PER_ORDER    = 3;
    public static final double PAYMENT_FAILURE_RATE = 0.10;  
    public static final double CANCEL_RATE          = 0.05;  

    
    public static final int CUSTOMER_THREADS = 100;  
    public static final int DELIVERY_THREADS = 10;   

    
    public static final int PAYMENT_MIN_MS   = 5,  PAYMENT_MAX_MS   = 15;
    public static final int WAREHOUSE_MIN_MS = 3,  WAREHOUSE_MAX_MS = 8;
    public static final int DELIVERY_MIN_MS  = 10, DELIVERY_MAX_MS  = 30;

    
    public static final boolean VERBOSE = false;
}