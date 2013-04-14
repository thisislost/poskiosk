package jpos.applet;

import java.applet.Applet;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jpos.BaseJposControl;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;

public class JposApplet extends Applet {

    private static final PrintStream err = System.err;
    private static final PrintStream out = System.out;
    private static boolean loaded = false;
    private static Applet applet = null;
    private final String nl = System.getProperty("line.separator");
    private static Pattern p[] = null;
    private static String r[] = null;

    static {
        try {
            p = new Pattern[4];
            r = new String[4];
            p[0] = Pattern.compile("\\D\\d{12,16}\\D");
            p[1] = Pattern.compile("key=\"\\S*\"");
            p[2] = Pattern.compile("PINBlock=\"\\S*\"");
            p[3] = Pattern.compile("Track2=\"\\S*\"");
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private static String panmask(final String message) {
        if (message == null) {
            return null;
        }
        String result = message;
        for (int i = 0; i < p.length; ++i) {
            Matcher m = p[i].matcher(result);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                if (i == 0) {
                    m.appendReplacement(sb, result.substring(m.start(), m.start() + 7) + "******" + result.substring(m.end() - 5, m.end()));
                } else {
                    m.appendReplacement(sb, r[i]);
                }

            }
            m.appendTail(sb);
            result = sb.toString();
        }
        return result;

    }

    public static void consoleOutput(final String logMethod, final String str) {
        consoleOutput(logMethod, str, 2);
    }
    private static SimpleDateFormat sdf;

    static {
        try {
            sdf = new SimpleDateFormat("dd.mm.yy HH:MM:SS");
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public static void consoleOutput(final String logMethod, final String str, int targetMethodLevel) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        String message = "";
        if (stackTraceElements.length >= targetMethodLevel + 1) {
            StackTraceElement element = stackTraceElements[targetMethodLevel];
            String className = element.getClassName();
            String methodName = element.getMethodName();
            message = className + "." + methodName + "(" + element.getLineNumber()  + ")";
        }
        try {
            JSObject window = JSObject.getWindow(applet);
            JSObject console = (JSObject) window.getMember("console");
            if (console != null) {

                console.call(logMethod, new Object[]{sdf.format(new GregorianCalendar().getTime()) + " " + (!message.startsWith("jpos.util.tracing.Tracer") ? message + " " : "") + str});
            }
        } catch (JSException e) {
        }
    }

    private static PrintStream createLoggingProxy(final PrintStream ps, final String logMethod) {

        return new PrintStream(ps) {
            @Override
            public void print(final String str) {
                if (str != null) {
                    ps.print(str);
                    consoleOutput(logMethod, str,4);
                }
            }
        };
    }

    public static void log(String message) {
        consoleOutput("log", message, 3);
    }

    public static void logError(String message) {
        consoleOutput("error", message, 3);
    }

    public static void logError(String message, Throwable t) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        PrintWriter pw = new PrintWriter(baos);
        t.printStackTrace(pw);
        pw.flush();
        try {
            consoleOutput("error", message + "\n" + t.getMessage() + "\n" + baos.toString("UTF8"), 3);
        } catch (UnsupportedEncodingException e) {
            logError(e.toString());
        }
    }

    private static void checkProperties() {
        synchronized (Runtime.getRuntime()) {
            if (!loaded) {
                log("Current directory is '" + getCurrentDirectory() + "'");
                try {
                    try (InputStream is = new FileInputStream("jpos.properties")) {
                        Properties properties = new Properties();
                        properties.load(is);
                        for (String key : properties.stringPropertyNames()) {
                            System.setProperty(key, properties.getProperty(key));
                        }
                        log("Properties are loaded from 'jpos.properties'");
                    }
                } catch (Throwable e) {
                    System.err.println("Error loading file 'jpos.properties'");
                }
                loaded = true;
            }
        }
    }

    /**
     * @return the defaultExecCharsetName
     */
    public static String getDefaultCharsetName() {
        return defaultCharsetName;
    }

    /**
     * @param aDefaultExecCharsetName the defaultExecCharsetName to set
     */
    public static void setDefaultCharsetName(String aDefaultExecCharsetName) {
        defaultCharsetName = aDefaultExecCharsetName;
    }

    @Override
    public void start() {
        synchronized (Runtime.getRuntime()) {
            if (applet == null) {
                applet = this;
                System.setErr(createLoggingProxy(JposApplet.err, "log"));
                System.setOut(createLoggingProxy(JposApplet.out, "log"));
            }
        }
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        synchronized (Runtime.getRuntime()) {
            if (applet == this) {
                System.setErr(JposApplet.err);
                System.setOut(JposApplet.out);
                applet = null;
            }
        }
    }

    public static BaseJposControl createDevice(String deviceName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        checkProperties();
        String deviceClassName = "jpos.applet." + deviceName;
        Class deviceClass = Class.forName(deviceClassName);
        BaseJposControl dev = (BaseJposControl) deviceClass.newInstance();
        log("Device '" + deviceName + "' created");
        return dev;
    }

    public static String traceDeviceOpen(String deviceName, String seviceName) {
        try {
            BaseJposControl control = createDevice(deviceName);
            control.open(seviceName);
            try {
                control.claim(0);
                control.setDeviceEnabled(true);
            } finally {
                control.close();
            }
            return "Success";
        } catch (Throwable e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return sw.toString();
        }
    }

    public static boolean setCurrentDirectory(String dirName) {
        boolean result = false;
        File directory;
        directory = new File(dirName).getAbsoluteFile();
        if (directory.exists() || directory.mkdirs()) {
            result = (System.setProperty("user.dir", directory.getAbsolutePath()) != null);
        }
        return result;
    }

    public static String getCurrentDirectory() {
        return System.getProperty("user.dir");
    }

    public static String readFile(String fileName) throws IOException {
        File file = new File(fileName);
        char[] buffer = new char[(int) file.length()];
        try (FileReader fr = new FileReader(file)) {
            int readed = fr.read(buffer);
            return String.copyValueOf(buffer, 0, readed);
        }
    }

    public static void writeFile(String fileName, String content) throws IOException {
        File file = new File(fileName);
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(content);
        }
    }

//    public static void exec(String command) throws IOException {
//        Runtime.getRuntime().exec(command);
//    }
    public static void load(String fileName) throws IOException {
        Runtime.getRuntime().load(fileName);
    }

