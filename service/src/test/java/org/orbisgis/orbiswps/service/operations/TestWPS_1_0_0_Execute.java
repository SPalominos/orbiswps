package org.orbisgis.orbiswps.service.operations;

import net.opengis.ows._1.BoundingBoxType;
import net.opengis.ows._1.CodeType;
import net.opengis.ows._1.ExceptionReport;
import net.opengis.wps._1_0_0.*;
import org.junit.Before;
import org.junit.Test;
import org.orbisgis.orbiswps.service.WpsServerImpl;
import org.orbisgis.orbiswps.service.process.ProcessManager;
import org.orbisgis.orbiswps.serviceapi.operations.WPS_1_0_0_Operations;

import javax.xml.namespace.QName;
import java.io.File;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * Test class for the WPS_2_0_OperationsImpl
 *
 * @author Sylvain PALOMINOS
 */
public class TestWPS_1_0_0_Execute {

    /** Wps Operation object. */
    private WPS_1_0_0_Operations minWps100Operations;
    private WPS_1_0_0_Operations fullWps100Operations;

    /**
     * Initialize a wps service for processing all the tests.
     */
    @Before
    public void initialize() {

        WpsServerImpl wpsServer = new WpsServerImpl();
        ProcessManager processManager = new ProcessManager(null, wpsServer);
        try {
            URL url = this.getClass().getResource("fullScript.groovy");
            assertNotNull("Unable to load the script 'fullScript.groovy'", url);
            File f = new File(url.toURI());
            wpsServer.addProcess(f);
            processManager.addScript(f.toURI());
            url = this.getClass().getResource("simpleScript.groovy");
            assertNotNull("Unable to load the script 'simpleScript.groovy'", url);
            f = new File(url.toURI());
            wpsServer.addProcess(f);
            processManager.addScript(f.toURI());
        } catch (URISyntaxException e) {
            fail("Error on loading the scripts : "+e.getMessage());
        }

        assertNotNull("Unable to load the file 'minWpsService100.json'",
                TestWPS_1_0_0_Execute.class.getResource("minWpsService100.json").getFile());
        WpsServerProperties_1_0_0 minWpsProps = new WpsServerProperties_1_0_0(
                TestWPS_1_0_0_Execute.class.getResource("minWpsService100.json").getFile());
        minWps100Operations =  new WPS_1_0_0_OperationsImpl(wpsServer, minWpsProps, processManager);

        assertNotNull("Unable to load the file 'fullWpsService100.json'",
                TestWPS_1_0_0_Execute.class.getResource("fullWpsService100.json").getFile());
        WpsServerProperties_1_0_0 fullWpsProps = new WpsServerProperties_1_0_0(
                TestWPS_1_0_0_Execute.class.getResource("fullWpsService100.json").getFile());
        fullWps100Operations =  new WPS_1_0_0_OperationsImpl(wpsServer, fullWpsProps, processManager);
    }

