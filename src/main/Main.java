package main;

import exceptions.WTFException;
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
import java.io.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Main {

    private static int simsRanSoFar = 0;

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
        openSimExperimentNumberWindow(simNumber);

        // Read the parameters from the param.txt file

        File paramFile = new File("params.txt");

        String SEEDs = "";
        String topologyStr = "HyperCube";
        String numPagesStr = "1000";
        String arrivalRateStr = "75";
        String DDPs = "";
        String DRPs = "";
        String PPs = "";
        String DetectIntervals = "";
        String maxActiveTransStr = "";
        String agentsHistoryLengthStr = "";
        String updateRateStr = "";

        try( BufferedReader br = new BufferedReader(new FileReader(paramFile))){

            Log.setLoggingEnabled(Boolean.parseBoolean(br.readLine().split(":")[1]));
            SEEDs = br.readLine().split(":")[1];
            topologyStr = br.readLine().split(":")[1];
            numPagesStr = br.readLine().split(":")[1];
            arrivalRateStr = br.readLine().split(":")[1];
            DDPs = br.readLine().split(":")[1];
            DRPs = br.readLine().split(":")[1];
            PPs  = br.readLine().split(":")[1];
            DetectIntervals  = br.readLine().split(":")[1];
            maxActiveTransStr  = br.readLine().split(":")[1];
            agentsHistoryLengthStr = br.readLine().split(":")[1];
            updateRateStr = br.readLine().split(":")[1];

        } catch (IOException e) {
            e.printStackTrace();
        }






        //This is just to tell you how many simulations will be run with the parameters chosen

        int numberOfSims = SEEDs.split(",").length * topologyStr.split(",").length * numPagesStr.split(",").length
                * arrivalRateStr.split(",").length * DDPs.split(",").length * DRPs.split(",").length * PPs.split(",").length * DetectIntervals.split(",").length ;

        System.out.println("Running " + numberOfSims + " simulations. This Test Number is " + simNumber);





        //These nested loops are to loop through all the different parameter combinations

        for(String SEEDStr : SEEDs.split(",")) {
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

                                    for(String detectIntervalStr : DetectIntervals.split(",") ) {
                                        int detectInterval = Integer.parseInt(detectIntervalStr);

                                        for(String maxActiveTransStr_ : maxActiveTransStr.split(",") ) {
                                            int maxActiveTrans = Integer.parseInt(maxActiveTransStr_);

                                            for(String agentsHistoryLengthStr_ : agentsHistoryLengthStr.split(",") ) {
                                                int agentsHistoryLength = Integer.parseInt(agentsHistoryLengthStr_);

                                                for(String updateRateStr_ : updateRateStr.split(",") ) {
                                                    double updateRate = Double.parseDouble(updateRateStr_);

                                                    if(updateRate >1 || updateRate < 0)
                                                        throw new WTFException("update rate has to be between 0 and 1, it was " + updateRate);

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
                                                            gui.setTitle(SEED + ":" + numPages + ":" + maxActiveTrans + ":" + 8 + ":" + arrivalRate + ":" + DDP + ":" + DRP + ":" + PP);
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
                                                        SimSetupParams params = new SimSetupParams(SEED, numPages, maxActiveTrans, 8, arrivalRate, DDP, DRP, log, stats, getSleepTime, updateTime);
                                                        params.setWfGraphConsumer(wfGraphConsumer);
                                                        params.setDeadlockListener(deadlockConsumer);
                                                        params.setDeadlockResolutionListener(deadlockResListener);
                                                        params.setPP(PP);
                                                        params.setDetectInterval(detectInterval);
                                                        params.setAgentsHistoryLength(agentsHistoryLength);
                                                        params.setUpdateRate(updateRate);

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

                                                        //Output results to the database
                                                        ExperimentResults expResults = new ExperimentResults(simNumber, PCOT, DDP, DRP, topStr, maxActiveTrans,
                                                                arrivalRate, PP, numPages, detectInterval, overheadIncurred, messageOverheadIncurred, updateRate);
                                                        DBConnection.insertResults(expResults);

                                                        System.out.println("--------------");
                                                        System.out.println(SEED + ":" + numPages + ":" + maxActiveTrans + ":" + 8 + ":" + arrivalRate + ":" + DDP + ":" + DRP + ":" + PP + ":" + detectInterval + ":" + updateRate);
                                                        System.out.println("PCOT: " + PCOT);
                                                        System.out.println("Completed On Time: " + stats.getCompletedOnTime());
                                                        System.out.println("Completed Late: " + stats.getCompletedLate());
                                                        System.out.println("Aborted: " + stats.getNumAborted());
                                                        if (stats.getCompletedOnTime() + stats.getCompletedLate() + stats.getNumAborted() != servers.size() * s.getSimParams().getNumTransPerServer())
                                                            System.out.println("ERROR: Completed + Late + Aborted != Total Num of Transactions!");
                                                        System.out.println("Timeouts: " + stats.getTimeouts());

                                                        simsRanSoFar++;
                                                        if (simsRanSoFar == numberOfSims) {
//                                                      System.exit(0);
                                                        }
                                                    };

                                                    //Run this simulation in a new thread
                                                    new Thread(r).start();
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
        frame.setSize(400,100);

        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }
}