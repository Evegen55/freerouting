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
 * DesignFile.java
 *
 * Created on 25. Oktober 2006, 07:48
 *
 */
package eu.mihosoft.freerouting.gui;

import eu.mihosoft.freerouting.datastructures.FileFilter;
import eu.mihosoft.freerouting.designforms.specctra.RulesFile;
import eu.mihosoft.freerouting.interactive.BoardHandling;
import eu.mihosoft.freerouting.logger.FRLogger;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ResourceBundle;

import static java.util.ResourceBundle.getBundle;

/**
 * File functionality
 *
 * @author Alfons Wirtz
 */
public class DesignFile {

    public static final String[] ALL_FILE_EXTENSIONS = {"bin", "dsn"};
    public static final String[] TEXT_FILE_EXTENSIONS = {"dsn"};
    public static final String BINARY_FILE_EXTENSION = "bin";

    /**
     * Used, if the application is run without Java Web Start.
     */
    private File outputFile;
    private final File inputFile;
    private JFileChooser fileChooser;
    private static final String RULES_FILE_EXTENSION = ".rules";

    public static DesignFile getInstance(String designFileName) {
        if (designFileName == null) {
            return null;
        }
        return new DesignFile(new File(designFileName), null);
    }

    /**
     * Shows a file chooser for opening a design file.
     */
    public static DesignFile openViaFileDialog(final String designDirName) {
        final JFileChooser fileChooser = new JFileChooser(designDirName);
        final FileFilter file_filter = new FileFilter(ALL_FILE_EXTENSIONS);
        fileChooser.setFileFilter(file_filter);
        fileChooser.showOpenDialog(null);
        final File currDesignFile = fileChooser.getSelectedFile();
        if (currDesignFile == null) {
            return null;
        }
        return new DesignFile(currDesignFile, fileChooser);
    }

    /**
     * Creates a new instance of DesignFile.
     */
    private DesignFile(
            final File designFile,
            final JFileChooser fileChooser
    ) {
        this.fileChooser = fileChooser;
        this.inputFile = designFile;
        this.outputFile = designFile;
        if (designFile != null) {
            final String designFileName = designFile.getName();
            final String[] nameParts = designFileName.split("\\.");
            if (!nameParts[nameParts.length - 1].equalsIgnoreCase(BINARY_FILE_EXTENSION)) {
                final String binfileName = nameParts[0] + "." + BINARY_FILE_EXTENSION;
                outputFile = new File(designFile.getParent(), binfileName);
            }
        }
    }

