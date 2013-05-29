/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jpos.ccp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.comm.CommPort;
import javax.comm.CommPortIdentifier;
import javax.comm.NoSuchPortException;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.UnsupportedCommOperationException;
import jpos.JposConst;
import jpos.JposException;

/**
 *
 * @author Maxim
 */
public class COMDriver implements PortDriver {

    public void close() throws JposException {
        port.close();
    }

    public String execute(String command, int replyLen) throws JposException {
        try {
            // Clear input
            port.enableReceiveTimeout(20);
            while (is.read() >= 0) {}
            // Send command
            for (char ch: command.toCharArray()) {
                os.write((int)ch);
            }
            os.write(13);
            os.flush();
            // Read response
            port.enableReceiveTimeout(2000);
            int i = is.read();
            port.enableReceiveTimeout(20);
            StringBuilder sb = new StringBuilder(replyLen);
            while ((i >= 0) && (i != 13)) {
                sb.append((char)i);
                i = is.read();
            }
            port.disableReceiveTimeout();
            if (i < 0)
                throw new JposException(JposConst.JPOS_E_TIMEOUT, "Port io timeout");
            return sb.toString();
        } catch (UnsupportedCommOperationException e) {
            throw new JposException(JposConst.JPOS_E_FAILURE, "Unsupported com operations", e);
        } catch (IOException e) {
            throw new JposException(JposConst.JPOS_E_FAILURE, "Port io error " + e.getMessage(), e);
        }
    }

    public void open(String portName) throws JposException {
        // Open port
        try {
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
            CommPort commPort = portIdentifier.open("UPS", 10000);
            if (commPort instanceof SerialPort) {
                port = ((SerialPort)commPort);
                port.setSerialPortParams(2400,
                        SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
                port.setDTR(true);
                port.setRTS(true);
                port.setInputBufferSize(1024);
                port.setOutputBufferSize(1024);
            } else
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Selected port is not serial");
        } catch (NoSuchPortException e) {
            throw new JposException(JposConst.JPOS_E_NOHARDWARE, "Not such port", e);
        } catch (PortInUseException e) {
            throw new JposException(JposConst.JPOS_E_EXISTS, "Port already is use", e);
        } catch (UnsupportedCommOperationException e) {
            throw new JposException(JposConst.JPOS_E_FAILURE, "Unsupported com operations", e);
        }
        // Streams
        try {
            is = port.getInputStream();
            os = port.getOutputStream();
        } catch (IOException e) {
            throw new JposException(JposConst.JPOS_E_FAILURE, "Error creation io streams");
        }
    }

    private SerialPort port;
    private InputStream is;
    private OutputStream os;

}
