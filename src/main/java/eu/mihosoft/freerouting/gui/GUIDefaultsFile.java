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
 * GUIDefaultsFile.java
 *
 * Created on 26. Dezember 2004, 08:29
 */

package eu.mihosoft.freerouting.gui;

import eu.mihosoft.freerouting.datastructures.IndentFileWriter;

import eu.mihosoft.freerouting.board.ItemSelectionFilter;
import eu.mihosoft.freerouting.interactive.BoardHandling;
import eu.mihosoft.freerouting.logger.FRLogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Description of a text file,  where the board independent interactive settings are stored.
 *
 * @author Alfons Wirtz
 */
public class GUIDefaultsFile {

    private final BoardFrame boardFrame;
    private final BoardHandling boardHandling;
    /**
     * Used, when reading a defaults file, null otherwise.
     */
    private final GUIDefaultsScanner scanner;
    /**
     * Used, when writing a defaults file; null otherwise.
     */
    private final IndentFileWriter indentFileWriter;
    /**
     * Keywords in the gui defaults file.
     */
    enum Keyword {
        ALL_VISIBLE, ASSIGN_NET_RULES, AUTOMATIC_LAYER_DIMMING, BACKGROUND, BOARD_FRAME, BOUNDS,
        CLEARANCE_COMPENSATION, CLEARANCE_MATRIX, CLOSED_BRACKET, COLOR_MANAGER, COLORS,
        COMPONENT_BACK, COMPONENT_FRONT, COMPONENT_GRID, COMPONENT_INFO, CONDUCTION, CURRENT_LAYER,
        CURRENT_ONLY, DESELECTED_SNAPSHOT_ATTRIBUTES, DISPLAY_MISCELLANIOUS, DISPLAY_REGION,
        DRAG_COMPONENTS_ENABLED, DYNAMIC, EDIT_VIAS, EDIT_NET_RULES, FIXED, FIXED_TRACES, FIXED_VIAS,
        FORTYFIVE_DEGREE, GUI_DEFAULTS, HILIGHT, HILIGHT_ROUTING_OBSTACLE, IGNORE_CONDUCTION_AREAS, INCOMPLETES, INCOMPLETES_INFO,
        INTERACTIVE_STATE, KEEPOUT, LAYER_VISIBILITY, LENGTH_MATCHING, MANUAL_RULES, MANUAL_RULE_SETTINGS,
        MOVE_PARAMETER, NET_INFO, NINETY_DEGREE, NONE, NOT_VISIBLE, OBJECT_COLORS, OBJECT_VISIBILITY,
        OPEN_BRACKET, OFF, ON, OUTLINE, PARAMETER, PACKAGE_INFO, PADSTACK_INFO, PINS, PULL_TIGHT_ACCURACY,
        PULL_TIGHT_REGION, PUSH_AND_SHOVE_ENABLED, ROUTE_DETAILS, ROUTE_MODE, ROUTE_PARAMETER,
        RULE_SELECTION, SELECT_PARAMETER, SELECTABLE_ITEMS, SELECTION_LAYERS, SNAPSHOTS,
        SHOVE_ENABLED, STITCHING, TRACES, UNFIXED, VIA_KEEPOUT, VISIBLE, VIA_RULES, VIA_SNAP_TO_SMD_CENTER,
        VIAS, VIOLATIONS, VIOLATIONS_INFO, WINDOWS
    }

    private GUIDefaultsFile(
            final BoardFrame boardFrame,
            final BoardHandling boardHandling,
            final GUIDefaultsScanner scanner,
            final IndentFileWriter indentFileWriter
    ) {
        this.boardFrame = boardFrame;
        this.boardHandling = boardHandling;
        this.scanner = scanner;
        this.indentFileWriter = indentFileWriter;
    }

    /**
     * Writes the GUI setting of boardFrame as default to p_file. Returns false, if an error occured.
     */
    public static boolean write(
            final BoardFrame boardFrame,
            final BoardHandling boardHandling,
            final OutputStream outputStream) {
        if (outputStream == null) {
            return false;
        }
        try (final IndentFileWriter outputFile = new IndentFileWriter(outputStream);) {
            GUIDefaultsFile result = new GUIDefaultsFile(boardFrame, boardHandling, null, outputFile);
            result.writeDefaultsScope();
        } catch (java.io.IOException e) {
            FRLogger.error("unable to write defaults file", e);
            return false;
        }
        return true;
    }

    /**
     * Reads the GUI setting of p_board_frame from file. Returns false, if an error occured while reading the file.
     */
    public static boolean read(
            final BoardFrame p_board_frame,
            final BoardHandling boardHandling,
            final InputStream inputStream
    ) {
        if (inputStream == null) {
            return false;
        }
        GUIDefaultsScanner scanner = new GUIDefaultsScanner(inputStream);
        GUIDefaultsFile new_instance = new GUIDefaultsFile(p_board_frame, boardHandling, scanner, null);
        boolean result;
        try {
            result = new_instance.readDefaultsScope();
        } catch (java.io.IOException e) {
            FRLogger.error("unable to read defaults file", e);
            result = false;
        }
        return result;
    }



    private void writeDefaultsScope() throws java.io.IOException {
        indentFileWriter.start_scope();
        indentFileWriter.write("gui_defaults");
        writeWindowsScope();
        write_colors_scope();
        write_parameter_scope();
        indentFileWriter.end_scope();
    }

    private boolean readDefaultsScope() throws IOException {
        Object nextToken = scanner.nextToken();

        if (nextToken != Keyword.OPEN_BRACKET) {
            return false;
        }
        nextToken = scanner.nextToken();
        if (nextToken != Keyword.GUI_DEFAULTS) {
            return false;
        }

        // read the direct subscopes of the gui_defaults scope
        for (; ; ) {
            Object prevToken = nextToken;
            nextToken = scanner.nextToken();
            if (nextToken == null) {
                // end of file
                return true;
            }
            if (nextToken == Keyword.CLOSED_BRACKET) {
                // end of scope
                break;
            }

            if (prevToken == Keyword.OPEN_BRACKET) {
                if (nextToken == Keyword.COLORS) {
                    if (!read_colors_scope()) {
                        return false;
                    }
                } else if (nextToken == Keyword.WINDOWS) {
                    if (!readWindowsScope()) {
                        return false;
                    }
                } else if (nextToken == Keyword.PARAMETER) {
                    if (!read_parameter_scope()) {
                        return false;
                    }
                } else {
                    // overread all scopes except the routes scope for the time being
                    skip_scope(this.scanner);
                }
            }
        }
        boardFrame.refreshWindows();
        return true;
    }

