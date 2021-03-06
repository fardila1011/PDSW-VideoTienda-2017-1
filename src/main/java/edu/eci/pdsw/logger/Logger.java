package edu.eci.pdsw.logger;

public class Logger {

    /**
     * OFF is a special level that can be used to turn off logging.
     * This level is initialized to Integer.MAX_VALUE.
     */
    public static final int OFF = Integer.MAX_VALUE; // Maps to java.util.logging.Level.OFF
    /**
     * ERROR is a message level indicating a serious failure.
     * This level is initialized to 4.
     */
    public static final int ERROR = 4; // Maps to java.util.logging.Level.SEVERE
    /**
     * WARNING is a message level indicating a potential problem.
     * This level is initialized to 3.
     */
    public static final int WARNING = 3; // Maps to java.util.logging.Level.WARNING
    /**
     * INFO is a message level for informational messages.
     * This level is initialized to 2.
     */
    public static final int INFO = 2; // Maps to java.util.logging.Level.INFO
    /**
     * DEBUG is a message level providing tracing information.
     * This level is initialized to 1.
     */
    public static final int DEBUG = 1; // Maps to java.util.logging.Level.FINE
    private static int currentLevel = OFF;
    private static long startTime = 0;
    private static final Object lock = new Object();

    static {
        startLogger();
    }

    /**
     * This must be run in a privileged context
     */
    private static void startLogger() {
        try {
            // Initialize logger
            Integer logLevel;
            String level = System.getProperty("jfxmedia.loglevel", "off").toLowerCase();

            if (level.equals("debug")) {
                logLevel = Integer.valueOf(Logger.DEBUG);
            } else if (level.equals("warning")) {
                logLevel = Integer.valueOf(Logger.WARNING);
            } else if (level.equals("error")) {
                logLevel = Integer.valueOf(Logger.ERROR);
            } else if (level.equals("info")) {
                logLevel = Integer.valueOf(Logger.INFO);
            } else {
                logLevel = Integer.valueOf(Logger.OFF);
            }

            setLevel(logLevel.intValue());

            startTime = System.currentTimeMillis();
        } catch (Exception e) {}

        if (Logger.canLog(Logger.DEBUG))
            Logger.logMsg(Logger.DEBUG, "Logger initialized");
    }

    private Logger() {
        // prevent instantiation of this class
    }

    /**
     * Initializes logger.
     * This function should be called once before using any other logger functions.
     * @return bla
     */
    public static boolean initNative() {
        if (nativeInit()) {
            nativeSetNativeLevel(currentLevel); // Propagate level to native layer, so it will not make unnecessary JNI calls.
            return true;
        } else {
            return false;
        }
    }

    private static native boolean nativeInit();

    /**
     * Sets logger level.
     * All messages below logger level will be ignored.
     * For example, if level is set to WARNING, only messages with ERROR and WARNING levels will be logged and messages with INFO and DEBUG levels will be ignored.
     *
     * @param level logger level
     */
    public static void setLevel(int level) {
        currentLevel = level;

        try {
            nativeSetNativeLevel(level); // Propagate level to native layer, so it will not make unnecessary JNI calls.
        } catch(UnsatisfiedLinkError e) {}
    }

    private static native void nativeSetNativeLevel(int level);

    /**
     * Checks if message at specific level can be logged.
     *
     * @param level messgae level
     * @return true if message will be logged, false otherwise.
     */
    public static boolean canLog(int level) {
        if (level < currentLevel) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Log message
     *
     * @param level message level
     * @param msg message
     */
    public static void logMsg(int level, String msg) {
        synchronized (lock) {
            if (level < currentLevel) {
                return;
            }

            if (level == ERROR) {
                System.err.println("Error (" + getTimestamp() + "): " + msg);
            } else if (level == WARNING) {
                System.err.println("Warning (" + getTimestamp() + "): " + msg);
            } else if (level == INFO) {
                System.out.println("Info (" + getTimestamp() + "): " + msg);
            } else if (level == DEBUG) {
                System.out.println("Debug (" + getTimestamp() + "): " + msg);
            }
        }
    }

    /**
     * Log message
     *
     * @param level message level
     * @param sourceClass name of class that issued the logging request
     * @param sourceMethod name of method that issued the logging request
     * @param msg message
     */
    public static void logMsg(int level, String sourceClass, String sourceMethod, String msg) {
        synchronized (lock) {
            if (level < currentLevel) {
                return;
            }

            logMsg(level, sourceClass + ":" + sourceMethod + "() " + msg);
        }
    }

    /**
     * Get timestamp based on current time
     *
     * @return timestamp
     */
    private static String getTimestamp() {
        long elapsed = System.currentTimeMillis() - startTime;
        long elapsedHours = elapsed / (60 * 60 * 1000);
        long elapsedMinutes = (elapsed - elapsedHours * 60 * 60 * 1000) / (60 * 1000);
        long elapsedSeconds = (elapsed - elapsedHours * 60 * 60 * 1000 - elapsedMinutes * 60 * 1000) / 1000;
        long elapsedMillis = elapsed - elapsedHours * 60 * 60 * 1000 - elapsedMinutes * 60 * 1000 - elapsedSeconds * 1000;

        return String.format("%d:%02d:%02d:%03d", elapsedHours, elapsedMinutes, elapsedSeconds, elapsedMillis);
    }
}