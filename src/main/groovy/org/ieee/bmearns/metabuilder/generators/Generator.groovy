
package org.ieee.bmearns.metabuilder.generators;

import org.ieee.bmearns.metabuilder.Buildable;

abstract class Generator {

    public String generate(Buildable buildable) {
        StringWriter writer = new StringWriter()
        generate(buildable, writer)
        return writer.toString()
    }

    abstract public void generate(Buildable buildable, Writer writer);

}

