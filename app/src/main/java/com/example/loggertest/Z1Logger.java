package com.example.loggertest;

import android.content.Context;
import android.util.Log;

import com.example.loggertest.custom.CustomRollingFileAppender;

import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.android.LogcatAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.joran.util.PropertySetter;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.RollingPolicy;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.rolling.TriggeringPolicy;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.util.FileSize;
import ch.qos.logback.core.util.StatusListenerConfigHelper;

import static org.slf4j.Logger.ROOT_LOGGER_NAME;


public class Z1Logger {

    private org.slf4j.Logger    main;
    private static String logFile;
    private static String SEPERATOR_PART       = "#########################################";
    private static String SEPERATOR_PART_LONG  = "##################################################################################";
    private static String SEPERATOR_PART_SMALL = "#######";
    private static final String LOG_FOLDER = "/log";


    private static String LOG_MESSAGE_PATTERN = "%d %.-1level/%logger: %msg%n";
    private static String LOGCAT_PATTERN = "%m%n";
    private static String  FILENAME = "my-log";
    private static String  FILENAME_EXT = ".log";
    private static String  MAX_FILE_SIZE = "30KB"; // limit on the file size in encrypted case.
    private static int  ONE_LOG_FILE = 1;


    public Z1Logger(org.slf4j.Logger logger) {
        main = logger;
    }

    public static org.slf4j.Logger loggerFor(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    public static void configureLogbackDirectly(Context context){

        String loggername  = "Z1 Logger";
        File file = context.getExternalFilesDir("logs");
        String logFileName = null;
        if (file != null) {
            logFileName = file.getAbsolutePath() + "/z1-myLogger";
        } else {
            Log.d("Blah", "file is null");
        }
        logFile = logFileName + ".log";

        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        if (file != null) {
            Log.d("Blah", "property set");
            try {
                lc.putProperty("path", file.getCanonicalPath());
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("Blah", "configureLogbackDirectly: unable to get canonical name");

            }
        }

        StatusListenerConfigHelper.addOnConsoleListenerInstance(lc, new OnConsoleStatusListener());

        // setup FileAppender
        PatternLayoutEncoder encoder1 = new PatternLayoutEncoder();
        encoder1.setContext(lc);
        encoder1.setPattern("%d{dd.MM.yyyy HH:mm:ss.SSS} \t [%logger{0}] \t %msg%n");
        encoder1.start();


        // setup LogcatAppender
        PatternLayoutEncoder encoder2 = new PatternLayoutEncoder();
        encoder2.setContext(lc);
        encoder2.setPattern("%msg");
        encoder2.start();


        PatternLayoutEncoder tagEncoder = new PatternLayoutEncoder();
        tagEncoder.setContext(lc);
        tagEncoder.setPattern("\t%d{HH:mm:ss.SSS} \t [%logger{0}]\t");
        tagEncoder.start();


        LogcatAppender logcatAppender = new LogcatAppender();
        logcatAppender.setContext(lc);
        logcatAppender.setEncoder(encoder2);
        logcatAppender.setTagEncoder(tagEncoder);
        logcatAppender.start();

        // add the newly created appenders to the root logger;
        // qualify Logger to disambiguate from org.slf4j.Logger
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ROOT_LOGGER_NAME);

        if (file != null) {
            root.addAppender(createFileAppender(context, lc, file.getAbsolutePath()));
        } else {
            Log.d("Blah", "file is null");
        }
        root.addAppender(logcatAppender);

    }

    public void info(String msg) {
        if(isClassOnBlacklist()) return;
        main.info(msg);
    }

    public void info(String msg, Object arg) {
        if(isClassOnBlacklist()) return;
        main.info(msg,arg);
    }

    public void debug(String msg) {
        if(isClassOnBlacklist()) return;
        main.debug(msg);
    }

    public void debug(String format, Object arg) {
        if(isClassOnBlacklist()) return;
        main.debug(format,arg);
    }

    public void debug(String format, Object arg1, Object arg2){
        if(isClassOnBlacklist()) return;
        main.debug(format,arg1,arg2);
    }

    public void debug(String format, Object... arguments){
        if(isClassOnBlacklist()) return;
        main.debug(format,arguments);
    }

    public void trace(String msg) {
        if(isClassOnBlacklist()) return;
        main.trace(msg);
    }

    public void error(Throwable e) {
        if(isClassOnBlacklist()) return;
        main.error("", e);
    }

    public void error(String msg) {
        if(isClassOnBlacklist()) return;
        main.error(msg);
    }

