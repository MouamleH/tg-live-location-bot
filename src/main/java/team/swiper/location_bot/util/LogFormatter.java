package team.swiper.location_bot.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {


    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    // ANSI escape code
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLACK = "\u001B[30m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE = "\u001B[37m";

    private static final Map<Level, String> levelColors = new HashMap<>();

    static {
        levelColors.put(Level.ALL, ANSI_BLACK);

        levelColors.put(Level.INFO, ANSI_CYAN);
        levelColors.put(Level.WARNING, ANSI_YELLOW);
        levelColors.put(Level.SEVERE, ANSI_RED);
        levelColors.put(Level.CONFIG, ANSI_PURPLE);
        levelColors.put(Level.FINE, ANSI_BLUE);
    }


    // format is called for every console log message
    @Override
    public String format(LogRecord record) {
        // This example will print date/time, class, and log level in the level color,
        // followed by the log message and it's parameters in white .

        String className = record.getSourceClassName();

        StringBuilder builder = new StringBuilder();
        builder.append(levelColors.get(record.getLevel()));

        builder.append("[");
        builder.append(calculateDate(record.getMillis()));
        builder.append("]");

        builder.append(" [");
        builder.append(className.substring(className.lastIndexOf(".") + 1));
        builder.append(':');
        builder.append(record.getSourceMethodName());
        builder.append("]");


        builder.append(" [");
        builder.append(record.getLevel().getName());
        builder.append("]");

        builder.append(ANSI_WHITE);
        builder.append(" - ");
        builder.append(record.getMessage());

        Object[] params = record.getParameters();

        if (params != null) {
            builder.append("    ");
            for (int i = 0; i < params.length; i++) {
                builder.append(params[i]);
                if (i < params.length - 1) {
                    builder.append(", ");
                }
            }
        }

        final StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        final Throwable exception = record.getThrown();
        if (exception != null) {
            exception.printStackTrace(printWriter);
            printWriter.close();
            builder.append('\n').append(stringWriter);
        }


        builder.append(ANSI_RESET);
        builder.append("\n");
        return builder.toString();
    }

    private String calculateDate(long milliSecs) {
        return dateFormat.format(new Date(milliSecs));
    }

}