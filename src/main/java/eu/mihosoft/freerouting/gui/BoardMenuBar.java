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
 * BoardMenuBar.java
 *
 * Created on 11. Februar 2005, 10:17
 */

package eu.mihosoft.freerouting.gui;

import javax.swing.*;

/**
 * Creates the menu bar of a board frame together with its menu items.
 *
 * @author Alfons Wirtz
 */
class BoardMenuBar extends JMenuBar {

    private final BoardMenuFile fileMenu;

    private BoardMenuBar(
            final BoardFrame boardFrame,
            final boolean sessionFileOption
    ) {
        fileMenu = BoardMenuFile.getInstance(boardFrame, sessionFileOption);
        add(fileMenu);
    }

    /**
     * Creates a new BoardMenuBar together with its menus
     */
    static BoardMenuBar getInstance(
            final BoardFrame boardFrame,
            final boolean helpSystemUsed,
            final boolean sessionFileOption
    ) {
        final BoardMenuBar menuBar = new BoardMenuBar(boardFrame, sessionFileOption);
        final JMenu displayMenu = BoardMenuDisplay.getInstance(boardFrame);
        menuBar.add(displayMenu);
        final JMenu parameterMenu = BoardMenuParameter.getInstance(boardFrame);
        menuBar.add(parameterMenu);
        final JMenu rulesMenu = BoardMenuRules.getInstance(boardFrame);
        menuBar.add(rulesMenu);
        final JMenu infoMenu = BoardMenuInfo.getInstance(boardFrame);
        menuBar.add(infoMenu);
        final JMenu otherMenu = BoardMenuOther.getInstance(boardFrame);
        menuBar.add(otherMenu);
        final JMenu helpMenu;
        if (helpSystemUsed) {
            helpMenu = new BoardMenuHelp(boardFrame);
        } else {
            helpMenu = new BoardMenuHelpReduced(boardFrame);
        }
        menuBar.add(helpMenu);
        return menuBar;
    }

    void addDesignDependentItems() {
        fileMenu.addDesignDependentItems();
    }

}
