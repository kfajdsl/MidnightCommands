package org.black.commands;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Singleton to schedule commands to be run
 * @author kfajdsl
 * @version 0.1, 1/13/2020
 * TODO: get threads working
 */
public class Scheduler {
    private static final Scheduler ourInstance = new Scheduler();

    public static synchronized Scheduler getInstance() {
        return ourInstance;
    }

    private Scheduler() {
        m_enabled = false;
    }

    private boolean m_enabled;

    // Maps subsystems with their default commands. Doesn't change after opmode init.
    private Map<Subsystem, Command> m_subsystems = new LinkedHashMap<>();

    // Maps commands that are queued for scheduling with whether or not they are interruptible
    private Map<Command, Boolean> m_toSchedule = new LinkedHashMap<>();

    // Maps currently running commands with whether or not they are interruptible
    private Map<Command, Boolean> m_currCommands = new LinkedHashMap<>();

    // Maps currently used subsystems with their required commands.
    private Map<Subsystem, Command> m_inUse = new LinkedHashMap<>();

    /**
     * Initializes subsystems. All subsystems must be registered before this is ran.
     */
    public void init() {
        for (Subsystem subsystem : m_subsystems.keySet()) {
            subsystem.init();
        }

        m_enabled = true;
    }

    /**
     * Manages command queue and executes subsystems' .periodic() and commands' .run()
     * Must be placed inside of OpMode loop.
     * Will not do anything until .init() is called and after .disable() is called.
     */
    public void run() {

        if (!m_enabled) {
            return;
        }

        for (Subsystem subsystem : m_subsystems.keySet()) {
            subsystem.periodic();
        }

        for (Command command : m_currCommands.keySet()) {
            if (command.isFinished()) command.stop(false);
            m_currCommands.remove(command);
            for (Subsystem subsystem : command.getRequiredSubsystems()) {
                m_inUse.remove(subsystem);
            }
        }

        // Start commands waiting in queue.
        for (Iterator<Command> itr=m_toSchedule.keySet().iterator(); itr.hasNext();) {
            Command command = itr.next();
            boolean blocked = false;
            for (Subsystem subsystem : command.getRequiredSubsystems()) {
                if (m_inUse.containsKey(subsystem)) {       // is the subsystem in use?
                    Command inUse = m_inUse.get(subsystem);
                    if(m_currCommands.get(inUse)) {         // if so, is the command interruptible?
                        inUse.stop(true);
                        m_currCommands.remove(m_inUse.get(subsystem));
                    } else {
                        blocked = true;
                        break;
                    }
                }
            }
            if (!blocked) {
                m_currCommands.put(command, m_toSchedule.get(command));
                for (Subsystem subsystem : command.getRequiredSubsystems()) {
                    m_inUse.put(subsystem, command);
                }
                m_toSchedule.remove(command);

                command.init();
            }
        }

        for (Subsystem subsystem : m_subsystems.keySet()) {
            Command defaultCommand = m_subsystems.get(subsystem);
            if (!m_inUse.keySet().contains(subsystem) && defaultCommand != null) {
                addCommand(defaultCommand, true);
            }
        }

        for (Command command : m_currCommands.keySet()) {
            command.run();
        }

    }

    /**
     * Disables the scheduler. After calling this, .run() won't do anything.
     */
    public void disable() {
        m_enabled = false;

        for (Command command : m_currCommands.keySet()) {
            command.stop(false);
        }

        for (Subsystem subsystem : m_subsystems.keySet()) {
            subsystem.stop();
        }
    }


    /**
     * Adds a command to the scheduling queue.
     *
     * @param command       Command to be added
     * @param interruptible Whether the command is interruptible
     */
    public void addCommand(Command command, boolean interruptible) {
        for(Subsystem subsystem : command.getRequiredSubsystems()) {
            if (!m_subsystems.containsKey(subsystem)) {
                throw new IllegalArgumentException("Required subsystem not registered!");
            }
        }

        if (!m_enabled) {
            return;
        }

        m_toSchedule.put(command, interruptible);
    }

    /**
     * Sets the default command for an already registered subsystem.
     * @param subsystem The subsystem
     * @param command   The command to set the default command to
     */
    public void setDefaultCommand(Subsystem subsystem, Command command) {
        if (m_subsystems.keySet().contains(subsystem)) {
            m_subsystems.put(subsystem, command);
        } else {
            throw new IllegalArgumentException("Can't set default command for unregistered subsystem!");
        }
    }

    /**
     * Registers one or more subsystems with the scheduler. Call this before .init().
     * @param subsystems The subsystem(s) to register
     */
    public void registerSubsystem(Subsystem... subsystems) {
        if (!m_enabled) {
            for (Subsystem subsystem : subsystems) {
                m_subsystems.put(subsystem, null);
            }
        } else {
            throw new IllegalStateException("Must register subsystems before init!");
        }
    }

}
