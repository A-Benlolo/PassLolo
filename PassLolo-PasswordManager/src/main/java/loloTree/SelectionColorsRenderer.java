package loloTree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

public class SelectionColorsRenderer extends DefaultTreeCellRenderer {
	private static final long serialVersionUID = 1728534187600812902L;
	private Color background;
	private Color foreground;
	
	// Get the cell rendering component
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        
        if(selected)
        	setForeground(foreground);
        
        return this;
    }
	
	// The painting for the background during selection
	public void paintComponent(Graphics g) {
		Color bColor = null, fColor = null;
		int imageOffset = -1;

		// Choose the selection colors
		if (selected) {
			bColor = background;
			fColor = foreground;
		}
		else {
			bColor = getBackgroundNonSelectionColor();
			if (bColor == null)
				bColor = getBackground();
			fColor = getBackgroundNonSelectionColor();
			if (fColor == null)
				fColor = getForeground();
		}
		
		// Get the offset to avoid painting the icon
		imageOffset = getLabelStart();
		g.setColor(bColor);
		if (getComponentOrientation().isLeftToRight()) {
			g.fillRect(imageOffset, 0, getWidth() - imageOffset, getHeight());
		} else {
			g.fillRect(0, 0, getWidth() - imageOffset, getHeight());
		}
		super.paintComponent(g);
	}

	// Set the colors
	public void setColors(Color background, Color foreground) {
		this.background = background;
		this.foreground = foreground;
	}
	
	// Determine the size of the label
	private int getLabelStart() {
		Icon currentI = getIcon();
		if (currentI != null && getText() != null) {
			return currentI.getIconWidth() + Math.max(0, getIconTextGap() - 1);
		}
		return 0;
	}
}
