package org.overworld.example.webservice.engine;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2016
 */

public class TaskTag {

    /**
     * The future that was obtained upon submission of the task
     */
    private final Future<String> future;

    /**
     * The id of the task
     */
    private final int id;

    /**
     * A reference to the progress within the task
     */
    private final AtomicInteger progress;

    /**
     * Creates a tag for a SeekTask running in the application
     *
     * @param id
     *            the id of the task
     * @param future
     *            the future result of the task
     * @param progress
     *            the progress of the task
     */
    public TaskTag(final Integer id, final Future<String> future, final AtomicInteger progress) {

        this.id = id;
        this.future = future;
        this.progress = progress;
    }

    /**
     * @return the future result of this task
     */
    public Future<String> getFuture() {

        return this.future;
    }

    /**
     * @return the id of this task
     */
    public int getId() {

        return this.id;
    }

    /**
     * @return the progress value of this task
     */
    public int getProgress() {

        return this.progress.get();
    }

    @Override
    public String toString() {

        return "TaskTag [id=" + this.id + ", future=" + this.future
            + ", progress=" + this.progress + "]";
    }
}
