package jpos.applet;

import netscape.javascript.JSObject;

public class Biometrics extends jpos.Biometrics {

    public void identify(int maxFARRequested, int maxFRRRequested, boolean FARPrecedence, byte[][] referenceBIRPopulation, JSObject candidateRanking, int timeout) throws jpos.JposException {
        int len = referenceBIRPopulation.length;
        int[][] ints = new int[len][];
        for (int i = 0; i < len; i++) {
            ints[i] = new int[1];
        }
        super.identify(maxFARRequested, maxFRRRequested, FARPrecedence, referenceBIRPopulation, ints, timeout);
        for (int i = 0; i < len; i++) {
            candidateRanking.setSlot(i, ints[i][0]);
        }
    }

    public void identifyMatch(int maxFARRequested, int maxFRRRequested, boolean FARPrecedence, byte[] sampleBIR, byte[][] referenceBIRPopulation, JSObject candidateRanking) throws jpos.JposException {
        int len = referenceBIRPopulation.length;
        int[][] ints = new int[len][];
        for (int i = 0; i < len; i++) {
            ints[i] = new int[1];
        }
        super.identifyMatch(maxFARRequested, maxFRRRequested, FARPrecedence, sampleBIR, referenceBIRPopulation, ints);
        for (int i = 0; i < len; i++) {
            candidateRanking.setSlot(i, ints[i][0]);
        }
    }

    public void processPrematchData(byte[] capturedBIR, byte[] prematchDataBIR, JSObject processedBIR) throws jpos.JposException {
        byte[][] bytes = new byte[1][];
        int len = Math.max(capturedBIR.length, prematchDataBIR.length);
        bytes[0] = new byte[len];
        super.processPrematchData(capturedBIR, prematchDataBIR, bytes);
        processedBIR.setSlot(0, bytes[0]);
    }

    public void verify(int maxFARRequested, int maxFRRRequested, boolean FARPrecedence, byte[] referenceBIR, JSObject adaptedBIR, JSObject result, JSObject FARAchieved, JSObject FRRAchieved, JSObject payload, int timeout) throws jpos.JposException {
        int len = referenceBIR.length;
        byte[][] bytes = new byte[1][];
        bytes[0] = new byte[len];
        int[] ints = new int[1];
        int[] ints2 = new int[1];
        boolean[] blns = new boolean[1];
        byte[][] bytes2 = new byte[1][];
        bytes2[0] = new byte[len];
        super.verify(maxFARRequested, maxFRRRequested, FARPrecedence, referenceBIR, bytes, blns, ints, ints2, bytes2, timeout);
        adaptedBIR.setSlot(0, bytes[0]);
        result.setSlot(0, blns[0]);
        FARAchieved.setSlot(0, ints[0]);
        FRRAchieved.setSlot(0, ints2[0]);
        payload.setSlot(0, bytes2[0]);
    }

    public void verifyMatch(int maxFARRequested, int maxFRRRequested, boolean FARPrecedence, byte[] sampleBIR, byte[] referenceBIR, JSObject adaptedBIR, JSObject result, JSObject FARAchieved, JSObject FRRAchieved, JSObject payload) throws jpos.JposException {
        int len = referenceBIR.length;
        byte[][] bytes = new byte[1][];
        bytes[0] = new byte[len];
        int[] ints = new int[1];
        int[] ints2 = new int[1];
        boolean[] blns = new boolean[1];
        byte[][] bytes2 = new byte[1][];
        bytes2[0] = new byte[len];
        super.verifyMatch(maxFARRequested, maxFRRRequested, FARPrecedence, sampleBIR, referenceBIR, bytes, blns, ints, ints2, bytes2);
        adaptedBIR.setSlot(0, bytes[0]);
        result.setSlot(0, blns[0]);
        FARAchieved.setSlot(0, ints[0]);
        FRRAchieved.setSlot(0, ints2[0]);
        payload.setSlot(0, bytes2[0]);
    }

    public void compareFirmwareVersion(String firmwareFileName, JSObject result) throws jpos.JposException {
        int[] ints = new int[1];
        super.compareFirmwareVersion(firmwareFileName, ints);
        result.setSlot(0, ints[0]);
    }

    public void retrieveStatistics(JSObject statisticsBuffer) throws jpos.JposException {
        String[] strings = new String[1];
        super.retrieveStatistics(strings);
        statisticsBuffer.setSlot(0, strings[0]);
    }

    public void addDataListener(JSObject event) {
        super.addDataListener(DataListener.get(event));
    }

    public void removeDataListener(JSObject event) {
        super.removeDataListener(DataListener.get(event));
    }

    public void addDirectIOListener(JSObject event) {
        super.addDirectIOListener(DirectIOListener.get(event));
    }

    public void removeDirectIOListener(JSObject event) {
        super.removeDirectIOListener(DirectIOListener.get(event));
    }

    public void addStatusUpdateListener(JSObject event) {
        super.addStatusUpdateListener(StatusUpdateListener.get(event));
    }

    public void removeStatusUpdateListener(JSObject event) {
        super.removeStatusUpdateListener(StatusUpdateListener.get(event));
    }

    public void addErrorListener(JSObject event) {
        super.addErrorListener(ErrorListener.get(event));
    }

    public void removeErrorListener(JSObject event) {
        super.removeErrorListener(ErrorListener.get(event));
    }
}
