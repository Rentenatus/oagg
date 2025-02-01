/**
 * <copyright>
 * Copyright (c) 1995, 2015 Technische Universität Berlin. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
// $Id: CenterOfPoints.java,v 1.6 2010/08/23 07:32:23 olga Exp $
package agg.editor.impl;

import java.awt.Point;
import java.util.List;

/**
 * A CenterOfPoints specifies a center of the polygon defined as a vector of points
 */
public class CenterOfPoints {

    /**
     * Creates a new CenterOfPoints whose center is (0,0) and whose points are specified by the List argument.
     */
    public CenterOfPoints(List<Point> v) {
        this.vec = v;
    }

    public void setPoints(List<Point> v) {
        this.vec = v;
    }

    public Point getCenter() {
        // System.out.println(">>> CenterOfPoints.getCenter");
        Point c = getCenterOfPoints(this.vec);
        return c;
    }

    private Point getCenterOfPoints(List<Point> v) {
        int sumx = 0;
        int sumy = 0;
        for (int i = 0; i < v.size(); i++) {
            sumx = sumx + v.get(i).x;
            sumy = sumy + v.get(i).y;
        }
        if (v.size() != 0) {
            return new Point(sumx / v.size(), sumy / v.size());
        }

        return new Point(0, 0);
    }

    private List<Point> vec;
}
