package greensnail_backend.GreenSnail.kakaopay.util;

import org.slf4j.Logger;

public final class LogUtils {

    private LogUtils() {}

    public static void info(Logger logger, String methodName, String message, Object... args) {
        logger.info("[{}] {} - {}", methodName, message, formatArgs(args));
    }

    public static void error(Logger logger, String methodName, String message, Throwable t, Object... args) {
        logger.error("[{}] {} - {}", methodName, message, formatArgs(args), t);
    }

    private static String formatArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "없음";
        }
        StringBuilder sb = new StringBuilder();
        for (Object arg : args) {
            if (arg instanceof String && ((String) arg).contains("Bearer")) {
                sb.append("authHeader: ****(masked), ");
            } else if (arg != null) {
                sb.append(arg).append(", ");
            }
        }
        return sb.length() > 2 ? sb.substring(0, sb.length() - 2) : "없음";
    }
}