
package org.ieee.bmearns.metabuilder;

import org.ieee.bmearns.metabuilder.generators.*;

class Main {
    public static void main(String[] args) {

        Buildable props = (new Buildable.BuildableBuilder())
            .name("Property")
            .packageName("org.ieee.bmearns.metabuilder")
            .props(new Buildable.Property.PropertyBuilder().update {
                name "name"
                type new Type("String")
            })
            .props(new Buildable.Property.PropertyBuilder().update {
                name "type"
                type new Type("Type")
            })
            .props(new Buildable.Property.PropertyBuilder().update {
                name "builder"
                type new Type("Type")
            })
            .props(new Buildable.Property.PropertyBuilder().update {
                name "array"
                type new Type("Boolean")
            })
            .props(new Buildable.Property.PropertyBuilder().update {
                name "comment"
                type new Type("String")
            })
            .build()

        Buildable buildable = (new Buildable.BuildableBuilder())
            .name("Buildable")
            .packageName("org.ieee.bmearns.metabuilder")
            .props(new Buildable.Property.PropertyBuilder().update {
                name "name"
                type new Type("String")
            })
            .props(new Buildable.Property.PropertyBuilder().update {
                name "builderName"
                type new Type("String")
            })
            .props(new Buildable.Property.PropertyBuilder().update {
                name "packageName"
                type new Type("String")
            })
            .props(new Buildable.Property.PropertyBuilder().update {
                name "props"
                type new Type("Property")
                array true
                builder new Type("PropertyBuilder")
            })
            .build()

        println props.generate(new GroovyGenerator())
        /*println buildable.generate(new GroovyGenerator())*/
    }
}