    private boolean readWindowsScope() throws IOException {
        // read the direct subscopes of the windows scope
        Object nextToken = null;
        for (; ; ) {
            Object prevToken = nextToken;
            nextToken = scanner.nextToken();
            if (nextToken == null) {
                // unexpected end of file
                return false;
            }
            if (nextToken == Keyword.CLOSED_BRACKET) {
                // end of scope
                break;
            }

            if (prevToken == Keyword.OPEN_BRACKET) {
                if (!(nextToken instanceof Keyword)) {
                    FRLogger.warn("GUIDefaultsFile.windows: Keyword expected");
                    return false;
                }
                if (!read_frame_scope((Keyword) nextToken)) {

                    return false;
                }
            }
        }
        return true;
    }

    private void writeWindowsScope() throws IOException {
        indentFileWriter.start_scope();
        indentFileWriter.write("windows");
        write_frame_scope(boardFrame, "board_frame");
        write_frame_scope(boardFrame.getColorManager(), "color_manager");
        write_frame_scope(boardFrame.getLayerVisibilityWindow(), "layer_visibility");
        write_frame_scope(boardFrame.getObjectVisibilityWindow(), "object_visibility");
        write_frame_scope(boardFrame.getDisplayMiscWindow(), "display_miscellanious");
        write_frame_scope(boardFrame.getSnapshotWindow(), "snapshots");
        write_frame_scope(boardFrame.getSelectParameterWindow(), "select_parameter");
        write_frame_scope(boardFrame.getRouteParameterWindow(), "route_parameter");
        write_frame_scope(boardFrame.getRouteParameterWindow().manualRuleWindow, "manual_rules");
        write_frame_scope(boardFrame.getRouteParameterWindow().detailWindow, "route_details");
        write_frame_scope(boardFrame.getMoveParameterWindow(), "move_parameter");
        write_frame_scope(boardFrame.getClearanceMatrixWindow(), "clearance_matrix");
        write_frame_scope(boardFrame.getViaWindow(), "via_rules");
        write_frame_scope(boardFrame.getEditViasWindow(), "edit_vias");
        write_frame_scope(boardFrame.getEditNetRulesWindow(), "edit_net_rules");
        write_frame_scope(boardFrame.getAssignNetClassesWindow(), "assign_net_rules");
        write_frame_scope(boardFrame.getPadstacksWindow(), "padstack_info");
        write_frame_scope(boardFrame.getPackagesWindow(), "package_info");
        write_frame_scope(boardFrame.getComponentsWindow(), "component_info");
        write_frame_scope(boardFrame.getNetInfoWindow(), "net_info");
        write_frame_scope(boardFrame.getIncompletesWindow(), "incompletes_info");
        write_frame_scope(boardFrame.getClearanceViolationsWindow(), "violations_info");
        indentFileWriter.end_scope();
    }

    private boolean read_frame_scope(Keyword p_frame) throws java.io.IOException {
        boolean is_visible;
        Object next_token = this.scanner.nextToken();
        if (next_token == Keyword.VISIBLE) {
            is_visible = true;
        } else if (next_token == Keyword.NOT_VISIBLE) {
            is_visible = false;
        } else {
            FRLogger.warn("GUIDefaultsFile.read_frame_scope: visible or not_visible expected");
            return false;
        }
        next_token = this.scanner.nextToken();
        if (next_token != Keyword.OPEN_BRACKET) {
            FRLogger.warn("GUIDefaultsFile.read_frame_scope: open_bracket expected");
            return false;
        }
        next_token = this.scanner.nextToken();
        if (next_token != Keyword.BOUNDS) {
            FRLogger.warn("GUIDefaultsFile.read_frame_scope: bounds expected");
            return false;
        }
        java.awt.Rectangle bounds = read_rectangle();
        if (bounds == null) {
            return false;
        }
        for (int i = 0; i < 2; ++i) {
            next_token = this.scanner.nextToken();
            if (next_token != Keyword.CLOSED_BRACKET) {
                FRLogger.warn("GUIDefaultsFile.read_frame_scope: closing bracket expected");
                return false;
            }
        }
        javax.swing.JFrame curr_frame;
        if (p_frame == Keyword.BOARD_FRAME) {
            curr_frame = this.boardFrame;
        } else if (p_frame == Keyword.COLOR_MANAGER) {
            curr_frame = this.boardFrame.getColorManager();
        } else if (p_frame == Keyword.OBJECT_VISIBILITY) {
            curr_frame = this.boardFrame.objectVisibilityWindow;
        } else if (p_frame == Keyword.LAYER_VISIBILITY) {
            curr_frame = this.boardFrame.layerVisibilityWindow;
        } else if (p_frame == Keyword.DISPLAY_MISCELLANIOUS) {
            curr_frame = this.boardFrame.displayMiscWindow;
        } else if (p_frame == Keyword.SNAPSHOTS) {
            curr_frame = this.boardFrame.snapshotWindow;
        } else if (p_frame == Keyword.SELECT_PARAMETER) {
            curr_frame = this.boardFrame.selectParameterWindow;
        } else if (p_frame == Keyword.ROUTE_PARAMETER) {
            curr_frame = this.boardFrame.routeParameterWindow;
        } else if (p_frame == Keyword.MANUAL_RULES) {
            curr_frame = this.boardFrame.routeParameterWindow.manualRuleWindow;
        } else if (p_frame == Keyword.ROUTE_DETAILS) {
            curr_frame = this.boardFrame.routeParameterWindow.detailWindow;
        } else if (p_frame == Keyword.MOVE_PARAMETER) {
            curr_frame = this.boardFrame.moveParameterWindow;
        } else if (p_frame == Keyword.CLEARANCE_MATRIX) {
            curr_frame = this.boardFrame.clearanceMatrixWindow;
        } else if (p_frame == Keyword.VIA_RULES) {
            curr_frame = this.boardFrame.viaWindow;
        } else if (p_frame == Keyword.EDIT_VIAS) {
            curr_frame = this.boardFrame.editViasWindow;
        } else if (p_frame == Keyword.EDIT_NET_RULES) {
            curr_frame = this.boardFrame.editNetRulesWindow;
        } else if (p_frame == Keyword.ASSIGN_NET_RULES) {
            curr_frame = this.boardFrame.assignNetClassesWindow;
        } else if (p_frame == Keyword.PADSTACK_INFO) {
            curr_frame = this.boardFrame.padstacksWindow;
        } else if (p_frame == Keyword.PACKAGE_INFO) {
            curr_frame = this.boardFrame.packagesWindow;
        } else if (p_frame == Keyword.COMPONENT_INFO) {
            curr_frame = this.boardFrame.componentsWindow;
        } else if (p_frame == Keyword.NET_INFO) {
            curr_frame = this.boardFrame.netInfoWindow;
        } else if (p_frame == Keyword.INCOMPLETES_INFO) {
            curr_frame = this.boardFrame.incompletesWindow;
        } else if (p_frame == Keyword.VIOLATIONS_INFO) {
            curr_frame = this.boardFrame.clearanceViolationsWindow;
        } else {
            FRLogger.warn("GUIDefaultsFile.read_frame_scope: unknown frame");
            return false;
        }
        curr_frame.setVisible(is_visible);
        if (p_frame == Keyword.BOARD_FRAME) {
            curr_frame.setBounds(bounds);
        } else {
            // Set only the location.
            // Do not change the size of the frame because it depends on the layer count.
            curr_frame.setLocation(bounds.getLocation());
        }
        return true;
    }

