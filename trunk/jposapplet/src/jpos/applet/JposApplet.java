package jpos.applet;

import java.applet.Applet;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;
import jpos.BaseJposControl;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;

public class JposApplet extends Applet {

    private static final PrintStream err = System.err;
    private static final PrintStream out = System.out;
    private static boolean loaded = false;
    private static Applet applet = null;

    private static PrintStream createLoggingProxy(final PrintStream ps) {

        return new PrintStream(ps) {
            @Override
            public void print(final String str) {
                if (str != null) {
                    ps.print(str);
                    try {
                        JSObject window = JSObject.getWindow(applet);
                        JSObject console = (JSObject) window.getMember("console");
                        if (console != null) {
                            console.call("log", new Object[]{str});
                        }
                    } catch (JSException e) {
                    }
                }
            }
        };
    }

    private static void checkProperties() {
        synchronized (Runtime.getRuntime()) {
            if (!loaded) {
                System.out.println("Current directory is '" + getCurrentDirectory() + "'");
                try {
                    try (InputStream is = new FileInputStream("jpos.properties")) {
                        Properties properties = new Properties();
                        properties.load(is);
                        for (String key : properties.stringPropertyNames()) {
                            System.setProperty(key, properties.getProperty(key));
                        }
                        System.out.println("Properties are loaded from 'jpos.properties'");
                    }
                } catch (Throwable e) {
                    System.err.println("Error loading file 'jpos.properties'");
                }
                loaded = true;
            }
        }
    }

    @Override
    public void start() {
        synchronized (Runtime.getRuntime()) {
            if (applet == null) {
                applet = this;
                System.setErr(createLoggingProxy(JposApplet.err));
                System.setOut(createLoggingProxy(JposApplet.out));
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
        System.out.print("Device '" + deviceName + "' created");
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

    public static void exec(String command) throws IOException {
        Runtime.getRuntime().exec(command);
    }

    public static void load(String fileName) throws IOException {
        Runtime.getRuntime().load(fileName);
    }

    public static void setProperty(String key, String value) {
        System.setProperty(key, value);
    }

    public static String getProperty(String key) {
        return System.getProperty(key);
    }
}
