package main;

import java.util.Optional ;

import javax.swing.*;
import simulator.enums.Topology;

public class ExperimentBuilder
{
    private Optional<Long>     seed ;
    private Optional<Topology> topology ;
    private Optional<Integer>  numPages ;
    private Optional<Integer>  arrivalRate ;
    private Optional<String>   deadlockDetectionProtocol ;
    private Optional<String>   deadlockResolutionProtocol ;
    private Optional<String>   priorityProtocol ;
    private Optional<Integer>  detectionInterval ;
    private Optional<Integer>  maxActiveTransferRate ;
    private Optional<Integer>  agentsHistoryLength ;
    private Optional<Double>   updateRate ;

    public  ExperimentBuilder()
        {
        seed                       = Optional/*<Long>     */.empty() ;
        topology                   = Optional/*<Topology> */.empty() ;
        numPages                   = Optional/*<Integer>  */.empty() ;
        arrivalRate                = Optional/*<Integer>  */.empty() ;
        deadlockDetectionProtocol  = Optional/*<String>   */.empty() ;
        deadlockResolutionProtocol = Optional/*<String>   */.empty() ;
        priorityProtocol           = Optional/*<String>   */.empty() ;
        detectionInterval          = Optional/*<Integer>  */.empty() ;
        maxActiveTransferRate      = Optional/*<Integer>  */.empty() ;
        agentsHistoryLength        = Optional/*<Integer>  */.empty() ;
        updateRate                 = Optional/*<Double>   */.empty() ;
        }

    public ExperimentBuilder setSeed                       (Long     seed                       ) { this.seed                       = Optional/*<Long>     */.of(seed                      ); return this; }
    public ExperimentBuilder setTopology                   (Topology topology                   ) { this.topology                   = Optional/*<Topology> */.of(topology                  ); return this; }
    public ExperimentBuilder setNumPages                   (Integer  numPages                   ) { this.numPages                   = Optional/*<Integer>  */.of(numPages                  ); return this; }
    public ExperimentBuilder setArrivalRate                (Integer  arrivalRate                ) { this.arrivalRate                = Optional/*<Integer>  */.of(arrivalRate               ); return this; }
    public ExperimentBuilder setDeadlockDetectionProtocol  (String   deadlockDetectionProtocol  ) { this.deadlockDetectionProtocol  = Optional/*<String>   */.of(deadlockDetectionProtocol ); return this; }
    public ExperimentBuilder setDeadlockResolutionProtocol (String   deadlockResolutionProtocol ) { this.deadlockResolutionProtocol = Optional/*<String>   */.of(deadlockResolutionProtocol); return this; }
    public ExperimentBuilder setPriorityProtocol           (String   priorityProtocol           ) { this.priorityProtocol           = Optional/*<String>   */.of(priorityProtocol          ); return this; }
    public ExperimentBuilder setDetectionInterval          (Integer  detectionInterval          ) { this.detectionInterval          = Optional/*<Integer>  */.of(detectionInterval         ); return this; }
    public ExperimentBuilder setMaxActiveTransferRate      (Integer  maxActiveTransferRate      ) { this.maxActiveTransferRate      = Optional/*<Integer>  */.of(maxActiveTransferRate     ); return this; }
    public ExperimentBuilder setAgentsHistoryLength        (Integer  agentsHistoryLength        ) { this.agentsHistoryLength        = Optional/*<Integer>  */.of(agentsHistoryLength       ); return this; }
    public ExperimentBuilder setUpdateRate                 (Double   updateRate                 ) { this.updateRate                 = Optional/*<Double>   */.of(updateRate                ); return this; }


    public Experiment build()
        {
        if (
            !seed                       .isPresent() ||
            !topology                   .isPresent() ||
            !numPages                   .isPresent() ||
            !arrivalRate                .isPresent() ||
            !deadlockDetectionProtocol  .isPresent() ||
            !deadlockResolutionProtocol .isPresent() ||
            !priorityProtocol           .isPresent() ||
            !detectionInterval          .isPresent() ||
            !maxActiveTransferRate      .isPresent() ||
            !agentsHistoryLength        .isPresent() ||
            !updateRate                 .isPresent() 
            )
            throw new RuntimeException("ExperimentBuilder not full initialized.") ;
        Experiment e = new Experiment(
            seed                       .get(),
            topology                   .get(),
            numPages                   .get(),
            arrivalRate                .get(),
            deadlockDetectionProtocol  .get(),
            deadlockResolutionProtocol .get(),
            priorityProtocol           .get(),
            detectionInterval          .get(),
            maxActiveTransferRate      .get(),
            agentsHistoryLength        .get(),
            updateRate                 .get()) ;
        return e ;
        }
}