    private java.awt.Rectangle read_rectangle() throws java.io.IOException {
        int[] coor = new int[4];
        for (int i = 0; i < 4; ++i) {
            Object next_token = this.scanner.nextToken();
            if (!(next_token instanceof Integer)) {
                FRLogger.warn("GUIDefaultsFile.read_rectangle: Integer expected");
                return null;
            }
            coor[i] = (Integer) next_token;
        }
        return new java.awt.Rectangle(coor[0], coor[1], coor[2], coor[3]);
    }

    private void write_frame_scope(javax.swing.JFrame p_frame, String p_frame_name)
            throws java.io.IOException {
        indentFileWriter.start_scope();
        indentFileWriter.write(p_frame_name);
        indentFileWriter.new_line();
        if (p_frame.isVisible()) {
            indentFileWriter.write("visible");
        } else {
            indentFileWriter.write("not_visible");
        }
        write_bounds(p_frame.getBounds());
        indentFileWriter.end_scope();
    }

    private void write_bounds(java.awt.Rectangle p_bounds) throws java.io.IOException {
        indentFileWriter.start_scope();
        indentFileWriter.write("bounds");
        indentFileWriter.new_line();
        Integer x = (int) p_bounds.getX();
        indentFileWriter.write(x.toString());
        Integer y = (int) p_bounds.getY();
        indentFileWriter.write(" ");
        indentFileWriter.write(y.toString());
        Integer width = (int) p_bounds.getWidth();
        indentFileWriter.write(" ");
        indentFileWriter.write(width.toString());
        Integer height = (int) p_bounds.getHeight();
        indentFileWriter.write(" ");
        indentFileWriter.write(height.toString());
        indentFileWriter.end_scope();
    }

    private boolean read_colors_scope() throws java.io.IOException {
        // read the direct subscopes of the colors scope
        Object next_token = null;
        for (; ; ) {
            Object prev_token = next_token;
            next_token = this.scanner.nextToken();
            if (next_token == null) {
                // unexpected end of file
                return false;
            }
            if (next_token == Keyword.CLOSED_BRACKET) {
                // end of scope
                break;
            }

            if (prev_token == Keyword.OPEN_BRACKET) {

                if (next_token == Keyword.BACKGROUND) {
                    if (!read_background_color()) {
                        return false;
                    }
                } else if (next_token == Keyword.CONDUCTION) {
                    if (!read_conduction_colors()) {
                        return false;
                    }
                } else if (next_token == Keyword.HILIGHT) {
                    if (!read_hilight_color()) {
                        return false;
                    }
                } else if (next_token == Keyword.INCOMPLETES) {
                    if (!read_incompletes_color()) {
                        return false;
                    }
                } else if (next_token == Keyword.KEEPOUT) {
                    if (!read_keepout_colors()) {
                        return false;
                    }
                } else if (next_token == Keyword.OUTLINE) {
                    if (!read_outline_color()) {
                        return false;
                    }
                } else if (next_token == Keyword.COMPONENT_FRONT) {
                    if (!read_component_color(true)) {
                        return false;
                    }
                } else if (next_token == Keyword.COMPONENT_BACK) {
                    if (!read_component_color(false)) {
                        return false;
                    }
                } else if (next_token == Keyword.LENGTH_MATCHING) {
                    if (!read_length_matching_color()) {
                        return false;
                    }
                } else if (next_token == Keyword.PINS) {
                    if (!read_pin_colors()) {
                        return false;
                    }
                } else if (next_token == Keyword.TRACES) {
                    if (!read_trace_colors(false)) {
                        return false;
                    }
                } else if (next_token == Keyword.FIXED_TRACES) {
                    if (!read_trace_colors(true)) {
                        return false;
                    }
                } else if (next_token == Keyword.VIA_KEEPOUT) {
                    if (!read_via_keepout_colors()) {
                        return false;
                    }
                } else if (next_token == Keyword.VIAS) {
                    if (!read_via_colors(false)) {
                        return false;
                    }
                } else if (next_token == Keyword.FIXED_VIAS) {
                    if (!read_via_colors(true)) {
                        return false;
                    }
                } else if (next_token == Keyword.VIOLATIONS) {
                    if (!read_violations_color()) {
                        return false;
                    }
                } else {
                    // skip unknown scope
                    skip_scope(this.scanner);
                }
            }
        }
        return true;
    }

    private boolean read_trace_colors(boolean p_fixed) throws java.io.IOException {
        double intensity = read_color_intensity();
        if (intensity < 0) {
            return false;
        }
        this.boardHandling.graphicsContext.set_trace_color_intensity(intensity);
        java.awt.Color[] curr_colors = read_color_array();
        if (curr_colors.length < 1) {
            return false;
        }
        this.boardHandling.graphicsContext.item_color_table.set_trace_colors(curr_colors, p_fixed);
        return true;
    }

    private boolean read_via_colors(boolean p_fixed) throws java.io.IOException {
        double intensity = read_color_intensity();
        if (intensity < 0) {
            return false;
        }
        this.boardHandling.graphicsContext.set_via_color_intensity(intensity);
        java.awt.Color[] curr_colors = read_color_array();
        if (curr_colors.length < 1) {
            return false;
        }
        this.boardHandling.graphicsContext.item_color_table.set_via_colors(curr_colors, p_fixed);
        return true;
    }

    private boolean read_pin_colors() throws java.io.IOException {
        double intensity = read_color_intensity();
        if (intensity < 0) {
            return false;
        }
        this.boardHandling.graphicsContext.set_pin_color_intensity(intensity);
        java.awt.Color[] curr_colors = read_color_array();
        if (curr_colors.length < 1) {
            return false;
        }
        this.boardHandling.graphicsContext.item_color_table.set_pin_colors(curr_colors);
        return true;
    }

    private boolean read_conduction_colors() throws java.io.IOException {
        double intensity = read_color_intensity();
        if (intensity < 0) {
            return false;
        }
        this.boardHandling.graphicsContext.set_conduction_color_intensity(intensity);
        java.awt.Color[] curr_colors = read_color_array();
        if (curr_colors.length < 1) {
            return false;
        }
        this.boardHandling.graphicsContext.item_color_table.set_conduction_colors(curr_colors);
        return true;
    }

    private boolean read_keepout_colors() throws java.io.IOException {
        double intensity = read_color_intensity();
        if (intensity < 0) {
            return false;
        }
        this.boardHandling.graphicsContext.set_obstacle_color_intensity(intensity);
        java.awt.Color[] curr_colors = read_color_array();
        if (curr_colors.length < 1) {
            return false;
        }
        this.boardHandling.graphicsContext.item_color_table.set_keepout_colors(curr_colors);
        return true;
    }

