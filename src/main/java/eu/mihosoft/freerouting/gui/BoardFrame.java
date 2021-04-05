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
 */

package eu.mihosoft.freerouting.gui;

import eu.mihosoft.freerouting.board.BoardObserverAdaptor;
import eu.mihosoft.freerouting.board.BoardObservers;
import eu.mihosoft.freerouting.board.ItemIdNoGenerator;
import eu.mihosoft.freerouting.board.TestLevel;
import eu.mihosoft.freerouting.datastructures.FileFilter;
import eu.mihosoft.freerouting.datastructures.IdNoGenerator;
import eu.mihosoft.freerouting.designforms.specctra.DsnFile;
import eu.mihosoft.freerouting.interactive.ScreenMessages;
import eu.mihosoft.freerouting.logger.FRLogger;

import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.AccessControlException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;
import java.util.ResourceBundle;

import static java.util.ResourceBundle.getBundle;

/**
 * Graphical frame of for interactive editing of a routing board.
 *
 * @author Alfons Wirtz
 */

public class BoardFrame extends JFrame {

    private final DesignFile designFile;
    /**
     * The scroll pane for the panel of the routing board.
     */
    final JScrollPane scrollPane;

    /**
     * The menubar of this frame
     */
    private final BoardMenuBar menubar;

    /**
     * The panel with the graphical representation of the board.
     */
    final BoardPanel boardPanel;

    /**
     * The panel with the toolbars
     */
    private final BoardToolbar toolbarPanel;

    /**
     * The toolbar used in the selected item state.
     */
    private final javax.swing.JToolBar selectToolbar;

    final ScreenMessages screenMessages;

    private final TestLevel testLevel;

    private final boolean helpSystemUsed;
    static HelpSet helpSet = null;
    static HelpBroker helpBroker = null;

    private final boolean confirmCancel;

    private final ResourceBundle resources;
    private final Locale locale;

    private final BoardObservers boardObservers;
    private final IdNoGenerator itemIdNoGenerator;

    WindowAbout windowAbout = null;
    WindowRouteParameter routeParameterWindow = null;
    WindowAutorouteParameter autorouteParameterWindow = null;
    WindowSelectParameter selectParameterWindow = null;
    WindowMoveParameter moveParameterWindow = null;
    WindowClearanceMatrix clearanceMatrixWindow = null;
    WindowVia viaWindow = null;
    WindowEditVias editViasWindow = null;
    WindowNetClasses editNetRulesWindow = null;
    WindowAssignNetClass assignNetClassesWindow = null;
    WindowPadstacks padstacksWindow = null;
    WindowPackages packagesWindow = null;
    WindowIncompletes incompletesWindow = null;
    WindowNets netInfoWindow = null;
    WindowClearanceViolations clearanceViolationsWindow = null;
    WindowLengthViolations lengthViolationsWindow = null;
    WindowUnconnectedRoute unconnectedRouteWindow = null;
    WindowRouteStubs routeStubsWindow = null;
    WindowComponents componentsWindow = null;
    WindowLayerVisibility layerVisibilityWindow = null;
    WindowObjectVisibility objectVisibilityWindow = null;
    WindowDisplayMisc displayMiscWindow = null;
    WindowSnapshot snapshotWindow = null;
    ColorManager colorManager = null;
    BoardSavableSubWindow[] permanentSubwindows = new BoardSavableSubWindow[24];//The windows above stored in an array

    Collection<BoardTemporarySubWindow> temporarySubWindows = new LinkedList<>();

    public static final String[] LOG_FILE_EXTENSIONS = {"log"};
    public static final FileFilter LOGFILE_FILTER = new FileFilter(LOG_FILE_EXTENSIONS);

    public static final String GUI_DEFAULTS_FILE_NAME = "gui_defaults.par";
    public static final String GUI_DEFAULTS_FILE_BACKUP_NAME = "gui_defaults.par.bak";

    public enum Option {
        FROM_START_MENU,
        SINGLE_FRAME,
        SESSION_FILE,
        EXTENDED_TOOL_BAR
    }