    /**
     * Test the response to an Execute request with 3 input/output defined with a minimal ResponseDocument
     */
    @Test
    public void testThreeOutputResponseDocumentExecute(){
        //Execute with only one request output
        Execute execute = new Execute();
        CodeType codeType = new CodeType();
        codeType.setValue("orbisgis:test:full");
        execute.setIdentifier(codeType);

        //Sets the inputs
        DataInputsType dataInputsType = new DataInputsType();
        execute.setDataInputs(dataInputsType);

        InputType inputType = new InputType();
        CodeType inCodeType = new CodeType();
        inCodeType.setValue("orbisgis:test:full:input:enumeration");
        inputType.setIdentifier(inCodeType);
        DataType dataType  = new DataType();
        inputType.setData(dataType);
        LiteralDataType literalDataType = new LiteralDataType();
        dataType.setLiteralData(literalDataType);
        literalDataType.setValue("value1");
        dataInputsType.getInput().add(inputType);

        inputType = new InputType();
        inCodeType = new CodeType();
        inCodeType.setValue("orbisgis:test:full:input:jdbctable");
        inputType.setIdentifier(inCodeType);
        dataType  = new DataType();
        inputType.setData(dataType);
        ComplexDataType complexDataType = new ComplexDataType();
        dataType.setComplexData(complexDataType);
        complexDataType.getOtherAttributes().put(new QName("string"), "jdbctable");
        dataInputsType.getInput().add(inputType);

        inputType = new InputType();
        inCodeType = new CodeType();
        inCodeType.setValue("orbisgis:test:full:input:boundingboxdata");
        inputType.setIdentifier(inCodeType);
        dataType  = new DataType();
        inputType.setData(dataType);
        BoundingBoxType boundingBoxType = new BoundingBoxType();
        dataType.setBoundingBoxData(boundingBoxType);
        boundingBoxType.setCrs("EPSG:4326");
        boundingBoxType.setDimensions(new BigInteger("2"));
        boundingBoxType.getUpperCorner().add(1.0);
        boundingBoxType.getUpperCorner().add(1.1);
        boundingBoxType.getLowerCorner().add(0.0);
        boundingBoxType.getLowerCorner().add(0.1);
        dataInputsType.getInput().add(inputType);

        //Sets the outputs
        ResponseFormType responseFormType = new ResponseFormType();
        ResponseDocumentType responseDocumentType = new ResponseDocumentType();
        responseFormType.setResponseDocument(responseDocumentType);
        execute.setResponseForm(responseFormType);

        CodeType outCodeType = new CodeType();
        outCodeType.setValue("orbisgis:test:full:output:enumeration");
        DocumentOutputDefinitionType documentOutputDefinitionType = new DocumentOutputDefinitionType();
        documentOutputDefinitionType.setIdentifier(outCodeType);
        responseDocumentType.getOutput().add(documentOutputDefinitionType);

        outCodeType = new CodeType();
        outCodeType.setValue("orbisgis:test:full:output:jdbctable");
        documentOutputDefinitionType = new DocumentOutputDefinitionType();
        documentOutputDefinitionType.setIdentifier(outCodeType);
        responseDocumentType.getOutput().add(documentOutputDefinitionType);

        outCodeType = new CodeType();
        outCodeType.setValue("orbisgis:test:full:output:boundingboxdata");
        documentOutputDefinitionType = new DocumentOutputDefinitionType();
        documentOutputDefinitionType.setIdentifier(outCodeType);
        responseDocumentType.getOutput().add(documentOutputDefinitionType);

        Object o = fullWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExecuteResponse", o instanceof ExecuteResponse);
        ExecuteResponse executeResponse = (ExecuteResponse)o;

        testMandatoryExecuteResponse(executeResponse);

        assertTrue("The 'processOutputs' property of ExecuteResponse should be set",
                executeResponse.isSetProcessOutputs());
        assertEquals("The 'processOutputs' property of ExecuteResponse should contains ten value",
                3, executeResponse.getProcessOutputs().getOutput().size());
        for(OutputDataType outputDataType : executeResponse.getProcessOutputs().getOutput()){
            assertTrue("The 'identifier' property of OutputDataType should be set", outputDataType.isSetIdentifier());
            assertTrue("The 'identifier' 'value' property of OutputDataType should be set",
                    outputDataType.getIdentifier().isSetValue());
            assertTrue("Unknown output '"+outputDataType.getIdentifier().getValue()+"'",
                    outputDataType.getIdentifier().getValue().startsWith("orbisgis:test:full:output:"));

            String type = identifierToType(outputDataType.getIdentifier());
            testMandatoryOutputDataType(outputDataType, type);

            switch(type){
                case "JDBCTable" :
                    assertFalse("The 'data' 'boundingBoxData' property of OutputDataType should not be set",
                            outputDataType.getData().isSetBoundingBoxData());
                    assertFalse("The 'data' 'literalData' property of OutputDataType should not be set",
                            outputDataType.getData().isSetLiteralData());
                    assertTrue("The 'data' 'complexData' property of OutputDataType should be set",
                            outputDataType.getData().isSetComplexData());

                    assertTrue("The 'data' 'complexData' 'mimeType' property of OutputDataType should be set",
                            outputDataType.getData().getComplexData().isSetMimeType());
                    assertEquals("The 'data' 'complexData' 'mimeType' property of OutputDataType should be set to 'text/xml'",
                            "text/xml", outputDataType.getData().getComplexData().getMimeType());

                    assertTrue("The 'data' 'complexData' 'content' property of OutputDataType should be set",
                            outputDataType.getData().getComplexData().isSetContent());
                    assertEquals("The 'data' 'complexData' 'content' property of OutputDataType should contains one value",
                            1, outputDataType.getData().getComplexData().getContent().size());
                    assertTrue("The 'data' 'complexData' 'content' property of OutputDataType should contains 'jdbctable'",
                            outputDataType.getData().getComplexData().getContent().contains("jdbctable"));
                    break;
                case "Enumeration" :
                    assertFalse("The 'data' 'boundingBoxData' property of OutputDataType should not be set",
                            outputDataType.getData().isSetBoundingBoxData());
                    assertTrue("The 'data' 'literalData' property of OutputDataType should be set",
                            outputDataType.getData().isSetLiteralData());
                    assertFalse("The 'data' 'complexData' property of OutputDataType should not be set",
                            outputDataType.getData().isSetComplexData());

                    assertTrue("The 'data' 'complexData' 'value' property of OutputDataType should be set",
                            outputDataType.getData().getLiteralData().isSetValue());
                    assertEquals("The 'data' 'complexData' 'value' property of OutputDataType should be set to 'value1'",
                            "value1", outputDataType.getData().getLiteralData().getValue());

                    assertTrue("The 'data' 'complexData' 'dataType' property of OutputDataType should be set",
                            outputDataType.getData().getLiteralData().isSetDataType());
                    assertEquals("The 'data' 'complexData' 'dataType' property of OutputDataType should be set to 'String'",
                            "string", outputDataType.getData().getLiteralData().getDataType().toLowerCase());
                    break;
                case "BoundingBoxData" :
                    assertTrue("The 'data' 'boundingBoxData' property of OutputDataType should be set",
                            outputDataType.getData().isSetBoundingBoxData());
                    assertFalse("The 'data' 'literalData' property of OutputDataType should not be set",
                            outputDataType.getData().isSetLiteralData());
                    assertFalse("The 'data' 'complexData' property of OutputDataType should not be set",
                            outputDataType.getData().isSetComplexData());

                    assertTrue("The 'data' 'boundingBoxData' 'upperCorner' property of OutputDataType should be set",
                            outputDataType.getData().getBoundingBoxData().isSetUpperCorner());
                    assertArrayEquals("The 'data' 'boundingBoxData' 'upperCorner' property of OutputDataType should be set to '[1.0, 1.1]'",
                            new Double[]{1.0, 1.1}, outputDataType.getData().getBoundingBoxData().getUpperCorner().toArray());

                    assertTrue("The 'data' 'boundingBoxData' 'lowerCorner' property of OutputDataType should be set",
                            outputDataType.getData().getBoundingBoxData().isSetLowerCorner());
                    assertArrayEquals("The 'data' 'boundingBoxData' 'lowerCorner' property of OutputDataType should be set to '[0.0, 0.1]'",
                            new Double[]{0.0, 0.1}, outputDataType.getData().getBoundingBoxData().getLowerCorner().toArray());

                    assertTrue("The 'data' 'boundingBoxData' 'crs' property of OutputDataType should be set",
                            outputDataType.getData().getBoundingBoxData().isSetCrs());
                    assertEquals("The 'data' 'boundingBoxData' 'crs' property of OutputDataType should be set to 'EPSG:4326'",
                            "EPSG:4326", outputDataType.getData().getBoundingBoxData().getCrs());

                    assertTrue("The 'data' 'boundingBoxData' 'dimensions' property of OutputDataType should be set",
                            outputDataType.getData().getBoundingBoxData().isSetDimensions());
                    assertEquals("The 'data' 'boundingBoxData' 'dimensions' property of OutputDataType should be set to '2'",
                            new BigInteger("2"), outputDataType.getData().getBoundingBoxData().getDimensions());
                    break;
                default :
                    fail("Invalid output '"+type+"'");
                    break;
            }
        }

    }
    /**
     * Test the response to an Execute request with all the input/output defined with a minimal ResponseDocument to
     * ensure all the data types are wel supported
     */
    @Test
    public void testFullProcessResponseDocumentExecute(){
        //Execute with only one request output
        Execute execute = new Execute();
        CodeType codeType = new CodeType();
        codeType.setValue("orbisgis:test:full");
        execute.setIdentifier(codeType);

        //Sets the inputs
        DataInputsType dataInputsType = new DataInputsType();
        execute.setDataInputs(dataInputsType);

        InputType inputType = new InputType();
        CodeType inCodeType = new CodeType();
        inCodeType.setValue("orbisgis:test:full:input:enumeration");
        inputType.setIdentifier(inCodeType);
        DataType dataType  = new DataType();
        inputType.setData(dataType);
        LiteralDataType literalDataType = new LiteralDataType();
        dataType.setLiteralData(literalDataType);
        literalDataType.setValue("value1");
        dataInputsType.getInput().add(inputType);

        inputType = new InputType();
        inCodeType = new CodeType();
        inCodeType.setValue("orbisgis:test:full:input:geometry");
        inputType.setIdentifier(inCodeType);
        dataType  = new DataType();
        inputType.setData(dataType);
        literalDataType = new LiteralDataType();
        dataType.setLiteralData(literalDataType);
        literalDataType.setValue("geometry");
        dataInputsType.getInput().add(inputType);

        inputType = new InputType();
        inCodeType = new CodeType();
        inCodeType.setValue("orbisgis:test:full:input:jdbctable");
        inputType.setIdentifier(inCodeType);
        dataType  = new DataType();
        inputType.setData(dataType);
        ComplexDataType complexDataType = new ComplexDataType();
        dataType.setComplexData(complexDataType);
        complexDataType.getOtherAttributes().put(new QName("string"), "jdbctable");
        dataInputsType.getInput().add(inputType);

        inputType = new InputType();
        inCodeType = new CodeType();
        inCodeType.setValue("orbisgis:test:full:input:jdbccolumn");
        inputType.setIdentifier(inCodeType);
        dataType  = new DataType();
        inputType.setData(dataType);
        literalDataType = new LiteralDataType();
        dataType.setLiteralData(literalDataType);
        literalDataType.setValue("jdbccolumn");
        dataInputsType.getInput().add(inputType);

        inputType = new InputType();
        inCodeType = new CodeType();
        inCodeType.setValue("orbisgis:test:full:input:jdbcvalue");
        inputType.setIdentifier(inCodeType);
        dataType  = new DataType();
        inputType.setData(dataType);
        literalDataType = new LiteralDataType();
        dataType.setLiteralData(literalDataType);
        literalDataType.setValue("jdbcvalue");
        dataInputsType.getInput().add(inputType);

        inputType = new InputType();
        inCodeType = new CodeType();
        inCodeType.setValue("orbisgis:test:full:input:rawdata");
        inputType.setIdentifier(inCodeType);
        dataType  = new DataType();
        inputType.setData(dataType);
        literalDataType = new LiteralDataType();
        dataType.setLiteralData(literalDataType);
        literalDataType.setValue("rawdata");
        dataInputsType.getInput().add(inputType);

        inputType = new InputType();
        inCodeType = new CodeType();
        inCodeType.setValue("orbisgis:test:full:input:password");
        inputType.setIdentifier(inCodeType);
        dataType  = new DataType();
        inputType.setData(dataType);
        literalDataType = new LiteralDataType();
        dataType.setLiteralData(literalDataType);
        literalDataType.setValue("password");
        dataInputsType.getInput().add(inputType);

        inputType = new InputType();
        inCodeType = new CodeType();
        inCodeType.setValue("orbisgis:test:full:input:literaldatadouble");
        inputType.setIdentifier(inCodeType);
        dataType  = new DataType();
        inputType.setData(dataType);
        literalDataType = new LiteralDataType();
        dataType.setLiteralData(literalDataType);
        literalDataType.setValue("10.0");
        dataInputsType.getInput().add(inputType);

        inputType = new InputType();
        inCodeType = new CodeType();
        inCodeType.setValue("orbisgis:test:full:input:literaldatastring");
        inputType.setIdentifier(inCodeType);
        dataType  = new DataType();
        inputType.setData(dataType);
        literalDataType = new LiteralDataType();
        dataType.setLiteralData(literalDataType);
        literalDataType.setValue("LiteralDataString");
        dataInputsType.getInput().add(inputType);

        inputType = new InputType();
        inCodeType = new CodeType();
        inCodeType.setValue("orbisgis:test:full:input:boundingboxdata");
        inputType.setIdentifier(inCodeType);
        dataType  = new DataType();
        inputType.setData(dataType);
        BoundingBoxType boundingBoxType = new BoundingBoxType();
        dataType.setBoundingBoxData(boundingBoxType);
        boundingBoxType.setCrs("EPSG:4326");
        boundingBoxType.setDimensions(new BigInteger("2"));
        boundingBoxType.getUpperCorner().add(1.0);
        boundingBoxType.getUpperCorner().add(1.1);
        boundingBoxType.getLowerCorner().add(0.0);
        boundingBoxType.getLowerCorner().add(0.1);
        dataInputsType.getInput().add(inputType);

        //Sets the outputs
        ResponseFormType responseFormType = new ResponseFormType();
        ResponseDocumentType responseDocumentType = new ResponseDocumentType();
        responseFormType.setResponseDocument(responseDocumentType);
        execute.setResponseForm(responseFormType);

        CodeType outCodeType = new CodeType();
        outCodeType.setValue("orbisgis:test:full:output:enumeration");
        DocumentOutputDefinitionType documentOutputDefinitionType = new DocumentOutputDefinitionType();
        documentOutputDefinitionType.setIdentifier(outCodeType);
        responseDocumentType.getOutput().add(documentOutputDefinitionType);

        outCodeType = new CodeType();
        outCodeType.setValue("orbisgis:test:full:output:geometry");
        documentOutputDefinitionType = new DocumentOutputDefinitionType();
        documentOutputDefinitionType.setIdentifier(outCodeType);
        responseDocumentType.getOutput().add(documentOutputDefinitionType);

        outCodeType = new CodeType();
        outCodeType.setValue("orbisgis:test:full:output:jdbctable");
        documentOutputDefinitionType = new DocumentOutputDefinitionType();
        documentOutputDefinitionType.setIdentifier(outCodeType);
        responseDocumentType.getOutput().add(documentOutputDefinitionType);

        outCodeType = new CodeType();
        outCodeType.setValue("orbisgis:test:full:output:jdbccolumn");
        documentOutputDefinitionType = new DocumentOutputDefinitionType();
        documentOutputDefinitionType.setIdentifier(outCodeType);
        responseDocumentType.getOutput().add(documentOutputDefinitionType);

        outCodeType = new CodeType();
        outCodeType.setValue("orbisgis:test:full:output:jdbcvalue");
        documentOutputDefinitionType = new DocumentOutputDefinitionType();
        documentOutputDefinitionType.setIdentifier(outCodeType);
        responseDocumentType.getOutput().add(documentOutputDefinitionType);

        outCodeType = new CodeType();
        outCodeType.setValue("orbisgis:test:full:output:rawdata");
        documentOutputDefinitionType = new DocumentOutputDefinitionType();
        documentOutputDefinitionType.setIdentifier(outCodeType);
        responseDocumentType.getOutput().add(documentOutputDefinitionType);

        outCodeType = new CodeType();
        outCodeType.setValue("orbisgis:test:full:output:password");
        documentOutputDefinitionType = new DocumentOutputDefinitionType();
        documentOutputDefinitionType.setIdentifier(outCodeType);
        responseDocumentType.getOutput().add(documentOutputDefinitionType);

        outCodeType = new CodeType();
        outCodeType.setValue("orbisgis:test:full:output:literaldatadouble");
        documentOutputDefinitionType = new DocumentOutputDefinitionType();
        documentOutputDefinitionType.setIdentifier(outCodeType);
        responseDocumentType.getOutput().add(documentOutputDefinitionType);

        outCodeType = new CodeType();
        outCodeType.setValue("orbisgis:test:full:output:literaldatastring");
        documentOutputDefinitionType = new DocumentOutputDefinitionType();
        documentOutputDefinitionType.setIdentifier(outCodeType);
        responseDocumentType.getOutput().add(documentOutputDefinitionType);

        outCodeType = new CodeType();
        outCodeType.setValue("orbisgis:test:full:output:boundingboxdata");
        documentOutputDefinitionType = new DocumentOutputDefinitionType();
        documentOutputDefinitionType.setIdentifier(outCodeType);
        responseDocumentType.getOutput().add(documentOutputDefinitionType);

        Object o = fullWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExecuteResponse", o instanceof ExecuteResponse);
        ExecuteResponse executeResponse = (ExecuteResponse)o;

        testMandatoryExecuteResponse(executeResponse);

        assertTrue("The 'processOutputs' property of ExecuteResponse should be set",
                executeResponse.isSetProcessOutputs());
        assertEquals("The 'processOutputs' property of ExecuteResponse should contains ten value",
                10, executeResponse.getProcessOutputs().getOutput().size());
        for(OutputDataType outputDataType : executeResponse.getProcessOutputs().getOutput()){
            assertTrue("The 'identifier' property of OutputDataType should be set", outputDataType.isSetIdentifier());
            assertTrue("The 'identifier' 'value' property of OutputDataType should be set",
                    outputDataType.getIdentifier().isSetValue());
            assertTrue("Unknown output '"+outputDataType.getIdentifier().getValue()+"'",
                    outputDataType.getIdentifier().getValue().startsWith("orbisgis:test:full:output:"));

            String type = identifierToType(outputDataType.getIdentifier());
            testMandatoryOutputDataType(outputDataType, type);

            switch(type){
                case "JDBCTable" :
                    assertFalse("The 'data' 'boundingBoxData' property of OutputDataType should not be set",
                            outputDataType.getData().isSetBoundingBoxData());
                    assertFalse("The 'data' 'literalData' property of OutputDataType should not be set",
                            outputDataType.getData().isSetLiteralData());
                    assertTrue("The 'data' 'complexData' property of OutputDataType should be set",
                            outputDataType.getData().isSetComplexData());

                    assertTrue("The 'data' 'complexData' 'mimeType' property of OutputDataType should be set",
                            outputDataType.getData().getComplexData().isSetMimeType());
                    assertEquals("The 'data' 'complexData' 'mimeType' property of OutputDataType should be set to 'text/xml'",
                            "text/xml", outputDataType.getData().getComplexData().getMimeType());

                    assertTrue("The 'data' 'complexData' 'content' property of OutputDataType should be set",
                            outputDataType.getData().getComplexData().isSetContent());
                    assertEquals("The 'data' 'complexData' 'content' property of OutputDataType should contains one value",
                            1, outputDataType.getData().getComplexData().getContent().size());
                    assertTrue("The 'data' 'complexData' 'content' property of OutputDataType should contains 'jdbctable'",
                            outputDataType.getData().getComplexData().getContent().contains("jdbctable"));
                    break;
                case "Enumeration" :
                    assertFalse("The 'data' 'boundingBoxData' property of OutputDataType should not be set",
                            outputDataType.getData().isSetBoundingBoxData());
                    assertTrue("The 'data' 'literalData' property of OutputDataType should be set",
                            outputDataType.getData().isSetLiteralData());
                    assertFalse("The 'data' 'complexData' property of OutputDataType should not be set",
                            outputDataType.getData().isSetComplexData());

                    assertTrue("The 'data' 'complexData' 'value' property of OutputDataType should be set",
                            outputDataType.getData().getLiteralData().isSetValue());
                    assertEquals("The 'data' 'complexData' 'value' property of OutputDataType should be set to 'value1'",
                            "value1", outputDataType.getData().getLiteralData().getValue());

                    assertTrue("The 'data' 'complexData' 'dataType' property of OutputDataType should be set",
                            outputDataType.getData().getLiteralData().isSetDataType());
                    assertEquals("The 'data' 'complexData' 'dataType' property of OutputDataType should be set to 'String'",
                            "string", outputDataType.getData().getLiteralData().getDataType().toLowerCase());
                    break;
                case "Geometry" :
                case "JDBCColumn" :
                case "JDBCValue" :
                case "RawData" :
                case "Password" :
                    assertFalse("The 'data' 'boundingBoxData' property of OutputDataType should not be set",
                            outputDataType.getData().isSetBoundingBoxData());
                    assertTrue("The 'data' 'literalData' property of OutputDataType should be set",
                            outputDataType.getData().isSetLiteralData());
                    assertFalse("The 'data' 'complexData' property of OutputDataType should not be set",
                            outputDataType.getData().isSetComplexData());

                    assertTrue("The 'data' 'complexData' 'value' property of OutputDataType should be set",
                            outputDataType.getData().getLiteralData().isSetValue());
                    assertEquals("The 'data' 'complexData' 'value' property of OutputDataType should be set to '"+type.toLowerCase()+"'",
                            type.toLowerCase(), outputDataType.getData().getLiteralData().getValue());

                    assertTrue("The 'data' 'complexData' 'dataType' property of OutputDataType should be set",
                            outputDataType.getData().getLiteralData().isSetDataType());
                    assertEquals("The 'data' 'complexData' 'dataType' property of OutputDataType should be set to 'String'",
                            "string", outputDataType.getData().getLiteralData().getDataType().toLowerCase());
                    break;
                case "LiteralDataDouble" :
                    assertFalse("The 'data' 'boundingBoxData' property of OutputDataType should not be set",
                            outputDataType.getData().isSetBoundingBoxData());
                    assertTrue("The 'data' 'literalData' property of OutputDataType should be set",
                            outputDataType.getData().isSetLiteralData());
                    assertFalse("The 'data' 'complexData' property of OutputDataType should not be set",
                            outputDataType.getData().isSetComplexData());

                    assertTrue("The 'data' 'literalData' 'dataType' property of OutputDataType should be set",
                            outputDataType.getData().getLiteralData().isSetDataType());
                    assertEquals("The 'data' 'literalData' 'dataType' property of OutputDataType should be set to 'Double'",
                            "double", outputDataType.getData().getLiteralData().getDataType());

                    assertTrue("The 'data' 'literalData' 'value' property of OutputDataType should be set",
                            outputDataType.getData().getLiteralData().isSetValue());
                    assertEquals("The 'data' 'literalData' 'value' property of OutputDataType should be set to '10.0'",
                            "10.0", outputDataType.getData().getLiteralData().getValue());
                    break;
                case "LiteralDataString" :
                    assertFalse("The 'data' 'boundingBoxData' property of OutputDataType should not be set",
                            outputDataType.getData().isSetBoundingBoxData());
                    assertTrue("The 'data' 'literalData' property of OutputDataType should be set",
                            outputDataType.getData().isSetLiteralData());
                    assertFalse("The 'data' 'complexData' property of OutputDataType should not be set",
                            outputDataType.getData().isSetComplexData());

                    assertTrue("The 'data' 'literalData' 'value' property of OutputDataType should be set",
                            outputDataType.getData().getLiteralData().isSetValue());
                    assertEquals("The 'data' 'literalData' 'value' property of OutputDataType should be set to '"+type+"'",
                            type, outputDataType.getData().getLiteralData().getValue());
                    break;
                case "BoundingBoxData" :
                    assertTrue("The 'data' 'boundingBoxData' property of OutputDataType should be set",
                            outputDataType.getData().isSetBoundingBoxData());
                    assertFalse("The 'data' 'literalData' property of OutputDataType should not be set",
                            outputDataType.getData().isSetLiteralData());
                    assertFalse("The 'data' 'complexData' property of OutputDataType should not be set",
                            outputDataType.getData().isSetComplexData());

                    assertTrue("The 'data' 'boundingBoxData' 'upperCorner' property of OutputDataType should be set",
                            outputDataType.getData().getBoundingBoxData().isSetUpperCorner());
                    assertArrayEquals("The 'data' 'boundingBoxData' 'upperCorner' property of OutputDataType should be set to '[1.0, 1.1]'",
                            new Double[]{1.0, 1.1}, outputDataType.getData().getBoundingBoxData().getUpperCorner().toArray());

                    assertTrue("The 'data' 'boundingBoxData' 'lowerCorner' property of OutputDataType should be set",
                            outputDataType.getData().getBoundingBoxData().isSetLowerCorner());
                    assertArrayEquals("The 'data' 'boundingBoxData' 'lowerCorner' property of OutputDataType should be set to '[0.0, 0.1]'",
                            new Double[]{0.0, 0.1}, outputDataType.getData().getBoundingBoxData().getLowerCorner().toArray());

                    assertTrue("The 'data' 'boundingBoxData' 'crs' property of OutputDataType should be set",
                            outputDataType.getData().getBoundingBoxData().isSetCrs());
                    assertEquals("The 'data' 'boundingBoxData' 'crs' property of OutputDataType should be set to 'EPSG:4326'",
                            "EPSG:4326", outputDataType.getData().getBoundingBoxData().getCrs());

                    assertTrue("The 'data' 'boundingBoxData' 'dimensions' property of OutputDataType should be set",
                            outputDataType.getData().getBoundingBoxData().isSetDimensions());
                    assertEquals("The 'data' 'boundingBoxData' 'dimensions' property of OutputDataType should be set to '2'",
                            new BigInteger("2"), outputDataType.getData().getBoundingBoxData().getDimensions());
                    break;
                default :
                    fail("Invalid output '"+type+"'");
                    break;
            }
        }

    }
    /**
     * Test the response to an Execute request with a minimal Execute request
     */
    @Test
    public void testMinResponseDocumentExecute(){
        //Execute with only one request output
        Execute execute = new Execute();
        CodeType codeType = new CodeType();
        codeType.setValue("orbisgis:test:full");
        execute.setIdentifier(codeType);

        Object o = fullWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExecuteResponse", o instanceof ExecuteResponse);
        ExecuteResponse executeResponse = (ExecuteResponse)o;

        testMandatoryExecuteResponse(executeResponse);

        assertTrue("The 'processOutputs' property of ExecuteResponse should be set",
                executeResponse.isSetProcessOutputs());
        assertEquals("The 'processOutputs' property of ExecuteResponse should contains ten value",
                10, executeResponse.getProcessOutputs().getOutput().size());
        for(OutputDataType outputDataType : executeResponse.getProcessOutputs().getOutput()){
            assertTrue("The 'identifier' property of OutputDataType should be set", outputDataType.isSetIdentifier());
            assertTrue("The 'identifier' 'value' property of OutputDataType should be set",
                    outputDataType.getIdentifier().isSetValue());
            assertTrue("Unknown output '"+outputDataType.getIdentifier().getValue()+"'",
                    outputDataType.getIdentifier().getValue().startsWith("orbisgis:test:full:output:"));

            String type = identifierToType(outputDataType.getIdentifier());
            testMandatoryOutputDataType(outputDataType, type);

            switch(type){
                case "JDBCTable" :
                    assertFalse("The 'data' 'boundingBoxData' property of OutputDataType should not be set",
                            outputDataType.getData().isSetBoundingBoxData());
                    assertFalse("The 'data' 'literalData' property of OutputDataType should not be set",
                            outputDataType.getData().isSetLiteralData());
                    assertTrue("The 'data' 'complexData' property of OutputDataType should be set",
                            outputDataType.getData().isSetComplexData());
                    break;
                case "Enumeration" :
                case "Geometry" :
                case "JDBCColumn" :
                case "JDBCValue" :
                case "RawData" :
                case "Password" :
                case "LiteralDataDouble" :
                case "LiteralDataString" :
                    assertFalse("The 'data' 'boundingBoxData' property of OutputDataType should not be set",
                            outputDataType.getData().isSetBoundingBoxData());
                    assertTrue("The 'data' 'literalData' property of OutputDataType should be set",
                            outputDataType.getData().isSetLiteralData());
                    assertFalse("The 'data' 'complexData' property of OutputDataType should not be set",
                            outputDataType.getData().isSetComplexData());
                    break;
                case "BoundingBoxData" :
                    assertTrue("The 'data' 'boundingBoxData' property of OutputDataType should be set",
                            outputDataType.getData().isSetBoundingBoxData());
                    assertFalse("The 'data' 'literalData' property of OutputDataType should not be set",
                            outputDataType.getData().isSetLiteralData());
                    assertFalse("The 'data' 'complexData' property of OutputDataType should not be set",
                            outputDataType.getData().isSetComplexData());
                    break;
                default :
                    fail("Invalid output '"+type+"'");
                    break;
            }
        }

    }

