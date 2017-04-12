package org.orbiswps.server

import org.orbiswps.groovyapi.input.JDBCColumnInput
import org.orbiswps.groovyapi.input.JDBCTableInput
import org.orbiswps.groovyapi.output.JDBCColumnOutput
import org.orbiswps.groovyapi.output.JDBCTableOutput
import org.orbiswps.groovyapi.process.Process

/********************/
/** Process method **/
/********************/

/**
 * Test script for the JDBCColumn
 * @author Sylvain PALOMINOS
 * @author Erwan Bocher
 */
@Process(title = ["JDBCColumn test","en","Test du JDBCColumn","fr"],
        description = ["Test script using the JDBCColumn ComplexData.","en",
                "Scripts test pour l'usage du ComplexData JDBCColumn.","fr"],
        keywords = ["test,script,wps", "en", "test,scripte,wps", "fr"],
        identifier = "orbisgis:test:jdbccolumn",
        metadata = ["website","metadata"]
)
def processing() {
    jdbcColumnOutput = inputJDBCColumn;
}


/****************/
/** INPUT Data **/
/****************/

@JDBCTableInput(title = "JDBCTable for the JDBCColumn",
        identifier = "orbisgis:test:jdbctable:input")
String jdbcTableInput

/** This JDBCColumn is the input data source. */
@JDBCColumnInput(
        title = ["Input JDBCColumn","en","Entrée JDBCColumn","fr"],
        description = ["A JDBCColumn input.","en","Une entrée JDBCColumn.","fr"],
        keywords = ["input","en","entrée","fr"],
        jdbcTableReference = "orbisgis:test:jdbctable:input",
        excludedTypes = ["BOOLEAN"],
        minOccurs = 0,
        maxOccurs = 2,
        identifier = "orbisgis:test:jdbccolumn:input",
        metadata = ["website","metadata"]
        )
String inputJDBCColumn

/*****************/
/** OUTPUT Data **/
/*****************/

@JDBCTableOutput(title = "JDBCTable for the JDBCColumn",
        identifier = "orbisgis:test:jdbctable:output")
String jdbcTableOutput

/** This JDBCColumn is the output data source. */
@JDBCColumnOutput(
        title = ["Output JDBCColumn","en","Sortie JDBCColumn","fr"],
        description = ["A JDBCColumn output.","en","Une sortie JDBCColumn.","fr"],
        keywords = ["output","en","sortie","fr"],
        jdbcTableReference = "orbisgis:test:jdbctable:output",
        dataTypes = ["GEOMETRY", "NUMBER"],
        multiSelection = true,
        identifier = "orbisgis:test:jdbccolumn:output",
        metadata = ["website","metadata"]
)
String jdbcColumnOutput