    /**
     * Creates a new board frame with the input design file embedded into a host cad software.
     */
    public static BoardFrame get_embedded_instance(
            String p_design_file_path_name,
            BoardObservers p_observers,
            IdNoGenerator p_id_no_generator,
            Locale p_locale
    ) {
        final DesignFile design_file = DesignFile.getInstance(p_design_file_path_name);
        if (design_file == null) {
            WindowMessage.show("designfile not found");
            return null;
        }
        BoardFrame board_frame = new BoardFrame(
                design_file,
                BoardFrame.Option.SINGLE_FRAME,
                TestLevel.RELEASE_VERSION,
                p_observers,
                p_id_no_generator,
                p_locale,
                false
        );

        if (board_frame == null) {
            WindowMessage.show("board_frame is null");
            return null;
        }
        InputStream inputStream = design_file.getInputStream();
        boolean read_ok = board_frame.read(inputStream, true, null);
        if (!read_ok) {
            String error_message = "Unable to read design file with pathname " + p_design_file_path_name;
            board_frame.setVisible(true); // to be able to display the status message
            board_frame.screenMessages.setStatusMessage(error_message);
        }
        return board_frame;
    }

    /**
     * Creates new form BoardFrame.
     * <p>
     * If option = FROM_START_MENU this frame is created from a start menu frame.
     * <p>
     * If option = SINGLE_FRAME, this frame is created directly a single frame.
     * <p>
     * If option = IN_SAND_BOX, no security sensitive actions like for example choosing
     * <p>
     * Currently Option.EXTENDED_TOOL_BAR is used only if a new board is created by the application from scratch.
     * <p>
     * If testLevel {@literal >} RELEASE_VERSION, functionality not yet ready for release is included. Also the warning
     * output depends on testLevel.
     */
    public BoardFrame(
            DesignFile designFile,
            Option option,
            TestLevel testLevel,
            Locale locale,
            boolean confirmCancel
    ) {
        this(
                designFile,
                option,
                testLevel,
                new BoardObserverAdaptor(),
                new ItemIdNoGenerator(),
                locale,
                confirmCancel
        );
    }

    /**
     * Creates new form BoardFrame.
     * <p>
     * The parameters p_item_observers and itemIdNoGenerator are used for syncronizing purposes, if the frame is
     * embedded into a host system
     */
    private BoardFrame(
            final DesignFile designFile,
            final Option option,
            final TestLevel testLevel,
            final BoardObservers observers,
            final IdNoGenerator itemIdNoGenerator,
            final Locale locale,
            final boolean confirmCancel
    ) {
        this.designFile = designFile;
        this.testLevel = testLevel;
        this.confirmCancel = confirmCancel;
        this.boardObservers = observers;
        this.itemIdNoGenerator = itemIdNoGenerator;
        this.locale = locale;
        this.resources = getBundle("eu.mihosoft.freerouting.gui.BoardFrame", locale);

        BoardMenuBar menuBar;
        boolean sessionFileOption = (option == Option.SESSION_FILE);
        boolean currHelpSystemUsed = true;
        try {
            menuBar = BoardMenuBar.getInstance(this, currHelpSystemUsed, sessionFileOption);
        } catch (java.lang.NoClassDefFoundError e) {
            // the system-file jh.jar may be missing
            currHelpSystemUsed = false;
            menuBar = BoardMenuBar.getInstance(this, false, sessionFileOption);
            FRLogger.warn("Online-Help deactivated because system file jh.jar is missing");
        }
        this.menubar = menuBar;
        this.helpSystemUsed = currHelpSystemUsed;
        setJMenuBar(menubar);

        this.toolbarPanel = new BoardToolbar(this);
        add(toolbarPanel, BorderLayout.NORTH);

        /**
         * The panel with the message line
         */
        final BoardPanelStatus messagePanel = new BoardPanelStatus(this.locale);
        add(messagePanel, BorderLayout.SOUTH);

        this.selectToolbar = new BoardToolbarSelectedItem(this, option == Option.EXTENDED_TOOL_BAR);
        this.screenMessages = new ScreenMessages(
                messagePanel.statusMessage,
                messagePanel.addMessage,
                messagePanel.currentLayer,
                messagePanel.mousePosition,
                this.locale
        );

        this.scrollPane = new JScrollPane();
        this.scrollPane.setPreferredSize(new Dimension(1150, 800));
        this.scrollPane.setVerifyInputWhenFocusTarget(false);
        add(scrollPane, BorderLayout.CENTER);

        this.boardPanel = new BoardPanel(
                screenMessages,
                this,
                locale
        );
        this.scrollPane.setViewportView(boardPanel);

        this.setTitle(resources.getString("title"));
        this.addWindowListener(new WindowStateListener());

        this.pack();
    }

