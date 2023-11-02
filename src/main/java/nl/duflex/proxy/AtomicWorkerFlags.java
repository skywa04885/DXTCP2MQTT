package nl.duflex.proxy;

public class AtomicWorkerFlags {
    public static final int RUNNING_BIT = 0; // Is set when the worker is running.
    public static final int STOP_BIT = 1; // Is set when the worker has to stop.

    private int flags;

    public AtomicWorkerFlags(final int flags) {
        this.flags = flags;
    }

    public int getFlags() {
        return flags;
    }

    /**
     * Sets the given bit when it's not yet set.
     * @param bit The bit to set.
     * @return True if the bit has been set, false if the bit was set already.
     */
    public boolean compareAndSetBit(final int bit) {
        synchronized (this) {
            if ((this.flags & (1 << bit)) != 0)
                return false;

            this.flags |= (1 << bit);

            return true;
        }
    }

    public boolean compareAndClearBit(final int bit) {
        synchronized (this) {
            if ((this.flags & (1 << bit)) == 0)
                return false;

            this.flags &= ~(1 << bit);

            return true;
        }
    }

    public boolean isBitSet(final int bit) {
        synchronized (this) {
            return (this.flags & (1 << bit)) != 0;
        }
    }

    public boolean isBitClear(final int bit) {
        synchronized (this) {
            return (this.flags & (1 << bit)) == 0;
        }
    }
}
