package simulator.protocols.deadlockDetection;

import exceptions.WTFException;
import simulator.SimParams;
import simulator.enums.ServerProcess;
import simulator.eventQueue.Event;
import simulator.protocols.deadlockDetection.WFG.Graph;
import simulator.protocols.deadlockDetection.WFG.GraphBuilder;
import simulator.protocols.deadlockDetection.WFG.Task;
import simulator.protocols.deadlockDetection.WFG.WFGNode;
import simulator.server.Server;
import simulator.server.lockManager.Lock;
import simulator.server.network.Message;
import simulator.server.network.NetworkInterface;
import simulator.server.transactionManager.TransInfo;
import ui.Log;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created by Mani
 *
 * t=0 Simulation starts
 * t=300 WFG get sent to all nodes
 * t=x all WFGs have been received, start searching, then periodically clear WFG and start over
 *
 */
public class WFG_DDP extends DeadlockDetectionProtocol{

    private final Log log;
    protected Consumer<Deadlock> deadlockListener;

    protected GraphBuilder<WFGNode> wfgBuilder = new GraphBuilder<>();


    private final List<Integer> receivedFromServers = new ArrayList<>();
    private final List<Integer> globalDetectors = Arrays.asList(0,3,5);
    private BiConsumer<Graph<WFGNode>, Integer> wfGraphConsumer;


    public WFG_DDP(Server server, SimParams simParams, Consumer<List<Deadlock>> resolver, Consumer<Integer> overheadIncurer, Consumer<Deadlock> deadlockListener) {
        super(server, simParams, resolver, overheadIncurer, deadlockListener);

        log = new Log(ServerProcess.DDP,serverID,simParams.timeProvider,simParams.log);

        this.deadlockListener = deadlockListener;
    }

    @Override
    public void start() {
        super.start();
        simParams.eventQueue.accept(new Event(simParams.timeProvider.get()+300,serverID,this::sendDeadlockInfo));
    }




    /**
     * Only receives one type of message, that is the wait for graph from another node.
     * The message's contents is just the serverID of the other server.
     * The message's object is the Wait for Graph
     */
    public void receiveMessage(Message msg) {
        if(Log.isLoggingEnabled()) log.log("Received message: " + msg);
        int remoteServerID = Integer.parseInt(msg.getContents());

        updateWFGraph((Graph<WFGNode>) msg.getObject(),remoteServerID);
    }




    /**
     * Sends WFG to all other nodes
     */
    protected void sendDeadlockInfo() {
        if(Log.isLoggingEnabled()) log.log("Sending deadlock info");
        Graph<WFGNode> localWFG = createLocalGraphOfWaits();


        int size = 0;
        for (Task<WFGNode> rt : localWFG.getTasks()) {
            size++;
            size += rt.getWaitsForTasks().size();
        }
        int delay = size / 100;
        simParams.incurOverhead(serverID, delay);

        //Search the local graph
        searchGraph(localWFG);


        NetworkInterface NIC = server.getNIC();

        //Send our graph to the detector nodes
        for (int i : globalDetectors) {
            if( i != serverID ) {
                Message message = new Message(i, ServerProcess.DDP, serverID+"" ,localWFG, simParams.timeProvider.get());
                message.setSize(size);
                message.setReoccuring(true);
                NIC.sendMessage(message);
            }
        }

        boolean isAGlobalDetector = globalDetectors.contains(serverID);
        if(!isAGlobalDetector) {
            //If this isn't a global detector it posts an event to check for deadlocks in the future and clears its WFGBuilder
            eventQueue.accept(new Event(simParams.timeProvider.get()+simParams.getDeadlockDetectInterval()+30,serverID,this::sendDeadlockInfo, true));
            wfgBuilder = new GraphBuilder<>();
            return;
        }

        updateWFGraph(localWFG, serverID);
    }