    public DesignFile getDesignFile() {
        return designFile;
    }

    /**
     * Returns the currently used locale for the language dependent output.
     */
    public Locale get_locale() {
        return this.locale;
    }

    /**
     * Sets the background of the board panel
     */
    public void setBoardBackground(Color color) {
        boardPanel.setBackground(color);
    }

    public BoardMenuBar getMenubar() {
        return menubar;
    }

    public BoardPanel getBoardPanel() {
        return boardPanel;
    }

    /**
     * Reads interactive actions from a logfile.
     */
    void readLogFile(final InputStream inputStream) {
        boardPanel.getBoardHandling()
                .readLogfile(inputStream);
    }


    /**
     * Reads an existing board design from file.
     * <p>
     * If isImport, the design is read from a specctra dsn file.
     * <p>
     * Returns false, if the file is invalid.
     */
    boolean read(
            final InputStream inputStream,
            final boolean isImport,
            final JTextField messageField
    ) {
        Point viewportPosition = null;
        DsnFile.ReadResult readResult = null;
        if (isImport) {
            readResult = boardPanel.getBoardHandling()
                    .importDesign(
                            inputStream,
                            boardObservers,
                            itemIdNoGenerator,
                            testLevel
                    );
            if (readResult == DsnFile.ReadResult.OK) {
                viewportPosition = new Point(0, 0);
                initialize_windows();
            }
        } else { //todo - isImport is always true, check case when isImport is false
            try (final ObjectInputStream objectStream = new ObjectInputStream(inputStream)) {
                boolean read_ok = boardPanel.getBoardHandling()
                        .readDesign(objectStream, testLevel);
                if (!read_ok) {
                    return false;
                }
                final Point frameLocation;
                final Rectangle frameBounds;
                try {
                    viewportPosition = (Point) objectStream.readObject();
                    frameLocation = (Point) objectStream.readObject();
                    frameBounds = (Rectangle) objectStream.readObject();
                } catch (Exception e) {
                    return false;
                }
                setLocation(frameLocation);
                setBounds(frameBounds);

                allocatePermanentSubwindows();

                for (final BoardSavableSubWindow permanentSubwindow : permanentSubwindows) {
                    permanentSubwindow.read(objectStream);
                }
            } catch (Exception e) {
                return false;
            }
        }

        return updateGui(isImport, readResult, viewportPosition, messageField);
    }

    private boolean updateGui(
            final boolean isImport,
            final DsnFile.ReadResult readResult,
            final Point viewportPosition,
            final JTextField messageField
    ) {
        if (isImport) {
            if (readResult != DsnFile.ReadResult.OK) {
                if (messageField != null) {
                    if (readResult == DsnFile.ReadResult.OUTLINE_MISSING) {
                        messageField.setText(resources.getString("error_7"));
                    } else {
                        messageField.setText(resources.getString("error_6"));
                    }
                }
                return false;
            }
        }

        final Dimension panelSize = boardPanel.getBoardHandling()
                .getGraphicsContext()
                .getPanelSize();
        boardPanel.setSize(panelSize);
        boardPanel.setPreferredSize(panelSize);
        if (viewportPosition != null) {
            boardPanel.setViewportPosition(viewportPosition);
        }
        boardPanel.createPopupMenus();
        boardPanel.initColors();
        boardPanel.getBoardHandling()
                .createRatsnest();
        hilightSelectedButton();
        toolbarPanel.unitFactorField
                .setValue(boardPanel.getBoardHandling()
                                  .getCoordinateTransform()
                                  .userUnitFactor
                );
        toolbarPanel.unitComboBox
                .setSelectedItem(boardPanel.getBoardHandling()
                                         .getCoordinateTransform()
                                         .userUnit
                );
        setVisible(true);
        if (isImport) {
            // Read the default gui settings, if gui default file exists.
            File defaultsFile = new File(designFile.getParent(), GUI_DEFAULTS_FILE_NAME);
            try (InputStream inputStream = new FileInputStream(defaultsFile);) {
                boolean read_ok = GUIDefaultsFile.read(this, boardPanel.getBoardHandling(), inputStream);
                if (!read_ok) {
                    screenMessages.setStatusMessage(resources.getString("error_1"));
                }
            } catch (IOException e) {
                FRLogger.error("Can't read gui settings " + GUI_DEFAULTS_FILE_NAME, e);
            }
            zoomAll();
        }
        return true;
    }


