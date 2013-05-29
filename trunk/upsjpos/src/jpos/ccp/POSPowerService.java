/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jpos.ccp;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import jpos.JposConst;
import jpos.JposException;
import jpos.POSPowerConst;
import jpos.events.StatusUpdateEvent;
import jpos.services.POSPowerService113;

/**
 *
 * @author Maxim
 */
public class POSPowerService extends DeviceService implements POSPowerService113 {

    public boolean getCapFanAlarm() throws JposException {
        return false;
    }

    public boolean getCapHeatAlarm() throws JposException {
        return false;
    }

    public int getCapPowerReporting() throws JposException {
        return JposConst.JPOS_PR_ADVANCED;
    }

    public boolean getCapQuickCharge() throws JposException {
        return false;
    }

    public boolean getCapShutdownPOS() throws JposException {
        return true;
    }

    public int getCapUPSChargeState() throws JposException {
        return POSPowerConst.PWR_UPS_FULL + POSPowerConst.PWR_UPS_LOW;
    }

    public int getEnforcedShutdownDelayTime() throws JposException {
        return shutdownDelayTime;
    }

    public int getPowerFailDelayTime() throws JposException {
        return powerFailDelayTime;
    }

    public int getPowerNotify() throws JposException {
        return powerNotify;
    }

    public int getPowerState() throws JposException {
        return powerState;
    }

    public boolean getQuickChargeMode() throws JposException {
        return false;
    }

    public int getQuickChargeTime() throws JposException {
        return 0;
    }

    public int getUPSChargeState() throws JposException {
        return chargeState;
    }

    public void setEnforcedShutdownDelayTime(int i) throws JposException {
        shutdownDelayTime = i;
    }

    public void setPowerNotify(int i) throws JposException {
        powerNotify = i;
    }

    public void shutdownPOS() throws JposException {
        try {
            shutdownActive = true;
            fireEvent(new StatusUpdateEvent(eventCallbacks.getEventSource(),
                    POSPowerConst.PWR_SUE_SHUTDOWN));
            Runtime.getRuntime().exec("shutdown -s");
            port.execute("S01R0001", 8);
        } catch (IOException e) {
            shutdownActive = false;
            throw new JposException(JposConst.JPOS_E_FAILURE,
                    getErrorDescription(JposConst.JPOS_E_FAILURE), e);
        }
    }

    public boolean getCapStatisticsReporting() throws JposException {
        return false;
    }

    public boolean getCapUpdateStatistics() throws JposException {
        return false;
    }

    public void resetStatistics(String string) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    public void retrieveStatistics(String[] strings) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    public void updateStatistics(String string) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    public void compareFirmwareVersion(String string, int[] ints) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    public int getBatteryCapacityRemaining() throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    public int getBatteryCriticallyLowThreshold() throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    public int getBatteryLowThreshold() throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    public boolean getCapBatteryCapacityRemaining() throws JposException {
        return false;
    }

    public boolean getCapCompareFirmwareVersion() throws JposException {
        return false;
    }

    public boolean getCapRestartPOS() throws JposException {
        return true;
    }

    public boolean getCapStandbyPOS() throws JposException {
        return false;
    }

    public boolean getCapSuspendPOS() throws JposException {
        return false;
    }

    public boolean getCapUpdateFirmware() throws JposException {
        return false;
    }

    public boolean getCapVariableBatteryCriticallyLowThreshold() throws JposException {
        return false;
    }

    public boolean getCapVariableBatteryLowThreshold() throws JposException {
        return false;
    }

    public int getPowerSource() throws JposException {
        return powerSource;
    }

    public void restartPOS() throws JposException {
        try {
            shutdownActive = true;
            fireEvent(new StatusUpdateEvent(eventCallbacks.getEventSource(),
                    POSPowerConst.PWR_SUE_RESTART));
            Process child = Runtime.getRuntime().exec("shutdown -r");
        } catch (IOException e) {
            shutdownActive = false;
            throw new JposException(JposConst.JPOS_E_FAILURE,
                    getErrorDescription(JposConst.JPOS_E_FAILURE), e);
        }
    }