    private void testMandatoryExecuteResponse(ExecuteResponse executeResponse){
        assertTrue("The 'lang' property of ExecuteResponse should be set", executeResponse.isSetLang());
        assertEquals("The 'lang' property of ExecuteResponse should be set to 'en'", "en", executeResponse.getLang());

        assertFalse("The 'statusLocation' property of ExecuteResponse should be set",
                executeResponse.isSetStatusLocation());

        assertTrue("The 'serviceInstance' property of ExecuteResponse should be set",
                executeResponse.isSetServiceInstance());
        assertEquals("The 'serviceInstance' property of ExecuteResponse should be set to 'getcapabilitiesurl1'",
                "getcapabilitiesurl1", executeResponse.getServiceInstance());

        assertTrue("The 'process' property of ExecuteResponse should be set",
                executeResponse.isSetProcess());
        assertTrue("The 'process' 'identifier' property of ExecuteResponse should be set",
                executeResponse.getProcess().isSetIdentifier());
        assertTrue("The 'process' 'identifier' 'value' property of ExecuteResponse should be set",
                executeResponse.getProcess().getIdentifier().isSetValue());
        assertEquals("The 'process' 'identifier' 'value' property of ExecuteResponse should be set to 'orbisgis:test:full'",
                "orbisgis:test:full", executeResponse.getProcess().getIdentifier().getValue());
        assertTrue("The 'process' 'title' property of ExecuteResponse should be set",
                executeResponse.getProcess().isSetTitle());
        assertTrue("The 'process' 'title' 'lang' property of ExecuteResponse should be set",
                executeResponse.getProcess().getTitle().isSetLang());
        assertEquals("The 'process' 'title' 'lang' property of ExecuteResponse should be set to 'en'", "en",
                executeResponse.getProcess().getTitle().getLang());
        assertTrue("The 'process' 'title' 'value' property of ExecuteResponse should be set",
                executeResponse.getProcess().getTitle().isSetValue());
        assertEquals("The 'process' 'title' 'value' property of ExecuteResponse should be set to 'full test script'",
                "full test script", executeResponse.getProcess().getTitle().getValue());

        assertTrue("The 'status' 'creationTime' property of ExecuteResponse should be set",
                executeResponse.getStatus().isSetCreationTime());
        assertTrue("The 'status' property of ExecuteResponse should be 'ProcessSucceeded'",
                executeResponse.getStatus().isSetProcessSucceeded());
        assertFalse("The 'status' property of ExecuteResponse should not be 'ProcessFailed'",
                executeResponse.getStatus().isSetProcessFailed());
        assertFalse("The 'status' property of ExecuteResponse should not be 'ProcessAccepted'",
                executeResponse.getStatus().isSetProcessAccepted());
        assertFalse("The 'status' property of ExecuteResponse should not be 'ProcessPaused'",
                executeResponse.getStatus().isSetProcessPaused());
        assertFalse("The 'status' property of ExecuteResponse should not be 'ProcessStarted'",
                executeResponse.getStatus().isSetProcessStarted());
    }
    private void testMandatoryOutputDataType(OutputDataType outputDataType, String type){

        assertTrue("The 'title' property of OutputDataType should be set",
                outputDataType.isSetTitle());
        assertTrue("The 'title' 'lang' property of OutputDataType should be set",
                outputDataType.getTitle().isSetLang());
        assertEquals("The 'title' 'lang' property of OutputDataType should be set to 'en'", "en",
                outputDataType.getTitle().getLang());
        assertTrue("The 'title' 'value' property of OutputDataType should be set",
                outputDataType.getTitle().isSetValue());
        assertEquals("The 'title' 'value' property of OutputDataType should be set to 'Output "+type+"'",
                "Output "+type, outputDataType.getTitle().getValue());

        assertTrue("The 'abstract' property of OutputDataType should be set",
                outputDataType.isSetAbstract());
        assertTrue("The 'abstract' 'lang' property of OutputDataType should be set",
                outputDataType.getAbstract().isSetLang());
        assertEquals("The 'abstract' 'lang' property of OutputDataType should be set to 'en'", "en",
                outputDataType.getAbstract().getLang());
        assertTrue("The 'abstract' 'value' property of OutputDataType should be set",
                outputDataType.getAbstract().isSetValue());
        assertEquals("The 'abstract' 'value' property of OutputDataType should be set to 'A "+type+" output.'",
                "A "+type+" output.", outputDataType.getAbstract().getValue());

        assertFalse("The 'reference' property of OutputDataType should not be set",
                outputDataType.isSetReference());
        assertTrue("The 'data' property of OutputDataType should be set",
                outputDataType.isSetData());
    }
    private String identifierToType(CodeType identifier){
        String type = identifier.getValue().substring("orbisgis:test:full:output:".length());

        if(type.startsWith("jdbc")){
            type = type.substring(0, 5).toUpperCase() + type.substring(5);
        }
        else if(type.equals("literaldatadouble")){
            type = "LiteralDataDouble";
        }
        else if(type.equals("rawdata")){
            type = "RawData";
        }
        else if(type.equals("literaldatastring")){
            type = "LiteralDataString";
        }
        else if(type.equals("boundingboxdata")){
            type = "BoundingBoxData";
        }
        else {
            type = type.substring(0, 1).toUpperCase() + type.substring(1);
        }
        return type;
    }

