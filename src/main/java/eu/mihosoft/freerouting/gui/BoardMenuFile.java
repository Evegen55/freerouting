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
 * BoardFileMenu.java
 *
 * Created on 11. Februar 2005, 11:26
 */
package eu.mihosoft.freerouting.gui;

import eu.mihosoft.freerouting.board.BasicBoard;
import eu.mihosoft.freerouting.board.TestLevel;
import eu.mihosoft.freerouting.logger.FRLogger;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static java.util.ResourceBundle.getBundle;

/**
 * Creates the file menu of a board frame.
 *
 * @author Alfons Wirtz
 */
public class BoardMenuFile extends JMenu {

    private final BoardFrame boardFrame;
    private final boolean sessionFileOption;
    private final java.util.ResourceBundle resources;

    private BoardMenuFile(
            final BoardFrame boardFrame,
            final boolean sessionFileOption
    ) {
        this.sessionFileOption = sessionFileOption;
        this.boardFrame = boardFrame;
        resources = getBundle("eu.mihosoft.freerouting.gui.BoardMenuFile", boardFrame.get_locale());
    }

    /**
     * Returns a new file menu for the board frame.
     */
    public static BoardMenuFile getInstance(
            final BoardFrame boardFrame,
            final boolean sessionFileOption
    ) {
        final BoardMenuFile fileMenu = new BoardMenuFile(boardFrame, sessionFileOption);
        fileMenu.setText(fileMenu.resources.getString("file"));

        // Create the menu items.
        if (!sessionFileOption) {
            final JMenuItem saveItem = new JMenuItem();
            saveItem.setText(fileMenu.resources.getString("save"));
            saveItem.setToolTipText(fileMenu.resources.getString("save_tooltip"));
            saveItem.addActionListener(evt -> {
                boolean save_ok = fileMenu.boardFrame.save();
                fileMenu.boardFrame
                        .getBoardPanel()
                        .getBoardHandling()
                        .close_files();
                if (save_ok) {
                    fileMenu.boardFrame
                            .screenMessages
                            .setStatusMessage(fileMenu.resources.getString("save_message"));
                }
            });
            fileMenu.add(saveItem);
        }


        final JMenuItem saveAndExitItem = new JMenuItem();
        saveAndExitItem.setText(fileMenu.resources.getString("save_and_exit"));
        saveAndExitItem.setToolTipText(fileMenu.resources.getString("save_and_exit_tooltip"));
        saveAndExitItem.addActionListener(evt -> {
            if (fileMenu.sessionFileOption) {
                fileMenu.boardFrame
                        .getDesignFile()
                        .writeSpecctraSessionFile(fileMenu.boardFrame);
            } else {
                fileMenu.boardFrame.save();
            }
            fileMenu.boardFrame.dispose();
        });
        fileMenu.add(saveAndExitItem);


        final JMenuItem cancelAndExitItem = new JMenuItem();
        cancelAndExitItem.setText(fileMenu.resources.getString("cancel_and_exit"));
        cancelAndExitItem.setToolTipText(fileMenu.resources.getString("cancel_and_exit_tooltip"));
        cancelAndExitItem.addActionListener(evt -> fileMenu.boardFrame.dispose());
        fileMenu.add(cancelAndExitItem);

        if (!fileMenu.sessionFileOption) {
            final JMenuItem saveAsItem = new JMenuItem();
            saveAsItem.setText(fileMenu.resources.getString("save_as"));
            saveAsItem.setToolTipText(fileMenu.resources.getString("save_as_tooltip"));
            saveAsItem.addActionListener(evt -> fileMenu.saveAsAction());
            fileMenu.add(saveAsItem);

            final JMenuItem write_logfile_item = new javax.swing.JMenuItem();
            write_logfile_item.setText(fileMenu.resources.getString("generate_logfile"));
            write_logfile_item.setToolTipText(fileMenu.resources.getString("generate_logfile_tooltip"));
            write_logfile_item.addActionListener(evt -> fileMenu.writeLogfileAction());
            fileMenu.add(write_logfile_item);

            final JMenuItem replayLogfileItem = new JMenuItem();
            replayLogfileItem.setText(fileMenu.resources.getString("replay_logfile"));
            replayLogfileItem.setToolTipText(fileMenu.resources.getString("replay_logfile_tooltip"));
            replayLogfileItem.addActionListener(evt -> fileMenu.readLogfileAction());
            fileMenu.add(replayLogfileItem);

        }

        fileMenu.addSaveSettingsItem();

        return fileMenu;
    }

