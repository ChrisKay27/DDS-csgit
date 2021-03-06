package main;

import exceptions.SimException;
import results.DBConnection;
import results.ExperimentResults;
import simulator.SimSetupParams;
import simulator.Simulation;
import simulator.enums.Topology;
import simulator.protocols.deadlockDetection.Deadlock;
import simulator.protocols.deadlockDetection.WFG.Graph;
import simulator.protocols.deadlockDetection.WFG.WFGNode;
import simulator.server.Server;
import simulator.server.network.HyperCube;
import stats.Statistics;
import ui.*;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Experiment
{
    private static int max_number_of_threads = 7;
    private static int concurrent_Execution = 0;
    public static final Object monitor = new Object();
    public static boolean monitorState = false;

    private Consumer<String> viewer ;
    private long simulationNumber ;
    
    private Long     seed ;
    private Topology topology ;
    private Integer  numPages ;
    private Integer  arrivalRate ;
    private String   deadlockDetectionProtocol ;
    private String   deadlockResolutionProtocol ;
    private String   priorityProtocol ;
    private Integer  detectionInterval ;
    private Integer  maxActiveTransferRate ;
    private Integer  agentsHistoryLength ;
    private Double   updateRate ;

    public  Experiment(
        Long     seed,
        Topology topology,
        Integer  numPages,
        Integer  arrivalRate,
        String   deadlockDetectionProtocol,
        String   deadlockResolutionProtocol,
        String   priorityProtocol,
        Integer  detectionInterval,
        Integer  maxActiveTransferRate,
        Integer  agentsHistoryLength,
        Double   updateRate
        )
        {
        this.seed                       = seed                       ;
        this.topology                   = topology                   ;
        this.numPages                   = numPages                   ;
        this.arrivalRate                = arrivalRate                ;
        this.deadlockDetectionProtocol  = deadlockDetectionProtocol  ;
        this.deadlockResolutionProtocol = deadlockResolutionProtocol ;
        this.priorityProtocol           = priorityProtocol           ;
        this.detectionInterval          = detectionInterval          ;
        this.maxActiveTransferRate      = maxActiveTransferRate      ;
        this.agentsHistoryLength        = agentsHistoryLength        ;
        this.updateRate                 = updateRate                 ;
        
        this.viewer                     = s -> {}                    ;
        this.simulationNumber           = -1L                        ;
        }
    
    public Experiment setViewer(Consumer<String> viewer)
        {
        this.viewer = viewer ;
        return this ;
        }

    public Experiment setSimulationNumber(long simulationNumber)
        {
        this.simulationNumber = simulationNumber ;
        return this ;
        }

    /**
     * doAnExperiment : run a simulation on a separate thread 
     */
    public void doAnExperiment()
        {
        ExecutorService executorService = Executors.newFixedThreadPool(max_number_of_threads);

        if (updateRate > 1 || updateRate < 0)
            throw new SimException("update rate has to be between 0 and 1, it was " + updateRate);

        //Run this simulation in a new thread
        concurrent_Execution++;
        Runnable r = () ->{
       // new Thread(() -> {
            try {
                runAnExperiment();
                concurrent_Execution--;
                if (concurrent_Execution < max_number_of_threads)
                    unlockWaiter();

            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        };//).start();
            //Run this simulation in a new thread
            //new Thread(r).start();
            executorService.submit(r);
            if (concurrent_Execution > max_number_of_threads)
                waitForThread();
        }
    
    /**
     * runAnExperiment : run a simulation on this thread, updating data
     * base and graphical display.
     */
    public void runAnExperiment() throws TimeoutException {
        Statistics stats = new Statistics();
        Simulation s = new Simulation(makeSimSetupParams(stats));

        //Setup topology.. should be in the simulation constructor..
        List<Server> servers = s.getServers();
        if (topology == Topology.HyperCube)
            HyperCube.setup(servers);

        //Run the simulation
        Object[] results = s.start();
        double PCOT = (double) results[0];
        int overheadIncurred = (int) results[1];
        int messageOverheadIncurred = (int) results[2];

        //Output results to the database
        ExperimentResults expResults = makeExperimentResults(
            PCOT, overheadIncurred, messageOverheadIncurred);
        DBConnection.insertResults(expResults);

        //Graphically display results (perhaps)
        viewer.accept(resultsAsHtml(s,stats,results)) ;
        }
    
    public String toString()
        {
        return
          seed + ":" +
          numPages + ":" +
          maxActiveTransferRate + ":" +
          8 + ":" +
          arrivalRate + ":" +
          deadlockDetectionProtocol + ":" +
          deadlockResolutionProtocol + ":" +
          priorityProtocol + ":" +
          detectionInterval + ":" +
          // no agentsHistoryLength ??
          updateRate ;
        }
    
    public SimSetupParams makeSimSetupParams(Statistics stats)
        {
        //We only display a window if logging is enabled
        Supplier<Long> getSleepTime = null;
        Consumer<Integer> updateTime = null;
        Consumer<String> log = null;
        BiConsumer<Graph<WFGNode>, Integer> wfGraphConsumer = null;
        Consumer<Deadlock> deadlockConsumer = null;
        BiConsumer<Deadlock, Integer> deadlockResListener = null;


        if (Log.isLoggingEnabled())
            {
            GUI gui = new GUI();
            gui.setTitle (this.toString());
            Output output = new Output();
            GraphVisualizer graphVisualizer = new GraphVisualizer();
            DeadlockPanel dPanel = new DeadlockPanel();
            gui.add(output, "Log");
            gui.add(graphVisualizer, "Wait for Graph");
            gui.add(dPanel, "Deadlocks");

            getSleepTime = gui::getSleepTime;
            updateTime = gui::updateTime;
            log = output::log;
            wfGraphConsumer = graphVisualizer::drawGraph;
            deadlockConsumer = dPanel::addDeadlock;
            deadlockResListener = dPanel::deadLockResolved;
            }
        else
            {
            getSleepTime        = () -> 0L;
            updateTime          = time -> {};
            log                 = logMsg -> {};
            wfGraphConsumer     = (wfgNodeWFGraph, i) -> {};
            deadlockConsumer    = deadlock -> {};
            deadlockResListener = (deadlock, f) -> {};
            }
        
        SimSetupParams params = new SimSetupParams(
            seed, numPages, maxActiveTransferRate,
            8, arrivalRate, updateRate, detectionInterval,
            deadlockDetectionProtocol,
            deadlockResolutionProtocol,
            priorityProtocol, log, stats, getSleepTime, updateTime);
        params.setWfGraphConsumer(wfGraphConsumer);
        params.setDeadlockListener(deadlockConsumer);
        params.setDeadlockResolutionListener(deadlockResListener);
        params.setAgentsHistoryLength(agentsHistoryLength);
        return params ;
        }
    
    public ExperimentResults makeExperimentResults(
        double PCOT,
        int overheadIncurred,
        int messageOverheadIncurred
        )
        {
        return new ExperimentResults(
            simulationNumber,
            PCOT, deadlockDetectionProtocol,
            deadlockResolutionProtocol,
            topology.toString(), maxActiveTransferRate,
            arrivalRate, priorityProtocol,
            numPages, detectionInterval, overheadIncurred,
            messageOverheadIncurred, updateRate, seed);
        }
    
    public String resultsAsHtml(Simulation s, Statistics stats, Object [] results)
        {
        List<Server> servers = s.getServers();
        StringBuilder sb = new StringBuilder();
        double PCOT = (double) results[0];
        int overheadIncurred = (int) results[1];
        int messageOverheadIncurred = (int) results[2];


        sb.append("<html>--------------").append("<br>");
        sb.append("<b>Parameters:</b><br>");
        sb.append("SEED:").append(seed).append("<br>NumPages:").append(numPages)
          .append("<br>Max active trans:").append(maxActiveTransferRate)
          .append("<br>servers:").append(8)
          .append("<br>arrival rate:").append(arrivalRate).append("<br>")
          .append("<br><font color=\"red\">"+deadlockDetectionProtocol+"</font>")
          .append("<br><br>").append("<font color=\"blue\">"+deadlockResolutionProtocol+"</font><br>")
          .append("<br>").append(priorityProtocol)
          .append("<br>Detection interval:").append(detectionInterval)
          .append("<br>Update Rate: ").append(updateRate).append("<br>");

        sb.append("Total Transactions: " + servers.size() * s.getSimParams().getNumTransPerServer()).append("<br><br>");

        sb.append("<b>Results:</b><br>");
        sb.append("Completed On Time: ").append(stats.getCompletedOnTime()).append("<br>");
        sb.append("Completed Late: " + stats.getCompletedLate()).append("<br>");
        sb.append("Aborted: " + stats.getNumAborted()).append("<br>");
        sb.append("Aborted and restarted: " + stats.getNumAbortedAndRestarted()).append("<br>");


        if (stats.getCompletedOnTime() + stats.getCompletedLate() + stats.getNumAborted() != servers.size() * s.getSimParams().getNumTransPerServer())
            sb.append("ERROR: Completed + Late + Aborted != Total Num of Transactions!").append("<br>");
        sb.append("Timeouts: " + stats.getTimeouts()).append("<br><br>");

        sb.append("Overhead (ticks): ").append(overheadIncurred).append("<br>");
        sb.append("Total Message Size: ").append(messageOverheadIncurred).append("<br><br>");

        sb.append("Deadlocks found: ").append(stats.getDeadlocksFound()).append("<br>");
        sb.append("Deadlocks resolved: ").append(stats.getDeadlocksResolved()).append("<br><br>");


        sb.append("<b><font color=\"red\">PCOT: " + PCOT).append("</font><br></b></html>");
        return sb.toString() ;
        }

    public static void waitForThread() {
        monitorState = true;
        while (monitorState) {
            synchronized (monitor) {
                try {
                    monitor.wait(); // wait until notified
                } catch (Exception e) {
                }
            }
        }
    }

    public static void unlockWaiter() {
        synchronized (monitor) {
            monitorState = false;
            monitor.notifyAll(); // unlock again
        }
    }
}
