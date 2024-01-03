package org.foxfairy.base.api.core.util;
import java.util.concurrent.ThreadFactory;

public class VirtualThreadUtil {
    private static final ThreadFactory factory = Thread.ofVirtual().name("virtual-", 0).factory();
    public static void run(Runnable runnable){
        factory.newThread(runnable)
                .start();
    }
}
