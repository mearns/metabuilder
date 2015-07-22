
package org.ieee.bmearns.metabuilder;

import org.ieee.bmearns.metabuilder.generators.*;

class Main {
    public static void main(String[] args) {
        Buildable buildable = (new Buildable.BuildableBuilder())
            .name("Airplane")
            .packageName("org.ieee.bmearns.example.planes")
            .props(new Buildable.Property.PropertyBuilder()
                .name("wings")
                .type(new Type("Wing", "org.ieee.bmearns.example.planes.parts"))
                .array(true)
                .builder(new Type("WingBuilder", "org.ieee.bmearns.example.planes.parts"))
                .comment("Specifies the wings that (typically) stick out of the side of the plane.")
            )
            .props(new Buildable.Property.PropertyBuilder()
                .name("tail")
                .type(new Type("Tail", "org.ieee.bmearns.example.planes.parts"))
                .builder(new Type("TailBuilder", "org.ieee.bmearns.example.planes.parts"))
            )
            .props(new Buildable.Property.PropertyBuilder()
                .name("model")
                .type(new Type("Model", "org.ieee.bmearns.example.planes"))
            )
            .props(new Buildable.Property.PropertyBuilder()
                .name("seats")
                .type(new Type("int"))
            )
            .build()

        println buildable.generate(new GroovyGenerator())
    }
}

