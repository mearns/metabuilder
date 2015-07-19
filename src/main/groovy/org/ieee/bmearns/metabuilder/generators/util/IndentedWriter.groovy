
package org.ieee.bmearns.metabuilder.generators.util;

/**
 * Simple utility class to write indented lines of text to an underlying
 * {@link java.io.Writer}.
 */
class IndentedWriter {

    private Writer writer;

    /**
     * The current level of indentation. Zero is flushed to the margin,
     * each subsequent level adds an addition {@link #tabString} to the
     * beginning of each line.
     *
     * Use {@link #setIndent}, {@link #indent(int)}, {@link #outdent(int)},
     * or related methods to set the indent level.
     */
    private int indentLevel;

    /**
     * The string to insert at the beginning of each written line for each
     * {@link #indentLevel}.
     */
    private String tabString;

    /**
     * The current indentation prefix.
     */
    private String indent;

    /**
     * Keeps track of the stack of open blocks. A block is just a placeholder
     * to keep track of indent levels. The indent level should be the same when
     * exiting a block as when entering.
     *
     * @see block(int)
     * @see endBlock(int)
     */
    private Stack<Integer> blockStack;

    public static class Factory {
        private final int indentLevel;
        private final String tabString;

        public static final String INDENT_2_SPACE = "  "
        public static final String INDENT_4_SPACE = "    "
        public static final String INDENT_8_SPACE = "        "
        public static final String INDENT_TAB = "\t"

        public Factory(String tabString, int indentLevel) {
            this.tabString = tabString
            this.indentLevel = indentLevel
        }

        public Factory(String tabString) {
            this(tabString, 0)
        }

        public static Factory TwoSpaces() {
            return new Factory(INDENT_2_SPACE)
        }

        public static Factory FourSpaces() {
            return new Factory(INDENT_4_SPACE)
        }

        public static Factory EightSpaces() {
            return new Factory(INDENT_8_SPACE)
        }

        public static Factory Tab() {
            return new Factory(INDENT_TAB)
        }

        public static Factory TwoSpaces(int level) {
            return new Factory(INDENT_2_SPACE, level)
        }

        public static Factory FourSpaces(int level) {
            return new Factory(INDENT_4_SPACE, level)
        }

        public static Factory EightSpaces(int level) {
            return new Factory(INDENT_8_SPACE, level)
        }

        public static Factory Tab(int level) {
            return new Factory(INDENT_TAB, level)
        }

        public IndentedWriter build(Writer writer) {
            return new IndentedWriter(writer, this.tabString, this.indentLevel);
        }
    }

    IndentedWriter(Writer writer, String tabString, int indentLevel) {
        this.blockStack = new Stack<>();
        this.writer = writer
        this.tabString = tabString
        this.indentLevel = indentLevel
        this.updateIndent()
    }

    public static class BlockException extends RuntimeException {}

    public Factory factory() {
        return new Factory(this.tabString, this.indentLevel)
    }

    public Factory factory(int level) {
        return new Factory(this.tabString, level)
    }

    /**
     * Creates a new {@link IndentedWriter} with the same parameters as this
     * one, but wrapping the given writer.
     */
    public IndentedWriter spawn(Writer writer) {
        return new IndentedWriter(writer, tabString, indentLevel)
    }

    /**
     * Called any time the {@link #indentLevel} is modified, to rebuild the
     * current {@link indent}.
     */
    protected void updateIndent() {
        StringBuilder sb = new StringBuilder(indentLevel * tabString.length())
        for(int i=0; i<indentLevel; i++) {
            sb.append(tabString);
        }
        this.indent = sb.toString()
    }

    /**
     * Closes the underlying {@link #writer}. Doesn't actually change the
     * state of this object, just the writer.
     */
    public void close() {
        writer.close()
    }

    /**
     * Simply writes the given text to the underlying {@link #writer}.
     * Doesn't add the indent prefix or the trailing linebreak
     * like {@link #writeLine} does.
     *
     * @return This object itself, for chaining.
     */
    public IndentedWriter write(String text) {
        writer.write(text)
        return this
    }

    /**
     * Write the given string to the underlying {@link #writer}
     * and adds an end-of-line, but doesn't add the indent prefix
     * like {@link #writeLine} does.
     *
     * @see #endLine()
     * @return This object itself, for chaining.
     */
    public IndentedWriter endLine(String text) {
        writer.write(text)
        endLine()
    }

    /**
     * Ends the current line by writing a line break.
     *
     * @see #endLine(String)
     * @return This object itself, for chaining.
     */
    public IndentedWriter endLine() {
        writer.write('\n')
    }

    /**
     * Skips lines by writing the specified number of line breaks.
     *
     * @return This object itself, for chaining.
     */
    public IndentedWriter skip(int lines) {
        for(int i=0; i<lines; i++) {
            endLine()
        }
        return this
    }

    /**
     * Skip a single line by writing a single linebreak.
     *
     * Delegates to {@link #skip(int)}, passing 1 as the argument.
     *
     * @return This object itself, for chaining.
     */
    public IndentedWriter skip() {
        skip(1)
    }

    /**
     * Creates the specified number of blank lines. A blank line
     * has the current indent string and a line break, but nothing else.
     *
     * @return This object itself, for chaining.
     */
    public IndentedWriter blank(int lines) {
        for(int i=0; i<lines; i++) {
            writeLine("")
        }
        return this
    }
        