    /**
     * Saves the interactive settings and the design file to disk. Returns false, if the save failed.
     */
    boolean save() {
        if (designFile == null) {
            return false;
        }
        FRLogger.info("Saving '" + designFile.getOutputFile().getName() + "'...");

        try (OutputStream outputStream = new FileOutputStream(designFile.getOutputFile());
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
            boolean save_ok = boardPanel.getBoardHandling()
                    .saveDesignFile(objectOutputStream);
            if (!save_ok) {
                return false;
            }

            objectOutputStream.writeObject(boardPanel.getViewportPosition());
            objectOutputStream.writeObject(getLocation());
            objectOutputStream.writeObject(getBounds());

            for (final BoardSavableSubWindow permanentSubwindow : permanentSubwindows) {
                permanentSubwindow.save(objectOutputStream);
            }
        } catch (IOException e) {
            screenMessages.setStatusMessage(resources.getString("error_2"));
            return false;
        } catch (AccessControlException e) {
            screenMessages.setStatusMessage(resources.getString("error_3"));
            return false;
        }
        return true;
    }

    /**
     * Sets contexts sensitive help for the input component, if the help system is used.
     */
    public void setContextSensitiveHelp(final Component component, final String helpId) {
        if (component == null) {
            throw new NullPointerException("component");
        }

        if (helpSystemUsed) {
            Component currComponent;
            if (component instanceof JFrame) {
                currComponent = ((JFrame) component).getRootPane();
            } else {
                currComponent = component;
            }
            String helpId_1 = "html_files." + helpId;
            CSH.setHelpIDString(currComponent, helpId_1);
            if (helpBroker == null) {
                FRLogger.warn("help_broker is null");
                return;
            }
            helpBroker.enableHelpKey(currComponent, helpId_1, helpSet);
        }
    }

    /**
     * Sets the toolbar to the buttons of the selected item state.
     */
    public void setSelectToolbar() {
        getContentPane().remove(toolbarPanel);
        getContentPane().add(selectToolbar, BorderLayout.NORTH);
        repaint();
    }

    /**
     * Sets the toolbar buttons to the select. route and drag menu buttons of the main menu.
     */
    public void setMenuToolbar() {
        getContentPane().remove(selectToolbar);
        getContentPane().add(toolbarPanel, BorderLayout.NORTH);
        repaint();
    }

    /**
     * Calculates the absolute location of the board frame in his outmost parent frame.
     */
    Point absolutePanelLocation() {
        int x = scrollPane.getX();
        int y = scrollPane.getY();
        Container currParent = scrollPane.getParent();
        while (currParent != null) {
            x += currParent.getX();
            y += currParent.getY();
            currParent = currParent.getParent();
        }
        return new Point(x, y);
    }

    /**
     * Sets the displayed region to the whole board.
     */
    public void zoomAll() {
        boardPanel.getBoardHandling().adjustDesignBounds();
        final Rectangle display_rect = boardPanel.getViewportBounds();
        final Rectangle design_bounds = boardPanel.getBoardHandling()
                .getGraphicsContext()
                .getDesignBounds();
        double width_factor = display_rect.getWidth() / design_bounds.getWidth();
        double height_factor = display_rect.getHeight() / design_bounds.getHeight();
        double zoom_factor = Math.min(width_factor, height_factor);
        Point2D zoom_center = boardPanel.getBoardHandling()
                .getGraphicsContext()
                .getDesignCenter();
        boardPanel.zoom(zoom_factor, zoom_center);
        Point2D new_vieport_center = boardPanel.getBoardHandling()
                .getGraphicsContext()
                .getDesignCenter();
        boardPanel.setViewportCenter(new_vieport_center);
    }

    /**
     * Actions to be taken when this frame vanishes.
     */
    public void dispose() {
        for (int i = 0; i < permanentSubwindows.length; ++i) {
            if (permanentSubwindows[i] != null) {
                permanentSubwindows[i].dispose();
                permanentSubwindows[i] = null;
            }
        }
        for (BoardTemporarySubWindow subWindow : this.temporarySubWindows) {
            if (subWindow != null) {
                subWindow.boardFrameDisposed();
            }
        }
        if (boardPanel.getBoardHandling() != null) {
            boardPanel.getBoardHandling()
                    .dispose();
            boardPanel.setBoardHandling(null);
        }
        super.dispose();
    }

