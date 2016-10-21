package simulator.server;

import exceptions.WTFException;
import simulator.SimParams;
import simulator.enums.ServerProcess;
import simulator.protocols.deadlockDetection.DeadlockDetectionProtocol;
import simulator.protocols.deadlockDetection.WFG_DDP;
import simulator.protocols.deadlockResolution.DeadlockResolutionProtocol;
import simulator.server.disk.Disk;
import simulator.server.lockManager.LockManager;
import simulator.server.lockManager.Range;
import simulator.server.network.Message;
import simulator.server.network.NetworkInterface;
import simulator.server.processor.Processor;
import simulator.server.transactionManager.Transaction;
import simulator.server.transactionManager.TransactionManager;
import ui.Log;

/**
 * Created by Mani
 */
public class Server {


    private final SimParams simParams;
    private final Log log;
    private final int serverID;

    //Essentials
    private final Processor CPU;
    private final TransactionManager TM;
    private final Disk disk;
    private final NetworkInterface NIC;
    private final LockManager LM;

    //Protocols
    private final DeadlockDetectionProtocol DDP;
    private final DeadlockResolutionProtocol DRP;

    public Server(SimParams simParams, int serverID, Range pageRange) {
        this.simParams = simParams;
        this.serverID = serverID;

        log = new Log(ServerProcess.Server, serverID, simParams.timeProvider,simParams.log);

        TM = new TransactionManager(this, simParams);
        disk = new Disk(serverID, simParams,pageRange);
        CPU = new Processor(serverID, simParams);
        NIC = new NetworkInterface(this, simParams);
        LM = new LockManager(this,simParams,pageRange);

        DRP = DeadlockResolutionProtocol.get(this, simParams.DRP);
        DDP = DeadlockDetectionProtocol.get(this, simParams.DDP, simParams.getDeadlockListener());


    }

    public NetworkInterface getNIC() {
        return NIC;
    }


    public void start() {
        TM.start();
        DDP.start();
    }

    public void acquireLocks(Transaction t) {
        LM.acquireLocks(t);
    }

    public int getID() {
        return serverID;
    }


    public void abort(Transaction t) {
        LM.abort(t);
        int transNum = t.getID();
        disk.abort(transNum);
        CPU.abort(transNum);
//        NIC.abort(transNum);
    }

    public TransactionManager getTM() {
        return TM;
    }

    public void receiveMessage(Message msg) {
        switch(msg.getProcess()){
            case TransactionManager:
                TM.receiveMessage(msg);
                break;
            case LockManager:
                LM.receiveMessage(msg);
                break;
            case Disk:
                disk.receiveMessage(msg);
                break;
            case DDP:
                DDP.receiveMessage(msg);
                break;
            case DRP:
                DRP.receiveMessage(msg);
                break;
            default:
                throw new WTFException(serverID+": Did not find correct process for this message! : " + msg);
        }
    }

    public SimParams getSimParams() {
        return simParams;
    }

    public Processor getCPU() {
        return CPU;
    }

    public Disk getDisk() {
        return disk;
    }

    public LockManager getLM() {
        return LM;
    }

    public void incurOverhead(int overhead) {
        simParams.incurOverhead(serverID,overhead);
    }

    public DeadlockResolutionProtocol getDRP() {
        return DRP;

    }

    public DeadlockDetectionProtocol getDDP() {
        return DDP;
    }
}
