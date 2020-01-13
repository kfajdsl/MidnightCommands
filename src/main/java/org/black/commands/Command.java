package org.black.commands;

import java.util.Set;

/**
 * Command interface for use with {@link org.black.commands.Scheduler}
 * @author kfajdsl
 * @version 0.1, 1/13/2020
 */
public interface Command {

    /**
     * Runs once when command is scheduled.
     */
    default void init() {}

    /**
     * Runs repeatedly until command stops.
     */
    default void run() {}

    /**
     * Runs once when command stops.
     * @param interrupted Whether the command was interrupted by another command
     */
    default void stop(boolean interrupted) {}

    /**
     * Determines if a command is done. When it returns true, .stop(false) is called by the Scheduler.
     * Should always be false for a default command
     * @return Whether the command is finished running
     */
    default boolean isFinished() { return false; }

    /**
     * Provides a list of required subsystems to the scheduler.
     * @return User defined list of subsystems required for command to run
     */
    Set<Subsystem> getRequiredSubsystems();

}
