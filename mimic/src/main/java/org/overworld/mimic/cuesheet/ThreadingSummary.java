package org.overworld.mimic.cuesheet;

import java.io.Serializable;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * A serialisable class to record the reconciliation of ephemeral thread ids to
 * a more permanent thread id intended to allow threads identified on record to
 * be identified again on replay
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2015-09-26
 */
public class ThreadingSummary extends TreeSet<ThreadStart> implements
Serializable {

    private static final long serialVersionUID = 2381808421129374383L;

    private long mainThreadId = -1;

    @Override
    public boolean equals(final Object obj) {

        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (this.getClass() != obj.getClass()) return false;
        final ThreadingSummary other = (ThreadingSummary) obj;
        if (this.mainThreadId != other.mainThreadId) return false;
        return true;
    }

    /**
     * @return the id of the main thread, the first thread that started all
     *         others
     */
    public long getMainThreadId() {

        return this.mainThreadId;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
            + (int) (this.mainThreadId ^ (this.mainThreadId >>> 32));
        return result;
    }

    /**
     * @param mainThreadId
     *            the id of the main thread, the first thread that started all
     *            others
     */
    public void setMainThreadId(final long mainThreadId) {

        this.mainThreadId = mainThreadId;
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder();

        final Iterator<ThreadStart> it = this.iterator();

        while (it.hasNext()) {
            final ThreadStart ts = it.next();
            sb.append(ts.getThreadTransientId() + "=" + ts.getThreadPersistId());
            if (it.hasNext()) sb.append(", ");
        }

        return "ThreadingSummary [mainThreadId=" + this.mainThreadId
            + ", Threads=[" + sb + "]";
    }
}