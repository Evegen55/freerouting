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
 * BoardRulesMenu.java
 *
 * Created on 20. Februar 2005, 06:00
 */

package eu.mihosoft.freerouting.gui;

import javax.swing.*;

import static java.util.ResourceBundle.getBundle;

/**
 * Creates the rules menu of a board frame.
 *
 * @author Alfons Wirtz
 */
public class BoardMenuRules extends JMenu {

    private final BoardFrame boardFrame;
    private final java.util.ResourceBundle resources;

    private BoardMenuRules(BoardFrame boardFrame) {
        this.boardFrame = boardFrame;
        resources = getBundle("eu.mihosoft.freerouting.gui.BoardMenuRules", boardFrame.get_locale());
    }

    /**
     * Returns a new windows menu for the board frame.
     */
    public static BoardMenuRules getInstance(BoardFrame boardFrame) {
        final BoardMenuRules rulesMenu = new BoardMenuRules(boardFrame);

        rulesMenu.setText(rulesMenu.resources.getString("rules"));

        final JMenuItem clearanceWindow = new JMenuItem();
        clearanceWindow.setText(rulesMenu.resources.getString("clearance_matrix"));
        clearanceWindow.addActionListener(evt -> rulesMenu.boardFrame.getClearanceMatrixWindow().setVisible(true));
        rulesMenu.add(clearanceWindow);

        final JMenuItem viaWindow = new JMenuItem();
        viaWindow.setText(rulesMenu.resources.getString("vias"));
        viaWindow.addActionListener(evt -> rulesMenu.boardFrame.getViaWindow().setVisible(true));
        rulesMenu.add(viaWindow);

        final JMenuItem netsWindow = new JMenuItem();
        netsWindow.setText(rulesMenu.resources.getString("nets"));
        netsWindow.addActionListener(evt -> rulesMenu.boardFrame.getNetInfoWindow().setVisible(true));

        rulesMenu.add(netsWindow);

        final JMenuItem netClassWindow = new JMenuItem();
        netClassWindow.setText(rulesMenu.resources.getString("net_classes"));
        netClassWindow.addActionListener(evt -> rulesMenu.boardFrame.getEditNetRulesWindow().setVisible(true));
        rulesMenu.add(netClassWindow);

        return rulesMenu;
    }

}
