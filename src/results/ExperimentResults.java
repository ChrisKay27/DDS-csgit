package results;

public class ExperimentResults {

    private final long expNum;
    private final double PCOT;
    private final String DDP;
    private final String DRP;
    private final String topology;
    private final int arrivalRate;
    private final String PP;
    private final int numPages;
    private final int detectInterval;

    public ExperimentResults(long expNum, double pcot, String ddp, String drp, String topology, int arrivalRate, String PP, int numPages, int detectInterval) {
        this.expNum = expNum;
        PCOT = pcot;
        DDP = ddp;
        DRP = drp;
        this.topology = topology;
        this.arrivalRate = arrivalRate;
        this.PP = PP;
        this.numPages = numPages;
        this.detectInterval = detectInterval;
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
}
