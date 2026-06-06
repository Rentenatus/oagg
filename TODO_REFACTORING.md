# Refactoring Tasks for agg.xt_basis

## Priority 1: Single-Letter Parameters [x] 
**Agent Instruction:** Search and replace all single-letter parameter names in Graph.java, Node.java, Arc.java, BaseFactory.java, GraphObject.java with descriptive English names. Verify no duplicates are created.

- [d] Graph.java - Replace single-letter parameters (addObserver, deleteObserver, setObservers, setKind, setNotificationRequired, setName, setHelpInfo, addNode, removeNode, newNode, newNodeFast, createNode, addArc, removeArc - DONE)
- [ ] Graph.java - Continue with remaining methods (createArc, copyArc, destroyArc, destroyNode, etc.)
- [ ] Node.java - Replace single-letter parameters  
- [ ] Arc.java - Replace remaining single-letter parameters
- [ ] BaseFactory.java - Replace single-letter parameters
- [ ] GraphObject.java - Replace single-letter parameters

## Priority 2: Javadoc Standardization [.] 
**Agent Instruction:** Ensure all Javadoc is in English, complete with @param and @return tags. Remove special characters (ä, ö, ü). Use consistent formatting.

- [ ] Graph.java - Standardize all Javadoc (partially done during Prio 1)
- [ ] Node.java - Standardize all Javadoc
- [ ] Arc.java - Standardize all Javadoc (partially done)
- [ ] GraphObject.java - Standardize all Javadoc
- [ ] GraphOrientation.java (interface) - Standardize Javadoc
- [d] AbstractGraphOrientation.java - Javadoc complete
- [d] GraphOrientationDirected.java - Javadoc complete
- [d] GraphOrientationUndirected.java - Javadoc complete

## Priority 3: GraphElement Interface [.] 
**Agent Instruction:** Create a new GraphElement interface with common methods (isNode(), isArc(), getContext(), getType()). Make GraphObject implement it.

- [ ] Create GraphElement.java interface in agg.xt_basis
- [ ] Update GraphObject.java to implement GraphElement
- [ ] Update all references if needed
- [ ] Verify build passes

## Priority 4: TypeValidation Strategy Pattern [.] 
**Agent Instruction:** Extract type validation logic from TypeSet into a strategy pattern with interface and implementations.

- [ ] Create TypeValidationStrategy.java interface
- [ ] Create TypeValidationDisabled.java implementation
- [ ] Create TypeValidationEnabled.java implementation  
- [ ] Create TypeValidationEnabledMax.java implementation
- [ ] Create TypeValidationEnabledMaxMin.java implementation
- [ ] Update TypeSet.java to use the strategy
- [ ] Verify build passes

## Priority 5: GraphFactory Class [.] 
**Agent Instruction:** Create a dedicated GraphFactory class to consolidate object creation logic from BaseFactory.

- [ ] Create GraphFactory.java class
- [ ] Move graph/node/arc creation methods from BaseFactory to GraphFactory
- [ ] Update BaseFactory to use GraphFactory
- [ ] Verify build passes

## Priority 6: Replace instanceof with Polymorphism [.] 
**Agent Instruction:** Identify instanceof checks that can be replaced with Visitor pattern or polymorphism. Focus on Rule hierarchy first.

- [ ] Analyze Rule hierarchy (Rule, KernelRule, MultiRule, RuleScheme, ParallelRule, ConcurrentRule)
- [ ] Create RuleVisitor interface
- [ ] Implement visitor pattern for rule-specific operations
- [ ] Replace instanceof checks with accept(Visitor) calls
- [ ] Verify build passes

---

## Status Legend
- [.] = Pending (not started)
- [x] = In Progress 
- [d] = Done (completed and verified)
- [s] = Skipped (not applicable or deprecated)

## Current Progress Summary
**Prio 1 (Graph.java):** ~40% complete
- Completed: addObserver, deleteObserver, setObservers, setKind, setNotificationRequired, setName, setHelpInfo, addNode, removeNode, newNode, newNodeFast, createNode (2 variants)
- Remaining: createArc, copyArc, destroyArc, destroyNode (various overloads), newArc, newArcFast, postCreatingArc, copyArc, and many others

**Last Task Resume Point:** Continue with Graph.java - createArc, copyArc, destroyArc methods

## Files Modified So Far
- [d] AbstractGraphOrientation.java (created + Javadoc)
- [d] GraphOrientationDirected.java (refactored + Javadoc)
- [d] GraphOrientationUndirected.java (refactored + Javadoc)
- [x] Graph.java (partial - single-letter params + Javadoc)
- [ ] Node.java (pending)
- [x] Arc.java (partial - single-letter params)
