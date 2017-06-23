package main;

import java.util.List ;
import java.util.Arrays ;
import java.util.function.Function ;
import java.util.function.UnaryOperator ;
import java.util.function.Consumer ;
import java.util.stream.Stream ;

import javax.swing.*;
import simulator.enums.Topology;

class AllExperiments
{
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


    public void doAllExperiments()
        {
        openSimExperimentNumberWindow();


        //This is just to tell you how many simulations will be run with the parameters chosen

        System.out.println(
            "Running " + numberOfSimulations() + " simulations. "+
            "This Test Number is " + simNumber);

        //These nested loops are to loop through all the different parameter combinations

        java.util.stream.Stream.of(new ExperimentBuilder())
          .flatMap(combineWith(SEEDs,         ExperimentBuilder::setSeed))
          .flatMap(combineWith(topologies,    ExperimentBuilder::setTopology))
          .flatMap(combineWith(numPagesList,  ExperimentBuilder::setNumPages))
          .flatMap(combineWith(arrivalRates,  ExperimentBuilder::setArrivalRate))
          .flatMap(combineWith(DDPs,          ExperimentBuilder::setDeadlockDetectionProtocol))
          .flatMap(combineWith(DRPs,          ExperimentBuilder::setDeadlockResolutionProtocol))
          .flatMap(combineWith(PPs,           ExperimentBuilder::setPriorityProtocol))
          .flatMap(combineWith(detectIntervals,
                                              ExperimentBuilder::setDetectionInterval))
          .flatMap(combineWith(maxActiveTrans,ExperimentBuilder::setMaxActiveTransferRate))
          .flatMap(combineWith(agentsHistoryLengths,
                                              ExperimentBuilder::setAgentsHistoryLength))
          .flatMap(combineWith(updateRates,   ExperimentBuilder::setUpdateRate))
          .map(eB -> eB.build())
          .map(e -> e.setViewer(viewer()))
          .map(e -> e.setSimulationNumber(simNumber))
          .forEach(e -> e.doAnExperiment()) ;
        }

    private static <D>
    java.util.function.Function<ExperimentBuilder,java.util.stream.Stream<ExperimentBuilder>>
    combineWith(D [] data,
                java.util.function.BiFunction<ExperimentBuilder,D,ExperimentBuilder>
                fred)
        {
        Function<D,UnaryOperator<ExperimentBuilder>> curried
          = d -> e -> fred.apply(e,d) ;
        Function<Stream<UnaryOperator<ExperimentBuilder>>,
                 Function<ExperimentBuilder,Stream<ExperimentBuilder>>>
              qf =
          strFs -> eB -> strFs.map(f -> f.apply(eB)) ;
        return qf.apply(Arrays.stream(data).map(curried)) ;
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

    public int numberOfSimulations()
        {
        return
          SEEDs.length * topologies.length * numPagesList.length
          * arrivalRates.length * DDPs.length * DRPs.length * PPs.length
          * detectIntervals.length * maxActiveTrans.length * updateRates.length
          ;
        }

    private void openSimExperimentNumberWindow() {
        JFrame frame = new JFrame("Experiment Number");

        frame.setContentPane(new JTextField("Experiment Number is " + simNumber));
        frame.pack();
        frame.setSize(400, 100);

        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
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
