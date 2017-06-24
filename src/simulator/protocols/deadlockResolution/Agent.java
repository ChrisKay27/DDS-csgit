package simulator.protocols.deadlockResolution;

import simulator.SimParams;
import simulator.enums.ServerProcess;
import simulator.protocols.deadlockDetection.Deadlock;
import simulator.server.Server;
import simulator.server.network.Message;
import simulator.server.transactionManager.TransInfo;
import simulator.server.transactionManager.Transaction;
import ui.Log;

import java.util.*;
import java.util.stream.Collectors;
import java.util.Comparator ;
import java.util.function.Function ;

/**
 * Create agents to handle deadlock situation. Each agent calculate dropability number based on priority, extra time, and workload.
 * The calculated number will be shared with other agents for further decision making
 * <p>
 * Created by Mani
 */
public class Agent {
    // MANI: Change!
    private static final long WORKLOAD_COEFF = 5;
    private static final long PRIORITY_COEFF = 10;
    private static final long EXTRATIME_COEFF = 1;

    private final Log log;
    private final Server server;
    private final SimParams simParams;

    private final int agentID;
    private final Deadlock deadlock;
    private final int deadlockID;

    private final TransInfo myTrans;
    private final List<TransInfo> transactionsInDeadlock;
    private final java.util.PriorityQueue<TransInfo2> transInfoAndDropabilities ;
    private long dropability;

    public Agent(int agentID, Deadlock deadlock, List<TransInfo> transactionsInDeadlock, Server server, int deadlockID) {
        this.agentID = agentID;
        this.deadlock = deadlock;
        this.deadlockID = deadlockID;

        myTrans = find(transactionsInDeadlock, agentID);
        this.transactionsInDeadlock = transactionsInDeadlock;
        this.server = server;
        simParams = server.getSimParams();
        TransInfo2.setPriorityGetter(t -> t.getPriority(transactionsInDeadlock, simParams.getPp())) ;
     log = new Log(ServerProcess.DRP, server.getID(), simParams.timeProvider, simParams.log);
    }

    public void resolve() {
        if (Log.isLoggingEnabled())
            log.log(agentID, deadlockID + ": Resolving deadlock for trans " + agentID + " for deadlock: " + transactionsInDeadlock);

        Transaction trans = server.getTM().getTransaction(agentID);

        long extraTime = trans.getDeadline() - server.getSimParams().timeProvider.get();
        long priority = trans.getPriority(transactionsInDeadlock, simParams.getPp());
        long workload = trans.getWorkload();

        dropability = (EXTRATIME_COEFF * extraTime) / ((PRIORITY_COEFF * priority) + (WORKLOAD_COEFF * workload));

//        if (simParams.getTime() + trans.getExecutionTime()> trans.getDeadline()) {
//            //System.out.println("####### HELP AGENT " + myTrans.getID() + " ##########");
//            dropability = Integer.MAX_VALUE;
//        }

//        System.out.println("*******************************");
//        System.out.print("AgentInfo: " + agentID + "; dropability = " + dropability);
//        System.out.print("; extraTime = " + extraTime);
//        System.out.print("; execution time = " + trans.getExecutionTime());
//        System.out.print("; deadline = " + trans.getDeadline());
//        System.out.print("; workload = " + workload);
//        System.out.println("; priority = " + priority);
//        System.out.println("*******************************");

        transInfoAndDropabilities.add(
            new TransInfo2(find(transactionsInDeadlock, agentID),dropability)) ;
        transactionsInDeadlock.forEach(ti -> {
            //If the transaction isn't that agent's transaction
            if (ti.transID != agentID) {
                if (Log.isLoggingEnabled())
                    log.log(agentID, deadlockID + ": Sending dropability (" + dropability + ") to other agent " + ti.transID);

                server.getNIC().sendMessage(new Message(ti.serverID, ServerProcess.DRP, "RD:" + ti.transID + ":" + agentID + ":" + deadlockID + ":" + dropability, ti.deadline));
            }
        });
    }


    public void receiveDropability(int dropability, int transID) {
        if (Log.isLoggingEnabled())
            log.log(this.agentID, deadlockID + ": Agent: receive Dropability (dropability = [" + dropability + "], transID = [" + transID + "])");

        TransInfo ti = find(transactionsInDeadlock, transID);

        transInfoAndDropabilities.add(new TransInfo2(ti,dropability)) ;
        if (transInfoAndDropabilities.size()+1<transactionsInDeadlock.size())
            {
            return ;
            }
        else
            {
            TransInfo2 smallest = transInfoAndDropabilities.element() ;
            if (smallest.getID()==agentID)
                {
                if (Log.isLoggingEnabled())
                    log.log(agentID, deadlockID + ": Agents determined that " + agentID + " should be dropped.");

                deadlock.setResolutionTime(simParams.getTime());
                simParams.getDeadlockResolutionListener().accept(deadlock, agentID);
                server.getTM().abort(agentID);
                return;
                }
            // else analysis of why not this transaction ...
            }

    }



    private TransInfo find(List<TransInfo> transactionsInDeadlock, int transID) {
        for (TransInfo ti : transactionsInDeadlock)
            if (ti.transID == transID)
                return ti;

        throw new NullPointerException("Server " + server.getID() + ": " + deadlockID + ": Could not find transaction t " + transID);
    }

    public long getAgentID() {
        return agentID;
    }

    public List<TransInfo> getTransactionsInDeadlock() {
        return transactionsInDeadlock;
    }

    public Server getServer() {
        return server;
    }
}

class TransInfo2 implements Comparable<TransInfo2>
{
    private static Comparator<TransInfo2> comparator =
          Comparator.comparingLong(TransInfo2::getDropability)
          .reversed()
          .thenComparing(Comparator.comparingInt(TransInfo2::getPriority))
          .thenComparing(Comparator.comparingInt(TransInfo2::getWorkload))
          .thenComparing(Comparator.comparingInt(TransInfo2::getID).reversed())
        ;

    private static Function<TransInfo,Integer> priorityGetter = null ;
    public static void setPriorityGetter(
        Function<TransInfo,Integer> priorityGetter)
        {
        TransInfo2.priorityGetter = priorityGetter ;
        }

    private TransInfo info1 ;
    private long dropability ;

    public TransInfo2(TransInfo info, long dropability)
        {
        this.info1 = info ;
        this.dropability = dropability ;
        }

    public int  getID()                        { return info1.getID() ; }
    public int  getDeadline()                  { return info1.getDeadline() ; }
    public int  getSlackTime()                 { return info1.getSlackTime(); }
    public int  getWorkload()                  { return info1.getworkload() ; }
    public long getDropability()               { return dropability ; }
    public int  getPriority()                  { return priorityGetter.apply(info1) ; }

    public int compareTo(TransInfo2 that)
        {
        return comparator.compare(this,that) ;
        }
}
