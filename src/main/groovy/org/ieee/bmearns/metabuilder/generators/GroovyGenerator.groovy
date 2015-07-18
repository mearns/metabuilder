
package org.ieee.bmearns.metabuilder.generators;

import org.ieee.bmearns.metabuilder.Buildable;
import org.ieee.bmearns.metabuilder.Builder;

class GroovyGenerator extends Generator {

    protected List<String> generateBuilderMethods(Buildable buildable, Buildable.Property prop) {
        List<String> methods;

        if(prop.array) {
            methods = new ArrayList<>(3);

            String upperCasePropName = prop.name.replaceFirst('.') { it.toUpperCase() }

            methods.add """        /**
         * Builder methods to clear out all elements currently specified for
         * the {@link ${buildable.name}#${prop.name}} property.
         */
        public ${buildable.builderName} clear${upperCasePropName}() {
            this.${prop.name}.clear();
            return this;
        }"""

            methods.add """        /**
         * Builder methods to add any number of elements for the
         * {@link ${buildable.name}#${prop.name}} property.
         */
        public ${buildable.builderName} ${prop.name}(${prop.type.name}... ${prop.name}) {
            this.${prop.name}.addAll(Arrays.<${prop.type.name}>asList(${prop.name}));
            return this;
        }"""
            
            methods.add """        /**
         * Builder methods to add any number of elements for the
         * {@link ${buildable.name}#${prop.name}} property.
         */
        public ${buildable.builderName} ${prop.name}(List<${prop.type.name}> ${prop.name}) {
            this.${prop.name}.addAll(${prop.name});
            return this;
        }"""
            
        }
        else {
            methods = new ArrayList<>(1)
            methods.add """        /**
         * Builder methods to set the value of the
         * {@link ${buildable.name}#${prop.name}} property.
         */
        public ${buildable.builderName} ${prop.name}(${prop.type.name} ${prop.name}) {
            this.${prop.name} = ${prop.name};
            return this;
        }"""
        }

        return methods
    }

    @Override
    public String generate(Buildable buildable) {
        StringBuilder sb = new StringBuilder()

        ArrayList<String> imports = new ArrayList<>(buildable.collectImports{ it })
        String builderInterfaceClass = Builder.class.getCanonicalName()
        if (!(builderInterfaceClass in imports)) {
            imports.add(builderInterfaceClass)
        }
        String importStatements = imports.collect{ "import $it;" }.join('\n')

        String propFields = buildable.props.collect {
            String comment = it.comment
            String tail = ""
            if(it.comment != null) {
                tail = "\n"
                comment = comment.split(/[\r\n]/).collect{ "     * ${it}" }.join('\n')
                comment = 
"""
    /**
$comment
     */
    """
            } else {
                comment = "    "
            }
            
            String arrayOp = ""
            if(it.array) {
                arrayOp = "[]";
            }

            "${comment}final ${it.type.name}${arrayOp} ${it.name};${tail}"
        }.join('\n')

        List<String> builderInitializers = new ArrayList<>(buildable.props.length)
        buildable.props.each {
            if (it.array) {
                builderInitializers.add("${it.name} = new LinkedList<${it.type.name}>();")
            }
        }

        String builderInitializerBlock = ""
        if (builderInitializers.size() > 0) {
            builderInitializerBlock = """

        {
${builderInitializers.collect{"            $it"}.join('\n')}
        }
"""
        }

        String builderPropFields = buildable.props.collect {
            String type;
            if (it.array) {
                type = "List<${it.type.name}>"
            } else {
                type = it.type.name
            }
"        ${type} ${it.name};"
        }.join('\n')

        String constructorParams = buildable.props.collect {
            "${it.type.name} ${it.name}"
        }.join(', ')

        String constructorArgs = buildable.props.collect {
            it.name
        }.join(', ')

        String constructorBody = buildable.props.collect {
"        this.${it.name} = ${it.name};"
        }.join('\n')

        String buildableField = buildable.name.replaceFirst('.') { it.toLowerCase() }

        List<String> builderMethods = new ArrayList<>(buildable.props.length*2)
        buildable.props.each {
            builderMethods.addAll(this.generateBuilderMethods(buildable, it))
        }

        sb.append("""

package ${buildable.packageName};

$importStatements

class ${buildable.name} implements Builder<${buildable.name}> {

$propFields

    ${buildable.name} (${constructorParams}) {
${constructorBody}
    }

    /**
     * Returns this object itself, to implement the @{link ${builderInterfaceClass})
     * interface.
     */
    @Override
    public ${buildable.name} build() {
        return this;
    }

    public class ${buildable.builderName} implements Builder<${buildable.name}> {

${builderPropFields}${builderInitializerBlock}

        @Override
        public ${buildable.name} build() {
            return new ${buildable.name}($constructorArgs);
        }

${builderMethods.join('\n\n')}
    }

}

""")

        return sb.toString()
    }

}

