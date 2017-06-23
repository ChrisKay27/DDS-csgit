package main;

import exceptions.WTFException;
import stats.Statistics;
import ui.Log;
import java.io.*;

public class Main {

    public static void main(String[] args) {
        try {
            // The newInstance() call is a work around for some
            // broken Java implementations
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            // handle the error
        }

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

        AllExperiments experiments = new AllExperiments(
            SEEDs,
            topologyStr,
            numPagesStr,
            arrivalRateStr,
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