    /**
     * Creates the WFG for this server
     * @return a new WFG instance
     */
    protected Graph<WFGNode> createLocalGraphOfWaits() {
        if(Log.isLoggingEnabled()) log.log("Creating local graph");

        //wfgBuilder = new GraphBuilder<>();

        Map<Integer, List<Lock>> heldLocksMap = server.getLM().getHeldLocks();
        Map<Integer, List<Lock>> waitingLocksMap = server.getLM().getWaitingLocks();

        for (Integer pageNum : waitingLocksMap.keySet()) {

            List<Lock> waitingLocks = waitingLocksMap.get(pageNum);

            for (Lock waitingLock : waitingLocks) {

                List<Lock> heldLocks = heldLocksMap.get(waitingLock.getPageNum());

                heldLocks.forEach(heldLock -> {
                    addWait(waitingLock, heldLock);
                });
            }
        }

        return wfgBuilder.build();
    }



    public void addWait(Lock waitingLock, Lock heldLock) {
        addWait(getTransInfo(waitingLock.getTransID()),getTransInfo(heldLock.getTransID()));
    }


    public void removeAllWaitsOn(Lock heldLock) {
        wfgBuilder.removeTask(getTransInfo(heldLock.getTransID()));
    }


    private TransInfo getTransInfo(int transID) {
        return simParams.transInfos.get(transID);
    }


    public final void addWait(TransInfo rfrom, TransInfo rto) {

        wfgBuilder.addTask(rfrom);
        wfgBuilder.addTask(rto);

        wfgBuilder.addTaskWaitsFor(rfrom, rto);
    }

//    public void updateWFGraph(List<Lock> waiting, List<Lock> current, Integer server) {
////        waitingLocks.addAll(waiting);
////        currentLocks.addAll(current);
////        waitingLocks.removeAll(current);
////        currentLocks.removeAll(waiting);
//
//        received(server);
//    }


    private List<Graph<WFGNode>> receivedWFGs = new ArrayList<>();

    /**
     * This is called when a WFG is received
     */
    public void updateWFGraph(Graph<WFGNode> graph, int server) {
        if(Log.isLoggingEnabled()) log.log("Updating graph with waits from server " + server);

        if( receivedWFGs.contains(graph) )
            throw new WTFException(serverID+": Have already received this WFG! " + graph);
        receivedWFGs.add(graph);


        graph.getTasks().forEach(wfgNodeTask -> {
            WFGNode trans = wfgNodeTask.getId();
            wfgBuilder.addTask(trans);
            wfgNodeTask.getWaitsForTasks().forEach(waitingFor -> wfgBuilder.addTaskWaitsFor(trans,waitingFor.getId()));
        });



        receivedFromServers.add(server);
        if (receivedFromServers.containsAll(simParams.allServersList)) {

            if( wfGraphConsumer != null ){
                //System.out.println("Graph has " + wfgBuilder.size() + " nodes at time " + simParams.timeProvider.get());

                Graph<WFGNode> copy = wfgBuilder.build();
                wfGraphConsumer.accept(copy, simParams.timeProvider.get());
            }

            searchGraph(wfgBuilder.build());
            eventQueue.accept(new Event(simParams.timeProvider.get()+simParams.getDeadlockDetectInterval(),serverID,this::sendDeadlockInfo));
            wfgBuilder = new GraphBuilder<>();


            receivedFromServers.clear();

        }
    }


    protected void searchGraph(Graph<WFGNode> wfg) {
        if(Log.isLoggingEnabled()) log.log("Search Graph (Nothing in default implementation)" );

    }





    public void setGraphListener(BiConsumer<Graph<WFGNode>, Integer> wfGraphConsumer){
        this.wfGraphConsumer = wfGraphConsumer;
    }



    public static List<Task<WFGNode>> convertToList(Set<Task<WFGNode>> waits){
        List<Task<WFGNode>> edges = new ArrayList<>();
        waits.forEach(edges::add);
        return edges;
    }
}
