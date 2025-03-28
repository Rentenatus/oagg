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
package agg.gui.icons;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

public class MatchIcon implements Icon {

    boolean isEnabled;

    Color color;

    public MatchIcon(Color aColor) {
        this.color = aColor;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (this.isEnabled) {
            g.setColor(this.color);
        } else {
            g.setColor(Color.gray.darker());
        }
        g.drawString("M", x + 2, y + 13);
    }

    public int getIconWidth() {
        return 16;
    }

    public int getIconHeight() {
        return 16;
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }
}
