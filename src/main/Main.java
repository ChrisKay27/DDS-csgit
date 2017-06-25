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

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Main {
    private static int max_number_of_threads = 8;
    private static int simsRanSoFar = 0;
    private static int concurrent_Execution = 0;

    public static void main(String[] args) {
        try {
            // The newInstance() call is a work around for some
            // broken Java implementations
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            // handle the error
        }

        // This is the simulation number associated with ALL the variations in the parameter file. It is used to compare results of an experiment*
        // *an experiment is a combination of parameters
        long simNumber = System.currentTimeMillis();
        // FIXME Uncomment
        //openSimExperimentNumberWindow(simNumber);

        // Read the parameters from the param.txt file

        File paramFile = new File("params.txt");

        String SEEDs = "";
        String topologyStr = "";
        String numPagesStr = "";
        String arrivalRateStr = "";
        String DDPs = "";
        String DRPs = "";
        String PPs = "";
        String DetectIntervals = "";
        String maxActiveTransStr = "";
        String agentsHistoryLengthStr = "";
        String updateRateStr = "";

        try (BufferedReader br = new BufferedReader(new FileReader(paramFile))) {

            String loggingEnabledLine = br.readLine();
            Log.setLoggingEnabled(Boolean.parseBoolean(loggingEnabledLine.split("//")[0].split(":")[1].trim()));
            SEEDs = br.readLine().split("//")[0].split(":")[1].trim();
            topologyStr = br.readLine().split("//")[0].split(":")[1].trim();
            numPagesStr = br.readLine().split("//")[0].split(":")[1].trim();
            arrivalRateStr = br.readLine().split("//")[0].split(":")[1].trim();
            DDPs = br.readLine().split("//")[0].split(":")[1].trim();
            DRPs = br.readLine().split("//")[0].split(":")[1].trim();
            PPs = br.readLine().split("//")[0].split(":")[1].trim();
            DetectIntervals = br.readLine().split("//")[0].split(":")[1].trim();
            maxActiveTransStr = br.readLine().split("//")[0].split(":")[1].trim();
            agentsHistoryLengthStr = br.readLine().split("//")[0].split(":")[1].trim();
            updateRateStr = br.readLine().split("//")[0].split(":")[1].trim();

        } catch (IOException e) {
            e.printStackTrace();
        }

        if(SEEDs.equals(""))
            SEEDs = simNumber + "";

        //This is just to tell you how many simulations will be run with the parameters chosen

        int numberOfSims = SEEDs.split(",").length * topologyStr.split(",").length * numPagesStr.split(",").length
                * arrivalRateStr.split(",").length * DDPs.split(",").length * DRPs.split(",").length * PPs.split(",").length
                * DetectIntervals.split(",").length * maxActiveTransStr.split(",").length * updateRateStr.split(",").length;

        System.out.println("Running " + numberOfSims + " simulations. This Test Number is " + simNumber);


        /*
         * FIXME Uncomment
         * Results summarizer
         */
//        JFrame resultsSummerizer = new JFrame("Results Summarizer");
//        {
//            JPanel content = new JPanel();
//            resultsSummerizer.setContentPane(content);
//        }


        ExecutorService executorService = Executors.newFixedThreadPool(max_number_of_threads);

        //These nested loops are to loop through all the different parameter combinations
        for (String SEEDStr : SEEDs.split(",")) {
            long SEED = Long.parseLong(SEEDStr);

            for (String topStr : topologyStr.split(",")) {
                Topology topology = Topology.fromString(topStr);

                for (String nPagesStr : numPagesStr.split(",")) {
                    int numPages = Integer.parseInt(nPagesStr);

                    for (String arrRateStr : arrivalRateStr.split(",")) {
                        int arrivalRate = Integer.parseInt(arrRateStr);

                        for (String DDP : DDPs.split(",")) {

                            for (String DRP : DRPs.split(",")) {

                                for (String PP : PPs.split(",")) {

                                    for (String detectIntervalStr : DetectIntervals.split(",")) {
                                        int detectInterval = Integer.parseInt(detectIntervalStr);

                                        for (String maxActiveTransStr_ : maxActiveTransStr.split(",")) {
                                            int maxActiveTrans = Integer.parseInt(maxActiveTransStr_);

                                            for (String agentsHistoryLengthStr_ : agentsHistoryLengthStr.split(",")) {
                                                int agentsHistoryLength = Integer.parseInt(agentsHistoryLengthStr_);

                                                for (String updateRateStr_ : updateRateStr.split(",")) {
                                                    double updateRate = Double.parseDouble(updateRateStr_);

                                                    if (updateRate > 1 || updateRate < 0)
                                                        throw new SimException("update rate has to be between 0 and 1, it was " + updateRate);

                                                    concurrent_Execution++;
                                                    Runnable r = () -> {
                                                        Statistics stats = new Statistics();


                                                        //We only display a window if logging is enabled
                                                        Supplier<Long> getSleepTime = null;
                                                        Consumer<Integer> updateTime = null;
                                                        Consumer<String> log = null;
                                                        BiConsumer<Graph<WFGNode>, Integer> wfGraphConsumer = null;
                                                        Consumer<Deadlock> deadlockConsumer = null;
                                                        BiConsumer<Deadlock, Integer> deadlockResListener = null;


                                                        if (Log.isLoggingEnabled()) {
                                                            GUI gui = new GUI();
                                                            gui.setTitle(SEED + ":" + numPages + ":" + maxActiveTrans + ":" + 8 + ":" + arrivalRate + ":" + DDP + ":" + DRP + ":" + PP + ":" + detectInterval + ":" + updateRate);
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
                                                        } else {
                                                            getSleepTime = () -> 0L;
                                                            updateTime = time -> {
                                                            };
                                                            log = logMsg -> {
                                                            };
                                                            wfGraphConsumer = (wfgNodeWFGraph, i) -> {
                                                            };
                                                            deadlockConsumer = deadlock -> {
                                                            };
                                                            deadlockResListener = (deadlock, f) -> {
                                                            };
                                                        }

                                                        //Setup params object
                                                        SimSetupParams params = new SimSetupParams(SEED, numPages, maxActiveTrans, 8, arrivalRate, updateRate, detectInterval, DDP, DRP, PP, log, stats, getSleepTime, updateTime);
                                                        params.setWfGraphConsumer(wfGraphConsumer);
                                                        params.setDeadlockListener(deadlockConsumer);
                                                        params.setDeadlockResolutionListener(deadlockResListener);
                                                        params.setAgentsHistoryLength(agentsHistoryLength);

                                                        Simulation s = new Simulation(params);

                                                        //Setup topology.. should be in the simulation constructor..
                                                        List<Server> servers = s.getServers();
                                                        if (topology == Topology.HyperCube)
                                                            HyperCube.setup(servers);

                                                        //Run the simulation
                                                        Object[] results = s.start();

                                                        double PCOT = (double) results[0];
                                                        int overheadIncurred = (int) results[1];
                                                        int messageOverheadIncurred = (int) results[2];

                                                        concurrent_Execution--;
                                                        if(concurrent_Execution < 2)
                                                            unlockWaiter();

                                                        //Output results to the database
                                                        ExperimentResults expResults = new ExperimentResults(simNumber, PCOT, DDP, DRP, topStr, maxActiveTrans,
                                                                arrivalRate, PP, numPages, detectInterval, overheadIncurred, messageOverheadIncurred, updateRate, SEED);
                                                        DBConnection.insertResults(expResults);


                                                        StringBuilder sb = new StringBuilder();

                                                        sb.append("<html>--------------").append("<br>");
                                                        sb.append("<b>Parameters:</b><br>");
                                                        sb.append("SEED:").append(SEED).append("<br>NumPages:").append(numPages)
                                                                .append("<br>Max active trans:").append(maxActiveTrans).append("<br>servers:")
                                                                .append(8).append("<br>arrival rate:").append(arrivalRate).append("<br>")
                                                                .append("<br><font color=\"red\">"+DDP+"</font>").append("<br><br>").append("<font color=\"blue\">"+DRP+"</font><br>")
                                                                .append("<br>").append(PP).append("<br>Detection interval:").append(detectInterval)
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
                                                        sb.append("Transactions aborted by DRP ").append(stats.getDeadlocksResolved()).append("<br><br>");


                                                        sb.append("<b><font color=\"red\">PCOT: " + PCOT).append("</font><br></b></html>");


                                                        // FIXME Uncomment
//                                                        JLabel label = new JLabel(sb.toString());
//                                                        resultsSummerizer.getContentPane().add(label);
//                                                        resultsSummerizer.setTitle("Results Summarizer - Experiment Number: "+ simNumber);
//                                                        resultsSummerizer.pack();
//                                                        resultsSummerizer.setVisible(true);

                                                        simsRanSoFar++;
                                                        System.out.println("Simulation ran so far: "+ simsRanSoFar + " out of " + numberOfSims);
                                                        if (simsRanSoFar == numberOfSims) {
                                                            System.exit(0);
                                                        }
                                                    };

                                                    //Run this simulation in a new thread
                                                    //new Thread(r).start();
                                                    executorService.submit(r);
                                                    if (concurrent_Execution > max_number_of_threads)
                                                        waitForThread();
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void openSimExperimentNumberWindow(long simNumber) {
        JFrame frame = new JFrame("Experiment Number");

        frame.setContentPane(new JTextField("Experiment Number is " + simNumber));
        frame.pack();
        frame.setSize(400, 100);

        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }
    public static final Object monitor = new Object();
    public static boolean monitorState = false;
    public static void waitForThread() {
        monitorState = true;
        while (monitorState) {
            synchronized (monitor) {
                try {
                    monitor.wait(); // wait until notified
                } catch (Exception e) {}
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