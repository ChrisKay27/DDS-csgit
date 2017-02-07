package simulator.protocols.deadlockDetection.mobileAgentEnabledApproach;

import exceptions.WTFException;
import simulator.SimParams;
import simulator.eventQueue.Event;
import simulator.protocols.deadlockDetection.Deadlock;
import simulator.protocols.deadlockDetection.WFG.Graph;
import simulator.protocols.deadlockDetection.WFG.GraphBuilder;
import simulator.protocols.deadlockDetection.WFG.Task;
import simulator.protocols.deadlockDetection.WFG.WFGNode;
import simulator.server.Server;
import simulator.server.transactionManager.TransInfo;
import ui.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static simulator.protocols.deadlockDetection.WFG_DDP.convertToList;

public class MobileAgent {

    private final MAEDD maedd;
    private final Log log;
    private final Server server;
    private final int serverID;
    private final SimParams simParams;
    private GraphBuilder<WFGNode> wfgBuilder;
    private final List<Integer> receivedFromServers;

    private List<List<WFGNode>> deadlocks = new LinkedList<>();
    private List<Graph<WFGNode>> receivedWFGs;
    private Consumer<Event> eventQueue;

    private final String direction;

    public MobileAgent(MAEDD maedd, Server server, String direction) {
        this.maedd = maedd;
        log = maedd.log;
        this.server = server;
        this.serverID = server.getID();
        simParams = server.getSimParams();
        receivedWFGs = maedd.getReceivedWFGs();
        wfgBuilder = maedd.getWfgBuilder();
        receivedFromServers = maedd.getReceivedFromServers();
        eventQueue = simParams.eventQueue;

        this.direction = direction;
    }

    /**
     * List of servers for this agent to visit.
     */
    private List<Integer> S_List = new LinkedList<>();


    /**
     * List of processes involved
     */
    private List<Integer> P_List = new LinkedList<>();

    /**
     * This is called when a WFG is received
     */
    public void updateWFGraph(Graph<WFGNode> graph, int server) {
        if (Log.isLoggingEnabled())
            log.log("Updating graph with waits from server " + server);

        if (receivedWFGs.contains(graph))
            throw new WTFException(serverID + ": Have already received this WFG! " + graph);

        receivedWFGs.add(graph);

        graph.getTasks().forEach(wfgNodeTask -> {
            WFGNode trans = wfgNodeTask.getId();
            wfgBuilder.addTask(trans);
            wfgNodeTask.getWaitsForTasks().forEach(waitingFor -> wfgBuilder.addTaskWaitsFor(trans, waitingFor.getId()));
        });

        receivedFromServers.add(server);

        if (receivedFromServers.containsAll(simParams.allServersList)) {
            BiConsumer<Graph<WFGNode>, Integer> wfGraphConsumer = maedd.getWfGraphConsumer();
            if (wfGraphConsumer != null) {
                //System.out.println("Graph has " + wfgBuilder.size() + " nodes at time " + simParams.getTime());
                Graph<WFGNode> copy = wfgBuilder.build();
                copy.setGlobal(true);
                wfGraphConsumer.accept(copy, simParams.getTime());
            }

            searchGraph(wfgBuilder.build());
            eventQueue.accept(new Event(simParams.getTime() + simParams.getDeadlockDetectInterval(), serverID, maedd::startDetectionIteration));

            //clear wfgBuilder so we can start fresh next round
            wfgBuilder = new GraphBuilder<>();

            receivedFromServers.clear();
        }
    }

    protected void searchGraph(Graph<WFGNode> build) {
        if (Log.isLoggingEnabled())
            log.log("Mobile Agent - Searching graph");

        maedd.calculateAndIncurOverhead(build);
        deadlocks.clear();

        // MANI: CHANGE!!!
        String direction = getDirection();

        List<Integer> globalDetectors = new ArrayList<>();
        for (int i = 0; i < maedd.getNumberGlobalDetectors(); i++) {
            globalDetectors.add(i);
        }

        int thisNodesIndex = globalDetectors.indexOf(server.getID());

        List<Task<WFGNode>> allTransactions = new ArrayList<>(build.getTasks());
        List<TransInfo> transThisAgentCaresAbout = new ArrayList<>();

        //collect all transactions this agent cares about
        for (int i = 0; i < allTransactions.size(); i++) {
            if (i % globalDetectors.size() == thisNodesIndex)
                transThisAgentCaresAbout.add((TransInfo) allTransactions.get(i).getId());
        }

        if (transThisAgentCaresAbout.isEmpty()) {
            if (Log.isLoggingEnabled())
                log.log("Global Agent - No transactions for this agent");

            return;
        }

        if (Log.isLoggingEnabled())
            log.log("Global Agent - This agent cares about - " + transThisAgentCaresAbout);

        //Get transInfo and start searching through its children
        for (TransInfo t : transThisAgentCaresAbout) {
            //Convert the TransInfo list to list of WFGNodes
            List<Task<WFGNode>> edgesFrom = build.getEdgesFrom(t);

            followCycle(t, edgesFrom, new LinkedList<>());
        }

        //Convert the list of lists of WFGNodes to a list of lists of Deadlocks
        List<List<TransInfo>> deadlocksTransInfo = new ArrayList<>();
        List<Deadlock> deadlocksList = new ArrayList<>();

        deadlocks.forEach(deadlock -> {
            List<TransInfo> deadlockTransInfo = new ArrayList<>();
            deadlock.forEach(wfgNode -> deadlockTransInfo.add((TransInfo) wfgNode));

            //If the deadlock was detected twice, ignore it the second time.
            if (!deadlocksTransInfo.contains(deadlockTransInfo)) {
                deadlocksTransInfo.add(deadlockTransInfo);

                deadlocksList.add(new Deadlock(deadlockTransInfo, server.getID(), simParams.getTime(), true));
            }
        });

        if (deadlocksList.isEmpty()) {
            if (Log.isLoggingEnabled())
                log.log("Global Agent - Found no deadlocks");

            return;
        }

        deadlocksList.forEach(maedd.getDeadlockListener());
        if (Log.isLoggingEnabled())
            log.log("Mobile Agent - Found deadlocks - " + deadlocksTransInfo);

        //Resolve the deadlocks
        maedd.getResolver().accept(deadlocksList);
    }

    private void followCycle(WFGNode lookingFor, List<Task<WFGNode>> edges, List<WFGNode> path) {
        if (edges.isEmpty())
            return;

        for (Task<WFGNode> t : edges) {
            WFGNode edge = t.getId();

            if (edge == lookingFor) {
                path.add(edge);
                LinkedList<WFGNode> deadlockPath = new LinkedList<>(path);
                deadlockPath.addFirst(deadlockPath.removeLast());

                deadlocks.add(deadlockPath);
                if (Log.isLoggingEnabled())
                    log.log("Global Agent - Found deadlock - " + deadlockPath);

                path.remove(edge);
            } else if ((edge.getID() > lookingFor.getID() && !path.contains(edge))) {
                path.add(edge);
                followCycle(lookingFor, convertToList(t.getWaitsForTasks()), path);
                path.remove(edge);
            }
        }
    }

    public String getDirection() {
        return direction;
    }
}
