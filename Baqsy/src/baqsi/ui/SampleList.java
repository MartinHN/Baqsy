package baqsi.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;

import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import javax.swing.border.LineBorder;

import baqsi.controllers.Controllers;
import baqsi.model.Audio;
import baqsi.model.Model;



@SuppressWarnings("serial")
public class SampleList extends JList implements DropTargetListener {
	SampleListRenderer renderer;
	SampleMouseAdapter mouseAdapter;
	Controllers c;
	ListSelectionModel selmod;

	DropTarget dt;
	// DragSource ds;
	StringSelection transferable;

	public SampleList(Controllers c) {
		super(c.m);
		this.c = c;
		dt = new DropTarget(this, this);

		setCellRenderer(renderer = new SampleListRenderer());

		this.addMouseListener(mouseAdapter = new SampleMouseAdapter());
		// setFocusable(false);
		setDragEnabled(true);

	}

	public class SampleMouseAdapter extends MouseAdapter {
		// toggle listener
		@Override
		public void mouseClicked(MouseEvent event) {

			JList list = (JList) event.getSource();
			if (((Model) list.getModel()).getSize() != 0) {
				// Get index of item clicked
				int index = list.locationToIndex(event.getPoint());
				Audio item = (Audio) list.getModel().getElementAt(index);

				// c.m.mp.setFile(item.path);

				// Toggle selected state
				if (event.getX() < 25) {
					c.setViewable(index, !item.isViewable());
					// Repaint cell
					list.repaint(list.getCellBounds(index, index));
					c.scalectl.updateTable();
					c.partctl.updateTable();
				}

			}
		}
	}

	public class SampleListRenderer extends JCheckBox implements
			ListCellRenderer {
		public SampleListRenderer() {
			setOpaque(true);

		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {

			setText(value.toString());

			Color background;
			Color foreground;

			setSelected(((Audio) value).isViewable());
			// check if this cell represents the current DnD drop location
			JList.DropLocation dropLocation = list.getDropLocation();
			if (dropLocation != null && !dropLocation.isInsert()
					&& dropLocation.getIndex() == index) {

				background = Color.BLUE;
				foreground = Color.WHITE;

				// check if this cell is selected
			} else if (isSelected) {
				LineBorder e = new LineBorder(Color.BLUE, 2, true);
				setBorder(e);
				setBorderPainted(true);
				
				c.w.graphw.repaint();
				c.w.histw.repaint();

				background = Color.green;
				foreground = ((Audio) value).color;
				

				// unselected, and not the DnD drop location
			} else {
				background = Color.WHITE;
				foreground = ((Audio) value).color;
				setBorderPainted(false);
			}
			;

			setBackground(background);
			setForeground(foreground);

			return this;
		}
	}

	@Override
	public void dragEnter(DropTargetDragEvent arg0) {
		if (arg0.getCurrentDataFlavors()[0].getHumanPresentableName().contains(
				"url")) {
			LineBorder e = new LineBorder(Color.BLUE, 2, true);
			setBorder(e);
		}

	}

	@Override
	public void dragExit(DropTargetEvent arg0) {
		setBorder(null);

	}

	@Override
	public void dragOver(DropTargetDragEvent arg0) {

	}

	@Override
	public void drop(DropTargetDropEvent dtde) {

		setBorder(null);
		try {
			System.out.println("transfert...");
			// Ok, get the dropped object and try to figure out what it is
			Transferable tr = dtde.getTransferable();
			DataFlavor[] flavors = tr.getTransferDataFlavors();
			for (int i = 0; i < flavors.length; i++) {
				// System.out.println("Possible flavor: " +
				// flavors[i].getMimeType());

				// Check for file lists specifically
				if (flavors[i].isFlavorJavaFileListType()) {
					// Great! Accept copy drops...
					dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

					// And add the list of file names to our text area
					java.util.List list = (java.util.List) tr
							.getTransferData(flavors[i]);

					ArrayList<Audio> alist = new ArrayList<Audio>();
					for (int k = 0; k < list.size(); k++) {
						
					
						
						if(((File)list.get(k)).getName().endsWith(".wav")||((File)list.get(k)).getName().endsWith(".WAV")){
							
							Audio a = new Audio((File) list.get(k));
							
							if (!c.savectl.checkandImport(a)&&a.fs != 0)
								alist.add(a);
							else if (a.fs==0)
								System.out.println("erreur d'importation");

						}
						else if(((File)list.get(k)).getName().endsWith(".bqsy")){
							Audio a = c.savectl.RestoreAudio(((File) list.get(k)).getAbsolutePath());
							c.m.importaudio(a);
						}
						
					}
					c.processAudio(alist);

					// If we made it this far, everything worked.
					dtde.dropComplete(true);
					return;
				}
				// Ok, is it another Java object?
				else if (flavors[i].isFlavorSerializedObjectType()) {
					dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

					Object o = tr.getTransferData(flavors[i]);

					dtde.dropComplete(true);
					return;
				}
				// How about an input stream?
				else if (flavors[i].isRepresentationClassInputStream()) {
					dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
					dtde.dropComplete(true);
					return;
				}
			}
			// Hmm, the user must not have dropped a file list
			System.out.println("Drop failed: " + dtde);
			dtde.rejectDrop();
		} catch (Exception e) {
			e.printStackTrace();
			dtde.rejectDrop();
		}
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent arg0) {

	}

}