    private boolean read_via_keepout_colors() throws java.io.IOException {
        double intensity = read_color_intensity();
        if (intensity < 0) {
            return false;
        }
        this.boardHandling.graphicsContext.set_via_obstacle_color_intensity(intensity);
        java.awt.Color[] curr_colors = read_color_array();
        if (curr_colors.length < 1) {
            return false;
        }
        this.boardHandling.graphicsContext.item_color_table.set_via_keepout_colors(curr_colors);
        return true;
    }

    private boolean read_background_color() throws java.io.IOException {
        java.awt.Color curr_color = read_color();
        if (curr_color == null) {
            return false;
        }
        this.boardHandling.graphicsContext.other_color_table.set_background_color(curr_color);
        this.boardFrame.setBoardBackground(curr_color);
        Object next_token = this.scanner.nextToken();
        if (next_token != Keyword.CLOSED_BRACKET) {
            FRLogger.warn("GUIDefaultsFile.read_background_color: closing bracket expected");
            return false;
        }
        return true;
    }

    private boolean read_hilight_color() throws java.io.IOException {
        double intensity = read_color_intensity();
        if (intensity < 0) {
            return false;
        }
        this.boardHandling.graphicsContext.set_hilight_color_intensity(intensity);
        java.awt.Color curr_color = read_color();
        if (curr_color == null) {
            return false;
        }
        this.boardHandling.graphicsContext.other_color_table.set_hilight_color(curr_color);
        Object next_token = this.scanner.nextToken();
        if (next_token != Keyword.CLOSED_BRACKET) {
            FRLogger.warn("GUIDefaultsFile.read_higlight_color: closing bracket expected");
            return false;
        }
        return true;
    }

    private boolean read_incompletes_color() throws java.io.IOException {
        double intensity = read_color_intensity();
        if (intensity < 0) {
            return false;
        }
        this.boardHandling.graphicsContext.set_incomplete_color_intensity(intensity);
        java.awt.Color curr_color = read_color();
        if (curr_color == null) {
            return false;
        }
        this.boardHandling.graphicsContext.other_color_table.set_incomplete_color(curr_color);
        Object next_token = this.scanner.nextToken();
        if (next_token != Keyword.CLOSED_BRACKET) {
            FRLogger.warn("GUIDefaultsFile.read_incompletes_color: closing bracket expected");
            return false;
        }
        return true;
    }

    private boolean read_length_matching_color() throws java.io.IOException {
        double intensity = read_color_intensity();
        if (intensity < 0) {
            return false;
        }
        this.boardHandling.graphicsContext.set_length_matching_area_color_intensity(intensity);
        java.awt.Color curr_color = read_color();
        if (curr_color == null) {
            return false;
        }
        this.boardHandling.graphicsContext.other_color_table.set_length_matching_area_color(curr_color);
        Object next_token = this.scanner.nextToken();
        if (next_token != Keyword.CLOSED_BRACKET) {
            FRLogger.warn("GUIDefaultsFile.read_length_matching_color: closing bracket expected");
            return false;
        }
        return true;
    }

    private boolean read_violations_color() throws java.io.IOException {
        java.awt.Color curr_color = read_color();
        if (curr_color == null) {
            return false;
        }
        this.boardHandling.graphicsContext.other_color_table.set_violations_color(curr_color);
        Object next_token = this.scanner.nextToken();
        if (next_token != Keyword.CLOSED_BRACKET) {
            FRLogger.warn("GUIDefaultsFile.read_violations_color: closing bracket expected");
            return false;
        }
        return true;
    }

    private boolean read_outline_color() throws java.io.IOException {
        java.awt.Color curr_color = read_color();
        if (curr_color == null) {
            return false;
        }
        this.boardHandling.graphicsContext.other_color_table.set_outline_color(curr_color);
        Object next_token = this.scanner.nextToken();
        if (next_token != Keyword.CLOSED_BRACKET) {
            FRLogger.warn("GUIDefaultsFile.read_outline_color: closing bracket expected");
            return false;
        }
        return true;
    }

    private boolean read_component_color(boolean p_front) throws java.io.IOException {
        java.awt.Color curr_color = read_color();
        if (curr_color == null) {
            return false;
        }
        this.boardHandling.graphicsContext.other_color_table.set_component_color(curr_color, p_front);
        Object next_token = this.scanner.nextToken();
        if (next_token != Keyword.CLOSED_BRACKET) {
            FRLogger.warn("GUIDefaultsFile.read_component_color: closing bracket expected");
            return false;
        }
        return true;
    }


    private double read_color_intensity() throws java.io.IOException {
        double result;
        Object next_token = this.scanner.nextToken();
        if (next_token instanceof Double) {
            result = (Double) next_token;
        } else if (next_token instanceof Integer) {
            result = (Integer) next_token;
        } else {
            FRLogger.warn("GUIDefaultsFile.read_color_intensity: Number expected");
            result = -1;
        }
        return result;
    }

    /**
     * reads a java.awt.Color from the defaults file. Returns null, if no valid color was found.
     */
    private java.awt.Color read_color() throws java.io.IOException {
        int[] rgb_color_arr = new int[3];
        for (int i = 0; i < 3; ++i) {
            Object next_token = this.scanner.nextToken();
            if (!(next_token instanceof Integer)) {
                if (next_token != Keyword.CLOSED_BRACKET) {
                    FRLogger.warn("GUIDefaultsFile.read_color: closing bracket expected");
                }
                return null;
            }
            rgb_color_arr[i] = (Integer) next_token;
        }
        return new java.awt.Color(rgb_color_arr[0], rgb_color_arr[1], rgb_color_arr[2]);
    }

    /**
     * reads a n array java.awt.Color from the defaults file. Returns null, if no valid colors were found.
     */
    private java.awt.Color[] read_color_array() throws java.io.IOException {
        java.util.Collection<java.awt.Color> color_list = new java.util.LinkedList<java.awt.Color>();
        for (; ; ) {
            java.awt.Color curr_color = read_color();
            if (curr_color == null) {
                break;
            }
            color_list.add(curr_color);
        }
        java.awt.Color[] result = new java.awt.Color[color_list.size()];
        java.util.Iterator<java.awt.Color> it = color_list.iterator();
        for (int i = 0; i < result.length; ++i) {
            result[i] = it.next();
        }
        return result;
    }

