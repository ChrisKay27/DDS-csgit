package main;

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

import java.io.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;


/**
 * Created by Chris on 6/8/2016.
 */
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



        File paramFile = new File("params.txt");

        String SEEDs = "";
        String topologyStr = "HyperCube";
        String numPagesStr = "1000";
        String arrivalRateStr = "75";
        String DDPs = "";
        String DRPs = "";
        String PPs = "";
        String DetectIntervals = "";

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

        } catch (IOException e) {
            e.printStackTrace();
        }

        ExecutorService executorService = Executors.newFixedThreadPool(1);

        long simNumber = System.currentTimeMillis();


        int numberOfSims = SEEDs.split(",").length * topologyStr.split(",").length * numPagesStr.split(",").length
                * arrivalRateStr.split(",").length * DDPs.split(",").length * DRPs.split(",").length * PPs.split(",").length * DetectIntervals.split(",").length ;

        System.out.println("Running " + numberOfSims + " simulations. This Test Number is " + simNumber);

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
                                                gui.setTitle(SEED + ":" + numPages + ":" + 30 + ":" + 8 + ":" + arrivalRate + ":" + DDP + ":" + DRP + ":" + PP);
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
                                                deadlockResListener = (deadlock,f) -> {
                                                };
                                            }


                                            //Setup params object
                                            SimSetupParams params = new SimSetupParams(SEED, numPages, 30, 8, arrivalRate, DDP, DRP, log, stats, getSleepTime, updateTime);
                                            params.setWfGraphConsumer(wfGraphConsumer);
                                            params.setDeadlockListener(deadlockConsumer);

                                            params.setDeadlockResolutionListener(deadlockResListener);
                                            params.setPP(PP);
                                            params.setDetectInterval(detectInterval);

                                            Simulation s = new Simulation(params);

                                            //Setup topology.. should be in the simulation constructor..
                                            List<Server> servers = s.getServers();
                                            if (topology == Topology.HyperCube)
                                                HyperCube.setup(servers);


                                            //Run the simulation
                                            double PCOT = s.start();

                                            ExperimentResults expResults = new ExperimentResults(simNumber, PCOT, DDP, DRP, topStr, arrivalRate, PP, numPages, detectInterval);
                                            DBConnection.insertResults(expResults);

                                            System.out.println("--------------");
                                            System.out.println(SEED + ":" + numPages + ":" + 30 + ":" + 8 + ":" + arrivalRate + ":" + DDP + ":" + DRP + ":" + PP+":"+detectInterval);
                                            System.out.println("PCOT: " + PCOT);
                                            System.out.println("Completed On Time: " + stats.getCompletedOnTime());
                                            System.out.println("Completed Late: " + stats.getCompletedLate());
                                            System.out.println("Aborted: " + stats.getNumAborted());
                                            if (stats.getCompletedOnTime() + stats.getCompletedLate() + stats.getNumAborted() != servers.size() * s.getSimParams().getNumTransPerServer())
                                                System.out.println("ERROR: Completed + Late + Aborted != Total Num of Transactions!");
                                            System.out.println("Timeouts: " + stats.getTimeouts());

                                            simsRanSoFar++;
                                            if (simsRanSoFar == numberOfSims) {
//                                            System.exit(0);
                                            }
                                        };
                                        new Thread(r).start();
//                                    executorService.submit(r);
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }
        executorService.shutdown();
    }
}
