package simulator.protocols.deadlockDetection.mobileAgentEnabledApproach;

import simulator.SimParams;
import simulator.enums.ServerProcess;
import simulator.eventQueue.Event;
import simulator.protocols.deadlockDetection.Deadlock;
import simulator.protocols.deadlockDetection.WFG.Graph;
import simulator.protocols.deadlockDetection.WFG.GraphBuilder;
import simulator.protocols.deadlockDetection.WFG.Task;
import simulator.protocols.deadlockDetection.WFG.WFGNode;
import simulator.protocols.deadlockDetection.WFG_DDP;
import simulator.server.Server;
import simulator.server.network.Message;
import simulator.server.network.NetworkInterface;
import ui.Log;

import java.util.*;
import java.util.function.Consumer;

public class MAEDD extends WFG_DDP {

    protected final Log log;
    protected List<Integer> allServers = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7);
    protected List<Integer> mobileAgentServers = Arrays.asList(0, 7);

    private final StaticAgent staticAgent;
    private final MobileAgent mobileAgent;

    private final Server server;
    private final int serverID;

    //TODO occupy network by mobile agent's size

    public MAEDD(Server server, SimParams simParams, Consumer<List<Deadlock>> resolver, Consumer<Integer> overheadIncurer, Consumer<Deadlock> deadlockListener) {
        super(server, simParams, resolver, overheadIncurer, deadlockListener);
        simParams.usesWFG = true;
        log = new Log(ServerProcess.DDP, server.getID(), simParams.timeProvider, simParams.log);

        staticAgent = new StaticAgent(this, server);
        mobileAgent = new MobileAgent(this, server);
        this.server = server;
        this.serverID = server.getID();
    }

    @Override
    public void start() {
        eventQueue.accept(new Event(simParams.getTime() + simParams.getDeadlockDetectInterval(), serverID, this::startDetectionIteration));
    }

    protected void searchGraph(Graph<WFGNode> build) {
        staticAgent.searchGraph(build);

        /**
         * if your on the same server as  global agent, update the list directly
         * if not, send the list to the mobile agents
         */
        //Posts an event to check for deadlocks in the future and clears its WFGBuilder
        eventQueue.accept(new Event(simParams.getTime() + simParams.getDeadlockDetectInterval() + 1, serverID, this::sendSListToMobiles));

        //post an event to send the local WFG to the global detectors. We wait for this so the local deadlocks can resolve before going global
        //eventQueue.accept(new Event(simParams.getTime() + 100, serverID, this::sendLocalWFGToGlobals, true));
    }

    protected void calculateAndIncurOverhead(Graph<WFGNode> WFG) {
        //calc overhead
        int overhead = 0;
        for (Task<WFGNode> rt : WFG.getTasks()) {
            //add one for each vertex
            overhead++;

            //add one for each edge
            overhead += rt.getWaitsForTasks().size();
        }

        if (Log.isLoggingEnabled())
            log.log("Incurring overhead- " + overhead);

        overheadIncurer.accept(overhead);
    }

    /**
     * This is called when a WFG is received
     */
    public void updateWFGraph(Graph<WFGNode> graph, int server) {
        if (Log.isLoggingEnabled())
            log.log("Updating graph (created at " + graph.getCreationTime() + ") with waits from server " + server);

        mobileAgent.updateWFGraph(graph, server);
        wfgBuilder = new GraphBuilder<>();
    }

    /**
     * Sends WFG to all other nodes
     */
    @Override
    public void startDetectionIteration() {
        if (Log.isLoggingEnabled())
            log.log("Starting Detection Iteration");

        //Create the local WFG
        Graph<WFGNode> localWFG = createLocalGraphOfWaits();

        //Calculate the amount of overhead to incur
        int size = localWFG.getNumberOfWaits();

        if (Log.isLoggingEnabled())
            log.log("Local WFG has " + size + " nodes.");

        //Search the local graph
        searchGraph(localWFG);

        //post an event to send the local WFG to the global detectors. We wait for this so the local deadlocks can resolve before going global
        //eventQueue.accept(new Event(simParams.getTime() + 100, serverID, this::sendLocalWFGToGlobals, true));
    }

    private void sendSListToMobiles() {
        // Create the S_List
        HashSet s_List = staticAgent.getS_List();
        System.out.println("sendSListToMobiles(): " + s_List + " Server: " + serverID);

        NetworkInterface NIC = server.getNIC();

        //Calculate the amount of overhead to incur
        int size = s_List.size();
        if (size == 0)
            size = 1;

        //Send our S_List to the detector nodes
        for (int i = 0; i < simParams.globalDetectors; i++) {
            int globalDetector = mobileAgentServers.get(i);
            System.out.println("+sending  " + s_List + " from " + serverID + " to " + globalDetector);
            Message message = new Message(globalDetector, ServerProcess.DDP, serverID + "", s_List, simParams.getTime());
            message.setSize(size);
            NIC.sendMessage(message);
        }
    }

    /**
     * Only receives one type of message, that is the wait for graph from another node.
     * The message's contents is just the serverID of the other server.
     * The message's object is the Wait for Graph
     */
    @Override
    public void receiveMessage(Message msg) {
        if (Log.isLoggingEnabled())
            log.log("Received message- " + msg);

        int remoteServerID = Integer.parseInt(msg.getContents());

        if (msg.getObject() instanceof HashSet)
            mobileAgent.update_S_List((HashSet) msg.getObject(), remoteServerID);
        else
            updateWFGraph((Graph<WFGNode>) msg.getObject(), remoteServerID);
    }

    @Override
    public void sendLocalWFGToGlobals() {
        //Create the local WFG
        Graph<WFGNode> localWFG = createLocalGraphOfWaits();

        //clear the wfgBuilder now that we have the local WFG
        wfgBuilder = new GraphBuilder<>();

        NetworkInterface NIC = server.getNIC();

        //Calculate the amount of overhead to incur
        int size = localWFG.getNumberOfWaits();
        if (size == 0)
            size = 1;

        //Send our graph to the detector nodes
        for (int i = 0; i < simParams.globalDetectors; i++) {
            int globalDetector = mobileAgentServers.get(i);
            if (globalDetector != serverID) {
                System.out.println("+sending lwfg " + localWFG + " from " + serverID + " to " + globalDetector);
                Message message = new Message(globalDetector, ServerProcess.DDP, serverID + "", localWFG, simParams.getTime());
                message.setSize(size);
                message.setReoccuring(true);
                NIC.sendMessage(message);
            }
        }

        updateWFGraph(localWFG, serverID);
    }
}