    private void write_colors_scope() throws java.io.IOException {
        eu.mihosoft.freerouting.boardgraphics.GraphicsContext graphics_context = this.boardHandling.graphicsContext;
        indentFileWriter.start_scope();
        indentFileWriter.write("colors");
        indentFileWriter.start_scope();
        indentFileWriter.write("background");
        write_color_scope(graphics_context.get_background_color());
        indentFileWriter.end_scope();
        indentFileWriter.start_scope();
        indentFileWriter.write("hilight");
        write_color_intensity(graphics_context.get_hilight_color_intensity());
        write_color_scope(graphics_context.get_hilight_color());
        indentFileWriter.end_scope();
        indentFileWriter.start_scope();
        indentFileWriter.write("incompletes");
        write_color_intensity(graphics_context.get_incomplete_color_intensity());
        write_color_scope(graphics_context.get_incomplete_color());
        indentFileWriter.end_scope();
        indentFileWriter.start_scope();
        indentFileWriter.write("outline");
        write_color_scope(graphics_context.get_outline_color());
        indentFileWriter.end_scope();
        indentFileWriter.start_scope();
        indentFileWriter.write("component_front");
        write_color_scope(graphics_context.get_component_color(true));
        indentFileWriter.end_scope();
        indentFileWriter.start_scope();
        indentFileWriter.write("component_back");
        write_color_scope(graphics_context.get_component_color(false));
        indentFileWriter.end_scope();
        indentFileWriter.start_scope();
        indentFileWriter.write("violations");
        write_color_scope(graphics_context.get_violations_color());
        indentFileWriter.end_scope();
        indentFileWriter.start_scope();
        indentFileWriter.write("length_matching");
        write_color_intensity(graphics_context.get_length_matching_area_color_intensity());
        write_color_scope(graphics_context.get_length_matching_area_color());
        indentFileWriter.end_scope();
        indentFileWriter.start_scope();
        indentFileWriter.write("traces");
        write_color_intensity(graphics_context.get_trace_color_intensity());
        write_color(graphics_context.get_trace_colors(false));
        indentFileWriter.end_scope();
        indentFileWriter.start_scope();
        indentFileWriter.write("fixed_traces");
        write_color_intensity(graphics_context.get_trace_color_intensity());
        write_color(graphics_context.get_trace_colors(true));
        indentFileWriter.end_scope();
        indentFileWriter.start_scope();
        indentFileWriter.write("vias");
        write_color_intensity(graphics_context.get_via_color_intensity());
        write_color(graphics_context.get_via_colors(false));
        indentFileWriter.end_scope();
        indentFileWriter.start_scope();
        indentFileWriter.write("fixed_vias");
        write_color_intensity(graphics_context.get_via_color_intensity());
        write_color(graphics_context.get_via_colors(true));
        indentFileWriter.end_scope();
        indentFileWriter.start_scope();
        indentFileWriter.write("pins");
        write_color_intensity(graphics_context.get_pin_color_intensity());
        write_color(graphics_context.get_pin_colors());
        indentFileWriter.end_scope();
        indentFileWriter.start_scope();
        indentFileWriter.write("conduction");
        write_color_intensity(graphics_context.get_conduction_color_intensity());
        write_color(graphics_context.get_conduction_colors());
        indentFileWriter.end_scope();
        indentFileWriter.start_scope();
        indentFileWriter.write("keepout");
        write_color_intensity(graphics_context.get_obstacle_color_intensity());
        write_color(graphics_context.get_obstacle_colors());
        indentFileWriter.end_scope();
        indentFileWriter.start_scope();
        indentFileWriter.write("via_keepout");
        write_color_intensity(graphics_context.get_via_obstacle_color_intensity());
        write_color(graphics_context.get_via_obstacle_colors());
        indentFileWriter.end_scope();
        indentFileWriter.end_scope();
    }

    private void write_color_intensity(double p_value) throws java.io.IOException {
        indentFileWriter.write(" ");
        Float value = (float) p_value;
        indentFileWriter.write(value.toString());
    }

    private void write_color_scope(java.awt.Color p_color) throws java.io.IOException {
        indentFileWriter.new_line();
        Integer red = p_color.getRed();
        indentFileWriter.write(red.toString());
        indentFileWriter.write(" ");
        Integer green = p_color.getGreen();
        indentFileWriter.write(green.toString());
        indentFileWriter.write(" ");
        Integer blue = p_color.getBlue();
        indentFileWriter.write(blue.toString());
    }

    private void write_color(java.awt.Color[] p_colors) throws java.io.IOException {
        for (int i = 0; i < p_colors.length; ++i) {
            write_color_scope(p_colors[i]);
        }
    }

    private boolean read_parameter_scope() throws java.io.IOException {
        // read the subscopes of the parameter scope
        Object next_token = null;
        for (; ; ) {
            Object prev_token = next_token;
            next_token = this.scanner.nextToken();
            if (next_token == null) {
                // unexpected end of file
                return false;
            }
            if (next_token == Keyword.CLOSED_BRACKET) {
                // end of scope
                break;
            }

            if (prev_token == Keyword.OPEN_BRACKET) {

                if (next_token == Keyword.SELECTION_LAYERS) {
                    if (!read_selection_layer_scope()) {
                        return false;
                    }
                } else if (next_token == Keyword.VIA_SNAP_TO_SMD_CENTER) {
                    if (!read_via_snap_to_smd_center_scope()) {
                        return false;
                    }
                } else if (next_token == Keyword.SHOVE_ENABLED) {
                    if (!read_shove_enabled_scope()) {
                        return false;
                    }
                } else if (next_token == Keyword.DRAG_COMPONENTS_ENABLED) {
                    if (!read_drag_components_enabled_scope()) {
                        return false;
                    }
                } else if (next_token == Keyword.ROUTE_MODE) {
                    if (!read_route_mode_scope()) {
                        return false;
                    }
                } else if (next_token == Keyword.PULL_TIGHT_REGION) {
                    if (!read_pull_tight_region_scope()) {
                        return false;
                    }
                } else if (next_token == Keyword.PULL_TIGHT_ACCURACY) {
                    if (!read_pull_tight_accuracy_scope()) {
                        return false;
                    }
                } else if (next_token == Keyword.IGNORE_CONDUCTION_AREAS) {
                    if (!read_ignore_conduction_scope()) {
                        return false;
                    }
                } else if (next_token == Keyword.AUTOMATIC_LAYER_DIMMING) {
                    if (!read_automatic_layer_dimming_scope()) {
                        return false;
                    }
                } else if (next_token == Keyword.CLEARANCE_COMPENSATION) {
                    if (!read_clearance_compensation_scope()) {
                        return false;
                    }
                } else if (next_token == Keyword.HILIGHT_ROUTING_OBSTACLE) {
                    if (!read_hilight_routing_obstacle_scope()) {
                        return false;
                    }
                } else if (next_token == Keyword.SELECTABLE_ITEMS) {
                    if (!read_selectable_item_scope()) {
                        return false;
                    }
                } else if (next_token == Keyword.DESELECTED_SNAPSHOT_ATTRIBUTES) {
                    if (!read_deselected_snapshot_attributes()) {
                        return false;
                    }
                } else {
                    // skip unknown scope
                    skip_scope(this.scanner);
                }
            }
        }
        return true;
    }

    private void write_parameter_scope() throws java.io.IOException {
        indentFileWriter.start_scope();
        indentFileWriter.write("parameter");
        write_selection_layer_scope();
        write_selectable_item_scope();
        write_via_snap_to_smd_center_scope();
        write_route_mode_scope();
        write_shove_enabled_scope();
        write_drag_components_enabled_scope();
        write_hilight_routing_obstacle_scope();
        write_pull_tight_region_scope();
        write_pull_tight_accuracy_scope();
        write_clearance_compensation_scope();
        write_ignore_conduction_scope();
        write_automatic_layer_dimming_scope();
        write_deselected_snapshot_attributes();
        indentFileWriter.end_scope();
    }