    public void error(String msg, Throwable e) {
        if(isClassOnBlacklist()) return;
        main.error(msg, e);
    }

    public void printStackTrace(Throwable exception) {
        if(BuildConfig.DEBUG){
            exception.printStackTrace();
        }

        error(exception);
    }

    private boolean isClassOnBlacklist(){
        boolean retValue = false;

        if(main == null)            return retValue;
        if(main.getName() == null)  return retValue;

        if(main.getName().contains("_DataSource"))          retValue = true;
        if(main.getName().contains("_DataSource"))          retValue = true;

        return retValue;
    }


    public void debug_addSeparatorLineWithMsgInCenter(String msg){
        debug_addSeparatorLineWithMsgInCenter(msg,(Object)null);
    }

    public void debug_addSeparatorLineWithMsgInCenter(String format, Object arg){
        debug_addSeparatorLineWithMsgInCenter(format,arg,null);
    }

    public void debug_addSeparatorLineWithMsgInCenter(String format, Object arg1, Object arg2){
        Object[] arguments = {arg1,arg2};
        debug_addSeparatorLineWithMsgInCenter(format,arguments);
    }


    public void debug_addSeparatorLineWithMsgInCenter(String format, Object... arguments){
        String newMessage = " \n";

        newMessage += SEPERATOR_PART_LONG  + "\n";
        newMessage += SEPERATOR_PART_SMALL + "\t\t\t" + format + "\n";
        newMessage += SEPERATOR_PART_LONG  + "\n";

        debug(newMessage,arguments);

    }

    private static Appender<ILoggingEvent> createFileAppender(Context context, LoggerContext loggerContext,
                                                              String rootPath) {

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern(LOG_MESSAGE_PATTERN);
        encoder.setImmediateFlush(true);
        encoder.start();

        CustomRollingFileAppender<ILoggingEvent> fileAppender = new CustomRollingFileAppender<>(context);
//        RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<>();

        fileAppender.setContext(loggerContext);
        fileAppender.setName("FILE");
        fileAppender.setEncoder(encoder);
        fileAppender.setFile(rootPath + "/" + FILENAME + FILENAME_EXT);
        fileAppender.setRollingPolicy(createFixedWindowRollingPolicy(loggerContext, rootPath, fileAppender));
        fileAppender.setTriggeringPolicy(createSizeBasedTiggeringPolicy(loggerContext, MAX_FILE_SIZE));
        fileAppender.start();

        /*
          The AsyncAppender lets users log events asynchronously.
          We wrap the file appender with async appender to write file in non-ui thread.

          https://logging.apache.org/log4j/1.2/apidocs/org/apache/log4j/AsyncAppender.html
         */
        AsyncAppender asyncAppender = new AsyncAppender();
        asyncAppender.setContext(loggerContext);
        asyncAppender.setName("ASYNC");
        asyncAppender.addAppender(fileAppender);
        asyncAppender.start();

        return asyncAppender;
    }

    /**
     * Creates [FixedWindowRollingPolicy] which takes care of rolling the files,
     * Naming with the fileNamePattern for the old files
     * Deleting the old files to make sure it doesn't exceed the provided [maxNumOfFiles]
     */
    private static RollingPolicy createFixedWindowRollingPolicy(
            LoggerContext loggerContext,
            String rootPath,
            FileAppender<ILoggingEvent> parent) {

        FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
        rollingPolicy.setContext(loggerContext);
        rollingPolicy.setParent(parent);
        rollingPolicy.setFileNamePattern(rootPath + "/" + FILENAME + "-%i" + FILENAME_EXT);
        rollingPolicy.setMinIndex(ONE_LOG_FILE);
        rollingPolicy.setMaxIndex(ONE_LOG_FILE);
        rollingPolicy.start();

        return rollingPolicy;
    }

    /**
     * Creates the [TriggeringPolicy] which takes care of triggering the rollover at the
     * specified [maxFileSize]
     */
    private static TriggeringPolicy<ILoggingEvent> createSizeBasedTiggeringPolicy(
            LoggerContext loggerContext,
            String maxFileSize
    ) {

        SizeBasedTriggeringPolicy<ILoggingEvent> triggeringPolicy = new SizeBasedTriggeringPolicy<ILoggingEvent>();
        triggeringPolicy.setContext(loggerContext);
        triggeringPolicy.setMaxFileSize(FileSize.valueOf(maxFileSize));
        triggeringPolicy.start();
        return triggeringPolicy;
    }

}
