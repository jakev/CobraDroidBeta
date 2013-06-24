
package dot.junit;


import com.android.ddmlib.IDevice;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

public class DeviceUtil {

    private static boolean DEBUG = System.getProperty("cts.vm-tests.debug") != null;

    /**
     * Executes the command and its arguments in a native process.
     * 
     * @param commandAndArgs a string array to be passed containing the
     *            executable and its arguments
     * @param okIndicator if not null, this String must occur in the stdout of
     *            the executable (since only checking for the return code is not
     *            sufficient e.g. for adb shell cmd)
     * @throws Exception thrown by the underlying command in case of an error.
     */
    public static void digestCommand(String[] commandAndArgs, String okIndicator) {
        RuntimeException re = null;
        try {
            String c = "";
            for (int i = 0; i < commandAndArgs.length; i++) {
                c += commandAndArgs[i] + " ";
            }
            if (DEBUG) System.out.print("com: " + c);
            StringBuilder sb = new StringBuilder();
            ProcessBuilder pb = new ProcessBuilder(commandAndArgs).redirectErrorStream(true);
            Process p = pb.start();

            InputStream is = p.getInputStream();
            Scanner scanner = new Scanner(is);
            int retCode = p.waitFor();
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine());
            }
            scanner.close();
            if (retCode != 0 || (okIndicator != null && !sb.toString().contains(okIndicator))) {
                String msg = sb.toString() + "\nreturn code: " + retCode;
                re = new RuntimeException(msg);
                if (DEBUG) System.out.println("-> error! msg:"+msg);
            } else {
                if (DEBUG) System.out.println(" -> " + retCode);
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred: " + e.getClass().getName() + ", msg:"
                    + e.getMessage());
        } finally {
            if (re != null) {
                throw re;
            }
        }
    }

    public static String createFilePath(String testName) throws IOException {
        // e.g. /dot/junit/opcodes/add_double/d/T_add_double_1.jar
        FileOutputStream fos = null;
        InputStream is = null;
        File f;
        try {
            is = DeviceUtil.class.getResourceAsStream("/tests/" + testName);
            if (is == null) {
                throw new RuntimeException("could not find resource /tests" + testName
                        + " in classpath");
            }
            f = File.createTempFile("cts-adbpush-", ".jar");
            int len = 4096;
            byte[] bytes = new byte[len];
            fos = new FileOutputStream(f);
            int b;
            while ((b = is.read(bytes)) > 0) {
                fos.write(bytes, 0, b);
            }
        } finally {
            if (is != null) {
                is.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
        return f.getAbsolutePath();
    }

    public static void adbPush(IDevice device, String source, String target)
            throws IOException {
        DeviceUtil.digestCommand(new String[] {"adb", "-s", device.getSerialNumber(), "push",
            DeviceUtil.createFilePath(source), target}, null);
    }

    public static void adbExec(IDevice device, String classpath, String mainclass) {
        DeviceUtil.digestCommand(new String[] {"adb", "-s", device.getSerialNumber(), "shell",
               "mkdir", "/data/local/tmp/dalvik-cache"}, null);
        DeviceUtil.digestCommand(new String[] {"adb", "-s", device.getSerialNumber(), "shell",
               "ANDROID_DATA=/data/local/tmp", "dalvikvm", "-Xint:portable", "-Xmx512M", "-Xss32K",
               "-Djava.io.tmpdir=/data/local/tmp", "-classpath", classpath, mainclass, "&&",
               "echo", "mk_dalvikvmok" }, "mk_dalvikvmok");
    }
 }