    public void setBatteryCriticallyLowThreshold(int i) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    public void setBatteryLowThreshold(int i) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    public void standbyPOS(int i) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    public void suspendPOS(int i) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    public void updateFirmware(String string) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    @Override
    protected String getStatusDescription(int status) {
        String s;
        if ((status & STATUS_UPS_INTERACTIVE) > 0) {
            s = "UPS type is interactive.";
        } else {
            s = "UPS type is online.";
        }
        if ((status & STATUS_UTILITY_FAIL) > 0) {
            s = s + " Utility Fail.";
        }
        if ((status & STATUS_BATTERY_LOW) > 0) {
            s = s + " Battery Low.";
        }
        if ((status & STATUS_AVR) > 0) {
            s = s + " AVR.";
        }
        if ((status & STATUS_UPS_FAILED) > 0) {
            s = s + " UPS Failed.";
        }
        if ((status & STATUS_TEST_IN_PROGRESS) > 0) {
            s = s + " Test in Progress.";
        }
        if ((status & STATUS_SHUTDOWN_ACTIVE) > 0) {
            s = s + " Shutdown Active.";
        }
        return s;
    }

    @Override
    protected void poll() throws JposException {
        String response = port.execute("Q1", 47);
        if (response.startsWith("(")) {
            try {
                String[] ra = response.substring(1).split(" ");
                inputVoltage = Double.valueOf(ra[0]);
                inputFaultVoltage = Double.valueOf(ra[1]);
                outputVoltage = Double.valueOf(ra[2]);
                outputCurrent = Integer.valueOf(ra[3]);
                outputFrequency = Double.valueOf(ra[4]);
                batteryVoltage = Double.valueOf(ra[5]);
                temperature = Double.valueOf(ra[6]);
                String rstatus = ra[7];
                // System.out.println(response);
                status = 0;
                for (int i = 0; i < 8; i++) {
                    status <<= 1;
                    if (i < rstatus.length()) {
                        if (rstatus.charAt(i) == '1') {
                            status = status | 0x01;
                        }
                    }
                }
                if ((status & STATUS_BATTERY_LOW) > 0) {
                    if (chargeState != POSPowerConst.PWR_UPS_LOW) {
                        chargeState = POSPowerConst.PWR_UPS_LOW;
                        fireEvent(new StatusUpdateEvent(eventCallbacks.getEventSource(),
                                POSPowerConst.PWR_SUE_UPS_LOW));
                    }
                } else {
                    if (chargeState != POSPowerConst.PWR_UPS_FULL) {
                        chargeState = POSPowerConst.PWR_UPS_FULL;
                        fireEvent(new StatusUpdateEvent(eventCallbacks.getEventSource(),
                                POSPowerConst.PWR_SUE_UPS_FULL));
                    }
                }
                if ((status & STATUS_UTILITY_FAIL) > 0) {
                    if (powerSource != POSPowerConst.PWR_SOURCE_BATTERY) {
                        powerSource = POSPowerConst.PWR_SOURCE_BATTERY;
                        fireEvent(new StatusUpdateEvent(eventCallbacks.getEventSource(),
                                POSPowerConst.PWR_SUE_PWR_SOURCE));
                        onPowerFail();
                    }
                } else {
                    if (powerSource != POSPowerConst.PWR_SOURCE_AC) {
                        powerSource = POSPowerConst.PWR_SOURCE_AC;
                        fireEvent(new StatusUpdateEvent(eventCallbacks.getEventSource(),
                                POSPowerConst.PWR_SUE_PWR_SOURCE));
                        onPowerRestored();
                    }
                }
            } catch (Exception e) {
                throw new JposException(JposConst.JPOS_E_FAILURE,
                        getErrorDescription(JposConst.JPOS_E_FAILURE));
            }
        } else {
            throw new JposException(JposConst.JPOS_E_FAILURE,
                    getErrorDescription(JposConst.JPOS_E_FAILURE));
        }
    }

