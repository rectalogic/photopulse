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

import javax.swing.DefaultListModel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JViewport;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.Autoscroll;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Since ListModel is not generic, we can't control what it contains, so we cast its elements to E when needed.

public class DragList<M extends ListModel,E> extends JList implements Autoscroll {

    // XXX javac crashes with NPE if we declare ListModelMutator(M,E), so use (E,M) instead
    // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4856983

    /**
     * An implementation of this interface must be provided to allow
     * the ListModel implementation to be modified.
     */
    public static interface ListModelMutator<E,M extends ListModel> {
        /** Same semantics as {@link java.util.List#add(int,Object)} */
        public void add(M model, int index, E element);

        /** Same semantics as {@link java.util.List#remove(int)} */
        public void remove(M model, int index);
    }

    /**
     * Helper class that can mutate DefaultListModel.
     */
    public static class DefaultListModelMutator<E,M extends DefaultListModel> implements ListModelMutator<E,M> {
        public void add(M model, int index, E element) {
            model.add(index, element);
        }
        public void remove(M model, int index) {
            model.remove(index);
        }
    }

    /**
     * Helper class that can mutate java.util.List.
     */
    public static class ListMutator<E,M extends ListModel & List<E>> implements ListModelMutator<E,M> {
        public void add(M model, int index, E element) {
            model.add(index, element);
        }
        public void remove(M model, int index) {
            model.remove(index);
        }
    }

    private static final int AUTOSCROLL_OFFSET = 15;

    private ListModelMutator<E,M> listModelMutator;
    private boolean consumeMice = false;

    private int gridRows = -1;
    private int gridColumns = -1;

    /**
     * @param mutator Implementation that understands how to mutate ListModel.
     *   If the mutator is null, then drag and drop is disabled
     */
    public DragList(M model, ListModelMutator<E,M> mutator) {
        super(model);
        setListModelMutator(mutator);
        init();
    }

    private void init() {
        ListDragGestureDropTargetListener listener = new ListDragGestureDropTargetListener();

        // Make this JList a drag source
        DragSource dragSource = DragSource.getDefaultDragSource();
        dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, listener);

