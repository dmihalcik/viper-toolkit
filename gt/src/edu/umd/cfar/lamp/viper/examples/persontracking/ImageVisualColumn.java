package edu.umd.cfar.lamp.viper.examples.persontracking;

import infovis.*;
import infovis.column.*;
import infovis.visualization.render.*;

import java.awt.*;
import java.awt.geom.*;

public class ImageVisualColumn extends AbstractVisualColumn {
	private ObjectColumn urlColumn;

	public ImageVisualColumn(String name) {
		super(name);
	}

    public Column getColumn() {
        return urlColumn;
    }

    public void setColumn(Column column) {
        if (column == urlColumn)
            return;
        super.setColumn(column);
        this.urlColumn = (ObjectColumn) column;
        invalidate();
    }
	
    public void paint(Graphics2D graphics, int row, Shape shape) {
        Rectangle2D bounds = shape.getBounds2D();
        Image icon = (Image) urlColumn.get(row);
        if (bounds.getWidth() >= 3 && bounds.getWidth() >= 3
                && icon != null) {
            int iw = icon.getWidth(null);
            int ih = icon.getHeight(null);
            if (iw != -1 && ih != -1 && !(iw == 1 && ih == 1)) {
                double sx = bounds.getWidth() / iw;
                double sy = bounds.getHeight() / ih;

                if (sx < sy) {
                    sy = sx;
                    iw = (int) (bounds.getWidth());
                    ih = (int) (ih * sy);
                } else {
                    sx = sy;
                    iw = (int) (iw * sx);
                    ih = (int) (bounds.getHeight());
                }

                //System.out.println("Painting " + row);
                int x = (int) (bounds.getX() + (bounds.getWidth() - iw) / 2);
                int y = (int) (bounds.getY() + (bounds.getHeight() - ih) / 2);
                graphics
                        .drawImage(icon, x, y, x + iw, y + ih, 0, 0,
                                icon.getWidth(null), icon
                                        .getHeight(null), null);
            }
        }
        super.paint(graphics, row, shape);
    }
}
