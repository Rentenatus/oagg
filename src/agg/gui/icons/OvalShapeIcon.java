/**
 **
 * ***************************************************************************
 * <copyright>
 * Copyright (c) 1995, 2015 Technische Universität Berlin. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * </copyright>
 ******************************************************************************
 */
//$Id: OvalShapeIcon.java,v 1.4 2010/09/23 08:20:21 olga Exp $
package agg.gui.icons;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

public class OvalShapeIcon implements Icon {

    Color color;
    boolean filled;

    public OvalShapeIcon(Color c) {
        this.color = c;
    }

    public OvalShapeIcon(Color c, boolean filledshape) {
        this.color = c;
        this.filled = filledshape;
    }

    public void setColor(Color c) {
        this.color = c;
    }

    public Color getColor() {
        return this.color;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        Color oldColor = g.getColor();
        g.setColor(this.color);
        if (this.filled) {
            g.fillOval(x + 1, y + 1, getIconWidth() - 2, getIconHeight() - 2);
        } else {
            g.drawOval(x + 1, y + 1, getIconWidth() - 2, getIconHeight() - 2);
        }
        g.setColor(oldColor);
    }

    public int getIconWidth() {
        return 14;
    }

    public int getIconHeight() {
        return 10;
    }
}
