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
 * BoardMenuOther.java
 *
 * Created on 19. Oktober 2005, 08:34
 *
 */

package eu.mihosoft.freerouting.gui;

import javax.swing.*;

import static java.util.ResourceBundle.getBundle;

/**
 * @author Alfons Wirtz
 */
public class BoardMenuOther extends JMenu {

    private final BoardFrame boardFrame;
    private final java.util.ResourceBundle resources;

    private BoardMenuOther(BoardFrame boardFrame) {
        this.boardFrame = boardFrame;
        resources = getBundle("eu.mihosoft.freerouting.gui.BoardMenuOther", boardFrame.get_locale());
    }

    /**
     * Returns a new other menu for the board frame.
     */
    public static BoardMenuOther getInstance(BoardFrame boardFrame) {
        final BoardMenuOther otherMenu = new BoardMenuOther(boardFrame);

        otherMenu.setText(otherMenu.resources.getString("other"));

        final JMenuItem snapshots = new JMenuItem();
        snapshots.setText(otherMenu.resources.getString("snapshots"));
        snapshots.setToolTipText(otherMenu.resources.getString("snapshots_tooltip"));
        snapshots.addActionListener(evt -> otherMenu.boardFrame.getSnapshotWindow().setVisible(true));

        otherMenu.add(snapshots);
        return otherMenu;
    }

}
