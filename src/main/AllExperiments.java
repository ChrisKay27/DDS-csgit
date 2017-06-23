package main;

import java.util.List ;
import java.util.Arrays ;
import java.util.function.Function ;
import java.util.function.Consumer ;

import javax.swing.*;
import simulator.enums.Topology;

class AllExperiments
{
    private static class Stage01 {} ;
    private static class Stage02 {} ;
    private static class Stage03 {} ;
    private static class Stage04 {} ;
    private static class Stage05 {} ;
    private static class Stage06 {} ;
    private static class Stage07 {} ;
    private static class Stage08 {} ;
    private static class Stage09 {} ;
    private static class Stage10 {} ;
    private static class Stage11 {} ;
    
    private final long simNumber ;
    private int simsRanSoFar = 0;
    private JFrame resultsSummarizer ;


    private Long [] SEEDs ;
    private Topology [] topologies ;
    private Integer [] numPagesList ;
    private Integer [] arrivalRates ;
    private String [] DDPs ;
    private String [] DRPs ;
    private String [] PPs ;
    private Integer [] detectIntervals ;
    private Integer [] maxActiveTrans ;
    private Integer [] agentsHistoryLengths ;
    private Double [] updateRates ;

    /**
     * convience constructor : takes strings read from input file.
     *
     */
    public AllExperiments(
            String SEEDs,
            String topologyStr,
            String numPagesStr,
            String arrivalRateStr,
            String DDPs,
            String DRPs,
            String PPs,
            String DetectIntervals,
            String maxActiveTransStr,
            String agentsHistoryLengthStr,
            String updateRateStr
            )
        {
        this(
            SEEDs.split(","),
            topologyStr.split(","),
            numPagesStr.split(","),
            arrivalRateStr.split(","),
            DDPs.split(","),
            DRPs.split(","),
            PPs.split(","),
            DetectIntervals.split(","),
            maxActiveTransStr.split(","),
            agentsHistoryLengthStr.split(","),
            updateRateStr.split(","));
        }
    
    /**
     * convience constructor : takes arrays of strings read from input file.
     *
     */
    public AllExperiments
    (
        String [] SEEDs,
        String [] topologyStr,
        String [] numPagesStr,
        String [] arrivalRateStr,
        String [] DDPs,
        String [] DRPs,
        String [] PPs,
        String [] DetectIntervals,
        String [] maxActiveTransStr,
        String [] agentsHistoryLengthStr,
        String [] updateRateStr
        )
        {
        this(
            map(SEEDs, Long::parseLong),
            map(topologyStr, Topology::fromString),
            map(numPagesStr   , Integer::parseInt),
            map(arrivalRateStr, Integer::parseInt),
            DDPs                         ,
            DRPs                         ,
            PPs                          ,
            map(DetectIntervals        , Integer::parseInt),
            map(maxActiveTransStr      , Integer::parseInt),
            map(agentsHistoryLengthStr , Integer::parseInt),
            map(updateRateStr, Double::parseDouble)
            ) ;
        }
    
    public AllExperiments(
        Long     [] SEEDs,
        Topology [] topologies,
        Integer  [] numPages,
        Integer  [] arrivalRates,
        String   [] DDPs,
        String   [] DRPs,
        String   [] PPs,
        Integer  [] detectIntervals,
        Integer  [] maxActiveTrans,
        Integer  [] agentsHistoryLengths,
        Double   [] updateRates
                               )
        {
        this.simsRanSoFar           = 0; // TODO ?? unused? ??
        // This is the simulation number associated with ALL the variations in the parameter file. It is used to compare results of an experiment*
        // *an experiment is a combination of parameters
        this.simNumber = System.currentTimeMillis();
        /*
         * Results summarizer
         */
        this.resultsSummarizer      = new JFrame("Results Summarizer");
        {
            JPanel content = new JPanel();
            resultsSummarizer.setContentPane(content);
        }

        this.SEEDs                = SEEDs                ;
        this.topologies           = topologies           ;
        this.numPagesList         = numPagesList         ;
        this.arrivalRates         = arrivalRates         ;
        this.DDPs                 = DDPs                 ;
        this.DRPs                 = DRPs                 ;
        this.PPs                  = PPs                  ;
        this.detectIntervals      = detectIntervals      ;
        this.maxActiveTrans       = maxActiveTrans       ;
        this.agentsHistoryLengths = agentsHistoryLengths ;
        this.updateRates          = updateRates          ;
        }


    private void openSimExperimentNumberWindow() {
        JFrame frame = new JFrame("Experiment Number");

        frame.setContentPane(new JTextField("Experiment Number is " + simNumber));
        frame.pack();
        frame.setSize(400, 100);

        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }

    public int numberOfSimulations()
        {
        return
          SEEDs.length * topologies.length * numPagesList.length
          * arrivalRates.length * DDPs.length * DRPs.length * PPs.length
          * detectIntervals.length * maxActiveTrans.length * updateRates.length
          ;
        }

    public void doAllExperiments()
        {
        openSimExperimentNumberWindow();


        //This is just to tell you how many simulations will be run with the parameters chosen

        System.out.println(
            "Running " + numberOfSimulations() + " simulations. "+
            "This Test Number is " + simNumber);

        //These nested loops are to loop through all the different parameter combinations

        ExperimentBuilder expBuilder = new ExperimentBuilder() ;
        doStage01(expBuilder) ;
        }

