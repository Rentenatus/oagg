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

/* Generated By:JavaCC: Do not edit this line. ASCII_CharStream.java Version 0.6 */
/**
 * An implementation of interface CharStream, where the stream is assumed to contain only ASCII characters (without
 * unicode processing).
 *
 * @version $Id: ASCII_CharStream.java,v 1.5 2010/08/23 07:31:25 olga Exp $
 * @author $Author: olga $
 */
public final class ASCII_CharStream {

    int bufsize;

    int available;

    int tokenBegin;

    public int bufpos = -1;

    private int bufline[];

    private int bufcolumn[];

    private int column = 0;

    private int line = 1;

    private boolean prevCharIsCR = false;

    private boolean prevCharIsLF = false;

    private java.io.InputStream inputStream;

    private byte[] buffer;

    private int maxNextCharInd = 0;

    private final void ExpandBuff(boolean wrapAround) {
        byte[] newbuffer = new byte[this.bufsize + 2048];
        int newbufline[] = new int[this.bufsize + 2048];
        int newbufcolumn[] = new int[this.bufsize + 2048];

        try {
            if (wrapAround) {
                System.arraycopy(this.buffer, this.tokenBegin, newbuffer, 0, this.bufsize
                        - this.tokenBegin);
                System.arraycopy(this.buffer, 0, newbuffer, this.bufsize - this.tokenBegin,
                        this.bufpos);
                this.buffer = newbuffer;

                System.arraycopy(this.bufline, this.tokenBegin, newbufline, 0, this.bufsize
                        - this.tokenBegin);
                System.arraycopy(this.bufline, 0, newbufline, this.bufsize - this.tokenBegin,
                        this.bufpos);
                this.bufline = newbufline;

                System.arraycopy(this.bufcolumn, this.tokenBegin, newbufcolumn, 0,
                        this.bufsize - this.tokenBegin);
                System.arraycopy(this.bufcolumn, 0, newbufcolumn, this.bufsize
                        - this.tokenBegin, this.bufpos);
                this.bufcolumn = newbufcolumn;

                this.maxNextCharInd = (this.bufpos += (this.bufsize - this.tokenBegin));
            } else {
                System.arraycopy(this.buffer, this.tokenBegin, newbuffer, 0, this.bufsize
                        - this.tokenBegin);
                this.buffer = newbuffer;

                System.arraycopy(this.bufline, this.tokenBegin, newbufline, 0, this.bufsize
                        - this.tokenBegin);
                this.bufline = newbufline;

                System.arraycopy(this.bufcolumn, this.tokenBegin, newbufcolumn, 0,
                        this.bufsize - this.tokenBegin);
                this.bufcolumn = newbufcolumn;

                this.maxNextCharInd = (this.bufpos -= this.tokenBegin);
            }
        } catch (Throwable t) {
            System.out.println("Error : " + t.getClass().getName());
            throw new Error();
        }

        this.bufsize += 2048;
        this.available = this.bufsize;
        this.tokenBegin = 0;
    }

    private final void FillBuff() throws java.io.IOException {
        if (this.maxNextCharInd == this.available) {
            if (this.available == this.bufsize) {
                if (this.tokenBegin > 2048) {
                    this.bufpos = this.maxNextCharInd = 0;
                    this.available = this.tokenBegin;
                } else if (this.tokenBegin < 0) {
                    this.bufpos = this.maxNextCharInd = 0;
                } else {
                    ExpandBuff(false);
                }
            } else if (this.available > this.tokenBegin) {
                this.available = this.bufsize;
            } else if ((this.tokenBegin - this.available) < 2048) {
                ExpandBuff(true);
            } else {
                this.available = this.tokenBegin;
            }
        }

        int i;
        if ((i = this.inputStream.read(this.buffer, this.maxNextCharInd, this.available
                - this.maxNextCharInd)) == -1) {
            --this.bufpos;
            backup(0);
            if (this.tokenBegin == -1) {
                this.tokenBegin = this.bufpos;
            }
            throw new java.io.IOException();
        }
        this.maxNextCharInd += i;
    }

    public final char BeginToken() throws java.io.IOException {
        this.tokenBegin = -1;
        char c = readChar();
        this.tokenBegin = this.bufpos;

        return c;
    }

    private final void UpdateLineColumn(char c) {
        this.column++;

        if (this.prevCharIsLF) {
            this.prevCharIsLF = false;
            this.line += (this.column = 1);
        } else if (this.prevCharIsCR) {
            this.prevCharIsCR = false;
            if (c == '\n') {
                this.prevCharIsLF = true;
            } else {
                this.line += (this.column = 1);
            }
        }

        switch (c) {
            case '\r':
                this.prevCharIsCR = true;
                break;
            case '\n':
                this.prevCharIsLF = true;
                break;
            case '\t':
                this.column += (9 - (this.column & 07));
                break;
            default:
                break;
        }

        this.bufline[this.bufpos] = this.line;
        this.bufcolumn[this.bufpos] = this.column;
    }

