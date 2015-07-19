
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

    protected void writeConstructor(IndentedWriter writer, Buildable buildable) {
        writer.writeStart("public ${buildable.name}(")
        writer.write(
            buildable.props.collect{ "${it.type.name} ${it.name}" }.join(', ')
        )
        writer.endLine(") {")
        writer.block(1)
        buildable.props.each {
            writer.writeLine("this.${it.name} = ${it.name};")
        }
        writer.endBlock(1)
        writer.writeLine("}")
    }

    protected void writeBuilderFields(IndentedWriter writer, Buildable buildable) {
        buildable.props.each {
            if(it.array) {
                writer.writeStart("List<${it.type.name}>")
            }
            else {
                writer.writeStart("${it.type.name}")
            }
            writer.endLine(" ${it.name};")
        }
    }

    protected void writeBuilderInitBlocks(IndentedWriter writer, Buildable buildable) {
        if (buildable.props.any{ it.array }) {
            writer
                .writeLine("{")
                .block(1)

            buildable.props.each {
                if(it.array) {
                    writer.writeLine("${it.name} = new LinkedList<${it.type.name}>();")
                }
            }
                    
            writer
                .endBlock(1)
                .writeLine("}")
                .skip()
        }
    }

    protected void writePropBuilderMethods(IndentedWriter writer, Buildable buildable, Buildable.Property prop) {

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
                .skip()

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
                .skip()
            
            writer
                .writeLines(
                    "/**",
                    " * Builder methods to add the given list of elements for the",
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
                .skip()
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
                .skip()
        }
    }

    protected void writeBuilderMethods(IndentedWriter writer, Buildable buildable) {
        buildable.props.each {
            writePropBuilderMethods(writer, buildable, it)
        }
    }

    protected void writeBuilderClass(IndentedWriter writer, Buildable buildable) {
        //TODO: Use an AbstractBuilder class.
        writer.writeLine("public static class ${buildable.builderName} implements Builder<${buildable.name}> {")
        writer.block(1)

        writeBuilderFields(writer, buildable)
        writer.skip()
        writeBuilderInitBlocks(writer, buildable)
        writeBuilderMethods(writer, buildable)

        writer.endBlock(1)
        writer.writeLine("}")
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

        iwriter.skip()
        writeConstructor(iwriter, buildable)

        iwriter.skip()
        writeBuilderClass(iwriter, buildable)

        iwriter.endBlock(1)
        iwriter.writeLine("}")
    }
}

