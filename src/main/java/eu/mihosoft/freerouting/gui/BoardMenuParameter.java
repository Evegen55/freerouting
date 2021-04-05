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
 * BoardWindowsMenu.java
 *
 * Created on 12. Februar 2005, 06:08
 */

package eu.mihosoft.freerouting.gui;

import javax.swing.*;

import static java.util.ResourceBundle.getBundle;

/**
 * Creates the parameter menu of a board frame.
 *
 * @author Alfons Wirtz
 */
public class BoardMenuParameter extends JMenu {

    private final BoardFrame boardFrame;
    private final java.util.ResourceBundle resources;

    private BoardMenuParameter(BoardFrame boardFrame) {
        this.boardFrame = boardFrame;
        resources = getBundle("eu.mihosoft.freerouting.gui.BoardMenuParameter", boardFrame.get_locale());
    }


    /**
     * Returns a new windows menu for the board frame.
     */
    public static BoardMenuParameter getInstance(BoardFrame boardFrame) {
        final BoardMenuParameter parameterMenu = new BoardMenuParameter(boardFrame);

        parameterMenu.setText(parameterMenu.resources.getString("parameter"));

        final JMenuItem selectwindow = new JMenuItem();
        selectwindow.setText(parameterMenu.resources.getString("select"));
        selectwindow.addActionListener(evt -> parameterMenu.boardFrame.getSelectParameterWindow().setVisible(true));
        parameterMenu.add(selectwindow);

        final JMenuItem routewindow = new JMenuItem();
        routewindow.setText(parameterMenu.resources.getString("route"));
        routewindow.addActionListener(evt -> parameterMenu.boardFrame.getRouteParameterWindow().setVisible(true));
        parameterMenu.add(routewindow);

        final JMenuItem autoroutewindow = new JMenuItem();
        autoroutewindow.setText(parameterMenu.resources.getString("autoroute"));
        autoroutewindow.addActionListener(evt -> parameterMenu.boardFrame.getAutorouteParameterWindow().setVisible(true));
        parameterMenu.add(autoroutewindow);

        final JMenuItem movewindow = new JMenuItem();
        movewindow.setText(parameterMenu.resources.getString("move"));
        movewindow.addActionListener(evt -> parameterMenu.boardFrame.getMoveParameterWindow().setVisible(true));
        parameterMenu.add(movewindow);

        return parameterMenu;
    }

}