    /**
     * Gets an InputStream from the file. Returns null, if the algorithm failed.
     */
    public InputStream getInputStream() {
        try {
            return new FileInputStream(this.inputFile);
        } catch (Exception e) {
            FRLogger.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * Gets the file name as a String. Returns null on failure.
     */
    public String getName() {
        return inputFile != null ?
                inputFile.getName() :
                null;
    }

    public void saveAsDialog(
            final Component component,
            final BoardFrame boardFrame
    ) {
        final ResourceBundle resources = getBundle("eu.mihosoft.freerouting.gui.BoardMenuFile", boardFrame.get_locale());
        final String[] fileNameParts = getName().split("\\.", 2);
        final String designName = fileNameParts[0];

        if (fileChooser == null) {
            String designDirName;
            if (outputFile == null) {
                designDirName = null;
            } else {
                designDirName = outputFile.getParent();
            }
            fileChooser = new JFileChooser(designDirName);//sets default dir for whole system for given user if designDirName = null
            FileFilter fileFilter = new FileFilter(ALL_FILE_EXTENSIONS);
            fileChooser.setFileFilter(fileFilter);
        }

        fileChooser.showSaveDialog(component);
        final File selectedFile = fileChooser.getSelectedFile();
        if (selectedFile == null) {
            boardFrame.screenMessages.setStatusMessage(resources.getString("message_1"));
            return;
        }

        final String selectedFileName = selectedFile.getName();
        FRLogger.info("Saving '" + selectedFileName + "'...");
        final String[] newNameParts = selectedFileName.split("\\.");
        final String foundFileExtension = newNameParts[newNameParts.length - 1];
        if (foundFileExtension.equalsIgnoreCase(BINARY_FILE_EXTENSION)) {
            boardFrame.screenMessages.setStatusMessage(resources.getString("message_2") + " " + selectedFile.getName());
            outputFile = selectedFile;
            boardFrame.save();
        } else {
            if (foundFileExtension.compareToIgnoreCase("dsn") != 0) {
                boardFrame.screenMessages.setStatusMessage(resources.getString("message_3"));
                return;
            }
            try (final OutputStream outputStream = new FileOutputStream(selectedFile)) {
                if (boardFrame.getBoardPanel()
                        .getBoardHandling()
                        .exportToDsnFile(outputStream, designName, false)
                ) {
                    boardFrame.screenMessages
                            .setStatusMessage(resources.getString("message_4") +
                                              " " + selectedFileName +
                                              " " + resources.getString("message_5"));
                } else {
                    boardFrame.screenMessages
                            .setStatusMessage(resources.getString("message_6") +
                                              " " + selectedFileName +
                                              " " + resources.getString("message_7"));
                }
            } catch (IOException e) {
                boardFrame.screenMessages
                        .setStatusMessage(resources.getString("message_6") +
                                          " " + selectedFileName +
                                          " " + resources.getString("message_7"));
            }
        }
    }

    /**
     * Writes a Specctra Session File to update the design file in the host system. Returns false, if the write failed
     */
    public boolean writeSpecctraSessionFile(final BoardFrame boardFrame) {
        final ResourceBundle resources = getBundle("eu.mihosoft.freerouting.gui.BoardMenuFile", boardFrame.get_locale());
        final String designFileName = getName();
        final String[] fileNameParts = designFileName.split("\\.", 2);
        final String designName = fileNameParts[0];
        final String outputFileName = designName + ".ses";
        FRLogger.info("Saving '" + outputFileName + "'...");
        final File currOutputFile = new File(getParent(), outputFileName);

        try (final OutputStream outputStream = new FileOutputStream(currOutputFile)) {
            if (boardFrame.getBoardPanel()
                    .getBoardHandling()
                    .exportSpecctraSessionFile(designFileName, outputStream)
            ) {
                boardFrame.screenMessages.setStatusMessage(resources.getString("message_11") +
                                                           " " + outputFileName +
                                                           " " + resources.getString("message_12"));
            } else {
                boardFrame.screenMessages.setStatusMessage(resources.getString("message_13") +
                                                           " " + outputFileName +
                                                           " " + resources.getString("message_7"));
                return false;
            }
        } catch (IOException e) {
            boardFrame.screenMessages.setStatusMessage(resources.getString("message_13") +
                                                       " " + outputFileName +
                                                       " " + resources.getString("message_7"));
            return false;
        }

        if (WindowMessage.confirm(resources.getString("confirm"))) {
            return writeRulesFile(designName, boardFrame.boardPanel.boardHandling);
        }
        return true;
    }

    /**
     * Saves the board rule to file, so that they can be reused later on.
     */
    private boolean writeRulesFile(
            final String designName,
            final BoardHandling boardHandling
    ) {
        final String rulesFileName = designName + RULES_FILE_EXTENSION;
        FRLogger.info("Saving '" + rulesFileName + "'...");
        final File rulesFile = new File(getParent(), rulesFileName);
        try (OutputStream outputStream = new FileOutputStream(rulesFile);) {
            RulesFile.write(boardHandling, outputStream, designName);
        } catch (IOException e) {
            FRLogger.error("unable to create rules file", e);
            return false;
        }
        return true;
    }

    /**
     * @param designName
     * @param parentName
     * @param rulesFileName
     * @param boardHandling
     * @param confirmMessage
     * @return
     */
    public static boolean readRulesFile(
            final String designName,
            final String parentName,
            final String rulesFileName,
            final BoardHandling boardHandling,
            final String confirmMessage
    ) {
        boolean dsnFileGeneratedByHost = boardHandling.getRoutingBoard()
                .communication
                .specctraParserInfo
                .dsnFileGeneratedByHost;

        if (dsnFileGeneratedByHost &&
            (confirmMessage == null || WindowMessage.confirm(confirmMessage))
        ) {
            FRLogger.info("Opening '" + rulesFileName + "'...");
            return RulesFile.read(parentName, rulesFileName, designName, boardHandling);
        } else {
            return false;
        }
    }

    /**
     *
     * @param boardFrame
     */
    public void updateEagle(final BoardFrame boardFrame) {
        final ResourceBundle resources = getBundle("eu.mihosoft.freerouting.gui.BoardMenuFile", boardFrame.get_locale());
        final String designFileName = getName();
        ByteArrayOutputStream sessionOutputStream = new ByteArrayOutputStream();
        if (!boardFrame.getBoardPanel()
                .getBoardHandling()
                .exportSpecctraSessionFile(designFileName, sessionOutputStream)) {
            return;
        }

        final String[] fileNameParts = designFileName.split("\\.", 2);
        final String designName = fileNameParts[0];
        final String outputFileName = designName + ".scr";
        FRLogger.info("Saving to'" + outputFileName + "'...");

        try (final InputStream inputStream = new ByteArrayInputStream(sessionOutputStream.toByteArray());
             final OutputStream outputStream = new FileOutputStream(new File(getParent(), outputFileName))) {
            if (boardFrame.getBoardPanel()
                    .getBoardHandling()
                    .exportEagleSessionFile(inputStream, outputStream)) {
                boardFrame.screenMessages
                        .setStatusMessage(resources.getString("message_14") +
                                          " " + outputFileName +
                                          " " + resources.getString("message_15"));
            } else {
                boardFrame.screenMessages
                        .setStatusMessage(resources.getString("message_16") +
                                          " " + outputFileName +
                                          " " + resources.getString("message_7"));
            }
        } catch (Exception e) {
            boardFrame.screenMessages
                    .setStatusMessage(resources.getString("message_16") +
                                      " " + outputFileName +
                                      " " + resources.getString("message_7"));
        }

        if (WindowMessage.confirm(resources.getString("confirm"))) {
            writeRulesFile(designName, boardFrame.boardPanel.boardHandling);
        }
    }

    /**
     * Gets the binary file for saving or null, if the design file is not available
     */
    public File getOutputFile() {
        return outputFile;
    }

    public File getInputFile() {
        return inputFile;
    }

    public String getParent() {
        if (inputFile != null) {
            return inputFile.getParent();
        }
        return null;
    }

    public File getParentFile() {
        if (inputFile != null) {
            return inputFile.getParentFile();
        }
        return null;
    }

    public boolean isCreatedFromTextFile() {
        return !inputFile.equals(outputFile);
    }
}
