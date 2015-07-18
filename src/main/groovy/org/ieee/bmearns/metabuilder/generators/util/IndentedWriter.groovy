
package org.ieee.bmearns.metabuilder.generators.util;

class IndentedWriter {

    private Writer writer;
    private int indentLevel;
    private String tabString;
    private String indent;

    IndentedWriter(Writer writer, String tabString, int indentLevel) {
        this.writer = writer
        this.tabString = tabString
        this.indentLevel = indentLevel
        this.updateIndent()
    }

    protected void updateIndent() {
        StringBuilder sb = new StringBuilder(indentLevel * tabString.length())
        for(int i=0; i<indentLevel; i++) {
            sb.append(tabString);
        }
        this.indent = sb.toString()
    }

    public void close() {
        writer.close()
    }

    public IndentedWriter writeLine(String line) {
        writer.write(this.indent)
        writer.write(line)
        return this;
    }

    public IndentedWriter writeLines(String... line) {
        line.each{ writeLine(it) }
    }

    public IndentedWriter writeText(String text) {
        text.split(/\n/).each{ writeLine(it) }
    }

    public IndentedWriter indent() {
        indent(1)
    }

    public IndentedWriter outdent() {
        outdent(1)
    }

    public int getIndent() {
        return indentLevel
    }

    public IndentedWriter flushLeft() {
        if(indentLevel != 0) {
            indentLevel = 0
            updateIndent()
        }
    }

    public IndentedWriter indent(int levels) {
        indentLevel += levels
        updateIndent()
    }

    pulic IndentedWriter outdent(int levels) {
        if (indentLevel >= levels) {
            indentLevel -= levels;
            updateIndent()
        }
    }





}
