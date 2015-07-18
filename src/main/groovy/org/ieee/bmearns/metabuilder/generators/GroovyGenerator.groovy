
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
                comment = 
"""
    /**
     $comment
     */
    """
            } else {
                comment = "    "
            }
            
            "${comment}final ${it.type.name} ${it.name};${tail}"
        }.join('\n')

        String builderPropFields = buildable.props.collect {
"        ${it.type.name} ${it.name};"
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

        String builderMethods = buildable.props.collect {
"""
        /**
         * Builder method for the {@link ${buildable.name}#${it.name}} property.
         */
        public ${buildable.builderName} ${it.name}(${it.type.name} ${it.name}) {
            this.${it.name} = ${it.name};
            return this;
        }
"""
        }.join('')


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

$builderPropFields

        @Override
        public ${buildable.name} build() {
            return new ${buildable.name}($constructorArgs);
        }

$builderMethods
    }

}

""")

        return sb.toString()
    }

}

