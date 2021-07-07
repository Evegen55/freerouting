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
 * BoardMenuHelpReduced.java
 *
 * Created on 21. Oktober 2005, 09:06
 *
 */

package eu.mihosoft.freerouting.gui;

import javax.swing.*;
import java.util.ResourceBundle;

import static java.util.ResourceBundle.getBundle;

/**
 * @author Alfons Wirtz
 */
public class BoardMenuHelpReduced extends JMenu {

    protected final BoardFrame boardFrame;
    protected final ResourceBundle resources;

    /**
     * Separated from BoardMenuHelp to avoid ClassNotFound exception when the library jh.jar is not found, which is only
     * used in the  extended help menu.
     */
    public BoardMenuHelpReduced(BoardFrame boardFrame) {
        this.boardFrame = boardFrame;
        this.resources = getBundle("eu.mihosoft.freerouting.gui.BoardMenuHelp", boardFrame.get_locale());
        this.setText(resources.getString("help"));

        final JMenuItem about_window = new JMenuItem();
        about_window.setText(resources.getString("about"));
        about_window.addActionListener((java.awt.event.ActionEvent evt) -> boardFrame.getWindowAbout().setVisible(true));
        add(about_window);
    }

}