    @Override
    protected void reset() throws JposException {
        String response = port.execute("F", 22);
        if (response.startsWith("#")) {
            String[] ra = response.substring(1).split(" ");
            ratingVoltage = Double.valueOf(ra[0]);
            ratingCurrent = Integer.valueOf(ra[1]);
            ratingBatteryVoltage = Double.valueOf(ra[2]);
            ratingFrequency = Double.valueOf(ra[3]);
        } else {
            throw new JposException(JposConst.JPOS_E_FAILURE,
                    getErrorDescription(JposConst.JPOS_E_FAILURE));
        }
    }

    private void startShutdown() {
        if (powerState != JposConst.JPOS_PS_OFF) {
            powerState = JposConst.JPOS_PS_OFF;
            if (powerNotify == JposConst.JPOS_PN_ENABLED) {
                fireEvent(new StatusUpdateEvent(eventCallbacks.getEventSource(),
                        JposConst.JPOS_SUE_POWER_OFF));
            }
        }
        if (shutdownDelayTime > 0) {
            TimerTask task = new TimerTask() {

                public void run() {
                    try {
                        shutdownTimer = null;
                        shutdownPOS();
                    } catch (JposException e) {}
                }
            };
            shutdownTimer = new Timer();
            shutdownTimer.schedule(task, shutdownDelayTime);
        } else {
            try {
                shutdownPOS();
            } catch (JposException e) {}
        }
    }

    private void stopShutdown() {
        if (shutdownTimer != null) {
            shutdownTimer.cancel();
            shutdownTimer.purge();
        }
        if (!shutdownActive) {
            if (powerState != JposConst.JPOS_PS_ONLINE) {
                powerState = JposConst.JPOS_PS_ONLINE;
                if (powerNotify == JposConst.JPOS_PN_ENABLED) {
                    fireEvent(new StatusUpdateEvent(eventCallbacks.getEventSource(),
                            JposConst.JPOS_SUE_POWER_ONLINE));
                }
            }
        }
    }

    private void onPowerFail() {
        if (powerFailDelayTime > 0) {
            TimerTask task = new TimerTask() {

                public void run() {
                    powerFailTimer = null;
                    startShutdown();
                }
            };
            powerFailTimer = new Timer();
            powerFailTimer.schedule(task, powerFailDelayTime);
        } else {
            startShutdown();
        }
    }

    private void onPowerRestored() {
        if (powerFailTimer != null) {
            powerFailTimer.cancel();
            powerFailTimer.purge();
            powerFailTimer = null;
        }
        stopShutdown();
    }

    private int chargeState = POSPowerConst.PWR_UPS_FULL;
    private int powerSource = POSPowerConst.PWR_SOURCE_AC;

    private int shutdownDelayTime = 0;
    private Timer shutdownTimer = null;
    private boolean shutdownActive = false;

    private int powerFailDelayTime = 0;
    private Timer powerFailTimer = null;

    private double ratingVoltage = 0.0;
    private int ratingCurrent = 0;
    private double ratingBatteryVoltage = 0.0;
    private double ratingFrequency = 0.0;

    private double inputVoltage = 0.0;
    private double inputFaultVoltage = 0.0;
    private double outputVoltage = 0.0;
    private int outputCurrent = 0;
    private double outputFrequency = 0.0;
    private double batteryVoltage = 0.0;
    private double temperature = 0.0;

    private static short STATUS_UTILITY_FAIL = 0x80; // Utility Fail (Immediate)
    private static short STATUS_BATTERY_LOW = 0x40; // Battery Low
    private static short STATUS_AVR = 0x20; // AVR
    private static short STATUS_UPS_FAILED = 0x10; // UPS Failed
    private static short STATUS_UPS_INTERACTIVE = 0x08; // UPS Type is Line-Interactive (0 is On_line)
    private static short STATUS_TEST_IN_PROGRESS = 0x04; // Test in Progress
    private static short STATUS_SHUTDOWN_ACTIVE = 0x02; // Shutdown Active
    private static short STATUS_BEEPER_ON = 0x01; // Beeper On
}
