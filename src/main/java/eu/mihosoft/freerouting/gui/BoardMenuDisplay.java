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
 * BoardDisplayMenu.java
 *
 * Created on 12. February 2005, 05:42
 */

package eu.mihosoft.freerouting.gui;

import javax.swing.*;
import java.util.ResourceBundle;

import static java.util.ResourceBundle.getBundle;

/**
 * Creates the display menu of a board frame.
 *
 * @author Alfons Wirtz
 */
public class BoardMenuDisplay extends JMenu {

    private final BoardFrame boardFrame;
    private final ResourceBundle resources;

    private BoardMenuDisplay(BoardFrame boardFrame) {
        this.boardFrame = boardFrame;
        resources = getBundle("eu.mihosoft.freerouting.gui.BoardMenuDisplay", boardFrame.get_locale());
    }

    /**
     * Returns a new display menu for the board frame.
     */
    public static BoardMenuDisplay getInstance(BoardFrame boardFrame) {
        final BoardMenuDisplay displayMenu = new BoardMenuDisplay(boardFrame);
        displayMenu.setText(displayMenu.resources.getString("display"));

        final JMenuItem itemvisibility = new JMenuItem();
        itemvisibility.setText(displayMenu.resources.getString("object_visibility"));
        itemvisibility.setToolTipText(displayMenu.resources.getString("object_visibility_tooltip"));
        itemvisibility.addActionListener(evt -> displayMenu.boardFrame.getObjectVisibilityWindow().setVisible(true));

        displayMenu.add(itemvisibility);

        final JMenuItem layervisibility = new javax.swing.JMenuItem();
        layervisibility.setText(displayMenu.resources.getString("layer_visibility"));
        layervisibility.setToolTipText(displayMenu.resources.getString("layer_visibility_tooltip"));
        layervisibility.addActionListener(evt -> displayMenu.boardFrame.getLayerVisibilityWindow().setVisible(true));

        displayMenu.add(layervisibility);

        final JMenuItem colors = new JMenuItem();
        colors.setText(displayMenu.resources.getString("colors"));
        colors.setToolTipText(displayMenu.resources.getString("colors_tooltip"));
        colors.addActionListener(evt -> displayMenu.boardFrame.getColorManager().setVisible(true));

        displayMenu.add(colors);

        final JMenuItem miscellaneous = new JMenuItem();
        miscellaneous.setText(displayMenu.resources.getString("miscellaneous"));
        miscellaneous.addActionListener(evt -> displayMenu.boardFrame.getDisplayMiscWindow().setVisible(true));

        displayMenu.add(miscellaneous);
        return displayMenu;
    }

}
