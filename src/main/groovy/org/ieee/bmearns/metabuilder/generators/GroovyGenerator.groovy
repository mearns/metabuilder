
package org.ieee.bmearns.metabuilder.generators;

import org.ieee.bmearns.metabuilder.Buildable;
import org.ieee.bmearns.metabuilder.Builder;
import org.ieee.bmearns.metabuilder.generators.util.IndentedWriter;

class GroovyGenerator extends Generator {

    IndentedWriter.Factory writerFactory

    public GroovyGenerator(IndentedWriter.Factory writerFactory) {
        this.writerFactory = writerFactory
    }

    public GroovyGenerator(IndentedWriter writerTemplate) {
        this(writerTemplate.factory())
    }

    public GroovyGenerator() {
        this(new IndentedWriter.Factory("    ", 0))
    }

    protected void writeBuilderMethods(IndentedWriter writer, Buildable buildable, Buildable.Property prop) {

        if(prop.array) {

            String upperCasePropName = prop.name.replaceFirst('.') { it.toUpperCase() }

            writer
                .writeLines(
                    "/**",
                    " * Builder method to clear out all elements currently specified for",
                    " * the {@link ${buildable.name}#${prop.name}} property.",
                    " */",
                    "public ${buildable.builderName} clear${upperCasePropName}() {"
                )
                .block(1).writeLines(
                        "this.${prop.name}.clear();",
                        "return this;"
                )
                .endBlock(1).writeLine(
                    "}"
                )

            writer
                .writeLines(
                    "/**",
                    " * Builder methods to add any number of elements for the",
                    " * {@link ${buildable.name}#${prop.name}} property.",
                    " */",
                    "public ${buildable.builderName} ${prop.name}(${prop.type.name}... ${prop.name}) {"
                )
                .block(1).writeLines(
                        "this.${prop.name}.addAll(Arrays.<${prop.type.name}>asList(${prop.name}));",
                        "return this;"
                )
                .endBlock(1).writeLine(
                    "}",
                )
            
            writer
                .writeLines(
                    "/**",
                    " * Builder methods to add any number of elements for the",
                    " * {@link ${buildable.name}#${prop.name}} property.",
                    " */",
                    "public ${buildable.builderName} ${prop.name}(List<${prop.type.name}> ${prop.name}) {"
                )
                .block(1).writeLines(
                    "this.${prop.name}.addAll(${prop.name});",
                    "return this;"
                )
                .endBlock(1).writeLine(
                    "}"
                )
            
        }
        else {
            writer
                .writeLines(
                    "/**",
                    " * Builder methods to set the value of the",
                    " * {@link ${buildable.name}#${prop.name}} property.",
                    " */",
                    "public ${buildable.builderName} ${prop.name}(${prop.type.name} ${prop.name}) {"
                )
                .block(1).writeLines(
                    "this.${prop.name} = ${prop.name};",
                    "return this;"
                )
                .endBlock(1).writeLine(
                    "}"
                )
        }
    }

    @Override
    public void generate(Buildable buildable, Writer writer) {
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

        StringWriter sw = new StringWriter()
        buildable.props.each {
            this.writeBuilderMethods(this.writerFactory.build(sw), buildable, it)
        }
        String builderMethods = sw.toString()

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

${builderMethods}
    }

}

""")

        writer.write(sb.toString())
    }

}