    public void addDesignDependentItems() {
        if (this.sessionFileOption) {
            return;
        }
        final BasicBoard routingBoard = this.boardFrame.getBoardPanel().getBoardHandling().getRoutingBoard();
        boolean hostCadIsEagle = routingBoard.communication.hostCadIsEagle();

        final JMenuItem writeSessionFileItem = new JMenuItem();
        writeSessionFileItem.setText(resources.getString("session_file"));
        writeSessionFileItem.setToolTipText(resources.getString("session_file_tooltip"));
        writeSessionFileItem.addActionListener(evt -> boardFrame.getDesignFile().writeSpecctraSessionFile(boardFrame));

        if ((routingBoard.getTestLevel() != TestLevel.RELEASE_VERSION || !hostCadIsEagle)) {
            this.add(writeSessionFileItem);
        }

        final JMenuItem writeEagleSessionScriptItem = new JMenuItem();
        writeEagleSessionScriptItem.setText(resources.getString("eagle_script"));
        writeEagleSessionScriptItem.setToolTipText(resources.getString("eagle_script_tooltip"));
        writeEagleSessionScriptItem.addActionListener(evt -> boardFrame.getDesignFile().updateEagle(boardFrame));

        if (routingBoard.getTestLevel() != TestLevel.RELEASE_VERSION || hostCadIsEagle) {
            this.add(writeEagleSessionScriptItem);
        }
    }

    /**
     * Adds a menu item for saving the current interactive settings as default.
     */
    private void addSaveSettingsItem() {
        final JMenuItem saveSettingsItem = new JMenuItem();
        saveSettingsItem.setText(resources.getString("settings"));
        saveSettingsItem.setToolTipText(resources.getString("settings_tooltip"));
        saveSettingsItem.addActionListener(evt -> saveDefaultsAction());
        add(saveSettingsItem);
    }

    private void saveAsAction() {
        if (boardFrame.getDesignFile() != null) {
            boardFrame.getDesignFile()
                    .saveAsDialog(this, boardFrame);
        }
    }

    private void writeLogfileAction() {
        final JFileChooser fileChooser = new JFileChooser();
        final File logfileDir = boardFrame.getDesignFile().getParentFile();
        fileChooser.setCurrentDirectory(logfileDir);
        fileChooser.setFileFilter(BoardFrame.LOGFILE_FILTER);
        fileChooser.showOpenDialog(this);
        final File filename = fileChooser.getSelectedFile();
        if (filename == null) {
            boardFrame.screenMessages.setStatusMessage(resources.getString("message_8"));
        } else {
            boardFrame.screenMessages.setStatusMessage(resources.getString("message_9"));
            boardFrame.getBoardPanel().getBoardHandling().start_logfile(filename);
        }
    }

    private void readLogfileAction() {
        final JFileChooser fileChooser = new JFileChooser();
        final File logfileDir = boardFrame.getDesignFile().getParentFile();
        fileChooser.setCurrentDirectory(logfileDir);
        fileChooser.setFileFilter(BoardFrame.LOGFILE_FILTER);
        fileChooser.showOpenDialog(this);
        final File filename = fileChooser.getSelectedFile();
        if (filename == null) {
            boardFrame.screenMessages.setStatusMessage(resources.getString("message_10"));
        } else {
            try (InputStream input_stream = new FileInputStream(filename)) {
                boardFrame.readLogFile(input_stream);
            } catch (IOException e) {
                FRLogger.error("readLogfileAction()", e);
            }
        }
    }

    private void saveDefaultsAction() {
        FRLogger.info("Saving '" + BoardFrame.GUI_DEFAULTS_FILE_NAME + "'...");
        final File defaultsFile = new File(boardFrame.getDesignFile().getParent(), BoardFrame.GUI_DEFAULTS_FILE_NAME);
        if (defaultsFile.exists()) {
            // Make a backup copy of the old defaulds file.
            final File defaultsFileBackup = new File(boardFrame.getDesignFile().getParent(), BoardFrame.GUI_DEFAULTS_FILE_BACKUP_NAME);
            if (defaultsFileBackup.exists()) {
                defaultsFileBackup.delete();
            }
            defaultsFile.renameTo(defaultsFileBackup);
        }
        try (final OutputStream outputStream = new FileOutputStream(defaultsFile)) {
            boolean write_ok = GUIDefaultsFile.write(boardFrame, boardFrame.boardPanel.boardHandling, outputStream);
            if (write_ok) {
                boardFrame.screenMessages.setStatusMessage(resources.getString("message_17"));
            } else {
                boardFrame.screenMessages.setStatusMessage(resources.getString("message_18"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
