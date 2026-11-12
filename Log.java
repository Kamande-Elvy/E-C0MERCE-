public final class Log {

    private Log() { }

    public static void event(String message) {
        if (Config.VERBOSE) {
            // %-18s = pad the thread name to 18 characters so the columns line up
            System.out.printf("[%-18s] %s%n", Thread.currentThread().getName(), message);
        }
    }
}