        // Make this JList a drag target
        new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, listener);
    }

    public void setListModelMutator(ListModelMutator<E,M> mutator) {
        this.listModelMutator = mutator;
    }

    public ListModelMutator<E,M> getListModelMutator() {
        return listModelMutator;
    }

    public void setModel(ListModel model) {
        super.setModel((M)model);
    }

    public M getModel() {
        return (M)super.getModel();
    }

    public List<E> getSelectedElements() {
        ListSelectionModel selectionModel = getSelectionModel();

        int min = selectionModel.getMinSelectionIndex();
        int max = selectionModel.getMaxSelectionIndex();

        if (min < 0 || max < 0)
            return new ArrayList<E>();

        M model = getModel();
        ArrayList<E> list = new ArrayList<E>(max - min + 1);
        for (int i = min; i <= max; i++) {
            // We have to cast to E
            if (selectionModel.isSelectedIndex(i))
                list.add((E)model.getElementAt(i));
        }
        return list;
    }

    /**
     * Determines visible viewport size.
     * Enabled if rows and columns are >0 and prototypeCellValue is set
     */
    public void setVisibleGrid(int rows, int columns) {
        this.gridRows = rows;
        this.gridColumns = columns;
    }

    /**
     * Return true if the selection is not empty and only a single item is selected.
     */
    public boolean isSelectionSingle() {
        return !isSelectionEmpty() && (getMinSelectionIndex() == getMaxSelectionIndex());
    }

    /**
     * Return true if the selection is not empty and more than one item is selected.
     */
    public boolean isSelectionMultiple() {
        return getMinSelectionIndex() != getMaxSelectionIndex();
    }

    /**
     * Compute size using grid rows/columns and fixed cell size - if all are set
     */
    public Dimension getPreferredScrollableViewportSize() {
        int fixedHeight = getFixedCellHeight();
        int fixedWidth = getFixedCellWidth();

        if (getLayoutOrientation() == JList.VERTICAL || gridRows == -1 || gridColumns == -1
                || fixedHeight == -1 || fixedWidth == -1)
            return super.getPreferredScrollableViewportSize();

        return new Dimension(fixedWidth * gridColumns, fixedHeight * gridRows);
    }

    public Insets getAutoscrollInsets() {
        Rectangle visibleRect = getVisibleRect();
        Dimension listSize = getSize();

        // We offset from each edge of the JList itself to just within the borders of the visible rect.
        //XXX need to cache and recompute only when changes
        return new Insets(visibleRect.y + AUTOSCROLL_OFFSET,
                visibleRect.x + AUTOSCROLL_OFFSET,
                listSize.height - (visibleRect.y + visibleRect.height) + AUTOSCROLL_OFFSET,
                listSize.width - (visibleRect.x + visibleRect.width) + AUTOSCROLL_OFFSET);
    }

    public void autoscroll(Point cursorPoint) {
        Rectangle visibleRect = getVisibleRect();
        Rectangle scrollRect = new Rectangle(cursorPoint.x,  cursorPoint.y, 0, 0);

        // If cursor is to the left, scroll left
        if (cursorPoint.x < visibleRect.x + AUTOSCROLL_OFFSET) {
            int dx = getScrollableUnitIncrement(visibleRect, SwingConstants.HORIZONTAL, -1);
            scrollRect.x = visibleRect.x - dx;
            scrollRect.width = dx;
        }
        // If cursor is to the right, scroll right
        else if (cursorPoint.x > visibleRect.x + visibleRect.width - AUTOSCROLL_OFFSET) {
            int dx = getScrollableUnitIncrement(visibleRect, SwingConstants.HORIZONTAL, 1);
            scrollRect.x = visibleRect.x + visibleRect.width;
            scrollRect.width = dx;
        }

        // If cursor is above, scroll up
        if (cursorPoint.y < visibleRect.y + AUTOSCROLL_OFFSET) {
            int dy = getScrollableUnitIncrement(visibleRect, SwingConstants.VERTICAL, -1);
            scrollRect.y = visibleRect.y - dy;
            scrollRect.height = dy;
        }
        // If cursor is below, scroll down
        else if (cursorPoint.y > visibleRect.y + visibleRect.height - AUTOSCROLL_OFFSET) {
            int dy = getScrollableUnitIncrement(visibleRect, SwingConstants.VERTICAL, 1);
            scrollRect.y = visibleRect.y + visibleRect.height;
            scrollRect.height = dy;
        }

        // Dragging in bitblt mode leaves graphic turds, so use backing store temporarily
        Component parent = getParent();
        if (parent instanceof JViewport) {
            JViewport viewport = (JViewport)parent;
            int mode = viewport.getScrollMode();
            viewport.setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
            scrollRectToVisible(scrollRect);
            viewport.setScrollMode(mode);
        }
        else
            scrollRectToVisible(scrollRect);
    }

    // XXX This works around drag/selection interaction issues
    // javax.swing.plaf.basic.BasicListUI registers two MouseListeners,
    // BasicListUI.MouseInputHandler and BasicListUI.ListDragGestureRecognizer (BasicDragGestureRecognizer subclass).
    // When BasicDragGestureRecognizer thinks it recognizes a drag, it consumes the event as a way
    // of indicating to MouseInputHandler that it should not adjust the selection.
    // So we override processMouseEvent/processMouseMotionEvent and consume events when we do
    // not want the selection modified.
    // This allows multiple selected cells to be dragged without collapsing the selection.
    protected void processMouseEvent(MouseEvent e) {
        consumeMice = false;
        // If there is no selection or a single selection, do not consume.
        // This way the single cell will be selected, and we will also allow it to be dragged.
        // If there is already a multiple selection, then consume so the selection is not modified.
        if (e.getID() == MouseEvent.MOUSE_PRESSED && !isSelectionEmpty() && !isSelectionSingle()) {
            consumeMice = true;
            e.consume();

            // Since we consumed the event, BasicListUI aborts focus and selection handling.
            // But we want focus handling, so do it here.
            // See javax.swing.plaf.basic.BasicListUI.adjustFocusAndSelection
            // Also http://developer.java.sun.com/developer/bugParade/bugs/4122345.html
            if (SwingUtilities.isLeftMouseButton(e) && isEnabled() && !isFocusOwner() && isRequestFocusEnabled())
                requestFocus();
        }
        super.processMouseEvent(e);
    }

    protected void processMouseMotionEvent(MouseEvent e) {
        if (consumeMice && e.getID() == MouseEvent.MOUSE_DRAGGED)
            e.consume();
        super.processMouseMotionEvent(e);
    }

    private class ListDragGestureDropTargetListener implements DragGestureListener, DropTargetListener, DragSourceListener {
        JLayeredPane layeredPane;

        private Point dragCellOffset;
        private CellImage cellImage;
        private InsertionCue insertionCue;
        private int[] sourceSelectedIndices;
        // Where to insert the dragged cells. Shifts the cell at this index to the right.
        private int insertionIndex;

        private Point lastDragOverPoint;

        // DragGestureListener

        public void dragGestureRecognized(DragGestureEvent e) {
            // No drag/drop if no mutator
            if (listModelMutator == null)
                return;

            Point dragOrigin = e.getDragOrigin();
            int dragIndex = locationToIndex(dragOrigin);

            // Drag must start on a selection
            if (dragIndex == -1)
                return;
            Rectangle dragCellBounds = getCellBounds(dragIndex, dragIndex);
            if (!dragCellBounds.contains(dragOrigin) || !DragList.this.isSelectedIndex(dragIndex))
                return;

            // Get drag point offset in cell
            dragCellOffset = new Point(dragOrigin.x - dragCellBounds.x, dragOrigin.y - dragCellBounds.y);

            // Get the cell renderer (which is a JLabel) for the path being dragged
            Component cellRenderer = getCellRenderer().getListCellRendererComponent(
                    DragList.this, getModel().getElementAt(dragIndex),
                    dragIndex, false, false);
            cellRenderer.setBounds(dragCellBounds);

            // Get a buffered image of the selection for dragging a ghost image
            BufferedImage image = new BufferedImage((int)dragCellBounds.getWidth(), (int)dragCellBounds.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
            Graphics2D g2 = image.createGraphics();

            // XXX represent multiple selection drag somehow - special cursor?

            // Ask the cell renderer to paint itself into the BufferedImage
            cellRenderer.paint(g2);
            g2.dispose();

            // Remember the selection being dragged (because if it is being moved, we will have to delete it later)
            sourceSelectedIndices = getSelectedIndices();

            // Wrap the selection being transferred into a Transferable object
            Transferable transferable = new ListSelectionTransferable(sourceSelectedIndices);

            try {
                e.startDrag(null, transferable, this);

                // Add drag components to layered pane, hidden for now
                layeredPane = SwingUtilities.getRootPane(DragList.this).getLayeredPane();
                cellImage = new CellImage(image);
                insertionCue = new InsertionCue();
                cellImage.setVisible(false);
                insertionCue.setVisible(false);
                layeredPane.add(cellImage, JLayeredPane.DRAG_LAYER);
                layeredPane.add(insertionCue, JLayeredPane.POPUP_LAYER);
            } catch (InvalidDnDOperationException ex) {
            }
        }


        // DragSourceListener

        public void dragEnter(DragSourceDragEvent e) {
        }

        public void dragOver(DragSourceDragEvent e) {
        }

        public void dropActionChanged(DragSourceDragEvent e) {
        }

        public void dragExit(DragSourceEvent e) {
        }

        public void dragDropEnd(DragSourceDropEvent e) {
            // Remove drag components
            cellImage.setVisible(false);
            layeredPane.remove(cellImage);
            insertionCue.setVisible(false);
            layeredPane.remove(insertionCue);

            if (e.getDropSuccess()) {
                int action = e.getDropAction();
                // If moving, remove source cells from list
                // (they were already copied to their new locations and indices adjusted)
                if (action == DnDConstants.ACTION_MOVE && listModelMutator != null) {
                    M model = getModel();
                    for (int i = sourceSelectedIndices.length - 1; i >= 0; i--)
                        listModelMutator.remove(model, sourceSelectedIndices[i]);
                }

                // Select the newly inserted cells
                setSelectionInterval(insertionIndex, insertionIndex + sourceSelectedIndices.length - 1);
            }

            layeredPane = null;
            cellImage = null;
            insertionCue = null;
            dragCellOffset = null;
            sourceSelectedIndices = null;
            lastDragOverPoint = null;
        }


        // DropTargetListener

        public void dragEnter(DropTargetDragEvent e) {
            lastDragOverPoint = null;

            if (!isDragAcceptable(e)) {
                e.rejectDrag();
                return;
            }

            e.acceptDrag(e.getDropAction());

            // Position and show drag components
            positionCellImage(e.getLocation());
            layoutInsertionCue(e.getLocation());
            cellImage.setVisible(true);
            insertionCue.setVisible(true);
        }

        public void dragOver(DropTargetDragEvent e) {
            if (!isDragAcceptable(e)) {
                e.rejectDrag();
                return;
            }

            // Even if the mouse is not moving, this method is still invoked 10 times per second
            Point dragOverPoint = e.getLocation();
            if (dragOverPoint.equals(lastDragOverPoint))
                return;

            lastDragOverPoint = dragOverPoint;

            // Position drag components
            positionCellImage(dragOverPoint);
            layoutInsertionCue(dragOverPoint);
        }

        public void dropActionChanged(DropTargetDragEvent e) {
            if (!isDragAcceptable(e))
                e.rejectDrag();
            else
                e.acceptDrag(e.getDropAction());
        }

        public void dragExit(DropTargetEvent e) {
            // Hide drag components
            if (cellImage != null)
                cellImage.setVisible(false);
            if (insertionCue != null)
                insertionCue.setVisible(false);
        }

        public void drop(DropTargetDropEvent e) {
            if (!isDropAcceptable(e)) {
                e.rejectDrop();
                return;
            }
            e.acceptDrop(e.getDropAction());

            Transferable transferable = e.getTransferable();

            boolean dropComplete = false;
            DataFlavor[] flavors = transferable.getTransferDataFlavors();
            for (int flavorIndex = 0; flavorIndex < flavors.length; flavorIndex++ ) {
                DataFlavor flavor = flavors[flavorIndex];
                if (flavor.isMimeTypeEqual(DataFlavor.javaJVMLocalObjectMimeType)) {
                    try {
                        // We are dropping on ourself, so this is the same as sourceSelectedIndices
                        dropComplete = copyCells((int[])transferable.getTransferData(flavor));
                        break;
                    }
                    catch (UnsupportedFlavorException ex) {
                        e.dropComplete(false);
                        return;
                    }
                    catch (IOException ex) {
                        e.dropComplete(false);
                        return;
                    }
                }
            }

            e.dropComplete(dropComplete);
        }

        private boolean copyCells(int[] dragSelectedIndices) {
            if (listModelMutator == null)
                return false;

            M model = getModel();

            // Insert each selected cell after insertionIndex.
            // Iterate backwards so they are inserted in the correct order.
            int leftCount = 0;
            for (int i = dragSelectedIndices.length - 1; i >= 0; i--) {
                // Adjust the indices of cells to the right of insertionIndex to account for the new cells inserted
                int sourceIndex = dragSelectedIndices[i];
                if (sourceIndex >= insertionIndex) {
                    // Need to offset the index we are copying by the number of cells already inserted
                    sourceIndex += dragSelectedIndices.length - 1 - i;
                    // Increment the array entry to account for all inserted cells when we are finished.
                    dragSelectedIndices[i] += dragSelectedIndices.length;
                }
                // Count the number of cells to the left of insertionIndex.
                // insertionIndex needs to be decremented by this amount for when we delete those cells on a move.
                else
                    leftCount++;
                // We must cast model elements to E
                listModelMutator.add(model, insertionIndex, (E)model.getElementAt(sourceIndex));
            }

            insertionIndex -= leftCount;

            return true;
        }

        private void positionCellImage(Point dragPoint) {
            // Translate from list to layeredPane coords
            dragPoint = SwingUtilities.convertPoint(DragList.this, dragPoint, layeredPane);
            cellImage.setLocation(dragPoint.x - dragCellOffset.x, dragPoint.y - dragCellOffset.y);
        }

        private void layoutInsertionCue(Point dragPoint) {
            insertionIndex = DragList.this.locationToIndex(dragPoint);
            Rectangle cueBounds = DragList.this.getCellBounds(insertionIndex, insertionIndex);

            // Position cue to left or right / above or below cell
            int orientation = getLayoutOrientation();
            if (orientation == JList.HORIZONTAL_WRAP) {
                int cellMidX = cueBounds.x + cueBounds.width / 2;
                if (dragPoint.x < cellMidX) {
                    cueBounds.setBounds(cueBounds.x - InsertionCue.CUE_WIDTH/2, cueBounds.y,
                            InsertionCue.CUE_WIDTH, cueBounds.height);
                }
                else {
                    cueBounds.setBounds(cueBounds.x + cueBounds.width - InsertionCue.CUE_WIDTH/2, cueBounds.y,
                            InsertionCue.CUE_WIDTH, cueBounds.height);
                    // Bump up index - insert to the right of the cell
                    insertionIndex++;
                }
            }
            // JList.VERTICAL or JList.VERTICAL_WRAP
            else {
                int cellMidY = cueBounds.y + cueBounds.height / 2;
                if (dragPoint.y < cellMidY) {
                    cueBounds.setBounds(cueBounds.x, cueBounds.y - InsertionCue.CUE_WIDTH/2,
                            cueBounds.width, InsertionCue.CUE_WIDTH);
                }
                else {
                    cueBounds.setBounds(cueBounds.x, cueBounds.y + cueBounds.height - InsertionCue.CUE_WIDTH/2,
                            cueBounds.width, InsertionCue.CUE_WIDTH);
                    // Bump up index - insert below the cell
                    insertionIndex++;
                }
            }

            // Translate from list to layeredPane coords
            cueBounds = SwingUtilities.convertRectangle(DragList.this, cueBounds, layeredPane);
            insertionCue.setBounds(cueBounds);
        }

        private boolean isDragDropAcceptable(DropTargetContext context, int dropAction) {
            // Only accept drags from ourself
            if (context.getComponent() != DragList.this)
                return false;

            // Only accept COPY or MOVE gestures
            if ((dropAction & DnDConstants.ACTION_COPY_OR_MOVE) == 0)
                return false;

            return true;
        }

        private boolean isDragAcceptable(DropTargetDragEvent e) {
            if (!isDragDropAcceptable(e.getDropTargetContext(), e.getDropAction()))
                return false;

            // Only accept this particular flavor
            if (!e.isDataFlavorSupported(ListSelectionTransferable.LISTSELECTION_FLAVOR))
                return false;

            return true;
        }

        private boolean isDropAcceptable(DropTargetDropEvent e) {
            if (!isDragDropAcceptable(e.getDropTargetContext(), e.getDropAction()))
                return false;

            // Only accept this particular flavor
            if (!e.isDataFlavorSupported(ListSelectionTransferable.LISTSELECTION_FLAVOR))
                return false;

            return true;
        }
    }

    private static class ListSelectionTransferable implements Transferable {
        public static final DataFlavor LISTSELECTION_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "DragListSelection");
        private static DataFlavor[] FLAVORS = { LISTSELECTION_FLAVOR };

        private int[] selectedIndices;

        public ListSelectionTransferable(int[] selectedIndices) {
            this.selectedIndices = selectedIndices;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return FLAVORS;
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return LISTSELECTION_FLAVOR.equals(flavor);
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (flavor.isMimeTypeEqual(LISTSELECTION_FLAVOR.getMimeType()))
                return selectedIndices;
            else
                throw new UnsupportedFlavorException(flavor);
        }
    }

    /**
     * Lightweight component to render list cell drag image
     */
    private static class CellImage extends Component {
        private static final Composite ALPHA_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);

        private Image image;
        public CellImage(Image image) {
            this.image = image;
            setSize(image.getWidth(null), image.getHeight(null));
        }
        public boolean isOpaque() {
            return true;
        }
        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D)g;
            g2.setComposite(ALPHA_COMPOSITE);
            g2.drawImage(image, 0, 0, null);
        }
    }

    /**
     * Lightweight component to render insertion cue marker
     */
    private class InsertionCue extends Component {
        public static final int CUE_WIDTH = 4;

        public InsertionCue() {
            setBackground(UIManager.getColor("List.selectionForeground"));
        }
        public boolean isOpaque() {
            return true;
        }
        public void paint(Graphics g) {
            // Clip the insertion cue to the lists visible rect.
            // Otherwise the cue can paint outside the list (since the cues parent is the JLayeredPane).
            Rectangle clipRect = DragList.this.getVisibleRect();
            clipRect = SwingUtilities.convertRectangle(DragList.this, clipRect, this);
            g.clipRect(clipRect.x, clipRect.y, clipRect.width, clipRect.height);

            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