    public void doStage01(ExperimentBuilder expBuilder)
        {
        Arrays.stream(SEEDs).map(expBuilder::setSeed)
          .flatMap(eB -> Arrays.stream(topologies).map(t -> eB.setTopology(t)))
          .flatMap(eB -> Arrays.stream(numPagesList).map(n -> eB.setNumPages(n)))
          .flatMap(eB -> Arrays.stream(arrivalRates).map(n -> eB.setArrivalRate(n)))
          .flatMap(eB -> Arrays.stream(DDPs).map(dp -> eB.setDeadlockDetectionProtocol(dp)))
          .flatMap(eB -> Arrays.stream(DRPs).map(drp -> eB.setDeadlockResolutionProtocol(drp)))
          .flatMap(eB -> Arrays.stream(PPs).map(pp -> eB.setPriorityProtocol(pp)))
          .flatMap(eB -> Arrays.stream(detectIntervals).map(di -> eB.setDetectionInterval(di)))
          .flatMap(eB -> Arrays.stream(maxActiveTrans).map(ma -> eB.setMaxActiveTransferRate(ma)))
          .flatMap(eB -> Arrays.stream(agentsHistoryLengths).map(ahl -> eB.setAgentsHistoryLength(ahl)))
          .forEach(this::doStage11) ;
        /*
        for (long SEED : SEEDs)
            {
            expBuilder.setSeed(SEED) ;
            doStage02(expBuilder) ;
            }
        */
        }

    public void doStage02(ExperimentBuilder expBuilder)
        {
        Arrays.stream(topologies).map(expBuilder::setTopology).
          forEach(this::doStage03) ;
        /*
        for (Topology topology : topologies)
            {
            expBuilder.setTopology(topology) ;
            doStage03(expBuilder) ;
            }
        */
        }
    

    public void doStage03(ExperimentBuilder expBuilder)
        {
        Arrays.stream(numPagesList).map(expBuilder::setNumPages).
          forEach(this::doStage04) ;
        /*
        for (int numPages : numPagesList)
            {
            expBuilder.setNumPages(numPages) ;
            doStage04(expBuilder) ;
            }
        */
        }

    public void doStage04(ExperimentBuilder expBuilder)
        {
        Arrays.stream(arrivalRates).map(expBuilder::setArrivalRate).
          forEach(this::doStage05) ;
        for (int arrivalRate : arrivalRates)
            {
            expBuilder.setArrivalRate(arrivalRate) ;
            doStage05(expBuilder) ;
            }
        }

    public void doStage05(ExperimentBuilder expBuilder)
        {
        Arrays.stream(DDPs).map(expBuilder::setDeadlockDetectionProtocol).forEach(this::doStage06) ;
        /*
        for (String DDP : DDPs)
            {
            expBuilder.setDeadlockDetectionProtocol(DDP) ;
            doStage06(expBuilder) ;
            }
        */
        }

    public void doStage06(ExperimentBuilder expBuilder)
        {
        Arrays.stream(DRPs).map(expBuilder::setDeadlockResolutionProtocol).
          forEach(this::doStage07) ;
        /*
        for (String DRP : DRPs)
            {
            expBuilder.setDeadlockResolutionProtocol(DRP) ;
            doStage07(expBuilder) ;
            }
        */
        }

    public void doStage07(ExperimentBuilder expBuilder)
        {
        Arrays.stream(PPs).map(expBuilder::setPriorityProtocol).forEach(this::doStage08) ;
        /*
        for (String PP : PPs)
            {
            expBuilder.setPriorityProtocol(PP) ;
            doStage08(expBuilder) ;
            }
        */
        }

    public void doStage08(ExperimentBuilder expBuilder)
        {
        Arrays.stream(detectIntervals).map(expBuilder::setDetectionInterval).forEach(this::doStage09) ;
        /*
        for (int detectInterval : detectIntervals)
            {
            expBuilder.setDetectionInterval(detectInterval) ;
            doStage09(expBuilder) ;
            }
        */
        }

    public void doStage09(ExperimentBuilder expBuilder)
        {
        Arrays.stream(maxActiveTrans).map(expBuilder::setMaxActiveTransferRate).forEach(this::doStage10) ;
        /*
        for (int maxActiveTrans_ : maxActiveTrans)
            {
            expBuilder.setMaxActiveTransferRate(maxActiveTrans_) ;
            doStage10(expBuilder) ;
            }
        */
        }

    public void doStage10(ExperimentBuilder expBuilder)
        {
        Arrays.stream(agentsHistoryLengths).map(expBuilder::setAgentsHistoryLength).forEach(this::doStage11) ;
        /*
        for (int agentsHistoryLength : agentsHistoryLengths)
            {
            expBuilder.setAgentsHistoryLength(agentsHistoryLength) ;
            doStage11(expBuilder) ;
            }
        */
        }

    public void doStage11(ExperimentBuilder expBuilder)
        {
        for (double updateRate : updateRates)
            {
            expBuilder.setUpdateRate (updateRate);
            Experiment e = expBuilder.build() ;
            e.setViewer (viewer()) ;
            e.setSimulationNumber(simNumber) ;
            e.doAnExperiment();
            }
        }

    private Consumer<String> viewer()
        {
        return (string) ->
            {
            JLabel label = new JLabel(string);
            resultsSummarizer.getContentPane().add(label);
            resultsSummarizer.setTitle(
                "Results Summarizer - Experiment Number: "+ simNumber);
            resultsSummarizer.pack();
            resultsSummarizer.setVisible(true);
            } ;
        }

    private static <E,F> E [] map(F [] in, Function<F,E> converter)
        {
        Object [] out = new Object [in.length] ;
        for (int i=0,ell=in.length; i<ell;++i)
            {
            out[i] = converter.apply(in[i]) ;
            }
        return (E []) out ;
        }
}
