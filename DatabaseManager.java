import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

public class DatabaseManager {

    private static final String URL = "jdbc:sqlite:ecommerce.db";

    public static void saveRun(Stats stats,
                               InventoryManager inventory,
                               List<DeliveryVehicle> vehicles,
                               List<CustomerOrder> allOrders,
                               long elapsedMillis) {
        try (Connection conn = DriverManager.getConnection(URL)) {
            createTables(conn);
            conn.setAutoCommit(false);
            try {
                int runId = insertRun(conn, stats, elapsedMillis);
                insertOrders(conn, runId, allOrders);
                insertInventory(conn, runId, inventory);
                insertVehicles(conn, runId, vehicles);
                conn.commit();
                System.out.println("Saved run #" + runId + " to ecommerce.db");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }

    public static void printRecentRuns(int limit) {
        String sql = "SELECT run_id, run_at, orders_received, orders_delivered, payments_failed, "
                   + "orders_cancelled, out_of_stock, elapsed_ms FROM runs ORDER BY run_id DESC LIMIT ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println();
                System.out.println("Recent runs saved in ecommerce.db:");
                System.out.printf("  %-4s %-19s %8s %9s %6s %9s %12s %8s%n",
                        "Run", "When", "Received", "Delivered", "Failed", "Cancelled", "Out of stock", "Time ms");
                while (rs.next()) {
                    System.out.printf("  %-4d %-19s %8d %9d %6d %9d %12d %8d%n",
                            rs.getInt("run_id"),
                            rs.getString("run_at"),
                            rs.getInt("orders_received"),
                            rs.getInt("orders_delivered"),
                            rs.getInt("payments_failed"),
                            rs.getInt("orders_cancelled"),
                            rs.getInt("out_of_stock"),
                            rs.getLong("elapsed_ms"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }

    private static void createTables(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS runs ("
                    + "run_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "run_at TEXT NOT NULL, "
                    + "orders_received INTEGER, out_of_stock INTEGER, "
                    + "payments_successful INTEGER, payments_failed INTEGER, "
                    + "orders_fulfilled INTEGER, orders_dispatched INTEGER, "
                    + "orders_delivered INTEGER, orders_cancelled INTEGER, "
                    + "elapsed_ms INTEGER)");
            st.execute("CREATE TABLE IF NOT EXISTS orders ("
                    + "run_id INTEGER NOT NULL REFERENCES runs(run_id), "
                    + "order_id INTEGER NOT NULL, "
                    + "customer_name TEXT NOT NULL, "
                    + "product TEXT NOT NULL, "
                    + "quantity INTEGER NOT NULL, "
                    + "status TEXT NOT NULL, "
                    + "PRIMARY KEY (run_id, order_id))");
            st.execute("CREATE TABLE IF NOT EXISTS inventory ("
                    + "run_id INTEGER NOT NULL REFERENCES runs(run_id), "
                    + "product TEXT NOT NULL, "
                    + "initial_stock INTEGER NOT NULL, "
                    + "remaining_stock INTEGER NOT NULL, "
                    + "PRIMARY KEY (run_id, product))");
            st.execute("CREATE TABLE IF NOT EXISTS vehicles ("
                    + "run_id INTEGER NOT NULL REFERENCES runs(run_id), "
                    + "vehicle_name TEXT NOT NULL, "
                    + "trips INTEGER NOT NULL, "
                    + "busy_ms INTEGER NOT NULL, "
                    + "PRIMARY KEY (run_id, vehicle_name))");
        }
    }

    private static int insertRun(Connection conn, Stats stats, long elapsedMillis) throws SQLException {
        String sql = "INSERT INTO runs (run_at, orders_received, out_of_stock, payments_successful, "
                   + "payments_failed, orders_fulfilled, orders_dispatched, orders_delivered, "
                   + "orders_cancelled, elapsed_ms) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, LocalDateTime.now().withNano(0).toString().replace('T', ' '));
            ps.setInt(2, stats.ordersReceived.get());
            ps.setInt(3, stats.outOfStock.get());
            ps.setInt(4, stats.paymentsSuccessful.get());
            ps.setInt(5, stats.paymentsFailed.get());
            ps.setInt(6, stats.ordersFulfilled.get());
            ps.setInt(7, stats.ordersDispatched.get());
            ps.setInt(8, stats.ordersDelivered.get());
            ps.setInt(9, stats.ordersCancelled.get());
            ps.setLong(10, elapsedMillis);
            ps.executeUpdate();
        }
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static void insertOrders(Connection conn, int runId, List<CustomerOrder> orders) throws SQLException {
        String sql = "INSERT INTO orders (run_id, order_id, customer_name, product, quantity, status) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (CustomerOrder o : orders) {
                ps.setInt(1, runId);
                ps.setInt(2, o.getOrderId());
                ps.setString(3, o.getCustomerName());
                ps.setString(4, o.getProduct());
                ps.setInt(5, o.getQuantity());
                ps.setString(6, o.getStatus().name());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void insertInventory(Connection conn, int runId, InventoryManager inventory) throws SQLException {
        String sql = "INSERT INTO inventory (run_id, product, initial_stock, remaining_stock) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            addInventoryRow(ps, runId, "A", Config.INITIAL_STOCK_A, inventory.getStock("A"));
            addInventoryRow(ps, runId, "B", Config.INITIAL_STOCK_B, inventory.getStock("B"));
            ps.executeBatch();
        }
    }

    private static void addInventoryRow(PreparedStatement ps, int runId, String product,
                                        int initial, int remaining) throws SQLException {
        ps.setInt(1, runId);
        ps.setString(2, product);
        ps.setInt(3, initial);
        ps.setInt(4, remaining);
        ps.addBatch();
    }

    private static void insertVehicles(Connection conn, int runId, List<DeliveryVehicle> vehicles) throws SQLException {
        String sql = "INSERT INTO vehicles (run_id, vehicle_name, trips, busy_ms) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (DeliveryVehicle v : vehicles) {
                ps.setInt(1, runId);
                ps.setString(2, v.getName());
                ps.setInt(3, v.getTripsCompleted());
                ps.setLong(4, v.getBusyMillis());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}