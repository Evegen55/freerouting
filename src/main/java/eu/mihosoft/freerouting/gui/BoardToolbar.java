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
 * BoardToolbarPanel.java
 *
 * Created on 15. Februar 2005, 09:44
 */

package eu.mihosoft.freerouting.gui;

import javax.swing.*;

import eu.mihosoft.freerouting.board.Unit;
import eu.mihosoft.freerouting.interactive.InteractiveState;

/**
 * Implements the toolbar panel of the board frame.
 *
 * @author Alfons Wirtz
 */
class BoardToolbar extends JPanel {

    private final BoardFrame board_frame;
    private final JToggleButton select_button;
    private final JToggleButton route_button;
    private final JToggleButton drag_button;
    final JFormattedTextField unitFactorField;
    final JComboBox<Unit> unitComboBox;

    /**
     * Creates a new instance of BoardToolbarPanel
     */
    BoardToolbar(BoardFrame p_board_frame) {
        this.board_frame = p_board_frame;

        java.util.ResourceBundle resources =
                java.util.ResourceBundle.getBundle("eu.mihosoft.freerouting.gui.BoardToolbar", p_board_frame.get_locale());

        this.setLayout(new java.awt.BorderLayout());

        // create the left toolbar

        final javax.swing.JToolBar left_toolbar = new javax.swing.JToolBar();
        final javax.swing.ButtonGroup toolbar_button_group = new javax.swing.ButtonGroup();
        this.select_button = new javax.swing.JToggleButton();
        this.route_button = new javax.swing.JToggleButton();
        this.drag_button = new javax.swing.JToggleButton();
        final javax.swing.JLabel jLabel1 = new javax.swing.JLabel();

        left_toolbar.setMaximumSize(new java.awt.Dimension(1200, 23));
        toolbar_button_group.add(select_button);
        select_button.setSelected(true);
        select_button.setText(resources.getString("select_button"));
        select_button.setToolTipText(resources.getString("select_button_tooltip"));
        select_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                board_frame.boardPanel.boardHandling.set_select_menu_state();
            }
        });

        left_toolbar.add(select_button);

        toolbar_button_group.add(route_button);
        route_button.setText(resources.getString("route_button"));
        route_button.setToolTipText(resources.getString("route_button_tooltip"));
        route_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                board_frame.boardPanel.boardHandling.set_route_menu_state();
            }
        });

        left_toolbar.add(route_button);

        toolbar_button_group.add(drag_button);
        drag_button.setText(resources.getString("drag_button"));
        drag_button.setToolTipText(resources.getString("drag_button_tooltip"));
        drag_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                board_frame.boardPanel.boardHandling.set_drag_menu_state();
            }
        });

        left_toolbar.add(drag_button);

        jLabel1.setMaximumSize(new java.awt.Dimension(30, 10));
        jLabel1.setMinimumSize(new java.awt.Dimension(3, 10));
        jLabel1.setPreferredSize(new java.awt.Dimension(30, 10));
        left_toolbar.add(jLabel1);

        this.add(left_toolbar, java.awt.BorderLayout.WEST);

        // create the middle toolbar

        final javax.swing.JToolBar middle_toolbar = new javax.swing.JToolBar();

        final javax.swing.JButton autoroute_button = new javax.swing.JButton();
        autoroute_button.setText(resources.getString("autoroute_button"));
        autoroute_button.setToolTipText(resources.getString("autoroute_button_tooltip"));
        autoroute_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                board_frame.boardPanel.boardHandling.startBatchAutorouter();
            }
        });

        middle_toolbar.add(autoroute_button);

        final javax.swing.JLabel separator_2 = new javax.swing.JLabel();
        separator_2.setMaximumSize(new java.awt.Dimension(10, 10));
        separator_2.setPreferredSize(new java.awt.Dimension(10, 10));
        separator_2.setRequestFocusEnabled(false);
        middle_toolbar.add(separator_2);

        final javax.swing.JButton undo_button = new javax.swing.JButton();
        undo_button.setText(resources.getString("undo_button"));
        undo_button.setToolTipText(resources.getString("undo_button_tooltip"));
        undo_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                board_frame.boardPanel.boardHandling.cancel_state();
                board_frame.boardPanel.boardHandling.undo();
                board_frame.refreshWindows();
            }
        });

        middle_toolbar.add(undo_button);

        final javax.swing.JButton redo_button = new javax.swing.JButton();
        redo_button.setText(resources.getString("redo_button"));
        redo_button.setToolTipText(resources.getString("redo_button_tooltip"));
        redo_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                board_frame.boardPanel.boardHandling.redo();
            }
        });

        middle_toolbar.add(redo_button);

        final javax.swing.JLabel separator_1 = new javax.swing.JLabel();
        separator_1.setMaximumSize(new java.awt.Dimension(10, 10));
        separator_1.setPreferredSize(new java.awt.Dimension(10, 10));
        middle_toolbar.add(separator_1);

        final javax.swing.JButton incompletes_button = new javax.swing.JButton();
        incompletes_button.setText(resources.getString("incompletes_button"));
        incompletes_button.setToolTipText(resources.getString("incompletes_button_tooltip"));
        incompletes_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                board_frame.boardPanel.boardHandling.toggle_ratsnest();
            }
        });

        middle_toolbar.add(incompletes_button);

        final javax.swing.JButton violation_button = new javax.swing.JButton();
        violation_button.setText(resources.getString("violations_button"));
        violation_button.setToolTipText(resources.getString("violations_button_tooltip"));
        violation_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                board_frame.boardPanel.boardHandling.toggle_clearance_violations();
            }
        });

        middle_toolbar.add(violation_button);

        final javax.swing.JLabel separator_3 = new javax.swing.JLabel();
        separator_3.setMaximumSize(new java.awt.Dimension(10, 10));
        separator_3.setPreferredSize(new java.awt.Dimension(10, 10));
        separator_3.setRequestFocusEnabled(false);
        middle_toolbar.add(separator_3);

        final javax.swing.JButton display_all_button = new javax.swing.JButton();
        display_all_button.setText(resources.getString("display_all_button"));
        display_all_button.setToolTipText(resources.getString("display_all_button_tooltip"));
        display_all_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                board_frame.zoomAll();
            }
        });

        middle_toolbar.add(display_all_button);

        final javax.swing.JButton display_region_button = new javax.swing.JButton();
        display_region_button.setText(resources.getString("display_region_button"));
        display_region_button.setToolTipText(resources.getString("display_region_button_tooltip"));
        display_region_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                board_frame.boardPanel.boardHandling.zoom_region();
            }
        });

        middle_toolbar.add(display_region_button);

        this.add(middle_toolbar, java.awt.BorderLayout.CENTER);

        // create the right toolbar

        final javax.swing.JToolBar right_toolbar = new javax.swing.JToolBar();
        final javax.swing.JLabel unit_label = new javax.swing.JLabel();
        java.text.NumberFormat number_format = java.text.NumberFormat.getInstance(p_board_frame.get_locale());
        number_format.setMaximumFractionDigits(7);
        this.unitFactorField = new javax.swing.JFormattedTextField(number_format);
        final javax.swing.JLabel jLabel4 = new javax.swing.JLabel();

        right_toolbar.setAutoscrolls(true);
        unit_label.setText(resources.getString("unit_button"));
        unit_label.setMaximumSize(new java.awt.Dimension(30, 21));
        unit_label.setPreferredSize(new java.awt.Dimension(30, 21));
        right_toolbar.add(unit_label);

        unitFactorField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        unitFactorField.setValue(1);
        unitFactorField.setMaximumSize(new java.awt.Dimension(100, 18));
        unitFactorField.setMinimumSize(new java.awt.Dimension(40, 18));
        unitFactorField.setPreferredSize(new java.awt.Dimension(80, 18));
        unitFactorField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                if (evt.getKeyChar() == '\n') {
                    Object input = unitFactorField.getValue();
                    if (input instanceof Number) {
                        double input_value = ((Number) input).doubleValue();
                        if (input_value > 0) {
                            board_frame.boardPanel.boardHandling.change_user_unit_factor(input_value);
                        }
                    }
                    double unit_factor = board_frame.boardPanel.boardHandling.coordinateTransform.userUnitFactor;
                    unitFactorField.setValue(unit_factor);

                    board_frame.refreshWindows();
                }
            }
        });

        right_toolbar.add(unitFactorField);

        unitComboBox = new javax.swing.JComboBox<>();
        unitComboBox.setModel(new DefaultComboBoxModel<>(Unit.values()));
        unitComboBox.setFocusTraversalPolicyProvider(true);
        unitComboBox.setInheritsPopupMenu(true);
        unitComboBox.setMaximumSize(new java.awt.Dimension(60, 18));
        unitComboBox.setMinimumSize(new java.awt.Dimension(60, 18));
        unitComboBox.setOpaque(false);
        unitComboBox.setPreferredSize(new java.awt.Dimension(60, 18));
        unitComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eu.mihosoft.freerouting.board.Unit new_unit = (eu.mihosoft.freerouting.board.Unit) unitComboBox.getSelectedItem();
                board_frame.boardPanel.boardHandling.change_user_unit(new_unit);
                board_frame.refreshWindows();
            }
        });

        right_toolbar.add(unitComboBox);

        jLabel4.setMaximumSize(new java.awt.Dimension(30, 14));
        jLabel4.setPreferredSize(new java.awt.Dimension(30, 14));
        right_toolbar.add(jLabel4);

        this.add(right_toolbar, java.awt.BorderLayout.EAST);
    }

    /**
     * Sets the selected button in the menu button button group
     */
    void hilightSelectedButton() {
        InteractiveState interactive_state = board_frame.getBoardPanel()
                .getBoardHandling()
                .get_interactive_state();
        if (interactive_state instanceof eu.mihosoft.freerouting.interactive.RouteMenuState) {
            this.route_button.setSelected(true);
        } else if (interactive_state instanceof eu.mihosoft.freerouting.interactive.DragMenuState) {
            this.drag_button.setSelected(true);
        } else if (interactive_state instanceof eu.mihosoft.freerouting.interactive.SelectMenuState) {
            this.select_button.setSelected(true);
        }
    }


}
