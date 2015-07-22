
package org.ieee.bmearns.metabuilder;

import org.ieee.bmearns.metabuilder.generators.Generator;

class Buildable {

    final String name
    final String builderName
    final String pseudoBuilderName
    final String packageName
    final Property[] props

    //XXX: Allow use of Builders for properties.

    protected Buildable(String name, String builderName, String pseudoBuilderName, String packageName, Property[] props) {
        this.name = name
        this.builderName = builderName
        this.pseudoBuilderName = pseudoBuilderName
        this.packageName = packageName
        this.props = props

        //TODO: Check for duplicate property names.
    }

    String generate(Generator gen) {
        gen.generate(this)
    }

    def collectImports(Closure visitor) {
        this.props
            .collect{ it.type.importable }
            .findAll{ it != null && it.compareTo(packageName) != 0 }
            .unique(false)
            .collect(visitor)
    }

    Builder<Buildable> builder() {
        return new BuildablePseudoBuilder(this)
    }

    public static class BuildablePseudoBuilder extends AbstractBuilder<Buildable, BuildablePseudoBuilder> {
        final Buildable buildable
        BuildablePseudoBuilder(Buildable buildable) {
            this.buildable = buildable
        }

        Buildable build() {
            return this.buildable
        }
    }

    public static class BuildableBuilder extends AbstractBuilder<Buildable, BuildableBuilder> {
        String name
        String builderName
        String pseudoBuilderName
        String packageName
        List<Builder<Property>> props

        {
            props = new LinkedList<Builder<Property>>()
        }

        @Override
        Buildable build() {
            String builderName = this.builderName ?: (this.name ? (this.name + "Builder") : null)
            String pseudoBuilderName = this.pseudoBuilderName ?: (this.name ? (this.name + "PseudoBuilder") : null)
            return new Buildable(name, builderName, pseudoBuilderName, packageName, props.collect { it.build() }.toArray(new Property[0]))
        }

        BuildableBuilder name(String name) {
            this.name = name
            return this
        }

        BuildableBuilder builderName(String builderName) {
            this.builderName = builderName
            return this
        }

        BuildableBuilder pseudoBuilderName(String pseudoBuilderName) {
            this.pseudoBuilderName = pseudoBuilderName
            return this
        }

        BuildableBuilder packageName(String packageName) {
            this.packageName = packageName
            return this
        }

        BuildableBuilder props(Builder<Property>... propBuilders) {
            propBuilders.each { this.props.add(it) }
            this
        }

        BuildableBuilder props(Property... props) {
            propBuilders.each { this.props.add(props.builder()) }
            this
        }
    }

    public static class Property {
        final Type type
        final boolean array
        final String name
        final String comment

        Property(Type type, boolean array, String name, String comment) {
            this.type = type
            this.array = array
            this.name = name
            this.comment = comment
        }

        Builder<Property> builder() {
            new PropertyPseudoBuilder(this)
        }

        public boolean usesBuilder() {
            return this.builder != null
        }

        public static class PropertyPseudoBuilder extends AbstractBuilder<Property, PropertyPseudoBuilder> {
            final Property property
            PropertyPseudoBuilder(Property property) {
                this.property = property
            }

            @Override
            Property build() {
                return this.property
            }
        }

        public static class PropertyBuilder extends AbstractBuilder<Property, PropertyBuilder> {
            Type type
            boolean array
            String name
            String comment

            PropertyBuilder type(Type type) {
                this.type = type
                this
            }

            PropertyBuilder array(boolean array) {
                this.array = array
                this
            }

            PropertyBuilder name(String name) {
                this.name = name
                this
            }

            PropertyBuilder comment(String comment) {
                this.comment = comment
                this
            }

            @Override
            Property build() {
                new Property(type, array, name, comment)
            }
        }
    }

}

