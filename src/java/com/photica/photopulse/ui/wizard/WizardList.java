/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Photica Photopulse.
 *
 * The Initial Developer of the Original Code is
 * Photica Inc.
 * Portions created by the Initial Developer are Copyright (C) 2002
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Andrew Wason, Mike Mills
 * info@photica.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.photica.photopulse.ui.wizard;

import com.photica.photopulse.model.ShowSelectionModel;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * JList that manages separate selection model.
 * Selected indices in that model are toggled with a click or spacebar press.
 * The internal JList selection model is ignored and not rendered.
 * Also renders cells as toggle buttons and allows visible rows/columns to be set.
 */
public class WizardList extends JList {
    private static WizardListCellRenderer cellRenderer = new WizardListCellRenderer();

    private int visibleRows;
    private int visibleColumns = 1;

    private ShowSelectionModel showSelectionModel;

    public WizardList(ListModel listModel, int rows, int columns) {
        super(listModel);

        // Maintain showSelectionModel state
        addMouseListener(new MouseHandler());

        visibleRows = rows;
        visibleColumns = columns;

        // Set rowcount to force correct number of columns
        setVisibleRowCount(listModel.getSize() / visibleColumns + 1);

        setLayoutOrientation(HORIZONTAL_WRAP);

        setCellRenderer(cellRenderer);
        setBackground(cellRenderer.getBackground());
        setForeground(cellRenderer.getForeground());
        // Draw checkboxes with a bold font
        setFont(cellRenderer.getFont().deriveFont(Font.BOLD));

        // Map spacebar to toggle selection
        ToggleAction action = new ToggleAction();
        String name = (String)action.getValue(Action.NAME);
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), name);
        getActionMap().put(name, action);
    }

    public ShowSelectionModel getShowSelectionModel() {
        return showSelectionModel;
    }

    public void setShowSelectionModel(ShowSelectionModel selectionModel) {
        // Use this model to maintain the checked state of each cell.
        // The internal JList selection model is ignored due to issues with
        // setLeadAnchorNotificationEnabled, focus cell rect etc.
        this.showSelectionModel = selectionModel;
        selectionModel.addShowSelectionListener(new WizardSelectionHandler());
        repaint();
    }

    // Override
    public Dimension getPreferredScrollableViewportSize() {
        // All cells are the same bounds when HORIZONTAL_WRAP
        Rectangle rect = getCellBounds(0, 0);
        if (rect == null)
            return super.getPreferredScrollableViewportSize();
        return new Dimension(rect.width * visibleColumns,
            rect.height * (visibleRows == 0 ? getVisibleRowCount() : visibleRows));
    }

    /**
     * Select or deselect all items
     */
    public void selectAll(boolean select) {
        if (select)
            showSelectionModel.setSelectionInterval(0, getModel().getSize() - 1);
        else
            showSelectionModel.clearSelection();
    }

    private void toggleSelection(int index) {
        if (index == -1)
            return;
        // Toggle the selected index selection
        showSelectionModel.toggleSelectionIndex(index);
    }

    private class MouseHandler extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            toggleSelection(locationToIndex(e.getPoint()));
        }
    }

    private class WizardSelectionHandler implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            // Repaint the cells in the selection range
            Rectangle bounds = getCellBounds(e.getFirstIndex(), e.getLastIndex());
            repaint(bounds);
        }
    }

    private class ToggleAction extends AbstractAction {
        public ToggleAction() {
            super("toggleWizardSelection");
        }

        public void actionPerformed(ActionEvent e) {
            // Toggle the wizard selection for the focussed cell
            int index = getLeadSelectionIndex();
            toggleSelection(index);
        }
    }
}
