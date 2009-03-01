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
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Andrew Wason, Mike Mills
 * info@photica.com
 *
 * ***** END LICENSE BLOCK ***** */
 
package com.photica.ui;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.Caret;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * ComboBox-like control that displays a label and pops up a text editor to edit that label.
 */
public class PopupEditor extends JButton {

    private static final Icon UP_ARROW = new ArrowIcon(SwingConstants.NORTH);
    private static final Icon DOWN_ARROW = new ArrowIcon(SwingConstants.SOUTH);

    private PromptTextArea textEditor;
    private JScrollPane scrollPane;
    private Dimension preferredEditorSize;
    private JPopupMenu popupMenu;
    private Icon icon;

    public PopupEditor(String promptText, int rows, int columns) {
        // We want left justified text, right justified icon.
        // So we manage our own icon and allocate margin space to draw it in.
        icon = DOWN_ARROW;
        setMargin(new Insets(1, 3, 1, 3 + ArrowIcon.ARROW_WIDTH));

        setHorizontalAlignment(SwingConstants.LEADING);

        // Lock down preferred size
        setText(promptText);
        setPreferredSize(getPreferredSize());
        setText(null);

        textEditor = new PromptTextArea(promptText, rows, columns);
        textEditor.setLineWrap(true);
        textEditor.setWrapStyleWord(true);

        popupMenu = new JPopupMenu();
        scrollPane = new JScrollPane(textEditor, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        popupMenu.add(scrollPane);
        preferredEditorSize = scrollPane.getPreferredSize();
        sizeScrollPane();

        PopupHandler handler = new PopupHandler();
        popupMenu.addPopupMenuListener(handler);

        // We need to register this listener early.
        // JPopupMenu also registers a MouseListener on us (the invoker) and cancels the menu on press.
        addMouseListener(handler);

        enableEvents(ComponentEvent.COMPONENT_EVENT_MASK);
    }

    private void sizeScrollPane() {
        scrollPane.setPreferredSize(new Dimension(Math.max(getWidth(), preferredEditorSize.width),
                preferredEditorSize.height));
    }

    protected ActionListener createActionListener() {
        // Superclass adds listener to the model and refires events to our listeners.
        // We only want our ActionListeners to get notified when edits are committed.
        return null;
    }

    /**
     * Popdown the editor if up, committing the edits and notifying ActionListeners
     */
    public void commitEdit() {
        hideEditor();
    }

    private void showEditor() {
        popupMenu.show(this, 0, getHeight());
    }

    private void hideEditor() {
        // Hide menu if visible, this will trigger PopupMenuListener to commit edits and fire ActionListener
        popupMenu.setVisible(false);
    }

    private void toggleEditor() {
        if (popupMenu.isVisible())
            hideEditor();
        else
            showEditor();
    }

    protected void processComponentEvent(ComponentEvent e) {
        super.processComponentEvent(e);
        // Make editor the max of our width and its preferred width
        if (e.getID() == ComponentEvent.COMPONENT_RESIZED)
            sizeScrollPane();
    }

    // XXX In JDK 1.5 there will be a way to properly disable <html> rendering in components
    // http://developer.java.sun.com/developer/bugParade/bugs/4652898.html
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (TEXT_CHANGED_PROPERTY.equals(propertyName))
            return;
        super.firePropertyChange(propertyName, oldValue, newValue);
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw our icon in the margin space we allocated for it
        icon.paintIcon(this, g,
                getWidth() - getInsets().right,
                getHeight() / 2 - icon.getIconHeight() / 2);
    }

    private class PopupHandler extends MouseAdapter implements PopupMenuListener {
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e) && isEnabled())
                toggleEditor();
        }

        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            textEditor.preparePopup(getText());
            icon = UP_ARROW;
            repaint();
        }

        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            // Copy editor text into button
            setText(textEditor.getText());
            textEditor.preparePopdown();

            icon = DOWN_ARROW;
            repaint();

            // Notify listeners
            ActionEvent event = new ActionEvent(PopupEditor.this, ActionEvent.ACTION_PERFORMED, getActionCommand());
            fireActionPerformed(event);
        }

        public void popupMenuCanceled(PopupMenuEvent e) {
        }
    }
}

class PromptTextArea extends JTextArea implements CaretListener {
    private String promptText;

    public PromptTextArea(String promptText, int rows, int columns) {
        super(rows, columns);
        this.promptText = promptText;
    }

    public void preparePopup(String text) {
        // Copy buttons text into editor
        // XXX the textEditor doesn't get focus if the popup is heavyweight
        // http://developer.java.sun.com/developer/bugParade/bugs/4676748.html
        setText(text);
        addCaretListener(this);
    }

    public void preparePopdown() {
        removeCaretListener(this);
    }

    private boolean canDrawPrompt() {
        Caret caret = getCaret();
        return promptText != null && !caret.isVisible() && !caret.isSelectionVisible() && getDocument().getLength() == 0;
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw prompt if no text and no caret
        if (canDrawPrompt()) {
            FontMetrics fm = g.getFontMetrics();
            Insets insets = getInsets();
            g.setColor(getDisabledTextColor());
            g.drawString(promptText, insets.left, insets.top + fm.getHeight());
        }
    }

    // When the caret or selection becomes visible or moves, redraw to erase prompt and remove listener
    public void caretUpdate(CaretEvent e) {
        if (!canDrawPrompt()) {
            removeCaretListener(this);
            repaint();
        }
    }
}

class ArrowIcon implements Icon {
    public static final int ARROW_WIDTH = 10;
    public static final int ARROW_HEIGHT = 5;

    private int direction;

    public ArrowIcon(int direction) {
        this.direction = direction;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.setColor(c.isEnabled() ? c.getForeground() : UIManager.getColor("ComboBox.disabledForeground"));
        g.translate(x, y);

        switch (direction) {
        case SwingConstants.SOUTH:
            for (int i = 0; i < ARROW_HEIGHT; i++)
                g.drawLine(i, i, i + (ARROW_WIDTH - (i*2 + 1)), i);
            break;

        case SwingConstants.NORTH:
            for (int i = 0; i < ARROW_HEIGHT; i++) {
                int j = ARROW_HEIGHT - (i + 1);
                g.drawLine(i, j, i + (ARROW_WIDTH - (i*2 + 1)), j);
            }
            break;
        }

        g.translate(-x, -y);
    }

    public int getIconWidth() {
        return ARROW_WIDTH;
    }

    public int getIconHeight() {
        return ARROW_HEIGHT;
    }
}