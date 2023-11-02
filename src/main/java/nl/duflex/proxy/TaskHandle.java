package nl.duflex.proxy;

public class TaskHandle {
    private final AtomicWorkerFlags atomicWorkerFlags = new AtomicWorkerFlags(0);
    private final ITask task;
    private Thread thread = null;

    public TaskHandle(final ITask worker) {
        this.task = worker;
    }

    public void stop() {
        if (this.atomicWorkerFlags.compareAndSetBit(AtomicWorkerFlags.STOP_BIT)) {
            this.thread.interrupt();
        }
    }

    public void start() {
        if (this.atomicWorkerFlags.compareAndSetBit(AtomicWorkerFlags.RUNNING_BIT)) {
            this.thread = new Thread(() -> {
                task.setup();
                task.run();
            });
        }
    }
}
