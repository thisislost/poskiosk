/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package monitor1;

/**
 *
 * @author Maxim
 */
public class WindowsUSB {

    public native boolean findUSBDevices();

    public native String OrderUPS(String s, int i);

    public native String OrderUPS(String s, int i, int j);

    public native void Init();

    public native String readPort();

    public native boolean checkReadOK();

    public native boolean writePort(String s, int i);

    static {
        String osname = System.getProperty("os.name");
        if (osname.startsWith("Windows")) {
            System.loadLibrary("jusb");
        } else if (osname.equals("Mac OS") || osname.equals("Mac OS X")) {
            if (System.getProperty("os.arch").equalsIgnoreCase("i386")) {
                System.loadLibrary("jusbMacOSXi386");
            } else {
                System.loadLibrary("jusbMacOSX");
            }
        } else if (osname.equalsIgnoreCase("Linux")) {
            if (System.getProperty("os.arch").equalsIgnoreCase("amd64")) {
                System.loadLibrary("usbLinuxamd");
            } else {
                System.loadLibrary("usbLinux");
            }
        }
    }
}
