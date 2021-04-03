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
 * WindowLengthViolations.java
 *
 * Created on 1. Juni 2005, 06:52
 *
 */

package eu.mihosoft.freerouting.gui;

import eu.mihosoft.freerouting.rules.Net;
import eu.mihosoft.freerouting.rules.Nets;
import eu.mihosoft.freerouting.rules.NetClass;

import eu.mihosoft.freerouting.interactive.RatsNest;

import java.util.List;

/**
 *
 * @author Alfons Wirtz
 */
public class WindowLengthViolations  extends WindowObjectListWithFilter
{
    
    /** Creates a new instance of WindowLengthViolations */
    public WindowLengthViolations(BoardFrame p_board_frame)
    {
        super(p_board_frame);
        this.resources = java.util.ResourceBundle.getBundle("eu.mihosoft.freerouting.gui.WindowLengthViolations", p_board_frame.get_locale());
        this.setTitle(resources.getString("title"));
        this.list_empty_message.setText(resources.getString("list_empty"));
        p_board_frame.set_context_sensitive_help(this, "WindowObjectList_LengthViolations");
    }
    
    protected void fill_list()
    {
        RatsNest ratsnest = this.board_frame.boardPanel.boardHandling.get_ratsnest();
        Nets net_list = this.board_frame.boardPanel.boardHandling.getRoutingBoard().rules.nets;
        java.util.SortedSet<LengthViolation> length_violations = new java.util.TreeSet<LengthViolation>();
        for (int net_index = 1; net_index <= net_list.max_net_no(); ++net_index)
        {
            double curr_violation_length = ratsnest.get_length_violation(net_index);
            if (curr_violation_length != 0)
            {
                LengthViolation curr_length_violation = new LengthViolation(net_list.get(net_index), curr_violation_length);
                length_violations.add(curr_length_violation);
            }
        }
        
        for (LengthViolation curr_violation : length_violations)
        {
            this.add_to_list(curr_violation);
        }
        this.list.setVisibleRowCount(Math.min(length_violations.size(), DEFAULT_TABLE_SIZE));
    }
    
    protected void select_instances()
    {
        List<Object> selected_violations = list.getSelectedValuesList();
        if (selected_violations.size() <= 0)
        {
            return;
        }
        java.util.Set<eu.mihosoft.freerouting.board.Item> selected_items = new java.util.TreeSet<eu.mihosoft.freerouting.board.Item>();
        for (int i = 0; i < selected_violations.size(); ++i)
        {
            LengthViolation curr_violation = ((LengthViolation) selected_violations.get(i));
            selected_items.addAll(curr_violation.net.get_items());            
        }
        eu.mihosoft.freerouting.interactive.BoardHandling board_handling = board_frame.boardPanel.boardHandling;
        board_handling.select_items(selected_items);
        board_handling.zoom_selection();
    }
    
    private final java.util.ResourceBundle resources;
    
    private class LengthViolation implements Comparable<LengthViolation>
    {
        LengthViolation(Net p_net, double p_violation_length)
        {
            net = p_net;
            violation_length = p_violation_length;
        }
        
        public int compareTo(LengthViolation p_other)
        {
            return this.net.name.compareToIgnoreCase(p_other.net.name);
        }
        
        public String toString()
        {
            eu.mihosoft.freerouting.board.CoordinateTransform coordinate_transform = board_frame.boardPanel.boardHandling.coordinate_transform;
            NetClass net_class = this.net.get_class();
            Float allowed_length;
            String allowed_string;
            if (violation_length > 0)
            {
                allowed_length = (float) coordinate_transform.board_to_user(net_class.get_maximum_trace_length());
                allowed_string = " " + resources.getString("maximum_allowed") + " ";
            }
            else
            {
                allowed_length = (float) coordinate_transform.board_to_user(net_class.get_minimum_trace_length());
                allowed_string = " " + resources.getString("minimum_allowed") + " ";
            }
            Float length = (float) coordinate_transform.board_to_user(this.net.get_trace_length());
            String result = resources.getString("net") + " " + this.net.name + resources.getString("trace_length") 
            + " " + length.toString() + allowed_string + allowed_length;
            return result;
        }
        
        final Net net;
        final double violation_length;
    }
}
