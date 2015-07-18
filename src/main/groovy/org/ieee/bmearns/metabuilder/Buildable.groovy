
package org.ieee.bmearns.metabuilder;

class Buildable {

    final String name
    final String packageName
    final Property[] props

    protected Buildable(String name, String packageName, Property[] props) {
        this.name = name
        this.packageName = packageName
        this.props = props
    }

    Builder<Buildable> builder() {
        return new BuildablePseudoBuilder(this)
    }


    String generateGroovy() {
        StringBuilder sb = new StringBuilder()

        sb.append("""

package ${packageName};

class ${name} {

}

""")

        return sb.toString()
    }


    public static class BuildablePseudoBuilder implements Builder<Buildable> {
        final Buildable buildable
        BuildablePseudoBuilder(Buildable buildable) {
            this.buildable = buildable
        }

        Buildable build() {
            return this.buildable
        }
    }

    public static class BuildableBuilder implements Builder<Buildable> {
        String name
        String packageName
        List<Builder<Property>> props

        @Override
        Buildable build() {
            return new Buildable(name, packageName, props.collect { it.build() }.toArray(new Property[0]))
        }

        BuildableBuilder name(String name) {
            this.name = name
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
        final String name

        Property(Type type, String name) {
            this.type = type
            this.name = name
        }

        Builder<Property> builder() {
            new PropertyPseudoBuilder(this)
        }

        public static class PropertyPseudoBuilder implements Builder<Property> {
            final Property property
            PropertyPseudoBuilder(Property property) {
                this.property = property
            }

            @Override
            Property build() {
                return this.property
            }
        }

        public static class PropertyBuilder implements Builder<Property> {
            Type type
            String name

            PropertyBuilder type(Type type) {
                this.type = type
                this
            }

            PropertyBuilder name(String name) {
                this.name = name
                this
            }

            @Override
            Property build() {
                new Property(type, name)
            }
        }
    }
}