    /**
     * Test an empty GetCapabilities request with a full WPS property
     */
    @Test
    public void testBadExecute(){
        //Test Execute without identifier
        Execute execute = new Execute();
        Object object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        ExceptionReport report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'MissingParameterValue'", "MissingParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'locator' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'locator' should be set to 'Identifier'", "Identifier",
                report.getException().get(0).getLocator());

        //Test Execute with bad identifier
        execute = new Execute();
        CodeType codeType = new CodeType();
        codeType.setValue("unicorn:process");
        execute.setIdentifier(codeType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'locator' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'locator' should be set to 'Identifier'", "Identifier",
                report.getException().get(0).getLocator());


        codeType = new CodeType();
        codeType.setValue("orbisgis:test:full");

        ////
        //DataInputs test
        ////

        //Test Execute with empty dataInputs
        execute = new Execute();
        execute.setIdentifier(codeType);
        DataInputsType dataInputsType = new DataInputsType();
        execute.setDataInputs(dataInputsType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'locator' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'locator' should be set to 'DataInputs'", "DataInputs",
                report.getException().get(0).getLocator());

        //Test Execute with an input without identifier
        execute = new Execute();
        execute.setIdentifier(codeType);
        dataInputsType = new DataInputsType();
        InputType inputType = new InputType();
        DataType dataType = new DataType();
        dataType.setLiteralData(new LiteralDataType());
        inputType.setData(dataType);
        dataInputsType.getInput().add(inputType);
        execute.setDataInputs(dataInputsType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'exceptionCode' should be set to 'DataInputs'", "DataInputs",
                report.getException().get(0).getLocator());