    private int inBuf = 0;

    public final char readChar() throws java.io.IOException {
        if (this.inBuf > 0) {
            --this.inBuf;
            return (char) ((char) 0xff & this.buffer[(this.bufpos == this.bufsize - 1) ? (this.bufpos = 0)
                    : ++this.bufpos]);
        }

        if (++this.bufpos >= this.maxNextCharInd) {
            FillBuff();
        }

        char c = (char) ((char) 0xff & this.buffer[this.bufpos]);

        UpdateLineColumn(c);
        return (c);
    }

    /**
     * @deprecated @see #getEndColumn
     */
    public final int getColumn() {
        return this.bufcolumn[this.bufpos];
    }

    /**
     * @deprecated @see #getEndLine
     */
    public final int getLine() {
        return this.bufline[this.bufpos];
    }

    public final int getEndColumn() {
        return this.bufcolumn[this.bufpos];
    }

    public final int getEndLine() {
        return this.bufline[this.bufpos];
    }

    public final int getBeginColumn() {
        return this.bufcolumn[this.tokenBegin];
    }

    public final int getBeginLine() {
        return this.bufline[this.tokenBegin];
    }

    public final void backup(int amount) {

        this.inBuf += amount;
        if ((this.bufpos -= amount) < 0) {
            this.bufpos += this.bufsize;
        }
    }

    public ASCII_CharStream(java.io.InputStream dstream, int startline,
            int startcolumn, int buffersize) {
        this.inputStream = dstream;
        this.line = startline;
        this.column = startcolumn - 1;

        this.available = this.bufsize = buffersize;
        this.buffer = new byte[buffersize];
        this.bufline = new int[buffersize];
        this.bufcolumn = new int[buffersize];
    }

    public ASCII_CharStream(java.io.InputStream dstream, int startline,
            int startcolumn) {
        this.inputStream = dstream;
        this.line = startline;
        this.column = startcolumn - 1;

        this.available = this.bufsize = 4096;
        this.buffer = new byte[4096];
        this.bufline = new int[4096];
        this.bufcolumn = new int[4096];
    }

    public final String GetImage() {
        if (this.bufpos >= this.tokenBegin) {
            return new String(this.buffer, this.tokenBegin, this.bufpos - this.tokenBegin + 1);
        }

        return new String(this.buffer, this.tokenBegin, this.bufsize - this.tokenBegin)
                + new String(this.buffer, 0, this.bufpos + 1);
    }

    public final byte[] GetSuffix(int len) {
        byte[] ret = new byte[len];

        if ((this.bufpos + 1) >= len) {
            System.arraycopy(this.buffer, this.bufpos - len + 1, ret, 0, len);
        } else {
            System.arraycopy(this.buffer, this.bufsize - (len - this.bufpos - 1), ret, 0, len
                    - this.bufpos - 1);
            System.arraycopy(this.buffer, 0, ret, len - this.bufpos - 1, this.bufpos + 1);
        }

        return ret;
    }

    public void Done() {
        this.buffer = null;
        this.bufline = null;
        this.bufcolumn = null;
    }

}
/*
 * $Log: ASCII_CharStream.java,v $
 * Revision 1.5  2010/08/23 07:31:25  olga
 * tuning
 *
 * Revision 1.4  2010/03/08 15:38:02  olga
 * code optimizing
 *
 * Revision 1.3  2007/09/10 13:05:48  olga
 * In this update:
 * - package xerces2.5.0 is not used anymore;
 * - class com.objectspace.jgl.Pair is replaced by the agg own generic class agg.util.Pair;
 * - bugs fixed in:  usage of PACs in rules;  match completion;
 * 	usage of static method calls in attr. conditions
 * - graph editing: added some new features
 * Revision 1.2 2006/08/09 07:42:18 olga API
 * docu
 * 
 * Revision 1.1 2005/08/25 11:56:52 enrico *** empty log message ***
 * 
 * Revision 1.1 2005/05/30 12:58:01 olga Version with Eclipse
 * 
 * Revision 1.2 2002/09/23 12:23:57 komm added type graph in xt_basis, editor
 * and GUI
 * 
 * Revision 1.1.1.1 2002/07/11 12:17:03 olga Imported sources
 * 
 * Revision 1.7 2000/03/14 10:58:38 shultzke Transformieren von Variablen auf
 * Variablen sollte jetzt funktionieren Ueber das Design der Copy-Methode des
 * abstrakten Syntaxbaumes sollte unbedingt diskutiert werden.
 * 
 */