    private void allocatePermanentSubwindows() {
        this.colorManager = new ColorManager(this);
        this.permanentSubwindows[0] = this.colorManager;
        this.objectVisibilityWindow = WindowObjectVisibility.get_instance(this);
        this.permanentSubwindows[1] = this.objectVisibilityWindow;
        this.layerVisibilityWindow = WindowLayerVisibility.get_instance(this);
        this.permanentSubwindows[2] = this.layerVisibilityWindow;
        this.displayMiscWindow = new WindowDisplayMisc(this);
        this.permanentSubwindows[3] = this.displayMiscWindow;
        this.snapshotWindow = new WindowSnapshot(this);
        this.permanentSubwindows[4] = this.snapshotWindow;
        this.routeParameterWindow = new WindowRouteParameter(this);
        this.permanentSubwindows[5] = this.routeParameterWindow;
        this.selectParameterWindow = new WindowSelectParameter(this);
        this.permanentSubwindows[6] = this.selectParameterWindow;
        this.clearanceMatrixWindow = new WindowClearanceMatrix(this);
        this.permanentSubwindows[7] = this.clearanceMatrixWindow;
        this.padstacksWindow = new WindowPadstacks(this);
        this.permanentSubwindows[8] = this.padstacksWindow;
        this.packagesWindow = new WindowPackages(this);
        this.permanentSubwindows[9] = this.packagesWindow;
        this.componentsWindow = new WindowComponents(this);
        this.permanentSubwindows[10] = this.componentsWindow;
        this.incompletesWindow = new WindowIncompletes(this);
        this.permanentSubwindows[11] = this.incompletesWindow;
        this.clearanceViolationsWindow = new WindowClearanceViolations(this);
        this.permanentSubwindows[12] = this.clearanceViolationsWindow;
        this.netInfoWindow = new WindowNets(this);
        this.permanentSubwindows[13] = this.netInfoWindow;
        this.viaWindow = new WindowVia(this);
        this.permanentSubwindows[14] = this.viaWindow;
        this.editViasWindow = new WindowEditVias(this);
        this.permanentSubwindows[15] = this.editViasWindow;
        this.editNetRulesWindow = new WindowNetClasses(this);
        this.permanentSubwindows[16] = this.editNetRulesWindow;
        this.assignNetClassesWindow = new WindowAssignNetClass(this);
        this.permanentSubwindows[17] = this.assignNetClassesWindow;
        this.lengthViolationsWindow = new WindowLengthViolations(this);
        this.permanentSubwindows[18] = this.lengthViolationsWindow;
        this.windowAbout = new WindowAbout(this.locale);
        this.permanentSubwindows[19] = this.windowAbout;
        this.moveParameterWindow = new WindowMoveParameter(this);
        this.permanentSubwindows[20] = this.moveParameterWindow;
        this.unconnectedRouteWindow = new WindowUnconnectedRoute(this);
        this.permanentSubwindows[21] = this.unconnectedRouteWindow;
        this.routeStubsWindow = new WindowRouteStubs(this);
        this.permanentSubwindows[22] = this.routeStubsWindow;
        this.autorouteParameterWindow = new WindowAutorouteParameter(this);
        this.permanentSubwindows[23] = this.autorouteParameterWindow;
    }

    /**
     * Creates the additional frames of the board frame.
     */
    private void initialize_windows() {
        allocatePermanentSubwindows();
        setLocation(120, 0);
        selectParameterWindow.setLocation(0, 0);
        selectParameterWindow.setVisible(true);
        routeParameterWindow.setLocation(0, 100);
        autorouteParameterWindow.setLocation(0, 200);
        moveParameterWindow.setLocation(0, 50);
        clearanceMatrixWindow.setLocation(0, 150);
        viaWindow.setLocation(50, 150);
        editViasWindow.setLocation(100, 150);
        editNetRulesWindow.setLocation(100, 200);
        assignNetClassesWindow.setLocation(100, 250);
        padstacksWindow.setLocation(100, 30);
        packagesWindow.setLocation(200, 30);
        componentsWindow.setLocation(300, 30);
        incompletesWindow.setLocation(400, 30);
        clearanceViolationsWindow.setLocation(500, 30);
        lengthViolationsWindow.setLocation(550, 30);
        netInfoWindow.setLocation(350, 30);
        unconnectedRouteWindow.setLocation(650, 30);
        routeStubsWindow.setLocation(600, 30);
        snapshotWindow.setLocation(0, 250);
        layerVisibilityWindow.setLocation(0, 450);
        objectVisibilityWindow.setLocation(0, 550);
        displayMiscWindow.setLocation(0, 350);
        colorManager.setLocation(0, 600);
        windowAbout.setLocation(200, 200);
    }


