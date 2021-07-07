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
 * SelectedItemPopupMenu.java
 *
 * Created on 17. Februar 2005, 07:47
 */

package eu.mihosoft.freerouting.gui;

/**
 * Popup menu used in the interactive selected item state..
 *
 * @author Alfons Wirtz
 */
class PopupMenuSelectedItems extends PopupMenuDisplay
{
    
    /** Creates a new instance of SelectedItemPopupMenu */
    PopupMenuSelectedItems(BoardFrame p_board_frame)
    {
        super(p_board_frame);
        java.util.ResourceBundle resources = 
                java.util.ResourceBundle.getBundle("eu.mihosoft.freerouting.gui.Default", p_board_frame.get_locale());
        javax.swing.JMenuItem copy_item = new javax.swing.JMenuItem();
        copy_item.setText(resources.getString("copy"));
        copy_item.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                board_panel.boardHandling.copy_selected_items(board_panel.rightButtonClickLocation);
            }
        });
        
        if (board_panel.boardHandling.getRoutingBoard().getTestLevel() != eu.mihosoft.freerouting.board.TestLevel.RELEASE_VERSION)
        {
            this.add(copy_item);
        }
        
        javax.swing.JMenuItem move_item = new javax.swing.JMenuItem();
        move_item.setText(resources.getString("move"));
        move_item.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                board_panel.boardHandling.move_selected_items(board_panel.rightButtonClickLocation);
            }
        });
        
        this.add(move_item, 0);
    }
}
