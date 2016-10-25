package ui;

import simulator.enums.ServerProcess;

import javax.swing.*;
import java.awt.*;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.function.Consumer;

public class Output extends JPanel{

    public static final String COLON = ":";
    private static final String ALL = "All";
    private final JTextPane txtArea;

    private final java.util.List<String> allMessages = new ArrayList<>();
    private final Map<String, java.util.List<String>> transIDToMessages = new HashMap<>();
    private final Map<String, java.util.List<String>> serverIDToMessages = new HashMap<>();
    private final Map<String, java.util.List<String>> processToMessages = new HashMap<>();


    public Output() throws HeadlessException {
        super(new BorderLayout());
        txtArea = new JTextPane();
        txtArea.setEditable(false);
        //txtArea.setContentType("text/html");
//        JPanel txtAreaPanel = new JPanel();
//        txtAreaPanel.add(txtArea);
        JScrollPane scrollPane = new JScrollPane(txtArea);
//        scrollPane.add();
        scrollPane.setSize(1400,750);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        JPanel topPanel = new JPanel();

        JLabel legend = new JLabel("<Time>:<NodeNum>:<Process>:<TransID>: <Message>");
        topPanel.add(legend);


        JComboBox<String> serverSelector = new JComboBox<>();
        serverSelector.addItem(ALL);
        for (int i = 0; i < 8; i++)
            serverSelector.addItem(""+i);
        serverSelector.setLightWeightPopupEnabled(false);

        JComboBox<String> processSelector = new JComboBox<>();
        processSelector.addItem(ALL);
        processSelector.addItem(ServerProcess.Server.toString());
        processSelector.addItem(ServerProcess.Disk.toString());
        processSelector.addItem(ServerProcess.DDP.toString());
        processSelector.addItem(ServerProcess.DRP.toString());
        processSelector.addItem(ServerProcess.NetworkConnection.toString());
        processSelector.addItem(ServerProcess.NetworkInterface.toString());
        processSelector.addItem(ServerProcess.LockManager.toString());
        processSelector.addItem(ServerProcess.TransactionManager.toString());
        processSelector.addItem(ServerProcess.Processor.toString());
        processSelector.setLightWeightPopupEnabled(false);

        JTextField transIDField = new JTextField("All",8);
        JTextField containsField = new JTextField("",15);

        ActionListener listener = e->{

            System.out.println("There are " + allMessages.size() + " log messages");

            txtArea.setText("");//<html>");
            StringBuilder sb = new StringBuilder();
            synchronized (allMessages) {
                for (String msg : allMessages) {

                    String[] split = msg.split(COLON);
                    if (split.length > 4) {
                        String selectedServer = (String) serverSelector.getSelectedItem();
                        if (!ALL.equals(selectedServer)) {
                            if (!split[1].equals(selectedServer))
                                continue;
                        }

                        String selectedProcess = (String) processSelector.getSelectedItem();
                        if (!ALL.equals(selectedProcess)) {
                            if (!split[2].equals(selectedProcess))
                                continue;
                        }

                        String selectedTrans = transIDField.getText();
                        if (!ALL.equals(selectedTrans)) {
                            if (!split[3].equals(selectedTrans))
                                continue;
                        }

                        String txt = containsField.getText();
                        if (!ALL.equals(txt)) {
                            if (!split[4].contains(txt))
                                continue;
                        }
                    }
                    if (sb.length() > 500000) {
                        sb.append("Too many messages, apply more filters!");
                        break;
                    }
                    sb.append(msg).append('\n');//"<br>");
                }
            }
            txtArea.setText(sb.toString());//+"</html>");
        };

        serverSelector.addActionListener(listener);
        processSelector.addActionListener(listener);
        transIDField.addActionListener(listener);
        containsField.addActionListener(listener);

        JLabel servSelLabel = new JLabel("Server: ");
        topPanel.add(servSelLabel);
        topPanel.add(serverSelector);

        JLabel procSelLabel = new JLabel("Process: ");
        topPanel.add(procSelLabel);
        topPanel.add(processSelector);

        JLabel transIDLabel = new JLabel("Trans ID: ");
        topPanel.add(transIDLabel);
        topPanel.add(transIDField);

        JLabel containsLabel = new JLabel("  Contains: ");
        topPanel.add(containsLabel);
        topPanel.add(containsField);


        JLabel selectFilter = new JLabel("Select a filter to show data!");
        topPanel.add(selectFilter);

        add(topPanel,BorderLayout.NORTH);

    }

    public void log(String s){
        synchronized (allMessages) {
            allMessages.add(s);
        }

        String[] split = s.split(COLON);

        if(split.length>4) {
            if (!serverIDToMessages.containsKey(split[1]))
                serverIDToMessages.put(split[1], new ArrayList<>());
            serverIDToMessages.get(split[1]).add(s);

            if (!processToMessages.containsKey(split[2]))
                processToMessages.put(split[2], new ArrayList<>());
            processToMessages.get(split[2]).add(s);

            if (!split[3].isEmpty()) {
                if (!transIDToMessages.containsKey(split[3]))
                    transIDToMessages.put(split[3], new ArrayList<>());
                transIDToMessages.get(split[3]).add(s);
            }
        }

//        txtArea.append(s+'\n');
    }



}