        //Test Execute with an input without DataType
        execute = new Execute();
        execute.setIdentifier(codeType);
        dataInputsType = new DataInputsType();
        inputType = new InputType();
        CodeType inputCodeType = new CodeType();
        inputCodeType.setValue("orbisgis:test:full:input:literaldatadouble");
        inputType.setIdentifier(inputCodeType);
        dataInputsType.getInput().add(inputType);
        execute.setDataInputs(dataInputsType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'exceptionCode' should be set to 'DataInputs'", "DataInputs",
                report.getException().get(0).getLocator());

        //Test Execute with an input with an empty dataType
        execute = new Execute();
        execute.setIdentifier(codeType);
        dataInputsType = new DataInputsType();
        inputType = new InputType();
        inputCodeType = new CodeType();
        inputCodeType.setValue("orbisgis:test:full:input:literaldatadouble");
        inputType.setIdentifier(inputCodeType);
        inputType.setData(new DataType());
        dataInputsType.getInput().add(inputType);
        execute.setDataInputs(dataInputsType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'exceptionCode' should be set to 'DataInputs'", "DataInputs",
                report.getException().get(0).getLocator());

        //Test Execute with an input with a too full dataType
        execute = new Execute();
        execute.setIdentifier(codeType);
        dataInputsType = new DataInputsType();
        inputType = new InputType();
        inputCodeType = new CodeType();
        inputCodeType.setValue("orbisgis:test:full:input:literaldatadouble");
        inputType.setIdentifier(inputCodeType);
        dataType = new DataType();
        dataType.setLiteralData(new LiteralDataType());
        dataType.setBoundingBoxData(new BoundingBoxType());
        dataType.setComplexData(new ComplexDataType());
        inputType.setData(dataType);
        dataInputsType.getInput().add(inputType);
        execute.setDataInputs(dataInputsType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'exceptionCode' should be set to 'DataInputs'", "DataInputs",
                report.getException().get(0).getLocator());

