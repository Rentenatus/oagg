/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.xt_basis.Match;

/**
 * Adapter for Match objects that implements the new XMLSerializable interface.
 */
public class MatchAdapter extends DomainObjectAdapter<Match> implements agg.xml.core.XMLSerializable {

    public MatchAdapter(Match match) {
        super(match);
    }

    public Match getMatch() {
        return getDomainObject();
    }
}
