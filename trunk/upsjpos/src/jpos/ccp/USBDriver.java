package jpos.ccp;

import monitor1.WindowsUSB;
import jpos.JposConst;
import jpos.JposException;

class USBDriver implements PortDriver {

    public void close() throws JposException {
    }

    public String execute(String command, int replyLen) throws JposException {
        String osname = System.getProperty("os.name");
        String response;
        if (osname.equalsIgnoreCase("Linux")) {
            response = usb.OrderUPS(command+"\r", command.length(), replyLen);
        } else {
            response = usb.OrderUPS(command+"\r", replyLen);
        }
        response = response.replace("\r", "");
        return response;
    }

    public void open(String portName) throws JposException {
        String osname = System.getProperty("os.name");
        if (osname.equalsIgnoreCase("Linux")) {
            usb.Init();
        }
        boolean foundDevice = false;
        if (osname.equals("Mac OS") || osname.equals("Mac OS X")) {
            foundDevice = true;
        } else if (osname.startsWith("Windows") || osname.equalsIgnoreCase("Linux")) {
            foundDevice = usb.findUSBDevices();
        }
        if (!foundDevice) {
            throw new JposException(JposConst.JPOS_E_NOHARDWARE, "UPS is not found on USB port");
        }
    }

    WindowsUSB usb = new WindowsUSB();
}