        //Test Execute with an input with a bad reference
        execute = new Execute();
        execute.setIdentifier(codeType);
        dataInputsType = new DataInputsType();
        inputType = new InputType();
        inputCodeType = new CodeType();
        inputCodeType.setValue("orbisgis:test:full:input:literaldatadouble");
        inputType.setIdentifier(inputCodeType);
        InputReferenceType inputReferenceType = new InputReferenceType();
        inputType.setReference(inputReferenceType);
        dataInputsType.getInput().add(inputType);
        execute.setDataInputs(dataInputsType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'exceptionCode' should be set to 'DataInputs'", "DataInputs",
                report.getException().get(0).getLocator());

        //Test Execute with an input with a bad reference format
        execute = new Execute();
        execute.setIdentifier(codeType);
        dataInputsType = new DataInputsType();
        inputType = new InputType();
        inputCodeType = new CodeType();
        inputCodeType.setValue("orbisgis:test:full:input:literaldatadouble");
        inputType.setIdentifier(inputCodeType);
        inputReferenceType = new InputReferenceType();
        inputReferenceType.setHref("file:href");
        inputReferenceType.setMimeType("unicorn/type");
        inputType.setReference(inputReferenceType);
        dataInputsType.getInput().add(inputType);
        execute.setDataInputs(dataInputsType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'exceptionCode' should be set to 'DataInputs'", "DataInputs",
                report.getException().get(0).getLocator());

