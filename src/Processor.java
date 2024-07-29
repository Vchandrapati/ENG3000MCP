import javax.xml.crypto.Data;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Processor implements Runnable {
    private static final Logger logger = Logger.getLogger(Processor.class.getName());
    private static Processor instance = null;
    private volatile boolean running = true;
    private Thread processorThread;

    private Database db;

    private Processor(Database db) {
        this.db = db;
    }

    public static synchronized Processor getInstance(Database db) {
        if(instance == null)
            instance = new Processor(db);

        return instance;
    }

    public void start() {
        if(processorThread == null || !processorThread.isAlive()) {
            processorThread = new Thread(this);
            processorThread.start();
        }
    }

    public void stop() {
        running = false;
        if(processorThread != null)
            try {
                processorThread.join();
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Error shutting down the thread", e);
            }
    }

    @Override
    public void run() {
        while(running) {
            processTrains();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Error sleeping Thread", e);
            }
        }
    }

    private void processTrains() {
        synchronized (this) {
            System.out.println("Top implement");
        }
    }
}
