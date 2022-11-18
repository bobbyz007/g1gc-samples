package org.example;

import org.apache.lucene.util.RamUsageEstimator;
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.vm.VM;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import static java.time.temporal.ChronoField.*;

public class G1Demo
{
    public static void main( String[] args ) throws Exception
    {
        if (args == null || args.length < 2) {
            System.err.println("error: args should be [sizeOfMemory] [interval] [rounds]");
            return;
        }
        // 申请的内存大小 和 间隔时间
        int sizeInMega = Integer.valueOf(args[0]);
        int intervalInSeconds = Integer.valueOf(args[1]);
        long rounds = Long.MAX_VALUE;
        if (args.length > 2) {
            rounds = Integer.valueOf(args[2]);
        }

        while (rounds-- > 0) {
            System.out.println(now() + " Staring apply memory(MB): " + sizeInMega);
            applyInMega(sizeInMega);
            System.out.println("Ending apply memory");
            Thread.sleep(Duration.ofSeconds(intervalInSeconds).toMillis());
        }
    }

    static void applyInMega(int size) {
        int[][] arr = new int[size][];
        for (int i = 0; i < size; i++) {
            arr[i] = applyOneMega();
        }
    }

    static int[] applyOneMega() {
        int[] oneMegaArr = new int[1024 * 256 - 4];
        return oneMegaArr;
    }

    public static final DateTimeFormatter LOCAL_DATE_TIME;
    static {
        LOCAL_DATE_TIME = new DateTimeFormatterBuilder().parseCaseInsensitive()
                .append(DateTimeFormatter.ISO_LOCAL_DATE)
                .appendLiteral(' ')
                .appendValue(HOUR_OF_DAY, 2)
                .appendLiteral(':')
                .appendValue(MINUTE_OF_HOUR, 2)
                .optionalStart()
                .appendLiteral(':')
                .appendValue(SECOND_OF_MINUTE, 2)
                .toFormatter();
    }

    static String now() {
        return LOCAL_DATE_TIME.format(LocalDateTime.now());
    }

    // just for testing
    static void testSize() {
        System.out.println(VM.current().details());
        System.out.println();

        int i = 0;
        // method 1: using lucene-core util
        System.out.println(RamUsageEstimator.sizeOf(i));
        // method 2: using jol
        System.out.println(ClassLayout.parseInstance(i).toPrintable());

        // object header 8 bytes, class 4 bytes, array length 4 bytes
        int[] a = new int[1024 * 256 - 4];
        System.out.println(RamUsageEstimator.sizeOfObject(a));
        System.out.println(ClassLayout.parseInstance(a).toPrintable());
    }

    static class A {
    }
}
