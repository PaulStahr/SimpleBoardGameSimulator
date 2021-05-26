package logging;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;


public class LockbackUtil {
	private static Logger logger = LoggerFactory.getLogger(LockbackUtil.class);
	private static String cachedLevel = "";
	public static void setLoglevel(String logLevel)
	{
        Level level = Level.toLevel(logLevel.toUpperCase());  // default to Level.DEBUG
        if (cachedLevel.equalsIgnoreCase(level.levelStr)) {
            logger.debug("level: {} not changed", cachedLevel);
            return;
        }
        logger.info("level will change from: {} to: {}", cachedLevel, level.levelStr);
        cachedLevel = level.levelStr;
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        List<ch.qos.logback.classic.Logger> loggerList = loggerContext.getLoggerList();
        loggerList.stream().forEach(new Consumer<ch.qos.logback.classic.Logger>() {
            @Override
            public void accept(ch.qos.logback.classic.Logger tmpLogger) {
                tmpLogger.setLevel(level);
            }
        });
	}
}
