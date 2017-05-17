package simulator.protocols.deadlockResolution;

import exceptions.WTFException;
import javafx.util.Pair;
import simulator.SimParams;
import simulator.enums.ServerProcess;
import simulator.protocols.deadlockDetection.Deadlock;
import simulator.server.Server;
import simulator.server.network.Message;
import simulator.server.transactionManager.TransInfo;
import simulator.server.transactionManager.Transaction;
import ui.Log;

import java.util.*;

/**
 * Select a transaction to kill based on agent decision making
 *
 * @author Mani
 */
public class AgentDeadlockResolutionProtocol implements DeadlockResolutionProtocol {
    private final Server server;
    private final SimParams simParams;
    private final Log log;

    private static final long WORKLOAD_COEFF = 5;
    private static final long PRIORITY_COEFF = 10;
    private static final long EXTRATIME_COEFF = 1;

    public AgentDeadlockResolutionProtocol(Server server) {
        this.server = server;
        this.simParams = server.getSimParams();
        log = new Log(ServerProcess.DRP, server.getID(), simParams.timeProvider, simParams.log);
    }

    /**
     * Picks the lowest priority transaction and aborts it
     */
    @Override
    public void resolveDeadlocks(Deadlock deadlock) {
        int lowestDropability = Integer.MAX_VALUE;
        List<TransInfo> transactionsInDeadlock = deadlock.getTransactionsInvolved();

        TransInfo lowest = null;
        long extraTime =0;
        long priority = 0;
        long  workload = 0;
        long  dropability = 0;
        for (TransInfo ti : transactionsInDeadlock) {




            extraTime = ti.getDeadline() - server.getSimParams().timeProvider.get();
            priority = ti.getPriority(transactionsInDeadlock, simParams.getPp());
            workload = ti.getworkload();

            dropability = (EXTRATIME_COEFF * extraTime) / ((PRIORITY_COEFF * priority) + (WORKLOAD_COEFF * workload));
            dropability = workload;

            if (dropability < lowestDropability) {
                lowest = ti;
                lowestDropability = lowest.getPriority(transactionsInDeadlock, simParams.getPp());
            }
        }

        if(simParams.agentBased){
            List<TransInfo> lowestDropabilitytrans = new ArrayList<>();

            for (TransInfo ti : transactionsInDeadlock) {

                extraTime = ti.getDeadline() - server.getSimParams().timeProvider.get();
                priority = ti.getPriority(transactionsInDeadlock, simParams.getPp());
                workload = ti.getworkload();

                dropability = (EXTRATIME_COEFF * extraTime) / ((PRIORITY_COEFF * priority) + (WORKLOAD_COEFF * workload));
                dropability = priority;

                if (dropability == lowestDropability)
                    lowestDropabilitytrans.add(ti);
            }

            if (lowestDropabilitytrans.isEmpty())
                return;

            TransInfo LowestTInfo = lowestDropabilitytrans.get((int) (simParams.rand.get() * lowestDropabilitytrans.size()));

            server.getNIC().sendMessage(new Message(LowestTInfo.serverID, ServerProcess.DRP, "A:" + LowestTInfo.getID() + ":" + server.getID(), deadlock, LowestTInfo.getDeadline()));
            return;
        }

        List<TransInfo> transAtThisServer = new ArrayList<>();

        for (TransInfo ti : transactionsInDeadlock)
            if (ti.getPriority(transactionsInDeadlock, simParams.getPp()) == lowestDropability && ti.serverID == server.getID())
                transAtThisServer.add(ti);

        if (transAtThisServer.isEmpty())
            return;

        TransInfo tInfo = transAtThisServer.get((int) (simParams.rand.get() * transAtThisServer.size()));

        deadlock.setResolutionTime(simParams.getTime());
        simParams.getDeadlockResolutionListener().accept(deadlock, tInfo.transID);
        server.getTM().abort(tInfo.getID());
    }

    @Override
    public void resolveMultiple(List<Deadlock> l) {
        l.forEach(this::resolveDeadlocks);
    }

