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
package agg.gui.parser.event;

import java.util.EventListener;

/**
 * Listens for events from several options.
 */
public interface OptionListener extends EventListener {

    public void optionEventOccurred(OptionEvent e);

}