    private boolean read_selection_layer_scope() throws java.io.IOException {
        Object next_token = this.scanner.nextToken();
        boolean select_on_all_layers;
        if (next_token == Keyword.ALL_VISIBLE) {
            select_on_all_layers = true;
        } else if (next_token == Keyword.CURRENT_ONLY) {
            select_on_all_layers = false;
        } else {
            FRLogger.warn("GUIDefaultsFile.read_selection_layer_scope: unexpected token");
            return false;
        }
        next_token = this.scanner.nextToken();
        if (next_token != Keyword.CLOSED_BRACKET) {
            FRLogger.warn("GUIDefaultsFile.read_selection_layer_scop: closing bracket expected");
            return false;
        }
        this.boardHandling.settings.set_select_on_all_visible_layers(select_on_all_layers);
        return true;
    }

    private boolean read_shove_enabled_scope() throws java.io.IOException {
        Object next_token = this.scanner.nextToken();
        boolean shove_enabled;
        if (next_token == Keyword.ON) {
            shove_enabled = true;
        } else if (next_token == Keyword.OFF) {
            shove_enabled = false;
        } else {
            FRLogger.warn("GUIDefaultsFile.read_shove_enabled_scope: unexpected token");
            return false;
        }
        next_token = this.scanner.nextToken();
        if (next_token != Keyword.CLOSED_BRACKET) {
            FRLogger.warn("GUIDefaultsFile.read_shove_enabled_scope: closing bracket expected");
            return false;
        }
        this.boardHandling.settings.set_push_enabled(shove_enabled);
        return true;
    }

    private boolean read_drag_components_enabled_scope() throws java.io.IOException {
        Object next_token = this.scanner.nextToken();
        boolean drag_components_enabled;
        if (next_token == Keyword.ON) {
            drag_components_enabled = true;
        } else if (next_token == Keyword.OFF) {
            drag_components_enabled = false;
        } else {
            FRLogger.warn("GUIDefaultsFile.read_drag_components_enabled_scope: unexpected token");
            return false;
        }
        next_token = this.scanner.nextToken();
        if (next_token != Keyword.CLOSED_BRACKET) {
            FRLogger.warn("GUIDefaultsFile.read_drag_components_enabled_scope: closing bracket expected");
            return false;
        }
        this.boardHandling.settings.set_drag_components_enabled(drag_components_enabled);
        return true;
    }

    private boolean read_ignore_conduction_scope() throws java.io.IOException {
        Object next_token = this.scanner.nextToken();
        boolean ignore_conduction;
        if (next_token == Keyword.ON) {
            ignore_conduction = true;
        } else if (next_token == Keyword.OFF) {
            ignore_conduction = false;
        } else {
            FRLogger.warn("GUIDefaultsFile.read_ignore_conduction_scope: unexpected token");
            return false;
        }
        next_token = this.scanner.nextToken();
        if (next_token != Keyword.CLOSED_BRACKET) {
            FRLogger.warn("GUIDefaultsFile.read_ignore_conduction_scope: closing bracket expected");
            return false;
        }
        this.boardHandling.set_ignore_conduction(ignore_conduction);
        return true;
    }

    private void write_shove_enabled_scope() throws java.io.IOException {
        indentFileWriter.start_scope();
        indentFileWriter.write("shove_enabled ");
        indentFileWriter.new_line();
        if (this.boardHandling.settings.get_push_enabled()) {
            indentFileWriter.write("on");
        } else {
            indentFileWriter.write("off");
        }
        indentFileWriter.end_scope();
    }

    private void write_drag_components_enabled_scope() throws java.io.IOException {
        indentFileWriter.start_scope();
        indentFileWriter.write("drag_components_enabled ");
        indentFileWriter.new_line();
        if (this.boardHandling.settings.get_drag_components_enabled()) {
            indentFileWriter.write("on");
        } else {
            indentFileWriter.write("off");
        }
        indentFileWriter.end_scope();
    }

    private void write_ignore_conduction_scope() throws java.io.IOException {
        indentFileWriter.start_scope();
        indentFileWriter.write("ignore_conduction_areas ");
        indentFileWriter.new_line();
        if (this.boardHandling.getRoutingBoard().rules.get_ignore_conduction()) {
            indentFileWriter.write("on");
        } else {
            indentFileWriter.write("off");
        }
        indentFileWriter.end_scope();
    }

    private void write_selection_layer_scope() throws java.io.IOException {
        indentFileWriter.start_scope();
        indentFileWriter.write("selection_layers ");
        indentFileWriter.new_line();
        if (this.boardHandling.settings.get_select_on_all_visible_layers()) {
            indentFileWriter.write("all_visible");
        } else {
            indentFileWriter.write("current_only");
        }
        indentFileWriter.end_scope();
    }

    private boolean read_route_mode_scope() throws java.io.IOException {
        Object next_token = this.scanner.nextToken();
        boolean is_stitch_mode;
        if (next_token == Keyword.STITCHING) {
            is_stitch_mode = true;
        } else if (next_token == Keyword.DYNAMIC) {
            is_stitch_mode = false;
        } else {
            FRLogger.warn("GUIDefaultsFile.read_roude_mode_scope: unexpected token");
            return false;
        }
        next_token = this.scanner.nextToken();
        if (next_token != Keyword.CLOSED_BRACKET) {
            FRLogger.warn("GUIDefaultsFile.read_selection_layer_scope: closing bracket expected");
            return false;
        }
        this.boardHandling.settings.set_stitch_route(is_stitch_mode);
        return true;
    }

    private void write_route_mode_scope() throws java.io.IOException {
        indentFileWriter.start_scope();
        indentFileWriter.write("route_mode ");
        indentFileWriter.new_line();
        if (this.boardHandling.settings.get_is_stitch_route()) {
            indentFileWriter.write("stitching");
        } else {
            indentFileWriter.write("dynamic");
        }
        indentFileWriter.end_scope();
    }

    private boolean read_pull_tight_region_scope() throws java.io.IOException {
        Object next_token = this.scanner.nextToken();
        if (!(next_token instanceof Integer)) {
            FRLogger.warn("GUIDefaultsFile.read_pull_tight_region_scope: Integer expected");
            return false;
        }
        int pull_tight_region = (Integer) next_token;
        next_token = this.scanner.nextToken();
        if (next_token != Keyword.CLOSED_BRACKET) {
            FRLogger.warn("GUIDefaultsFile.read_pull_tight_region_scope: closing bracket expected");
            return false;
        }
        this.boardHandling.settings.set_current_pull_tight_region_width(pull_tight_region);
        return true;
    }

