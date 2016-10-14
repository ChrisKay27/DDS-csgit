package simulator.server.transactionManager;

import simulator.SimParams;
import simulator.eventQueue.Event;
import simulator.server.Server;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static simulator.SimParams.*;

/**
 * Created by Chris on 6/10/2016.
 */
public class TransactionGenerator {
    private int remainingTransactions;

    private final Supplier<Double> rand;
    private final Supplier<Integer> timeProvider;
    private final Supplier<Integer> IDProvider;
    private final Supplier<Integer> pageNumProvider;
    private final SimParams simParams;
    private final Consumer<Transaction> transConsumer;

    private final Server server;
    private final int serverID;
    private final Consumer<Event> eventQueue;


    public void start() {
        generateTransaction();
    }

    private void generateTransaction() {
        if (remainingTransactions-- == 0)
            return;

        int nextTransArriveTime = timeProvider.get() + (int) (simParams.arrivalRateMin + (rand.get() * (simParams.arrivalRateMax- simParams.arrivalRateMin)));


        int numReadPages = (int) (rand.get() * 4) + 1;
        int numWritePages = (int) (rand.get() * 5);

        int deadline = timeProvider.get() + (numReadPages * 200) + (numWritePages * 300);


        Transaction t = new Transaction(IDProvider.get(), server.getID(), deadline);

        List<Integer> allReadPageNums = t.getAllReadPageNums();
        List<Integer> readPageNums = t.getReadPageNums();
        for (int i = 0; i < numReadPages; i++) {
            int pageNum = pageNumProvider.get();
            if (!readPageNums.contains(pageNum)) {
                allReadPageNums.add(pageNum);
                readPageNums.add(pageNum);
            }
            else{
                i--;
            }
        }

        List<Integer> allWritePageNums = t.getAllWritePageNums();
        List<Integer> writePageNums = t.getWritePageNums();
        for (int i = 0; i < numWritePages; i++) {
            int pageNum = pageNumProvider.get();
            if (!readPageNums.contains(pageNum) && !writePageNums.contains(pageNum)) {
                allWritePageNums.add(pageNum);
                writePageNums.add(pageNum);
            }
            else{
                i--;
            }
        }

        t.setWorkload(allReadPageNums.size()+allWritePageNums.size());
        t.setExecutionTime((numReadPages * 200) + (numWritePages * 300));

        transConsumer.accept(t);

        eventQueue.accept(new Event(nextTransArriveTime, serverID, this::generateTransaction, false));
    }

    public int getRemainingTransactions() {
        return remainingTransactions;
    }

    public TransactionGenerator(Server server, SimParams simParams, Consumer<Transaction> transactionConsumer) {
        this.server = server;
        this.eventQueue = simParams.eventQueue;
        this.rand = simParams.rand;
        this.timeProvider = simParams.timeProvider;
        IDProvider = simParams.IDProvider;
        this.pageNumProvider = simParams.pageNumProvider;
        this.simParams = simParams;
        this.transConsumer = transactionConsumer;

        remainingTransactions = simParams.getNumTransPerServer();


        serverID = server.getID();
    }


}
