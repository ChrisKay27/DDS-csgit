package ui;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;
import javafx.util.Pair;
import simulator.protocols.deadlockDetection.WFG.Graph;
import simulator.protocols.deadlockDetection.WFG.Task;
import simulator.protocols.deadlockDetection.WFG.WFGNode;

public class GraphVisualizer extends JPanel {

    public static final String TIME_GRAPH_WAS_COMPLETED = "Time graph was completed: ";
    private final mxGraph graph;
    private final List<Pair<Graph<WFGNode>,Integer>> graphs = new ArrayList<>();
    private final Map<WFGNode,Object> nodesToGraphObj = new HashMap<>();

    private final HashMap<String, Object> edgeStyle, nodeStyle;
    private final JLabel timeField;
    private final List<Integer> onlyShowTheseTransIDs = new ArrayList<>();

    {
        edgeStyle = new HashMap<>();
        edgeStyle.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_ORTHOGONAL);
//        edgeStyle.put(mxConstants.STYLE_SHAPE,    mxConstants.SHAPE_CONNECTOR);
        edgeStyle.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC);
        edgeStyle.put(mxConstants.STYLE_STROKECOLOR, "#FF0000");
        edgeStyle.put(mxConstants.STYLE_FONTCOLOR, "#FF0000");
        edgeStyle.put(mxConstants.STYLE_LABEL_BACKGROUNDCOLOR, "#ff0000");

        nodeStyle = new HashMap<>();
//        nodeStyle.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_ORTHOGONAL);
        nodeStyle.put(mxConstants.STYLE_SHAPE,    mxConstants.SHAPE_CONNECTOR);
//        nodeStyle.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC);
        nodeStyle.put(mxConstants.STYLE_STROKECOLOR, "#FF0000");
        nodeStyle.put(mxConstants.STYLE_FONTCOLOR, "#FF0000");
        nodeStyle.put(mxConstants.STYLE_LABEL_BACKGROUNDCOLOR, "#007000");

    }

    public GraphVisualizer() {
        super(new BorderLayout());

        JPanel top = new JPanel();

        add(top,BorderLayout.NORTH);

        JTextField graphNumField = new JTextField("Enter Graph Number here", 20);
        graphNumField.addActionListener(e->{
            int graphNum = Integer.parseInt(graphNumField.getText());
            int graphsSize = graphs.size();
            if( graphNum >= graphsSize){
                graphNum = graphsSize-1;
                graphNumField.setText(""+graphNum);
            }
            showGraph(graphs.get(graphNum));

        });
        top.add(graphNumField);

        timeField = new JLabel(TIME_GRAPH_WAS_COMPLETED);
        top.add(timeField);


        JLabel onlyShowTheseTransLabel = new JLabel("Only show these trans:");
        JTextField onlyShowTheseTrans = new JTextField("All", 20);
        onlyShowTheseTrans.addActionListener(e->{
            try {
                onlyShowTheseTransIDs.clear();
                String trans = onlyShowTheseTrans.getText();
                if (trans.equals("All"))
                    return;

                String[] split = trans.split(",");
                for (String transNum : split)
                    onlyShowTheseTransIDs.add(Integer.valueOf(transNum));
            }
            catch (Exception ex){
                onlyShowTheseTrans.setText("Bad format, please do: TransID,TransID,TransID (etc ...)");
            }

            int graphNum = Integer.parseInt(graphNumField.getText());
            int graphsSize = graphs.size();
            if( graphNum >= graphsSize){
                graphNum = graphsSize-1;
                graphNumField.setText(""+graphNum);
            }
            showGraph(graphs.get(graphNum));
        });
        top.add(onlyShowTheseTransLabel);
        top.add(onlyShowTheseTrans);


        graph = new mxGraph();



        mxGraphComponent graphComponent = new mxGraphComponent(graph);
        graphComponent.setSize(1500,800);
        graphComponent.setMaximumSize(new Dimension(1500,800));
        graphComponent.setPreferredSize(new Dimension(1500,800));
        add(graphComponent,BorderLayout.CENTER);

        setSize(1500,800);
        setMaximumSize(new Dimension(1500,800));
        setPreferredSize(new Dimension(1500,800));
        setVisible(true);
    }

    private void showGraph(Pair<Graph<WFGNode>,Integer> wfGraphPair){
        Graph<WFGNode> wfGraph = wfGraphPair.getKey();

        timeField.setText(TIME_GRAPH_WAS_COMPLETED+wfGraphPair.getValue() + "   serverID: " + wfGraph.getServerID() + "  Global: " + wfGraph.isGlobal() + "  ");


        // System.out.println("Graph nodes.size = " + wfGraph.getTasks().size());

        Object parent = graph.getDefaultParent();

        Random rand = new Random(wfGraph.getTasks().size());

        int width = getWidth();
        int height = getHeight();

        graph.getModel().beginUpdate();
        try
        {
            nodesToGraphObj.values().forEach(graph.getModel()::remove);
            nodesToGraphObj.clear();

            List<String> nodes = new ArrayList<>();

            for( Task<WFGNode> t : wfGraph.getTasks() ) {
                WFGNode wfgNode = t.getId();

                if( !onlyShowTheseTransIDs.isEmpty() && !onlyShowTheseTransIDs.contains(wfgNode.getID()))
                    continue;

                String name = wfgNode.getID() + "";
                Object v1 = graph.insertVertex(parent, null, name, rand.nextDouble() * (width - 200) + 100, rand.nextDouble() * (height - 200) + 100, 40,
                        40);
                nodes.add(name);
                nodesToGraphObj.put(wfgNode, v1);

            }

            List<WFGNode> cycle = null;//wfGraph.getCycle();

            List<String> deadlockEdges = new ArrayList<>();

            for( Task<WFGNode> t : wfGraph.getTasks() ) {
                WFGNode wfgNode = t.getId();

                if( !onlyShowTheseTransIDs.isEmpty() && !onlyShowTheseTransIDs.contains(wfgNode.getID()))
                    continue;

                Set<Task<WFGNode>> edgesFrom = t.getWaitsForTasks();
                edgesFrom.forEach(dest -> {
                    String name = "";
                    if( cycle != null && cycle.contains(wfgNode) && cycle.indexOf(wfgNode) == (cycle.indexOf(dest)-1))
                        deadlockEdges.add(name);



                    graph.insertEdge(parent, null, name, nodesToGraphObj.get(wfgNode), nodesToGraphObj.get(dest.getId()));
                });
            }


//
//            mxStylesheet stylesheet = graph.getStylesheet();
//            Map<String, Map<String, Object>> edgeStyles = new HashMap<>();
//
//            deadlockEdges.forEach(name -> {
//                edgeStyles.put(name,edgeStyle);
//            });
//
//            nodes.forEach(name->{
//                edgeStyles.put(name, stylesheet.getDefaultVertexStyle());
//            });
//
//            stylesheet.setStyles(edgeStyles);
//            graph.setStylesheet(stylesheet);


        }
        finally
        {
            graph.getModel().endUpdate();
        }

//        mxOrganicLayout layout = new mxOrganicLayout(graph);
//        layout.execute(graph.getDefaultParent());

    }


    public void drawGraph(Graph<WFGNode> wfGraph, int time){
        graphs.add(new Pair<>(wfGraph,time));
    }
}