    @Override
    public void receiveMessage(Message msg) {
        String message = msg.getContents();
        String[] components = message.split(":");
        int transID = Integer.parseInt(components[1]);
        Deadlock deadlock = (Deadlock) msg.getObject();

        if (Log.isLoggingEnabled())
            log.log("AgentDeadlockResolution - Server " + components[2] + " told to abort transaction " + transID);

        deadlock.setResolutionTime(simParams.getTime());
        simParams.getDeadlockResolutionListener().accept(deadlock, transID);
        server.getTM().abort(transID);
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
//    private final Log log;
//    private final Server server;
//    private final Map<Pair<Integer, Integer>, Agent> agentList = new HashMap<>();
//    private final Map<Integer, Object[]> delayedReceiveDropability = new HashMap<>();
//
//    public int numberOfResolvedDeadlocks = 0;
//    private final List<List<TransInfo>> beingResolvedDeadlocks = new ArrayList<>();
//
//    public AgentDeadlockResolutionProtocol(Server server) {
//        this.server = server;
//        SimParams simParams = server.getSimParams();
//        log = new Log(ServerProcess.DRP, server.getID(), simParams.timeProvider, simParams.log);
//    }
//
//    /**
//     * RD: Receive dropability
//     * R: Resolve deadlock
//     *
//     * @param message
//     */
//    @Override
//    public void receiveMessage(Message message) {
//        String[] msg = message.getContents().split(":");
//
//        switch (msg[0]) {
//            case "RD": {
//                if (Log.isLoggingEnabled())
//                    log.log(Integer.parseInt(msg[1]), "Receive message: " + message);
//
//                receiveDropability(Integer.parseInt(msg[1]), Integer.parseInt(msg[2]), Integer.parseInt(msg[3]), Integer.parseInt(msg[4]));
//                break;
//            }
//            case "R": {
//                if (Log.isLoggingEnabled())
//                    log.log("Resolve- " + message.getObject());
//
//                resolve((Deadlock) message.getObject(), true);
//                break;
//            }
//            default:
//                throw new WTFException("Badly formatted message! : " + message.getContents());
//        }
//    }
//
//    public List<TransInfo> resolve(Deadlock deadlock) {
//        return resolve(deadlock, false);
//    }
//
//    public List<TransInfo> resolve(Deadlock deadlock, boolean fromMsg) {
//        List<TransInfo> transactionsInDeadlock = deadlock.getTransactionsInvolved();
//        if (beingResolvedDeadlocks.contains(transactionsInDeadlock)) {
//            if (Log.isLoggingEnabled())
//                log.log("Already resolving deadlock- " + transactionsInDeadlock);
//
//            return null;
//        }
//
//        beingResolvedDeadlocks.add(transactionsInDeadlock);
//
//        if (Log.isLoggingEnabled())
//            log.log("Resolving deadlock involving- " + transactionsInDeadlock);
//
//        //List of servers we have informed about the deadlock already
//        List<Integer> sentToAlready = new ArrayList<>();
//
//        for (TransInfo ti : transactionsInDeadlock) {
//            int serverID = ti.serverID;
//
//            if (server.getID() == serverID) {
//                Agent a = new Agent(ti.transID, deadlock, transactionsInDeadlock, server, deadlock.getDeadlockID());
//                a.resolve();
//
//                //Store a reference to the agent for this transaction + deadlock
//                agentList.put(new Pair<>(ti.transID, deadlock.getDeadlockID()), a);
//
//                //See if we have received dropabilities before this agent was created on this node
//                if (delayedReceiveDropability.containsKey(ti.transID)) {
//                    Object[] args = delayedReceiveDropability.remove(ti.transID);
//                    receiveDropability(ti.transID, (int) args[0], (int) args[1], (int) args[2]);
//                }
//
//            } else if (!fromMsg && !sentToAlready.contains(serverID)) {
//                server.getNIC().sendMessage(new Message(serverID, ServerProcess.DRP, "R:", deadlock, ti.deadline));
//                sentToAlready.add(serverID);
//            }
//        }
//
//        numberOfResolvedDeadlocks++;
//
//        return null;
//    }
//
//    public void receiveDropability(int transID, int agentID, int deadlockID, int dropability) {
//        if (Log.isLoggingEnabled())
//            log.log(transID, "Received dropability:" + dropability + " from agent: " + agentID + " for deadlock: " + deadlockID);
//
//        Pair<Integer, Integer> transDeadLockPair = new Pair<>(transID, deadlockID);
//
//        //If we have received the dropability before this server knows about the deadlock
//        if (!agentList.containsKey(transDeadLockPair))
//            delayedReceiveDropability.put(transID, new Object[]{agentID, deadlockID, dropability});
//        else
//            agentList.get(transDeadLockPair).receiveDropability(dropability, agentID);
//    }
//
//    public void resolveMultiple(List<Deadlock> l) {
//        l.forEach(this::resolve);
//    }
//
//    public int getNumberOfDeadlocks() {
//        return numberOfResolvedDeadlocks;
//    }
//
//    @Override
//    public void resolveDeadlocks(Deadlock deadlock) {
//
//    }
}