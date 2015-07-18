
package org.ieee.bmearns.metabuilder.generators;

import org.ieee.bmearns.metabuilder.Buildable;
import org.ieee.bmearns.metabuilder.Builder;

class GroovyGenerator extends Generator {

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

        List<String> builderMethods = buildable.props.collect {
            if (it.array) {
                return """        /**
         * Builder method to add one element for the {@link ${buildable.name}#${it.name}} property.
         */
        public ${buildable.builderName} ${it.name}(${it.type.name}... ${it.name}) {
            this.${it.name}.addAll(Arrays.<${it.type.name}>asList(${it.name}))
            return this;
        }"""
            } else {
                return """        /**
         * Builder method for the {@link ${buildable.name}#${it.name}} property.
         */
        public ${buildable.builderName} ${it.name}(${it.type.name} ${it.name}) {
            this.${it.name} = ${it.name};
            return this;
        }"""
            }
        }

        buildable.props.findAll{ it.array }.each {
            String upperCasePropName = it.name.replaceFirst('.') { it.toUpperCase() }
            builderMethods.add("""        /**
         * Builder method to remove all current elements of the {@link ${buildable.name}#${it.name}} property.
         */
        public ${buildable.builderName} clear${upperCasePropName}() {
            this.${it.name}.clear();
            return this;
        }""")
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