    private void write_pull_tight_region_scope() throws java.io.IOException {
        indentFileWriter.start_scope();
        indentFileWriter.write("pull_tight_region ");
        indentFileWriter.new_line();
        Integer pull_tight_region = this.boardHandling.settings.get_trace_pull_tight_region_width();
        indentFileWriter.write(pull_tight_region.toString());
        indentFileWriter.end_scope();
    }

    private boolean read_pull_tight_accuracy_scope() throws java.io.IOException {
        Object next_token = this.scanner.nextToken();
        if (!(next_token instanceof Integer)) {
            FRLogger.warn("GUIDefaultsFile.read_pull_tight_accuracy_scope: Integer expected");
            return false;
        }
        int pull_tight_accuracy = (Integer) next_token;
        next_token = this.scanner.nextToken();
        if (next_token != Keyword.CLOSED_BRACKET) {
            FRLogger.warn("GUIDefaultsFile.read_pull_tight_accuracy_scope: closing bracket expected");
            return false;
        }
        this.boardHandling.settings.set_current_pull_tight_accuracy(pull_tight_accuracy);
        return true;
    }

    private void write_pull_tight_accuracy_scope() throws java.io.IOException {
        indentFileWriter.start_scope();
        indentFileWriter.write("pull_tight_accuracy ");
        indentFileWriter.new_line();
        Integer pull_tight_accuracy = this.boardHandling.settings.get_trace_pull_tight_accuracy();
        indentFileWriter.write(pull_tight_accuracy.toString());
        indentFileWriter.end_scope();
    }

    private boolean read_automatic_layer_dimming_scope() throws java.io.IOException {
        Object next_token = this.scanner.nextToken();
        double intensity;
        if (next_token instanceof Double) {
            intensity = (Double) next_token;
        } else if (next_token instanceof Integer) {
            intensity = (Integer) next_token;
        } else {
            FRLogger.warn("GUIDefaultsFile.read_automatic_layer_dimming_scope: Integer expected");
            return false;
        }
        next_token = this.scanner.nextToken();
        if (next_token != Keyword.CLOSED_BRACKET) {
            FRLogger.warn("GUIDefaultsFile.read_automatic_layer_dimming_scope: closing bracket expected");
            return false;
        }
        this.boardHandling.graphicsContext.set_auto_layer_dim_factor(intensity);
        return true;
    }

    private void write_automatic_layer_dimming_scope() throws java.io.IOException {
        indentFileWriter.start_scope();
        indentFileWriter.write("automatic_layer_dimming ");
        indentFileWriter.new_line();
        Float layer_dimming = (float) this.boardHandling.graphicsContext.get_auto_layer_dim_factor();
        indentFileWriter.write(layer_dimming.toString());
        indentFileWriter.end_scope();
    }

    private boolean read_hilight_routing_obstacle_scope() throws java.io.IOException {
        Object next_token = this.scanner.nextToken();
        boolean hilight_obstacle;
        if (next_token == Keyword.ON) {
            hilight_obstacle = true;
        } else if (next_token == Keyword.OFF) {
            hilight_obstacle = false;
        } else {
            FRLogger.warn("GUIDefaultsFile.read_hilight_routing_obstacle_scope: unexpected token");
            return false;
        }
        next_token = this.scanner.nextToken();
        if (next_token != Keyword.CLOSED_BRACKET) {
            FRLogger.warn("GUIDefaultsFile.read_hilight_routing_obstacle_scope: closing bracket expected");
            return false;
        }
        this.boardHandling.settings.set_hilight_routing_obstacle(hilight_obstacle);
        return true;
    }


    private void write_hilight_routing_obstacle_scope() throws java.io.IOException {
        indentFileWriter.start_scope();
        indentFileWriter.write("hilight_routing_obstacle ");
        indentFileWriter.new_line();
        if (this.boardHandling.settings.get_hilight_routing_obstacle()) {
            indentFileWriter.write("on");
        } else {
            indentFileWriter.write("off");
        }
        indentFileWriter.end_scope();
    }

    private boolean read_clearance_compensation_scope() throws java.io.IOException {
        Object next_token = this.scanner.nextToken();
        boolean clearance_compensation;
        if (next_token == Keyword.ON) {
            clearance_compensation = true;
        } else if (next_token == Keyword.OFF) {
            clearance_compensation = false;
        } else {
            FRLogger.warn("GUIDefaultsFile.read_clearance_compensation_scope: unexpected token");
            return false;
        }
        next_token = this.scanner.nextToken();
        if (next_token != Keyword.CLOSED_BRACKET) {
            FRLogger.warn("GUIDefaultsFile.read_clearance_compensation_scope: closing bracket expected");
            return false;
        }
        this.boardHandling.set_clearance_compensation(clearance_compensation);
        return true;
    }

    private void write_clearance_compensation_scope() throws java.io.IOException {
        indentFileWriter.start_scope();
        indentFileWriter.write("clearance_compensation ");
        indentFileWriter.new_line();
        if (this.boardHandling.getRoutingBoard().searchTreeManager.is_clearance_compensation_used()) {
            indentFileWriter.write("on");
        } else {
            indentFileWriter.write("off");
        }
        indentFileWriter.end_scope();
    }

    private boolean read_via_snap_to_smd_center_scope() throws java.io.IOException {
        Object next_token = this.scanner.nextToken();
        boolean snap;
        if (next_token == Keyword.ON) {
            snap = true;
        } else if (next_token == Keyword.OFF) {
            snap = false;
        } else {
            FRLogger.warn("GUIDefaultsFile.read_via_snap_to_smd_center_scope: unexpected token");
            return false;
        }
        next_token = this.scanner.nextToken();
        if (next_token != Keyword.CLOSED_BRACKET) {
            FRLogger.warn("GUIDefaultsFile.read_via_snap_to_smd_center_scope: closing bracket expected");
            return false;
        }
        this.boardHandling.settings.set_via_snap_to_smd_center(snap);
        return true;
    }

    private void write_via_snap_to_smd_center_scope() throws java.io.IOException {
        indentFileWriter.start_scope();
        indentFileWriter.write("via_snap_to_smd_center ");
        indentFileWriter.new_line();
        if (this.boardHandling.settings.get_via_snap_to_smd_center()) {
            indentFileWriter.write("on");
        } else {
            indentFileWriter.write("off");
        }
        indentFileWriter.end_scope();
    }

