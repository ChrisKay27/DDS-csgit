package simulator;

import simulator.eventQueue.Event;
import simulator.protocols.deadlockDetection.Deadlock;
import simulator.protocols.priority.PriorityProtocol;
import simulator.server.lockManager.Range;
import simulator.server.transactionManager.TransInfo;
import stats.Statistics;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created by Mani
 */
public class SimParams {
    public static final int diskReadWriteTime = 30;
    public static final int processTime = 15;
    public static int Bandwidth = 10000;
    public final int arrivalRateMin;
    public final int arrivalRateMax;

    public final Map<Integer,Range> serverToPageRange = new HashMap<>();
    public final Consumer<Event> eventQueue;
    public final Supplier<Double> rand;
    public final Supplier<Integer> timeProvider;
    public final Supplier<Integer> IDProvider;
    public final Supplier<Integer> pageNumProvider;
    private BiConsumer<Integer,Integer> overheadIncurer;
    public final int maxActiveTrans;
    public final Consumer<String> log;
    public final Statistics stats;

    private int numTransPerServer = 500;
    public String DRP;
    public String DDP;
    public final int numberOfServers = 8;

    public boolean usesWFG = false;
    public int messageOverhead = 0;
    public final List<Integer> allServersList;
    private int overIncurred;
    private Consumer<Deadlock> deadlockListener;
    private BiConsumer<Deadlock,Integer> deadlockResolutionListener;
    public final Map<Integer,TransInfo> transInfos = new HashMap<>();
    private PriorityProtocol pp;
    private int searchInterval;


    public SimParams(Consumer<Event> eventQueue, Supplier<Double> rand, Supplier<Integer> timeProvider, Supplier<Integer> IDProvider,
                     Supplier<Integer> pageNumProvider, int maxActiveTrans, int arrivalRate, Consumer<String> log, Statistics stats, BiConsumer<Integer,Integer> incurOverhead) {
        this.eventQueue = eventQueue;
        this.rand = rand;
        this.timeProvider = timeProvider;
        this.IDProvider = IDProvider;
        this.pageNumProvider = pageNumProvider;
        this.maxActiveTrans = maxActiveTrans;
        this.log = log;
        this.stats = stats;
        overheadIncurer = incurOverhead;

        arrivalRateMin = arrivalRate - 25;
        arrivalRateMax = arrivalRate + 25;

        List<Integer> allServersList = new ArrayList<>();
        for (int i = 0; i < numberOfServers; i++)
            allServersList.add(i);
        this.allServersList = Collections.unmodifiableList(allServersList);
    }

    public List<Integer> getServersWithPage(int pageNum){
        List<Integer> serverIDs = new ArrayList<>();
        serverToPageRange.keySet().forEach(serverID -> {
            Range range = serverToPageRange.get(serverID);
            if(range.contains(pageNum))
                serverIDs.add(serverID);
        });
        return serverIDs;
    }

    public int getNumTransPerServer() {
        return numTransPerServer;
    }

    public void setNumTransPerServer(int numTransPerServer) {
        this.numTransPerServer = numTransPerServer;
    }

    public void incurOverhead(int serverID, int overhead){
        overIncurred += overhead;
        //overheadIncurer.accept(serverID,overhead);
    }

    void setDeadlockListener(Consumer<Deadlock> deadlockListener) {
        this.deadlockListener = deadlockListener;
    }

    public Consumer<Deadlock> getDeadlockListener() {
        return deadlockListener;
    }

    public PriorityProtocol getPp() {
        return pp;
    }

    public void setPp(PriorityProtocol pp) {
        this.pp = pp;
    }

    public int getDeadlockDetectInterval() {
        return searchInterval;
    }

    public void setDeadlockDetectInterval(int searchInterval) {
        this.searchInterval = searchInterval;
    }

    public BiConsumer<Deadlock, Integer> getDeadlockResolutionListener() {
        return deadlockResolutionListener;
    }

    public void setDeadlockResolutionListener(BiConsumer<Deadlock, Integer> deadlockResolutionListener) {
        this.deadlockResolutionListener = deadlockResolutionListener;
    }
}
