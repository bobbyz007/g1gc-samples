package org.example;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.util.RamUsageEstimator;
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.vm.VM;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.temporal.ChronoField.*;

public class G1Demo
{
    public static void main( String[] args ) throws Exception
    {
        // testSize();
        Args parsedArgs = parseArgs(args);
        if (!parsedArgs.isValid()) {
            System.err.println("error: args should be [sizeOfMemory KB/MB] [sizeUnit KB/MB] [interval m/s] [rounds]");
            return;
        }

        while (parsedArgs.rounds-- > 0) {
            System.out.println(now() + " Staring apply memory: " + parsedArgs.sizeInKB + " KB");
            apply(parsedArgs.sizeInKB, parsedArgs.sizeUnitInKB);
            System.out.println("Ending apply memory");
            System.out.println();
            Thread.sleep(Duration.ofSeconds(parsedArgs.intervalInSecs).toMillis());
        }
    }

    static class Args{
        // sizeInMega 本次周期需申请的总内存大小
        int sizeInKB = -1;

        // sizeUnit 申请内存的基本单位大小，比如sizeInMega为1M，sizeUnit为1KB，则需要申请1MB / 1KB = 1024次
        int sizeUnitInKB = -1;

        // interval 本次周期间隔时间
        int intervalInSecs = -1;

        // rounds 多少个周期，不指定则默认最大Long.MAX_VALUE
        long rounds = Long.MAX_VALUE;

        public boolean isValid() {
            return sizeInKB != -1 && sizeUnitInKB != -1 && intervalInSecs != -1;
        }
    }

    private static Args parseArgs(String[] args) {
        Args result = new Args();
        if (args == null || args.length < 3) {
            return result;
        }

        result.sizeInKB = sizeInKilos(args[0]);
        result.sizeUnitInKB = sizeInKilos(args[1]);
        result.intervalInSecs = intervalInSecs(args[2]);

        if (args.length > 3 && result.isValid()) {
            result.rounds = Integer.valueOf(args[3]);
        }

        return result;
    }

    static Pattern PATTERN_VALUE_UNIT = Pattern.compile("^(\\d+)([a-zA-Z]+)$");
    static Pair<Integer, String> parseArg(String arg) {
        Matcher m = PATTERN_VALUE_UNIT.matcher(arg);
        return m.matches() ? Pair.of(Integer.valueOf(m.group(1)), m.group(2)) : null;
    }

    static int intervalInSecs(String interval) {
        Pair<Integer, String> p = parseArg(interval);
        if (p == null) {
            return -1;
        }

        if (StringUtils.equalsAnyIgnoreCase(p.getRight(), "m", "min", "mins")) {
            return p.getLeft() * 60;
        } else if (StringUtils.equalsAnyIgnoreCase(p.getRight(), "s", "sec", "secs")) {
            return p.getLeft();
        }
        return -1;
    }

    static int sizeInKilos(String size) {
        Pair<Integer, String> p = parseArg(size);
        if (p == null) {
            return -1;
        }
        int result;
        if (StringUtils.equalsAnyIgnoreCase(p.getRight(), "m", "mb")) {
            return p.getLeft() * 1024;
        } else if (StringUtils.equalsAnyIgnoreCase(p.getRight(), "k", "kb")) {
            return p.getLeft();
        } else {
            return -1;
        }
    }

    static void apply(int sizeInKB, int sizeUnitInKB) {
        // x / y + (x % y != 0 ? 1 : 0)
        int applyCount = (sizeInKB + sizeUnitInKB - 1) / sizeUnitInKB;
        int[][] arr = new int[applyCount][];
        System.out.println(String.format("Applied count of size unit(%s KB): %s", sizeUnitInKB, applyCount));
        for (int i = 0; i < applyCount; i++) {
            arr[i] = applyInKB(sizeUnitInKB);
        }
    }

    static int[] applyInKB(int howMany) {
        int[] kilosArr = new int[howMany * 256 - 4];
        return kilosArr;
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
        int[] sizeMega = new int[1024 * 256 - 4];
        System.out.println(RamUsageEstimator.sizeOfObject(sizeMega));
        System.out.println(ClassLayout.parseInstance(sizeMega).toPrintable());

        // object header 8 bytes, class 4 bytes, array length 4 bytes
        int[] sizeKibo = new int[256 - 4];
        System.out.println(RamUsageEstimator.sizeOfObject(sizeKibo));
        System.out.println(ClassLayout.parseInstance(sizeKibo).toPrintable());
    }

    static class A {
    }
}
