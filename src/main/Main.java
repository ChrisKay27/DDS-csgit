package main;

import ui.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Main {


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

        // Read the parameters from the param.txt file

        File paramFile = new File("params.txt");

        String SEEDs = "";
        String topologyStr = "";
        String numPagesStr = "";
        String arrivalIntervalStr = "";
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
            arrivalIntervalStr = br.readLine().split("//")[0].split(":")[1].trim();
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

        if (SEEDs.equals(""))
            SEEDs = simNumber + "";

        AllExperiments experiments = new AllExperiments(
            SEEDs,
            topologyStr,
            numPagesStr,
            arrivalIntervalStr,
            DDPs,
            DRPs,
            PPs,
            DetectIntervals,
            maxActiveTransStr,
            agentsHistoryLengthStr,
            updateRateStr) ;
        // experiments.setGlobalFrameMaker(null) ;
        experiments.setExperimentReporter(experiments.defaultViewer()) ;
        // experiments.setExperimentReporter(null)) ;
        experiments.doAllExperiments() ;
    }
}