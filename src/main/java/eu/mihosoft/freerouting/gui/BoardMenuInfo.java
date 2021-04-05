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
 * BoardLibraryMenu.java
 *
 * Created on 6. Maerz 2005, 05:37
 */

package eu.mihosoft.freerouting.gui;

import javax.swing.*;

import static java.util.ResourceBundle.getBundle;

/**
 * @author Alfons Wirtz
 */
public class BoardMenuInfo extends JMenu {
    private final BoardFrame boardFrame;
    private final java.util.ResourceBundle resources;

    private BoardMenuInfo(BoardFrame boardFrame) {
        this.boardFrame = boardFrame;
        resources = getBundle("eu.mihosoft.freerouting.gui.BoardMenuInfo", boardFrame.get_locale());
    }

    /**
     * Returns a new info menu for the board frame.
     */
    public static BoardMenuInfo getInstance(BoardFrame boardFrame) {
        final BoardMenuInfo infoMenu = new BoardMenuInfo(boardFrame);

        infoMenu.setText(infoMenu.resources.getString("info"));

        final JMenuItem packageWindow = new JMenuItem();
        packageWindow.setText(infoMenu.resources.getString("library_packages"));
        packageWindow.addActionListener(evt -> infoMenu.boardFrame.getPackagesWindow().setVisible(true));
        infoMenu.add(packageWindow);

        final JMenuItem padstacksWindow = new JMenuItem();
        padstacksWindow.setText(infoMenu.resources.getString("library_padstacks"));
        padstacksWindow.addActionListener(evt -> infoMenu.boardFrame.getPadstacksWindow().setVisible(true));
        infoMenu.add(padstacksWindow);

        final JMenuItem componentsWindow = new JMenuItem();
        componentsWindow.setText(infoMenu.resources.getString("board_components"));
        componentsWindow.addActionListener(evt -> infoMenu.boardFrame.getComponentsWindow().setVisible(true));
        infoMenu.add(componentsWindow);

        final JMenuItem incompletesWindow = new JMenuItem();
        incompletesWindow.setText(infoMenu.resources.getString("incompletes"));
        incompletesWindow.addActionListener(evt -> infoMenu.boardFrame.getIncompletesWindow().setVisible(true));
        infoMenu.add(incompletesWindow);


        final JMenuItem lengthViolationsWindow = new JMenuItem();
        lengthViolationsWindow.setText(infoMenu.resources.getString("length_violations"));
        lengthViolationsWindow.addActionListener(evt -> infoMenu.boardFrame.getLengthViolationsWindow().setVisible(true));
        infoMenu.add(lengthViolationsWindow);

        final JMenuItem clearanceViolationsWindow = new JMenuItem();
        clearanceViolationsWindow.setText(infoMenu.resources.getString("clearance_violations"));
        clearanceViolationsWindow.addActionListener(evt -> infoMenu.boardFrame.getClearanceViolationsWindow().setVisible(true));
        infoMenu.add(clearanceViolationsWindow);

        final JMenuItem unconnnectedRouteWindow = new JMenuItem();
        unconnnectedRouteWindow.setText(infoMenu.resources.getString("unconnected_route"));
        unconnnectedRouteWindow.addActionListener(evt -> infoMenu.boardFrame.getUnconnectedRouteWindow().setVisible(true));
        infoMenu.add(unconnnectedRouteWindow);

        final JMenuItem routeStubsWindow = new JMenuItem();
        routeStubsWindow.setText(infoMenu.resources.getString("route_stubs"));
        routeStubsWindow.addActionListener(evt -> infoMenu.boardFrame.getRouteStubsWindow().setVisible(true));
        infoMenu.add(routeStubsWindow);

        return infoMenu;
    }

}
