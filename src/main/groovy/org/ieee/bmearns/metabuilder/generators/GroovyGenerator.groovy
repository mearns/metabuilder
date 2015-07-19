
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

    protected void writeImports(IndentedWriter writer, Buildable buildable) {
        ArrayList<String> imports = new ArrayList<>(buildable.collectImports{ it })

        //Add an import for the Builder interface.
        String builderInterfaceClass = Builder.class.getCanonicalName()
        if (!(builderInterfaceClass in imports)) {
            imports.add(builderInterfaceClass)
        }

        imports.each {
            writer.writeLine("import $it;")
        }
    }

    protected void writePropFields(IndentedWriter writer, Buildable buildable) {

        buildable.props.each {
            String comment = it.comment
            String tail = ""
            if(it.comment != null) {
                writer
                    .skip()
                    .writeLine("/**")
                comment.split(/\n/).each {
                    writer.writeStart(" * ").endLine(it)
                }
                writer
                    .writeLine(" */")
            }

            writer.writeStart("final ${it.type.name}")
            if(it.array) {
                writer.write("[]")
            }
            writer.endLine(" ${it.name};")
            if(it.comment != null) {
                writer.skip()
            }
        }
    }

    @Override
    public void generate(Buildable buildable, Writer writer) {
        IndentedWriter iwriter = writerFactory.build(writer)

        iwriter.writeLines(
            "",
            "package ${buildable.packageName};",
            "",
        )
        writeImports(iwriter, buildable)
        iwriter.writeLines(
            "",
            "class ${buildable.name} implements Builder<${buildable.name}> {",
            ""
        )
        iwriter.block(1)
        writePropFields(iwriter, buildable)
        iwriter.endBlock(1)

    }
}