        //Test Execute with bad complexData
        execute = new Execute();
        execute.setIdentifier(codeType);
        dataInputsType = new DataInputsType();
        inputType = new InputType();
        CodeType rawInputCodeType = new CodeType();
        rawInputCodeType.setValue("orbisgis:test:full:input:rawdata");
        inputType.setIdentifier(rawInputCodeType);
        dataType = new DataType();
        ComplexDataType complexDataType = new ComplexDataType();
        dataType.setComplexData(complexDataType);
        inputType.setData(dataType);
        dataInputsType.getInput().add(inputType);
        execute.setDataInputs(dataInputsType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'locator' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'locator' should be set to 'DataInputs'", "DataInputs",
                report.getException().get(0).getLocator());

        //Test Execute with ComplexData with unknown input format
        execute = new Execute();
        dataInputsType = new DataInputsType();
        inputType = new InputType();
        dataType = new DataType();
        complexDataType = new ComplexDataType();
        complexDataType.getOtherAttributes().put(new QName("string"), "test");
        complexDataType.setMimeType("unicorn/type");
        dataType.setComplexData(complexDataType);
        inputType.setData(dataType);
        rawInputCodeType = new CodeType();
        rawInputCodeType.setValue("orbisgis:test:full:input:rawdata");
        inputType.setIdentifier(rawInputCodeType);
        dataInputsType.getInput().add(inputType);
        execute.setDataInputs(dataInputsType);
        execute.setIdentifier(codeType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'locator' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'locator' should be set to 'DataInputs'", "DataInputs",
                report.getException().get(0).getLocator());

        //Test Execute with LiteralData with unknown dataType
        execute = new Execute();
        dataInputsType = new DataInputsType();
        inputType = new InputType();
        dataType = new DataType();
        LiteralDataType literalDataType = new LiteralDataType();
        literalDataType.setDataType("unicornType");
        literalDataType.setValue("value");
        dataType.setLiteralData(literalDataType);
        inputType.setData(dataType);
        inputCodeType = new CodeType();
        inputCodeType.setValue("orbisgis:test:full:input:literaldatadouble");
        inputType.setIdentifier(inputCodeType);
        dataInputsType.getInput().add(inputType);
        execute.setDataInputs(dataInputsType);
        execute.setIdentifier(codeType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'locator' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'locator' should be set to 'DataInputs'", "DataInputs",
                report.getException().get(0).getLocator());

        //Test Execute with LiteralData with no value
        execute = new Execute();
        dataInputsType = new DataInputsType();
        inputType = new InputType();
        dataType = new DataType();
        literalDataType = new LiteralDataType();
        literalDataType.setDataType(org.orbisgis.orbiswps.service.model.DataType.DOUBLE.getUri().toString());
        dataType.setLiteralData(literalDataType);
        inputType.setData(dataType);
        inputCodeType = new CodeType();
        inputCodeType.setValue("orbisgis:test:full:input:literaldatadouble");
        inputType.setIdentifier(inputCodeType);
        dataInputsType.getInput().add(inputType);
        execute.setDataInputs(dataInputsType);
        execute.setIdentifier(codeType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'locator' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'locator' should be set to 'DataInputs'", "DataInputs",
                report.getException().get(0).getLocator());

        //Test Execute with BoundingBox without CRS
        execute = new Execute();
        dataInputsType = new DataInputsType();
        inputType = new InputType();
        dataType = new DataType();
        BoundingBoxType boundingBoxType = new BoundingBoxType();
        boundingBoxType.getLowerCorner().add(0.0);
        boundingBoxType.getLowerCorner().add(1.0);
        boundingBoxType.getUpperCorner().add(0.0);
        boundingBoxType.getUpperCorner().add(1.0);
        dataType.setBoundingBoxData(boundingBoxType);
        inputType.setData(dataType);
        inputCodeType = new CodeType();
        inputCodeType.setValue("orbisgis:test:full:input:boundingboxdata");
        inputType.setIdentifier(inputCodeType);
        dataInputsType.getInput().add(inputType);
        execute.setDataInputs(dataInputsType);
        execute.setIdentifier(codeType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'locator' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'locator' should be set to 'DataInputs'", "DataInputs",
                report.getException().get(0).getLocator());

        //Test Execute with bad BoundingBox without corners
        execute = new Execute();
        dataInputsType = new DataInputsType();
        inputType = new InputType();
        dataType = new DataType();
        boundingBoxType = new BoundingBoxType();
        boundingBoxType.setCrs("http://www.opengis.net/def/crs/EPSG/8.9.2/4326");
        dataType.setBoundingBoxData(boundingBoxType);
        inputType.setData(dataType);
        inputCodeType = new CodeType();
        inputCodeType.setValue("orbisgis:test:full:input:boundingboxdata");
        inputType.setIdentifier(inputCodeType);
        dataInputsType.getInput().add(inputType);
        execute.setDataInputs(dataInputsType);
        execute.setIdentifier(codeType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'locator' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'locator' should be set to 'DataInputs'", "DataInputs",
                report.getException().get(0).getLocator());

        //Test Execute without a mandatory input
        execute = new Execute();
        CodeType simpleCodeType = new CodeType();
        simpleCodeType.setValue("orbisgis:test:simple");
        execute.setIdentifier(simpleCodeType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'exceptionCode' should be set to 'DataInputs'", "DataInputs",
                report.getException().get(0).getLocator());


