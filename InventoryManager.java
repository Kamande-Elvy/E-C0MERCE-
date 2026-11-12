import java.util.HashMap;
import java.util.Map;

public class InventoryManager {

    private final Map<String, Integer> stock = new HashMap<>();

    public InventoryManager(int initialA, int initialB) {
        stock.put("A", initialA);
        stock.put("B", initialB);
    }

    public synchronized boolean reserve(String product, int qty) {
        int available = stock.get(product);
        if (available < qty) {
            return false;
        }
        stock.put(product, available - qty);
        return true;
    }

    public synchronized void release(String product, int qty) {
        stock.put(product, stock.get(product) + qty);
    }

    public synchronized int getStock(String product) {
        return stock.get(product);
    }
}
