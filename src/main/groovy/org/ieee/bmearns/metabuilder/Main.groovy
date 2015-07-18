
package org.ieee.bmearns.metabuilder;

class Main {
    public static void main(String[] args) {
        Buildable buildable = (new Buildable.BuildableBuilder())
            .name("Airplane")
            .packageName("org.bmearns.ieee.example")
            .build()

        println buildable.generateGroovy()
    }
}

