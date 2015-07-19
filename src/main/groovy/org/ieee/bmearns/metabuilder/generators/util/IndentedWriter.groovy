
package org.ieee.bmearns.metabuilder.generators.util;

class IndentedWriter {

    private Writer writer;
    private int indentLevel;
    private String tabString;
    private String indent;
    private Stack<Integer> blockStack;

    IndentedWriter(Writer writer, String tabString, int indentLevel) {
        this.blockStack = new Stack<>();
        this.writer = writer
        this.tabString = tabString
        this.indentLevel = indentLevel
        this.updateIndent()
    }

    public static class BlockException extends RuntimeException {}

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

    public IndentedWriter block(int indent) {
        blockStack.push(this.indentLevel)
        this.indent(indent)
    }

    public IndentedWriter endBlock(int outdent) 
        throws IndexOutOfBoundsException, BlockException {
        this.outdent(outdent)
        if (blockStack.empty()) {
            throw new IndexOutOfBoundsException("Attempted to close a block, but no blocks currently open.")
        }
        int expectedLevel = blockStack.pop()
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
