package results;

public class ExperimentResults {

    private final long expNum;
    private final double PCOT;
    private final String DDP;
    private final String DRP;
    private final String topology;
    private final int maxActiveTrans;
    private final int arrivalRate;
    private final String PP;
    private final int numPages;
    private final int detectInterval;
    private final int overheadIncurred;
    private final int messageOverheadIncurred;
    private final double updateRate;
    private final double SEED;

    public ExperimentResults(long expNum, double pcot, String ddp, String drp, String topology, int maxActiveTrans,
                             int arrivalRate, String PP, int numPages, int detectInterval, int overheadIncurred, int messageOverheadIncurred,
                             double updateRate, double SEED) {
        this.expNum = expNum;
        PCOT = pcot;
        DDP = ddp;
        DRP = drp;
        this.topology = topology;
        this.maxActiveTrans = maxActiveTrans;
        this.arrivalRate = arrivalRate;
        this.PP = PP;
        this.numPages = numPages;
        this.detectInterval = detectInterval;
        this.overheadIncurred = overheadIncurred;
        this.messageOverheadIncurred = messageOverheadIncurred;
        this.updateRate = updateRate;
        this.SEED = SEED;
    }

    public long getExpNum() {
        return expNum;
    }

    public double getPCOT() {
        return PCOT;
    }

    public String getDDP() {
        return DDP;
    }

    public String getDRP() {
        return DRP;
    }

    public String getTopology() {
        return topology;
    }

    public int getArrivalRate() {
        return arrivalRate;
    }

    public String getPP() {
        return PP;
    }

    public int getNumPages() {
        return numPages;
    }

    public int getDetectInterval() {
        return detectInterval;
    }

    public int getMaxActiveTrans() {
        return maxActiveTrans;
    }

    public int getOverheadIncurred() {
        return overheadIncurred;
    }

    public int getMessageOverheadIncurred() {
        return messageOverheadIncurred;
    }

    public double getUpdateRate() { return updateRate; }

    public double getSEED() { return SEED; }
}