    public static void setProperty(String key, String value) {
        System.setProperty(key, value);
    }

    public static String getProperty(String key) {
        return System.getProperty(key);
    }
    /**
     * Вычитыватель потоков вывода - если не вычитывать отдельными потоками то
     * может подвисать....
     */
    private static String defaultCharsetName = "cp866";

    private static final class StreamGrabber extends Thread {

        InputStream is;
        String type;
        StringBuilder out = new StringBuilder("");

        StreamGrabber(InputStream is, String type) {
            super("OSExecutor$" + type);
            this.is = is;
            this.type = type;
        }

        @Override
        public void run() {
            try {
                final InputStreamReader inputStreamReader = new InputStreamReader(is, defaultCharsetName);
                final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    out.append(line).append("\n");
                }
            } catch (IOException ioe) {
                ioe.printStackTrace(System.err);
            }
        }

        @Override
        public String toString() {
            return out.toString();
        }
    }

    public static void exec(final String commandLine, final JSObject event) {
        System.out.println("enter OS exec");
        new Thread("OSExecutor") {
            public void run() {
                try {
                    String cmdLine1 = commandLine; //"cmd  /C" + commandLine; //"c:\\temp\\mkftp.cmd mkftp.cmd";

                    log("Начало выполнение команды OS:" + cmdLine1);

                    Process p = Runtime.getRuntime().exec(cmdLine1);
                    StreamGrabber err = new StreamGrabber(p.getErrorStream(), "ERROR");
                    err.start();
                    StreamGrabber out = new StreamGrabber(p.getInputStream(), "OUTPUT");
                    out.start();
                    int status = -99;
                    while (true) {
                        try {
                            status = p.waitFor();
                            break;
                        } catch (InterruptedException ie) {
                            log("child process output reader interrupted");
                            p.destroy();
                            return;
                        }
                    }
                    log("Выполнена команда OS:" + commandLine + "  ,status=" + status);
                    if (event != null) {
                        event.call("call", new Object[]{applet, status, out.toString(), err.toString(), commandLine});
                    }

                } catch (Throwable ex) {
                    logError("Ошибка при выполнении команды OS:" + ex.getMessage());
                    if (event != null) {
                        event.call("call", new Object[]{applet, null, null, ex.getMessage(), commandLine});
                    }
                }
            }
        }.start();
    }
}
