
package org.bmearns.ieee.metabuilder;

class Type {

    final String name
    final String importable

    Type(String name, String importable) {
        this.name = name
        this.importable = importable
    }

    Type(String name) {
        this.name = name
        this.importable = null
    }
}

