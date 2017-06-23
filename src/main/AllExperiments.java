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
          .flatMap(eB -> Arrays.stream(SEEDs).map(eB::setSeed))
          .flatMap(eB -> Arrays.stream(topologies).map(eB::setTopology))
          .flatMap(eB -> Arrays.stream(numPagesList).map(eB::setNumPages))
          .flatMap(eB -> Arrays.stream(arrivalRates).map(eB::setArrivalRate))
          .flatMap(eB -> Arrays.stream(DDPs).map(eB::setDeadlockDetectionProtocol))
          .flatMap(eB -> Arrays.stream(DRPs).map(eB::setDeadlockResolutionProtocol))
          .flatMap(eB -> Arrays.stream(PPs).map(eB::setPriorityProtocol))
          .flatMap(eB -> Arrays.stream(detectIntervals).map(eB::setDetectionInterval))
          .flatMap(eB -> Arrays.stream(maxActiveTrans).map(eB::setMaxActiveTransferRate))
          .flatMap(eB -> Arrays.stream(agentsHistoryLengths).map(eB::setAgentsHistoryLength))
          .flatMap(eB -> Arrays.stream(updateRates).map(eB::setUpdateRate))
          .map(eB -> eB.build())
          .map(e -> e.setViewer(viewer()))
          .map(e -> e.setSimulationNumber(simNumber))
          .forEach(e -> e.doAnExperiment()) ;
        }

    private static <D>
    java.util.function.UnaryOperator<java.util.stream.Stream<ExperimentBuilder>>
    combineWith(D [] data,
                java.util.function.BiFunction<ExperimentBuilder,D,ExperimentBuilder>
                fred)
        {
        Function<D,UnaryOperator<ExperimentBuilder>> curried
          = d -> e -> fred.apply(e,d) ;
        java.util.function.BiFunction<Stream<ExperimentBuilder>,
          Stream<UnaryOperator<ExperimentBuilder>>,
          Stream<ExperimentBuilder>> qf =
          (str,strFs) -> str.flatMap(e -> strFs.map(f -> f.apply(e))) ;
        return (stream -> qf.apply(stream, Arrays.stream(data).map(curried))) ;
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
