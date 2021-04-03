/*
 *   Copyright (C) 2014  Alfons Wirtz
 *   website www.freerouting.net
 *
 *   Copyright (C) 2017 Michael Hoffer <info@michaelhoffer.de>
 *   Website www.freerouting.mihosoft.eu
*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License at <http://www.gnu.org/licenses/> 
 *   for more details.
 *
 * InteractiveActionThread.java
 *
 * Created on 2. Maerz 2006, 07:23
 *
 */
package eu.mihosoft.freerouting.interactive;

import eu.mihosoft.freerouting.logger.FRLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for running an interactive action in a separate thread,
 * that can be stopped by the user.
 *
 * @author Alfons Wirtz
 */
public abstract class InteractiveActionThread extends Thread implements eu.mihosoft.freerouting.datastructures.Stoppable
{
    protected List<ThreadActionListener> listeners = new ArrayList<ThreadActionListener>();

    public void addListener(ThreadActionListener toAdd) {
        listeners.add(toAdd);
    }

    public static InteractiveActionThread get_autoroute_instance(BoardHandling p_board_handling)
    {
        return new AutorouteThread(p_board_handling);
    }

    public static InteractiveActionThread getBatchAutorouterInstance(BoardHandling boardHandling)
    {
        return new BatchAutorouterThread(boardHandling);
    }

    public static InteractiveActionThread get_fanout_instance(BoardHandling p_board_handling)
    {
        return new FanoutThread(p_board_handling);
    }

    public static InteractiveActionThread get_pull_tight_instance(BoardHandling p_board_handling)
    {
        return new PullTightThread(p_board_handling);
    }

    public static InteractiveActionThread get_read_logfile_instance(BoardHandling p_board_handling, java.io.InputStream p_input_stream)
    {
        return new ReadLogfileThread(p_board_handling, p_input_stream);
    }

    /** Creates a new instance of InteractiveActionThread */
    protected InteractiveActionThread(BoardHandling p_board_handling)
    {
        this.hdlg = p_board_handling;
    }

    protected abstract void thread_action();

    public void run()
    {
        thread_action();
        hdlg.repaint();
    }

    public synchronized void request_stop()
    {
        stop_requested = true;
    }

    public synchronized boolean is_stop_requested()
    {
        return stop_requested;
    }

    public synchronized void draw(java.awt.Graphics p_graphics)
    {
    // Can be overwritten in derived classes.
    }
    private boolean stop_requested = false;
    public final BoardHandling hdlg;

    private static class AutorouteThread extends InteractiveActionThread
    {

        private AutorouteThread(BoardHandling p_board_handling)
        {
            super(p_board_handling);
        }

        protected void thread_action()
        {
            if (!(hdlg.interactive_state instanceof SelectedItemState))
            {
                return;
            }
            InteractiveState return_state = ((SelectedItemState) hdlg.interactive_state).autoroute(this);
            hdlg.set_interactive_state(return_state);
        }
    }

    private static class FanoutThread extends InteractiveActionThread
    {

        private FanoutThread(BoardHandling p_board_handling)
        {
            super(p_board_handling);
        }

        protected void thread_action()
        {
            if (!(hdlg.interactive_state instanceof SelectedItemState))
            {
                return;
            }
            InteractiveState return_state = ((SelectedItemState) hdlg.interactive_state).fanout(this);
            hdlg.set_interactive_state(return_state);
        }
    }

    private static class PullTightThread extends InteractiveActionThread
    {

        private PullTightThread(BoardHandling p_board_handling)
        {
            super(p_board_handling);
        }

        protected void thread_action()
        {
            if (!(hdlg.interactive_state instanceof SelectedItemState))
            {
                return;
            }
            InteractiveState return_state = ((SelectedItemState) hdlg.interactive_state).pull_tight(this);
            hdlg.set_interactive_state(return_state);
        }
    }

    private static class ReadLogfileThread extends InteractiveActionThread
    {

        private ReadLogfileThread(BoardHandling p_board_handling, java.io.InputStream p_input_stream)
        {
            super(p_board_handling);
            this.input_stream = p_input_stream;
        }

        protected void thread_action()
        {

            java.util.ResourceBundle resources =
                    java.util.ResourceBundle.getBundle("eu.mihosoft.freerouting.interactive.InteractiveState", hdlg.get_locale());
            boolean saved_board_read_only = hdlg.is_board_read_only();
            hdlg.set_board_read_only(true);
            String start_message = resources.getString("logfile") + " " + resources.getString("stop_message");
            hdlg.screen_messages.setStatusMessage(start_message);
            hdlg.screen_messages.set_write_protected(true);
            boolean done = false;
            InteractiveState previous_state = hdlg.interactive_state;
            if (!hdlg.activityReplayFile.start_read(this.input_stream))
            {
                done = true;
            }
            boolean interrupted = false;
            int debug_counter = 0;
            hdlg.get_panel().boardFrame.refreshWindows();
            hdlg.paint_immediately = true;
            while (!done)
            {
                if (is_stop_requested())
                {
                    interrupted = true;
                    done = true;
                }
                ++debug_counter;
                ActivityReplayFileScope logfile_scope = hdlg.activityReplayFile.start_read_scope();
                if (logfile_scope == null)
                {
                    done = true; // end of logfile
                }
                if (!done)
                {
                    try
                    {
                        InteractiveState new_state =
                                logfile_scope.read_scope(hdlg.activityReplayFile, hdlg.interactive_state, hdlg);
                        if (new_state == null)
                        {
                            FRLogger.warn("BoardHandling:read_logfile: inconsistent logfile scope");
                            new_state = previous_state;
                        }
                        hdlg.repaint();
                        hdlg.set_interactive_state(new_state);
                    } catch (Exception e)
                    {
                        done = true;
                    }

                }
            }
            hdlg.paint_immediately = false;
            try
            {
                this.input_stream.close();
            } catch (java.io.IOException e)
            {
                FRLogger.error("ReadLogfileThread: unable to close input stream", e);
            }
            hdlg.get_panel().boardFrame.refreshWindows();
            hdlg.screen_messages.set_write_protected(false);
            String curr_message;
            if (interrupted)
            {
                curr_message = resources.getString("interrupted");
            }
            else
            {
                curr_message = resources.getString("completed");
            }
            String end_message = resources.getString("logfile") + " " + curr_message;
            hdlg.screen_messages.setStatusMessage(end_message);
            hdlg.set_board_read_only(saved_board_read_only);
            hdlg.get_panel().boardFrame.repaint_all();
        }
        private final java.io.InputStream input_stream;
    }
}