        ////
        //ResponseForm test
        ////

        //Test Execute with empty responseForm
        execute = new Execute();
        execute.setIdentifier(codeType);
        ResponseFormType responseFormType = new ResponseFormType();
        execute.setResponseForm(responseFormType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'locator' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'locator' should be set to 'ResponseForm'", "ResponseForm",
                report.getException().get(0).getLocator());

        //Test Execute with too full responseForm
        execute = new Execute();
        execute.setIdentifier(codeType);
        responseFormType = new ResponseFormType();
        responseFormType.setResponseDocument(new ResponseDocumentType());
        responseFormType.setRawDataOutput(new OutputDefinitionType());
        execute.setResponseForm(responseFormType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'locator' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'locator' should be set to 'ResponseForm'", "ResponseForm",
                report.getException().get(0).getLocator());

        //Test Execute with storageNotSupported
        execute = new Execute();
        execute.setIdentifier(codeType);
        responseFormType = new ResponseFormType();
        ResponseDocumentType responseDocumentType = new ResponseDocumentType();
        responseDocumentType.setStoreExecuteResponse(true);
        DocumentOutputDefinitionType output = new DocumentOutputDefinitionType();
        CodeType outputCodeType = new CodeType();
        outputCodeType.setValue("orbisgis:test:full:output:rawdata");
        output.setIdentifier(outputCodeType);
        responseDocumentType.getOutput().add(output);
        responseFormType.setResponseDocument(responseDocumentType);
        execute.setResponseForm(responseFormType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'StorageNotSupported'", "StorageNotSupported",
                report.getException().get(0).getExceptionCode());

        //Test Execute with statusNotSupported
        execute = new Execute();
        execute.setIdentifier(codeType);
        responseFormType = new ResponseFormType();
        responseDocumentType = new ResponseDocumentType();
        responseDocumentType.setStatus(true);
        output = new DocumentOutputDefinitionType();
        outputCodeType = new CodeType();
        outputCodeType.setValue("orbisgis:test:full:output:rawdata");
        output.setIdentifier(outputCodeType);
        responseDocumentType.getOutput().add(output);
        responseFormType.setResponseDocument(responseDocumentType);
        execute.setResponseForm(responseFormType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'locator' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'locator' should be set to 'ResponseForm'", "ResponseForm",
                report.getException().get(0).getLocator());

        //Test Execute with responseDocument with output without identifier
        execute = new Execute();
        responseFormType = new ResponseFormType();
        responseDocumentType = new ResponseDocumentType();
        DocumentOutputDefinitionType outputDefinitionsType = new DocumentOutputDefinitionType();
        responseDocumentType.getOutput().add(outputDefinitionsType);
        responseFormType.setResponseDocument(responseDocumentType);
        execute.setResponseForm(responseFormType);
        execute.setIdentifier(codeType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'locator' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'locator' should be set to 'ResponseForm'", "ResponseForm",
                report.getException().get(0).getLocator());

        //Test Execute with responseDocument with unknown output format
        execute = new Execute();
        responseFormType = new ResponseFormType();
        responseDocumentType = new ResponseDocumentType();
        outputDefinitionsType = new DocumentOutputDefinitionType();
        outputDefinitionsType.setMimeType("unicorn/type");
        CodeType rawOutputCodeType = new CodeType();
        rawOutputCodeType.setValue("orbisgis:test:full:output:rawdata");
        outputDefinitionsType.setIdentifier(rawOutputCodeType);
        responseDocumentType.getOutput().add(outputDefinitionsType);
        responseFormType.setResponseDocument(responseDocumentType);
        execute.setResponseForm(responseFormType);
        execute.setIdentifier(codeType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'locator' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'locator' should be set to 'ResponseForm'", "ResponseForm",
                report.getException().get(0).getLocator());
        assertTrue("The exception 'exceptionText' should be set", report.getException().get(0).isSetExceptionText());
        assertEquals("The exception 'exceptionText' should be set to 'The format unicorn/type is not supported'",
                "The format unicorn/type is not supported", report.getException().get(0).getExceptionText().get(0));

        //Test Execute with rawData output  without identifier
        execute = new Execute();
        responseFormType = new ResponseFormType();
        outputDefinitionsType = new DocumentOutputDefinitionType();
        responseFormType.setRawDataOutput(outputDefinitionsType);
        execute.setResponseForm(responseFormType);
        execute.setIdentifier(codeType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'locator' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'locator' should be set to 'ResponseForm'", "ResponseForm",
                report.getException().get(0).getLocator());

        //Test Execute with rawData output format
        execute = new Execute();
        responseFormType = new ResponseFormType();
        outputDefinitionsType = new DocumentOutputDefinitionType();
        outputDefinitionsType.setMimeType("unicorn/type");
        rawOutputCodeType = new CodeType();
        rawOutputCodeType.setValue("orbisgis:test:full:output:rawdata");
        outputDefinitionsType.setIdentifier(rawOutputCodeType);
        responseFormType.setRawDataOutput(outputDefinitionsType);
        execute.setResponseForm(responseFormType);
        execute.setIdentifier(codeType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'locator' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'locator' should be set to 'ResponseForm'", "ResponseForm",
                report.getException().get(0).getLocator());
        assertTrue("The exception 'exceptionText' should be set", report.getException().get(0).isSetExceptionText());
        assertEquals("The exception 'exceptionText' should be set to 'The format unicorn/type is not supported'",
                "The format unicorn/type is not supported", report.getException().get(0).getExceptionText().get(0));


        ////
        //Other code types
        ////

        //Test Execute with bad language
        execute = new Execute();
        execute.setIdentifier(codeType);
        execute.setLanguage("uni:co:rn");
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'locator' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'locator' should be set to 'language'", "language",
                report.getException().get(0).getLocator());

        //Test Execute with serverBusy
        /*execute = new Execute();
        execute.setIdentifier(codeType);
        minWps100Operations.execute(execute);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'ServerBusy'", "ServerBusy",
                report.getException().get(0).getExceptionCode());*/


        //Test Execute with fileSizeExceeded
        /*execute = new Execute();
        execute.setIdentifier(codeType);
        dataInputsType = new DataInputsType();
        inputType = new InputType();
        rawInputCodeType = new CodeType();
        rawInputCodeType.setValue("orbisgis:test:full:input:rawdata");
        inputType.setIdentifier(rawInputCodeType);
        DataType dataType = new DataType();
        dataType.setComplexData(new ComplexDataType());
        inputType.setData(dataType);
        inputReferenceType = new InputReferenceType();
        try {
            inputReferenceType.setHref(TestWPS_1_0_0_Execute.class.getResource("tooBigFile.txt").toURI().toString());
        } catch (URISyntaxException ignored) {}
        inputType.setReference(inputReferenceType);
        dataInputsType.getInput().add(inputType);
        execute.setDataInputs(dataInputsType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'FileSizeExceeded'", "FileSizeExceeded",
                report.getException().get(0).getExceptionCode());*/
    }
}
