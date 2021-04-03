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

import eu.mihosoft.freerouting.logger.FRLogger;

import javax.swing.*;

/**
 * Creates the file menu of a board frame.
 *
 * @author Alfons Wirtz
 */
public class BoardMenuFile extends JMenu {

    private final BoardFrame boardFrame;
    private final boolean session_file_option;
    private final java.util.ResourceBundle resources;

    private BoardMenuFile(
            final BoardFrame boardFrame,
            final boolean sessionFileOption
    ) {
        session_file_option = sessionFileOption;
        this.boardFrame = boardFrame;
        resources = java.util.ResourceBundle.getBundle("eu.mihosoft.freerouting.gui.BoardMenuFile", boardFrame.get_locale());
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
        if (!sessionFileOption && !boardFrame.is_web_start) {
            javax.swing.JMenuItem save_item = new javax.swing.JMenuItem();
            save_item.setText(fileMenu.resources.getString("save"));
            save_item.setToolTipText(fileMenu.resources.getString("save_tooltip"));
            save_item.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    boolean save_ok = fileMenu.boardFrame.save();
                    fileMenu.boardFrame.boardPanel.boardHandling.close_files();
                    if (save_ok) {
                        fileMenu.boardFrame.screenMessages.setStatusMessage(fileMenu.resources.getString("save_message"));
                    }
                }
            });

            fileMenu.add(save_item);
        }

        if (!boardFrame.is_web_start) {
            javax.swing.JMenuItem save_and_exit_item = new javax.swing.JMenuItem();
            save_and_exit_item.setText(fileMenu.resources.getString("save_and_exit"));
            save_and_exit_item.setToolTipText(fileMenu.resources.getString("save_and_exit_tooltip"));
            save_and_exit_item.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    if (fileMenu.session_file_option) {
                        fileMenu.boardFrame.design_file.writeSpecctraSessionFile(fileMenu.boardFrame);
                    } else {
                        fileMenu.boardFrame.save();
                    }
                    fileMenu.boardFrame.dispose();
                }
            });

            fileMenu.add(save_and_exit_item);
        }

        javax.swing.JMenuItem cancel_and_exit_item = new javax.swing.JMenuItem();
        cancel_and_exit_item.setText(fileMenu.resources.getString("cancel_and_exit"));
        cancel_and_exit_item.setToolTipText(fileMenu.resources.getString("cancel_and_exit_tooltip"));
        cancel_and_exit_item.addActionListener(evt -> fileMenu.boardFrame.dispose());

        fileMenu.add(cancel_and_exit_item);

        if (!fileMenu.session_file_option) {
            javax.swing.JMenuItem save_as_item = new javax.swing.JMenuItem();
            save_as_item.setText(fileMenu.resources.getString("save_as"));
            save_as_item.setToolTipText(fileMenu.resources.getString("save_as_tooltip"));
            save_as_item.addActionListener(evt -> fileMenu.saveAsAction());
            fileMenu.add(save_as_item);
            if (!boardFrame.is_web_start) {
                javax.swing.JMenuItem write_logfile_item = new javax.swing.JMenuItem();
                write_logfile_item.setText(fileMenu.resources.getString("generate_logfile"));
                write_logfile_item.setToolTipText(fileMenu.resources.getString("generate_logfile_tooltip"));
                write_logfile_item.addActionListener(new java.awt.event.ActionListener() {

                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        fileMenu.write_logfile_action();
                    }
                });

                fileMenu.add(write_logfile_item);

                javax.swing.JMenuItem replay_logfile_item = new javax.swing.JMenuItem();
                replay_logfile_item.setText(fileMenu.resources.getString("replay_logfile"));
                replay_logfile_item.setToolTipText(fileMenu.resources.getString("replay_logfile_tooltip"));
                replay_logfile_item.addActionListener(new java.awt.event.ActionListener() {

                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        fileMenu.read_logfile_action();
                    }
                });

                fileMenu.add(replay_logfile_item);
            }
        }

        fileMenu.add_save_settings_item();

        return fileMenu;
    }

    public void addDesignDependentItems() {
        if (this.session_file_option) {
            return;
        }
        eu.mihosoft.freerouting.board.BasicBoard routing_board = this.boardFrame.boardPanel.boardHandling.getRoutingBoard();
        boolean host_cad_is_eagle = routing_board.communication.host_cad_is_eagle();

        javax.swing.JMenuItem write_session_file_item = new javax.swing.JMenuItem();
        write_session_file_item.setText(resources.getString("session_file"));
        write_session_file_item.setToolTipText(resources.getString("session_file_tooltip"));
        write_session_file_item.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boardFrame.design_file.writeSpecctraSessionFile(boardFrame);
            }
        });

        if ((routing_board.get_test_level() != eu.mihosoft.freerouting.board.TestLevel.RELEASE_VERSION || !host_cad_is_eagle)) {
            this.add(write_session_file_item);
        }

        javax.swing.JMenuItem write_eagle_session_script_item = new javax.swing.JMenuItem();
        write_eagle_session_script_item.setText(resources.getString("eagle_script"));
        write_eagle_session_script_item.setToolTipText(resources.getString("eagle_script_tooltip"));
        write_eagle_session_script_item.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boardFrame.design_file.updateEagle(boardFrame);
            }
        });

        if (routing_board.get_test_level() != eu.mihosoft.freerouting.board.TestLevel.RELEASE_VERSION || host_cad_is_eagle) {
            this.add(write_eagle_session_script_item);
        }
    }

    /**
     * Adds a menu item for saving the current interactive settings as default.
     */
    private void add_save_settings_item() {
        javax.swing.JMenuItem save_settings_item = new javax.swing.JMenuItem();
        save_settings_item.setText(resources.getString("settings"));
        save_settings_item.setToolTipText(resources.getString("settings_tooltip"));
        save_settings_item.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                save_defaults_action();
            }
        });
        add(save_settings_item);
    }

    private void saveAsAction() {
        if (boardFrame.getDesignFile() != null) {
            boardFrame.getDesignFile()
                    .saveAsDialog(this, boardFrame);
        }
    }

    private void write_logfile_action() {
        javax.swing.JFileChooser file_chooser = new javax.swing.JFileChooser();
        java.io.File logfile_dir = boardFrame.design_file.getParentFile();
        file_chooser.setCurrentDirectory(logfile_dir);
        file_chooser.setFileFilter(BoardFrame.logfile_filter);
        file_chooser.showOpenDialog(this);
        java.io.File filename = file_chooser.getSelectedFile();
        if (filename == null) {
            boardFrame.screenMessages.setStatusMessage(resources.getString("message_8"));
        } else {

            boardFrame.screenMessages.setStatusMessage(resources.getString("message_9"));
            boardFrame.boardPanel.boardHandling.start_logfile(filename);
        }
    }

    private void read_logfile_action() {
        javax.swing.JFileChooser file_chooser = new javax.swing.JFileChooser();
        java.io.File logfile_dir = boardFrame.design_file.getParentFile();
        file_chooser.setCurrentDirectory(logfile_dir);
        file_chooser.setFileFilter(BoardFrame.logfile_filter);
        file_chooser.showOpenDialog(this);
        java.io.File filename = file_chooser.getSelectedFile();
        if (filename == null) {
            boardFrame.screenMessages.setStatusMessage(resources.getString("message_10"));
        } else {
            java.io.InputStream input_stream = null;
            try {
                input_stream = new java.io.FileInputStream(filename);
            } catch (java.io.FileNotFoundException e) {
                return;
            }
            boardFrame.read_logfile(input_stream);
        }
    }

    private void save_defaults_action() {
        java.io.OutputStream output_stream = null;

        FRLogger.info("Saving '" + BoardFrame.GUI_DEFAULTS_FILE_NAME + "'...");
        java.io.File defaults_file = new java.io.File(boardFrame.design_file.getParent(), BoardFrame.GUI_DEFAULTS_FILE_NAME);
        if (defaults_file.exists()) {
            // Make a backup copy of the old defaulds file.
            java.io.File defaults_file_backup = new java.io.File(boardFrame.design_file.getParent(), BoardFrame.GUI_DEFAULTS_FILE_BACKUP_NAME);
            if (defaults_file_backup.exists()) {
                defaults_file_backup.delete();
            }
            defaults_file.renameTo(defaults_file_backup);
        }
        try {
            output_stream = new java.io.FileOutputStream(defaults_file);
        } catch (Exception e) {
            output_stream = null;
        }

        boolean write_ok;
        if (output_stream == null) {
            write_ok = false;
        } else {
            write_ok = GUIDefaultsFile.write(boardFrame, boardFrame.boardPanel.boardHandling, output_stream);
        }
        if (write_ok) {
            boardFrame.screenMessages.setStatusMessage(resources.getString("message_17"));
        } else {
            boardFrame.screenMessages.setStatusMessage(resources.getString("message_18"));
        }

    }


}
