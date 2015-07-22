
package org.ieee.bmearns.metabuilder.generators;

import org.ieee.bmearns.metabuilder.Buildable;
import org.ieee.bmearns.metabuilder.AbstractBuilder;
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

        //Add an import for the AbstractBuilder
        String builderInterfaceClass = AbstractBuilder.class.getCanonicalName()
        if (!(builderInterfaceClass in imports)) {
            imports.add(builderInterfaceClass)
        }

        //And Supplier and Suppliers
        String suppliersImport = 'com.google.common.base.Suppliers'
        if(!(suppliersImport in imports)) {
            imports.add(suppliersImport)
        }
        
        String supplierImport = 'com.google.common.base.Supplier'
        if(!(supplierImport in imports)) {
            imports.add(supplierImport)
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
                writer.writeStart("List<Supplier<${it.type.name}>>")
            }
            else {
                writer.writeStart("Suplier<${it.type.name}>")
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
                    writer.writeLine("${it.name} = new LinkedList<Supplier<${it.type.name}>>();")
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
                        "this.${prop.name}(Arrays.<${prop.type.name}>asList(${prop.name}));",
                        "return this;"
                )
                .endBlock(1).writeLine(
                    "}",
                )
                .skip()
            
            writer
                .writeLines(
                    "/**",
                    " * Builder methods to add any number of elements for the",
                    " * {@link ${buildable.name}#${prop.name}} property.",
                    " */",
                    "public ${buildable.builderName} ${prop.name}(Supplier<${prop.type.name}>... ${prop.name}Suppliers) {"
                )
                .block(1).writeLines(
                        "this.${prop.name}(Arrays.<Supplier<${prop.type.name}>>asList(${prop.name}Supplier));",
                        "return this;"
                )
                .endBlock(1).writeLine(
                    "}",
                )
                .skip()

            if(prop.usesBuilder()) {
                writer
                    .writeLines(
                        "/**",
                        " * Builder methods to add any number of elements for the",
                        " * {@link ${buildable.name}#${prop.name}} property.",
                        " */",
                        "public ${buildable.builderName} ${prop.name}(Closure... ${prop.name}Builders) {"
                    )
                    .block(1).writeLines(
                        "this.${prop.name}(${prop.name}Builders.collect{new ${prop.builder.name}().update(it)});",
                        "return this;"
                    )
                    .endBlock(1).writeLine(
                        "}"
                    )
                    .skip()
            }
            
            writer
                .writeLines(
                    "/**",
                    " * Builder methods to add the given list of elements for the",
                    " * {@link ${buildable.name}#${prop.name}} property.",
                    " */",
                    "public ${buildable.builderName} ${prop.name}(List<${prop.type.name}> ${prop.name}) {"
                )
                .block(1).writeLines(
                    "this.${prop.name}(${prop.name}.collect{Suppliers.<${prop.type.name}>ofInstance(it)});",
                    "return this;"
                )
                .endBlock(1).writeLine(
                    "}"
                )
                .skip()

            writer
                .writeLines(
                    "/**",
                    " * Builder methods to add the given list of elements for the",
                    " * {@link ${buildable.name}#${prop.name}} property.",
                    " */",
                    "public ${buildable.builderName} ${prop.name}(List<Supplier<${prop.type.name}>> ${prop.name}Suppliers) {"
                )
                .block(1).writeLines(
                    "this.${prop.name}.addAll(${prop.name}Suppliers);",
                    "return this;"
                )
                .endBlock(1).writeLine(
                    "}"
                )
                .skip()

            if(prop.usesBuilder()) {
                writer
                    .writeLines(
                        "/**",
                        " * Builder methods to add the given list of elements for the",
                        " * {@link ${buildable.name}#${prop.name}} property.",
                        " */",
                        "public ${buildable.builderName} ${prop.name}(List<Closure>... ${prop.name}Builders) {"
                    )
                    .block(1).writeLines(
                        "this.${prop.name}(${prop.name}Builders.collect{new ${prop.builder.name}().update(it)});",
                        "return this;"
                    )
                    .endBlock(1).writeLine(
                        "}"
                    )
                    .skip()
            }
            
        }
        else {
            writer
                .writeLines(
                    "/**",
                    " * Builder methods to set the value of the",
                    " * {@link ${buildable.name}#${prop.name}} property.",
                    " */",
                    "public ${buildable.builderName} ${prop.name}(Supplier<${prop.type.name}> ${prop.name}Supplier) {"
                )
                .block(1).writeLines(
                    "this.${prop.name} = ${prop.name}Supplier;",
                    "return this;"
                )
                .endBlock(1).writeLine(
                    "}"
                )
                .skip()

            writer
                .writeLines(
                    "/**",
                    " * Builder methods to set the value of the",
                    " * {@link ${buildable.name}#${prop.name}} property.",
                    " */",
                    "public ${buildable.builderName} ${prop.name}(${prop.type.name} ${prop.name}) {"
                )
                .block(1).writeLines(
                    "this.${prop.name}(Suppliers.ofInstance(${prop.name}));",
                    "return this;"
                )
                .endBlock(1).writeLine(
                    "}"
                )
                .skip()

            if(prop.usesBuilder()) {
                writer
                    .writeLines(
                        "/**",
                        " * Builder methods to set the value of the",
                        " * {@link ${buildable.name}#${prop.name}} property.",
                        " */",
                        "public ${buildable.builderName} ${prop.name}(Closure ${prop.name}Builder) {"
                    )
                    .block(1).writeLines(
                        "this.${prop.name}(new ${prop.builder.name}().update(${prop.name}Builder));",
                        "return this;"
                    )
                    .endBlock(1).writeLine(
                        "}"
                    )
                    .skip()
            }
        }
    }

    protected void writeBuilderMethods(IndentedWriter writer, Buildable buildable) {
        buildable.props.each {
            writePropBuilderMethods(writer, buildable, it)
        }
    }

    protected void writeBuildMethod(IndentedWriter writer, Buildable buildable) {
        writer
            .writeLines(
                "/**",
                " * Builds and returns a new {@link ${buildable.name}} instance from the",
                " * currently configured properties.",
                " */",
                "@Override",
                "public ${buildable.name} build() {"
            )
            .block(1)
            .writeStart(
                "return new ${buildable.name}("
            )
            .write(buildable.props.collect {
                "${it.name}.get()"
            }.join(', '))
            .endLine(");")

        writer
            .endBlock(1)
            .writeLine(
                "}"
            )
    }

    protected void writeBuilderClass(IndentedWriter writer, Buildable buildable) {
        //TODO: Use an AbstractBuilder class.
        writer.writeLine("public static class ${buildable.builderName} extends AbstractBuilder<${buildable.name}> {")
        writer.block(1)

        writeBuilderFields(writer, buildable)
        writer.skip()
        writeBuilderInitBlocks(writer, buildable)
        writeBuilderMethods(writer, buildable)
        writeBuildMethod(writer, buildable)

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
            "class ${buildable.name} {",
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