    /**
     * Writes a single blank line. A blank line
     * has the current indent string and a line break, but nothing else.
     *
     * Delegates to {@link #blank(int)}, passing 1 as the argument.
     *
     * @return This object itself, for chaining.
     */
    public IndentedWriter blank() {
        blank(1)
    }

    /**
     * Write the given string as an indented line.
     * 
     * The text is prefixed with the current {@link #indent},
     * and ends with a linebreak.
     *
     * @return This object itself, for chaining.
     */
    public IndentedWriter writeLine(String line) {
        write(this.indent)
        endLine(line)
    }

    /**
     * Write the given strings, each as an indented line.
     * 
     * @see #writeLine
     * @return This object itself, for chaining.
     */
    public IndentedWriter writeLines(String... line) {
        line.each{ writeLine(it) }
        this
    }

    /**
     * Pushes a new block onto the {@link #blockStack}.
     *
     * This simply saves the current indent level. When the block is ended
     * (with {@link #endBlock}), it verifies that the indent level is the same
     * as it was when the block was started.
     *
     * @param indent    The number of additional levels to indent <em>after</em>
     *  the block is started.
     *
     * @return This object itself, for chaining.
     */
    public IndentedWriter block(int indent) {
        blockStack.push(this.indentLevel)
        this.indent(indent)
        return this
    }

    /**
     * Pushes a new block without any additional indentation.
     *
     * Delegates to {@link #block(int)}, passing 0 as the parameter.
     *
     * @return This object itself, for chaining.
     */
    public IndentedWriter block() {
        block(0)
    }

    /**
     * Pops a block off the {@link #blockStack}, outdenting by the specified number
     * of indent levels <em>first</em>.
     *
     * If there are no blocks on the stack, throws a {@link IndexOutOfBoundsException}.
     *
     * If the indent level <em>after</em> the specified outdenting does not match the
     * level at the time when the block was opened (e.g., with {@link block(int)}),
     * raises a {@link BlockException}.
     *
     * @param outdent    The number of levels to outdent <em>before</em>
     *  the block is ended.
     *
     * @return This object itself, for chaining.
     */
    public IndentedWriter endBlock(int outdent) 
        throws IndexOutOfBoundsException, BlockException {
        this.outdent(outdent)
        if (blockStack.empty()) {
            throw new IndexOutOfBoundsException("Attempted to close a block, but no blocks currently open.")
        }
        final int expectedLevel = blockStack.pop()
        if (indentLevel != expectedLevel) {
            throw new BlockException("Block ended with incorrect indentation. Expected $expectedLevel but current level is $indentLevel.")
        }
        return this;
    }

    /**
     * Ends a new block without any additional outdent.
     *
     * Delegates to {@link #endBlock(int)}, passing 0 as the parameter.
     *
     * @return This object itself, for chaining.
     */
    public IndentedWriter endBlock() {
        block(0)
    }

    /**
     * Ends the current block, forcing the indent level to the opening level.
     *
     * Unlike {@link #endBlock(int)}, this doesn't throw a {@link BlockException}
     * even if the current indent level doesn't match the indent level from when
     * the block was opened. Instead, it simply sets the indent level to match
     * what it was when opened.
     *
     * @throws IndexOutOfBoundsException If there is no block currently open.
     *
     * @return This object itself, for chaining.
     */
    public IndentedWriter forceBlockEnd()
        throws IndexOutOfBoundsException {
        if (blockStack.empty()) {
            throw new IndexOutOfBoundsException("Attempted to close a block, but no blocks currently open.")
        }
        final int level = blockStack.pop()
        this.setIndent(level)
        return this;
    }

    /**
     * Sets the indent level.
     *
     * @param level The level to set the indent level to. Values less than
     * zero are ignored.
     *
     * @return This object itself, for chaining.
     */
    public IndentedWriter setIndent(int level) {
        if(level >= 0 && this.indentLevel != level) {
            this.indentLevel = level;
            this.updateIndent();
        }
        return this;
    }

    /**
     * Increase the current indent level by the specified amount.
     *
     * @see #setIndent
     * @see #outdent(int)
     * @see #indent()
     *
     * @return This object itself, for chaining.
     */
    public IndentedWriter indent(int levels) {
        indentLevel += levels
        updateIndent()
    }

    /**
     * Decrease the current indent level by the specified amount.
     * If the resulting level would be less than zero, it is ignored.
     *
     * @see #setIndent
     * @see #outdent()
     * @see #indent(int)
     *
     * @return This object itself, for chaining.
     */
    public IndentedWriter outdent(int levels) {
        if (indentLevel >= levels) {
            indentLevel -= levels;
            updateIndent()
        }
    }

    /**
     * Increase the current indent level by one.
     *
     * @see #indent(int)
     * @see #outdent
     *
     * @return This object itself, for chaining.
     */
    public IndentedWriter indent() {
        indent(1)
    }

    /**
     * Decrease the current indent level by one, not less than zero.
     *
     * @see #outdent(int)
     * @see #indent
     *
     * @return This object itself, for chaining.
     */
    public IndentedWriter outdent() {
        outdent(1)
    }

    /**
     * Return the current {@link #indentLevel}.
     */
    public int getIndent() {
        return indentLevel
    }



}
