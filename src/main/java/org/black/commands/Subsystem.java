package org.black.commands;

/**
 * Subsystem interface for use with {@link org.black.commands.Scheduler}.
 * You shouldn't be calling subsystem methods in your OpMode, but rather in Command classes.
 * @author kfajdsl
 * @version 0.1, 1/13/2020
 */
public interface Subsystem {

    /**
     * Ran once at the beginning of the OpMode.
     */
    default void init() {}

    /**
     * Ran repeatedly until the end of the OpMode.
     */
    default void periodic() {}

    /**
     * Ran once at the end of the OpMode.
     */
    default void stop() {}

}