    private boolean read_selectable_item_scope() throws java.io.IOException {
        ItemSelectionFilter item_selection_filter = this.boardHandling.settings.get_item_selection_filter();
        item_selection_filter.deselect_all();
        for (; ; ) {
            Object next_token = this.scanner.nextToken();
            if (next_token == Keyword.CLOSED_BRACKET) {
                break;
            }
            if (next_token == Keyword.TRACES) {
                item_selection_filter.set_selected(ItemSelectionFilter.SelectableChoices.TRACES, true);
            } else if (next_token == Keyword.VIAS) {
                item_selection_filter.set_selected(ItemSelectionFilter.SelectableChoices.VIAS, true);
            } else if (next_token == Keyword.PINS) {
                item_selection_filter.set_selected(ItemSelectionFilter.SelectableChoices.PINS, true);
            } else if (next_token == Keyword.CONDUCTION) {
                item_selection_filter.set_selected(ItemSelectionFilter.SelectableChoices.CONDUCTION, true);
            } else if (next_token == Keyword.KEEPOUT) {
                item_selection_filter.set_selected(ItemSelectionFilter.SelectableChoices.KEEPOUT, true);
            } else if (next_token == Keyword.VIA_KEEPOUT) {
                item_selection_filter.set_selected(ItemSelectionFilter.SelectableChoices.VIA_KEEPOUT, true);
            } else if (next_token == Keyword.FIXED) {
                item_selection_filter.set_selected(ItemSelectionFilter.SelectableChoices.FIXED, true);
            } else if (next_token == Keyword.UNFIXED) {
                item_selection_filter.set_selected(ItemSelectionFilter.SelectableChoices.UNFIXED, true);
            } else {
                FRLogger.warn("GUIDefaultsFile.read_selectable_item_scope: unexpected token");
                return false;
            }
        }
        return true;
    }


    private void write_selectable_item_scope() throws java.io.IOException {
        indentFileWriter.start_scope();
        indentFileWriter.write("selectable_items ");
        indentFileWriter.new_line();
        ItemSelectionFilter item_selection_filter = this.boardHandling.settings.get_item_selection_filter();
        ItemSelectionFilter.SelectableChoices[] selectable_choices
                = ItemSelectionFilter.SelectableChoices.values();
        for (int i = 0; i < selectable_choices.length; ++i) {
            if (item_selection_filter.is_selected(selectable_choices[i])) {
                indentFileWriter.write(selectable_choices[i].toString());
                indentFileWriter.write(" ");
            }
        }
        indentFileWriter.end_scope();
    }

    private void write_deselected_snapshot_attributes() throws java.io.IOException {
        eu.mihosoft.freerouting.interactive.SnapShot.Attributes attributes = this.boardHandling.settings.get_snapshot_attributes();
        indentFileWriter.start_scope();
        indentFileWriter.write("deselected_snapshot_attributes ");
        if (!attributes.object_colors) {
            indentFileWriter.new_line();
            indentFileWriter.write("object_colors ");
        }
        if (!attributes.object_visibility) {
            indentFileWriter.new_line();
            indentFileWriter.write("object_visibility ");
        }
        if (!attributes.layer_visibility) {
            indentFileWriter.new_line();
            indentFileWriter.write("layer_visibility ");
        }
        if (!attributes.display_region) {
            indentFileWriter.new_line();
            indentFileWriter.write("display_region ");
        }
        if (!attributes.interactive_state) {
            indentFileWriter.new_line();
            indentFileWriter.write("interactive_state ");
        }
        if (!attributes.selection_layers) {
            indentFileWriter.new_line();
            indentFileWriter.write("selection_layers ");
        }
        if (!attributes.selectable_items) {
            indentFileWriter.new_line();
            indentFileWriter.write("selectable_items ");
        }
        if (!attributes.current_layer) {
            indentFileWriter.new_line();
            indentFileWriter.write("current_layer ");
        }
        if (!attributes.rule_selection) {
            indentFileWriter.new_line();
            indentFileWriter.write("rule_selection ");
        }
        if (!attributes.manual_rule_settings) {
            indentFileWriter.new_line();
            indentFileWriter.write("manual_rule_settings ");
        }
        if (!attributes.push_and_shove_enabled) {
            indentFileWriter.new_line();
            indentFileWriter.write("push_and_shove_enabled ");
        }
        if (!attributes.drag_components_enabled) {
            indentFileWriter.new_line();
            indentFileWriter.write("drag_components_enabled ");
        }
        if (!attributes.pull_tight_region) {
            indentFileWriter.new_line();
            indentFileWriter.write("pull_tight_region ");
        }
        if (!attributes.component_grid) {
            indentFileWriter.new_line();
            indentFileWriter.write("component_grid ");
        }
        indentFileWriter.end_scope();
    }

    private boolean read_deselected_snapshot_attributes() throws java.io.IOException {
        eu.mihosoft.freerouting.interactive.SnapShot.Attributes attributes = this.boardHandling.settings.get_snapshot_attributes();
        for (; ; ) {
            Object next_token = this.scanner.nextToken();
            if (next_token == Keyword.CLOSED_BRACKET) {
                break;
            }
            if (next_token == Keyword.OBJECT_COLORS) {
                attributes.object_colors = false;
            } else if (next_token == Keyword.OBJECT_VISIBILITY) {
                attributes.object_visibility = false;
            } else if (next_token == Keyword.LAYER_VISIBILITY) {
                attributes.layer_visibility = false;
            } else if (next_token == Keyword.DISPLAY_REGION) {
                attributes.display_region = false;
            } else if (next_token == Keyword.INTERACTIVE_STATE) {
                attributes.interactive_state = false;
            } else if (next_token == Keyword.SELECTION_LAYERS) {
                attributes.selection_layers = false;
            } else if (next_token == Keyword.SELECTABLE_ITEMS) {
                attributes.selectable_items = false;
            } else if (next_token == Keyword.CURRENT_LAYER) {
                attributes.current_layer = false;
            } else if (next_token == Keyword.RULE_SELECTION) {
                attributes.rule_selection = false;
            } else if (next_token == Keyword.MANUAL_RULE_SETTINGS) {
                attributes.manual_rule_settings = false;
            } else if (next_token == Keyword.PUSH_AND_SHOVE_ENABLED) {
                attributes.push_and_shove_enabled = false;
            } else if (next_token == Keyword.DRAG_COMPONENTS_ENABLED) {
                attributes.drag_components_enabled = false;
            } else if (next_token == Keyword.PULL_TIGHT_REGION) {
                attributes.pull_tight_region = false;
            } else if (next_token == Keyword.COMPONENT_GRID) {
                attributes.component_grid = false;
            } else {
                FRLogger.warn("GUIDefaultsFile.read_deselected_snapshot_attributes: unexpected token");
                return false;
            }
        }
        return true;
    }

    /**
     * Skips the current scope. Returns false, if no legal scope was found.
     */
    private static boolean skip_scope(GUIDefaultsScanner p_scanner) {
        int open_bracked_count = 1;
        while (open_bracked_count > 0) {
            Object curr_token = null;
            try {
                curr_token = p_scanner.nextToken();
            } catch (Exception e) {
                FRLogger.error("GUIDefaultsFile.skip_scope: Error while scanning file", e);
                return false;
            }
            if (curr_token == null) {
                return false; // end of file
            }
            if (curr_token == Keyword.OPEN_BRACKET) {
                ++open_bracked_count;
            } else if (curr_token == Keyword.CLOSED_BRACKET) {
                --open_bracked_count;
            }
        }
        FRLogger.warn("GUIDefaultsFile.skip_spope: unknown scope skipped");
        return true;
    }


}
