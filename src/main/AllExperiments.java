package main;

import java.util.List ;
import java.util.Arrays ;
import java.util.function.Function ;
import java.util.function.BiFunction ;
import java.util.function.UnaryOperator ;
import java.util.function.Consumer ;
import java.util.stream.Stream ;

import javax.swing.*;
import simulator.enums.Topology;
import utilities.Function11 ;

class AllExperiments
{
    private static final
    Function11<Long, Topology, Integer, Integer,
        String, String, String, Integer,
        Integer, Integer, Double, Experiment> experimentConstructor =
      Experiment::new ;
    private static final
    Function<Long, Function<Topology, Function<Integer, Function<Integer,
        Function<String, Function<String, Function<String, Function<Integer,
        Function<Integer, Function<Integer,
        Function<Double, Experiment>>>>>>>>>>>
    curriedConstructor =
      experimentConstructor.curry() ;
    
    private static Consumer<String> theNullViewer = s -> {} ;
    public static Consumer<String> nullViewer()
        {
        return theNullViewer ;
        }
    private static Function<AllExperiments,Consumer<String>> theDefaultViewer = null ;
    public Consumer<String> defaultViewer()
        {
        if (theDefaultViewer==null)
            {
            /*
             * Results summarizer
             */
            JFrame resultsSummarizer = new JFrame("Results Summarizer");
            JPanel content = new JPanel();
            resultsSummarizer.setContentPane(content);
            theDefaultViewer = allExperiment -> string -> 
              {
                  JLabel label = new JLabel(string);
                  resultsSummarizer.getContentPane().add(label);
                  resultsSummarizer.setTitle(
                      "Results Summarizer - Experiment Number: "+
                      allExperiment.simNumber);
                  resultsSummarizer.pack();
                  resultsSummarizer.setVisible(true);
              } ;
            }
        return theDefaultViewer.apply(this) ;
        }
    
    private Consumer<AllExperiments> globalFrameMaker   ;
    private Consumer<String>         experimentReporter ;
    
    private final long simNumber ;
    private int simsRanSoFar = 0;
    


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
        
        this.experimentReporter   = defaultViewer()      ;
        this.globalFrameMaker     = AllExperiments::openSimExperimentNumberWindow
          /**/                                           ;
        }

    public AllExperiments setExperimentReporter(Consumer<String> x)
        {
        this.experimentReporter = (x==null) ? (s ->{}) : x ;
        return this ;
        }

    public AllExperiments setGlobalFrameMaker(Consumer<AllExperiments> x)
        {
        this.globalFrameMaker = (x==null) ? (a -> {}) : x ;
        return this ;
        }

    public void doAllExperiments()
        {
        globalFrameMaker.accept(this) ;
        /*  // was 
        openSimExperimentNumberWindow();
        */

        //This is just to tell you how many simulations will be run with the parameters chosen

        System.out.println(
            "Running " + numberOfSimulations() + " simulations. "+
            "This Test Number is " + simNumber);

        //These nested loops are to loop through all the different parameter combinations

        java.util.stream.Stream.of(curriedConstructor)
          .flatMap( applying( SEEDs                ))
          .flatMap( applying( topologies           ))
          .flatMap( applying( numPagesList         ))
          .flatMap( applying( arrivalRates         ))
          .flatMap( applying( DDPs                 ))
          .flatMap( applying( DRPs                 ))
          .flatMap( applying( PPs                  ))
          .flatMap( applying( detectIntervals      ))
          .flatMap( applying( maxActiveTrans       ))
          .flatMap( applying( agentsHistoryLengths ))
          .flatMap( applying( updateRates          ))
          .forEach( e ->
                    {
                    e.setViewer(experimentReporter) ;
                    e.setSimulationNumber(simNumber) ;
                    e.doAnExperiment() ;
                    }) ;
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


    
    private static <D,E>
    Function<Function<D,E>,Stream<E>> applying(Stream<D> stream)
        {
        return  f -> stream.map(f) ;
        }

    private static <D,E>
    Function<Function<D,E>,Stream<E>>
    applying(D [] array)
        {
        return  applying(java.util.Arrays.stream(array)) ;
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