    /**
     * Refreshs all displayed coordinates after the user unit has changed.
     */
    public void refreshWindows() {
        for (final BoardSavableSubWindow permanentSubwindow : permanentSubwindows) {
            if (permanentSubwindow != null) {
                permanentSubwindow.refresh();
            }
        }
    }

    /**
     * Sets the selected button in the menu button button group
     */
    public void hilightSelectedButton() {
        toolbarPanel.hilightSelectedButton();
    }

    /**
     * Restore the selected snapshot in the snapshot window.
     */
    public void gotoSelectedSnapshot() {
        if (snapshotWindow != null) {
            snapshotWindow.gotoSelected();
        }
    }

    /**
     * Selects  the snapshot, which is previous to the current selected snapshot. Thecurent selected snapshot will be no
     * more selected.
     */
    public void selectPreviousSnapshot() {
        if (snapshotWindow != null) {
            snapshotWindow.selectPreviousItem();
        }
    }

    /**
     * Selects  the snapshot, which is next to the current selected snapshot. The current selected snapshot will be no
     * more selected.
     */
    public void selectNextSnapshot() {
        if (snapshotWindow != null) {
            snapshotWindow.select_next_item();
        }
    }

    /**
     * Used for storing the subwindowfilters in a snapshot.
     */
    public SubwindowSelections getSnapshotSubwindowSelections() {
        SubwindowSelections result = new SubwindowSelections();
        result.incompletes_selection = incompletesWindow.getSnapshotInfo();
        result.packages_selection = packagesWindow.getSnapshotInfo();
        result.nets_selection = netInfoWindow.getSnapshotInfo();
        result.components_selection = componentsWindow.getSnapshotInfo();
        result.padstacks_selection = padstacksWindow.getSnapshotInfo();
        return result;
    }

    /**
     * Used for restoring the subwindowfilters from a snapshot.
     */
    public void setSnapshotSubwindowSelections(SubwindowSelections filters) {
        incompletesWindow.setSnapshotInfo(filters.incompletes_selection);
        packagesWindow.setSnapshotInfo(filters.packages_selection);
        netInfoWindow.setSnapshotInfo(filters.nets_selection);
        componentsWindow.setSnapshotInfo(filters.components_selection);
        padstacksWindow.setSnapshotInfo(filters.padstacks_selection);
    }

    /**
     * Repaints this board frame and all the subwindows of the board.
     */
    public void repaintAll() {
        repaint();
        for (final BoardSavableSubWindow permanentSubwindow : permanentSubwindows) {
            permanentSubwindow.repaint();
        }
    }


    private class WindowStateListener extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent evt) {
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            if (confirmCancel) {
                int option = JOptionPane.showConfirmDialog(
                        null,
                        resources.getString("confirm_cancel"),
                        null,
                        JOptionPane.YES_NO_OPTION
                );
                if (option == JOptionPane.NO_OPTION) {
                    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
                }
            }
        }

        public void windowIconified(WindowEvent evt) {
            for (final BoardSavableSubWindow permanentSubwindow : permanentSubwindows) {
                permanentSubwindow.parentIconified();
            }
            for (BoardSubWindow subWindow : temporarySubWindows) {
                if (subWindow != null) {
                    subWindow.parentIconified();
                }
            }
        }

        public void windowDeiconified(WindowEvent evt) {
            for (final BoardSavableSubWindow permanentSubwindow : permanentSubwindows) {
                if (permanentSubwindow != null) {
                    permanentSubwindow.parentDeiconified();
                }
            }
            for (final BoardSubWindow subWindow : temporarySubWindows) {
                if (subWindow != null) {
                    subWindow.parentDeiconified();
                }
            }
        }
    }

    /**
     * Used for storing the subwindow filters in a snapshot.
     */
    public static class SubwindowSelections implements Serializable {
        private WindowObjectListWithFilter.SnapshotInfo incompletes_selection;
        private WindowObjectListWithFilter.SnapshotInfo packages_selection;
        private WindowObjectListWithFilter.SnapshotInfo nets_selection;
        private WindowObjectListWithFilter.SnapshotInfo components_selection;
        private WindowObjectListWithFilter.SnapshotInfo padstacks_selection;
    }
}

