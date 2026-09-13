/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.parser.ConflictsDependenciesContainer;
import agg.parser.DependencyPairContainer;
import agg.parser.ExcludePairContainer;
import agg.parser.LayerFunction;
import agg.parser.LayeredDependencyPairContainer;
import agg.parser.LayeredExcludePairContainer;
import agg.parser.PriorityDependencyPairContainer;
import agg.parser.PriorityExcludePairContainer;

/**
 * Adapters for parser container classes.
 * These adapters wrap parser container objects and delegate
 * serialization to their XMLObject methods.
 */
public final class ParserAdapters {

    private ParserAdapters() {
    }

    public static final class ConflictsDependenciesContainerAdapter
            extends DomainObjectAdapter<ConflictsDependenciesContainer>
            implements agg.xml.core.XMLSerializable {
        public ConflictsDependenciesContainerAdapter(ConflictsDependenciesContainer container) { super(container); }
        public ConflictsDependenciesContainer getContainer() { return getDomainObject(); }
    }

    public static final class DependencyPairContainerAdapter
            extends DomainObjectAdapter<DependencyPairContainer>
            implements agg.xml.core.XMLSerializable {
        public DependencyPairContainerAdapter(DependencyPairContainer container) { super(container); }
        public DependencyPairContainer getContainer() { return getDomainObject(); }
    }

    public static final class ExcludePairContainerAdapter
            extends DomainObjectAdapter<ExcludePairContainer>
            implements agg.xml.core.XMLSerializable {
        public ExcludePairContainerAdapter(ExcludePairContainer container) { super(container); }
        public ExcludePairContainer getContainer() { return getDomainObject(); }
    }

    public static final class LayerFunctionAdapter
            extends DomainObjectAdapter<LayerFunction>
            implements agg.xml.core.XMLSerializable {
        public LayerFunctionAdapter(LayerFunction layerFunction) { super(layerFunction); }
        public LayerFunction getLayerFunction() { return getDomainObject(); }
    }

    public static final class LayeredDependencyPairContainerAdapter
            extends DomainObjectAdapter<LayeredDependencyPairContainer>
            implements agg.xml.core.XMLSerializable {
        public LayeredDependencyPairContainerAdapter(LayeredDependencyPairContainer container) { super(container); }
        public LayeredDependencyPairContainer getContainer() { return getDomainObject(); }
    }

    public static final class LayeredExcludePairContainerAdapter
            extends DomainObjectAdapter<LayeredExcludePairContainer>
            implements agg.xml.core.XMLSerializable {
        public LayeredExcludePairContainerAdapter(LayeredExcludePairContainer container) { super(container); }
        public LayeredExcludePairContainer getContainer() { return getDomainObject(); }
    }

    public static final class PriorityDependencyPairContainerAdapter
            extends DomainObjectAdapter<PriorityDependencyPairContainer>
            implements agg.xml.core.XMLSerializable {
        public PriorityDependencyPairContainerAdapter(PriorityDependencyPairContainer container) { super(container); }
        public PriorityDependencyPairContainer getContainer() { return getDomainObject(); }
    }

    public static final class PriorityExcludePairContainerAdapter
            extends DomainObjectAdapter<PriorityExcludePairContainer>
            implements agg.xml.core.XMLSerializable {
        public PriorityExcludePairContainerAdapter(PriorityExcludePairContainer container) { super(container); }
        public PriorityExcludePairContainer getContainer() { return getDomainObject(); }
    }
}
