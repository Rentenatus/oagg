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
package agg.attribute.parser.javaExpr;

/* JJT: 0.2.2 */
/**
 * @version $Id: BoolNode.java,v 1.2 2007/09/10 13:05:47 olga Exp $
 * @author $Author: olga $
 */
public class BoolNode extends SimpleNode {

    static final long serialVersionUID = 1L;

    BoolNode(String id) {
        super(id);
    }

    public static Node jjtCreate(String id) {
        return new BoolNode(id);
    }

    public void checkContext() throws ASTWrongTypeException {
        setNodeClass(Boolean.TYPE);
    }
}
/*
 * $Log: BoolNode.java,v $
 * Revision 1.2  2007/09/10 13:05:47  olga
 * In this update:
 * - package xerces2.5.0 is not used anymore;
 * - class com.objectspace.jgl.Pair is replaced by the agg own generic class agg.util.Pair;
 * - bugs fixed in:  usage of PACs in rules;  match completion;
 * 	usage of static method calls in attr. conditions
 * - graph editing: added some new features
 * Revision 1.1 2005/08/25 11:56:51 enrico *** empty log
 * message ***
 * 
 * Revision 1.1 2005/05/30 12:58:01 olga Version with Eclipse
 * 
 * Revision 1.2 2003/03/05 18:24:15 komm sorted/optimized import statements
 * 
 * Revision 1.1.1.1 2002/07/11 12:17:04 olga Imported sources
 * 
 * Revision 1.5 2000/04/05 12:10:46 shultzke serialVersionUID aus V1.0.0
 * generiert
 * 
 * Revision 1.4 2000/03/14 10:59:36 shultzke Transformieren von Variablen auf
 * Variablen sollte jetzt funktionieren Ueber das Design der Copy-Methode des
 * abstrakten Syntaxbaumes sollte unbedingt diskutiert werden.
 * 
 */
