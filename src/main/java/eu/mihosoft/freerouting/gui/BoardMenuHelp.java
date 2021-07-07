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
 * BoardMenuHelp.java
 *
 * Created on 19. Oktober 2005, 08:15
 *
 */

package eu.mihosoft.freerouting.gui;

import eu.mihosoft.freerouting.logger.FRLogger;

import javax.help.CSH;
import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.swing.*;
import java.net.URL;
import java.util.Locale;

/**
 * @author Alfons Wirtz
 */
public class BoardMenuHelp extends BoardMenuHelpReduced {
    private static CSH.DisplayHelpFromSource contents_help = null;
    private static CSH.DisplayHelpAfterTracking direct_help = null;

    /**
     * Creates a new instance of BoardMenuHelp Separated from BoardMenuHelpReduced to avoid ClassNotFound exception when
     * the library jh.jar is not found, which is only used in this extended class.
     */
    public BoardMenuHelp(BoardFrame boardFrame) {
        super(boardFrame);
        initializeHelp(boardFrame.get_locale());

        final JMenuItem directHelpWindow = new JMenuItem();
        directHelpWindow.setText(resources.getString("direct_help"));
        if (direct_help != null) {
            directHelpWindow.addActionListener(direct_help);
        }
        add(directHelpWindow, 0);

        final JMenuItem contentsWindow = new JMenuItem();
        contentsWindow.setText(this.resources.getString("contents"));
        if (contents_help != null) {
            contentsWindow.addActionListener(contents_help);
        }
        this.add(contentsWindow, 0);
    }

    private void initializeHelp(Locale locale) {
        // try to find the helpset and create a HelpBroker object
        if (BoardFrame.helpBroker == null) {
            String language = locale.getLanguage();
            String helpset_name;
            if (language.equalsIgnoreCase("de")) {
                helpset_name = "/eu/mihosoft/freerouting/helpset/de/Help.hs";
            } else {
                helpset_name = "/eu/mihosoft/freerouting/helpset/en/Help.hs";
            }
            try {
                // original author tries to get language specific url
                // via HelpSet utility methods which does not work that well
                // and doesn't really make sense if the language is specified
                // manually
                // TODO find out why previous approach does not work reliably
                URL hsURL = getClass().getResource(helpset_name);
                if (hsURL == null) {
                    FRLogger.warn("HelpSet " + helpset_name + " not found.");
                } else {
                    BoardFrame.helpSet = new HelpSet(null, hsURL);
                }
            } catch (HelpSetException ee) {
                FRLogger.error("HelpSet " + helpset_name + " could not be opened.", ee);
            }
            if (BoardFrame.helpSet != null) {
                BoardFrame.helpBroker = BoardFrame.helpSet.createHelpBroker();
            }
            if (BoardFrame.helpBroker != null) {
                // CSH.DisplayHelpFromSource is a convenience class to display the helpset
                contents_help = new CSH.DisplayHelpFromSource(BoardFrame.helpBroker);
                direct_help = new CSH.DisplayHelpAfterTracking(BoardFrame.helpBroker);
            }
        }
    }


}
