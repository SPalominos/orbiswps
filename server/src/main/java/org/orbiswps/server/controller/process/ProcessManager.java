/**
 * OrbisGIS is a java GIS application dedicated to research in GIScience.
 * OrbisGIS is developed by the GIS group of the DECIDE team of the 
 * Lab-STICC CNRS laboratory, see <http://www.lab-sticc.fr/>.
 *
 * The GIS group of the DECIDE team is located at :
 *
 * Laboratoire Lab-STICC – CNRS UMR 6285
 * Equipe DECIDE
 * UNIVERSITÉ DE BRETAGNE-SUD
 * Institut Universitaire de Technologie de Vannes
 * 8, Rue Montaigne - BP 561 56017 Vannes Cedex
 * 
 * OrbisGIS is distributed under GPL 3 license.
 *
 * Copyright (C) 2007-2014 CNRS (IRSTV FR CNRS 2488)
 * Copyright (C) 2015-2017 CNRS (Lab-STICC UMR CNRS 6285)
 *
 * This file is part of OrbisGIS.
 *
 * OrbisGIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OrbisGIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */

package org.orbiswps.server.controller.process;

import groovy.lang.GroovyObject;
import net.opengis.ows._2.CodeType;
import net.opengis.ows._2.MetadataType;
import net.opengis.wps._2_0.*;
import org.orbisgis.commons.progress.ProgressMonitor;
import org.orbiswps.groovyapi.attributes.DescriptionTypeAttribute;
import org.orbiswps.server.WpsServer;
import org.orbiswps.server.controller.parser.ParserController;
import org.orbiswps.server.controller.utils.CancelClosure;
import org.orbiswps.server.controller.utils.WpsSql;
import org.orbiswps.server.model.*;
import org.orbiswps.server.model.Enumeration;
import org.orbiswps.server.utils.ProcessMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import javax.sql.DataSource;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.*;

/**
 * Class used to manage process.
 * It manages the sources (remote or local) and keeps the list of instantiated processes.
 *
 * @author Sylvain PALOMINOS
 **/

public class ProcessManager {
    /** List of process identifier*/
    private List<ProcessIdentifier> processIdList;
    /** Controller used to parse process */
    private ParserController parserController;
    private DataSource dataSource;
    private WpsServer wpsService;
    private Map<UUID, CancelClosure> closureMap;
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessManager.class);
    /** I18N object */
    private static final I18n I18N = I18nFactory.getI18n(ProcessManager.class);

    /**
     * Main constructor.
     * @param dataSource
     * @param wpsService
     */
    public ProcessManager(DataSource dataSource, WpsServer wpsService){
        processIdList = new ArrayList<>();
        parserController = new ParserController();
        this.setDataSource(dataSource);
        this.wpsService = wpsService;
        this.closureMap = new HashMap<>();
    }

    /**
     * Sets the DataSource that should be used by the ProcessManager
     * @param dataSource The DataSource that should be used by the ProcessManager
     */
    public void setDataSource(DataSource dataSource){
        this.dataSource = dataSource;
    }

    public ProcessIdentifier addScript(URI scriptUri, String[] category, boolean isRemovable, String nodePath){
        File f = new File(scriptUri);
        if(!f.exists()){
            LOGGER.error(I18N.tr("The script file doesn't exists."));
            return null;
        }
        //Test that the script name is not only '.groovy'
        if (f.getName().endsWith(".groovy") && f.getName().length()>7) {
            //Ensure that the process does not already exists.
            //Parse the process
            ProcessOffering processOffering = null;
            try {
                processOffering = parserController.parseProcess(f.getAbsolutePath());
                if(processOffering == null){
                    LOGGER.error(I18N.tr("Unable to parse the process {0}.", scriptUri));
                    return null;
                }
                if(getProcess(processOffering.getProcess().getIdentifier()) != null){
                    LOGGER.warn(I18N.tr("A process with the identifier {0} already exists.",
                            processOffering.getProcess().getIdentifier().getValue()));
                    return null;
                }
                //Check if the process is compatible with the DBMS connected to OrbisGIS.
                boolean isAcceptedDBMS = true;
                for(MetadataType metadata : processOffering.getProcess().getMetadata()){
                    if(metadata.getRole().equalsIgnoreCase(ProcessMetadata.DBMS_TYPE_NAME)){
                        isAcceptedDBMS = false;
                    }
                }
                if(! isAcceptedDBMS){
                    for(MetadataType metadata : processOffering.getProcess().getMetadata()){
                        if(wpsService.getDatabase() == null ||
                                (metadata.getRole().equalsIgnoreCase(ProcessMetadata.DBMS_TYPE_NAME) &&
                                metadata.getTitle().toLowerCase().equals(wpsService.getDatabase().name().toLowerCase()))){
                            isAcceptedDBMS = true;
                        }
                    }
                }
                if(!isAcceptedDBMS){
                    return new ProcessIdentifier(null, null, null, null);
                }

                //Sets the metadatas used by the OrbisGIS wps client
                MetadataType isRemovableMetadata = new MetadataType();
                isRemovableMetadata.setTitle(ProcessMetadata.INTERNAL_METADATA.IS_REMOVABLE.name());
                isRemovableMetadata.setRole(ProcessMetadata.INTERNAL_METADATA_NAME);
                isRemovableMetadata.setAbstractMetaData(isRemovable);
                processOffering.getProcess().getMetadata().add(isRemovableMetadata);
                if(nodePath != null) {
                    MetadataType nodePathMetadata = new MetadataType();
                    nodePathMetadata.setTitle(ProcessMetadata.INTERNAL_METADATA.NODE_PATH.name());
                    nodePathMetadata.setRole(ProcessMetadata.INTERNAL_METADATA_NAME);
                    nodePathMetadata.setAbstractMetaData(nodePath);
                    processOffering.getProcess().getMetadata().add(nodePathMetadata);
                }
                if(category != null) {
                    MetadataType iconArrayMetadata = new MetadataType();
                    iconArrayMetadata.setTitle(ProcessMetadata.INTERNAL_METADATA.ICON_ARRAY.name());
                    iconArrayMetadata.setRole(ProcessMetadata.INTERNAL_METADATA_NAME);
                    String iconString = "";
                    for (String icon : category) {
                        if (!iconString.isEmpty()) {
                            iconString += ";";
                        }
                        iconString += icon;
                    }
                    iconArrayMetadata.setAbstractMetaData(iconString);
                    processOffering.getProcess().getMetadata().add(iconArrayMetadata);
                }
            } catch (MalformedScriptException e) {
                LOGGER.error(I18N.tr("Unable to parse the process {0}.\nCause : {1}", scriptUri, e.getMessage()), e);
            }
            //If the process is not already registered
            if(processOffering != null) {
                //Save the process in a ProcessIdentifier
                ProcessIdentifier pi = new ProcessIdentifier(processOffering, scriptUri, f.getParentFile().toURI(),
                        nodePath);
                pi.setCategory(category);
                pi.setRemovable(isRemovable);
                processIdList.add(pi);
                return pi;
            }
        }
        LOGGER.error(I18N.tr("The script is not valid."));
        return null;
    }

    /**
     * Adds a local source to the toolbox and get all the groovy script.
     * @param uri URI to the local source.
     * @param category
     * @return 
     */
    public List<ProcessIdentifier> addLocalSource(URI uri, String[] category){
        List<ProcessIdentifier> piList = new ArrayList<>();
        File folder = new File(uri);
        if(folder.exists() && folder.isDirectory()){
            for(File f : folder.listFiles()){
                ProcessIdentifier pi = addScript(f.toURI(), category, true, "localhost");
                if(pi != null) {
                    piList.add(pi);
                }
            }
        }
        return piList;
    }

    /**
     * Execute the given process with the given data.
     * @param jobId UUID of the job to execute.
     * @param processIdentifier ProcessIdentifier of the process to execute.
     * @param dataMap Map containing the data for the process.
     * @param propertiesMap Map containing the properties for the GroovyObject.
     * @param progressMonitor ProgressMonitor associated to the process execution.
     * @return The groovy object on which the 'processing' method will be called.
     */
    public GroovyObject executeProcess(
            UUID jobId,
            ProcessIdentifier processIdentifier,
            Map<URI, Object> dataMap,
            Map<String, Object> propertiesMap,
            ProgressMonitor progressMonitor){

        ProcessDescriptionType process = processIdentifier.getProcessDescriptionType();
        Class clazz = parserController.getProcessClass(processIdentifier.getSourceFileURI());
        GroovyObject groovyObject = createProcess(process, clazz, dataMap);
        if(groovyObject != null) {
            CancelClosure closure = new CancelClosure(this);
            closureMap.put(jobId, closure);
            if (dataSource != null) {
                WpsSql sql = new WpsSql(dataSource);
                sql.withStatement(closure);
                groovyObject.setProperty("sql", sql);
                groovyObject.setProperty("isH2", wpsService.getDatabase().equals(WpsServer.Database.H2GIS));
            }
            groovyObject.setProperty("logger", LoggerFactory.getLogger(ProcessManager.class));
            groovyObject.setProperty("progressMonitor", progressMonitor);
            for(Map.Entry<String, Object> entry : propertiesMap.entrySet()){
                groovyObject.setProperty(entry.getKey(), entry.getValue());
            }
            groovyObject.invokeMethod("processing", null);
            retrieveData(process, clazz, groovyObject, dataMap);
        }
        return groovyObject;
    }

    /**
     * Retrieve the data from the groovy object and store the into the dataMap.
     * @param process Process that has generate the groovy object.
     * @param groovyObject GroovyObject containing the processed data.
     * @param dataMap Map linking the data and their identifier.
     */
    private void retrieveData(ProcessDescriptionType process, Class clazz, GroovyObject groovyObject, Map<URI, Object> dataMap){
        ProcessIdentifier pi = null;
        for(ProcessIdentifier proId : processIdList){
            if(proId.getProcessDescriptionType().getIdentifier().getValue().equals(process.getIdentifier().getValue())){
                pi = proId;
            }
        }
        if(pi == null){
            return;
        }
        try {
            for(InputDescriptionType i : process.getInput()) {
                Field field = null;
                for(Field f : clazz.getDeclaredFields()){
                    for(Annotation a : f.getDeclaredAnnotations()){
                        if(a instanceof DescriptionTypeAttribute){
                            DescriptionTypeAttribute descriptionTypeAttribute = (DescriptionTypeAttribute) a;
                            String id = descriptionTypeAttribute.identifier();
                            String inputId = i.getIdentifier().getValue();
                            String processId = process.getIdentifier().getValue();
                            if(inputId.equals(processId+":"+id) || inputId.equals(id) ||
                                    inputId.equals(processId+":"+f.getName())){
                                field = f;
                            }
                        }
                    }
                }
                if(field != null) {
                    field.setAccessible(true);
                    dataMap.put(URI.create(i.getIdentifier().getValue()), field.get(groovyObject));
                }
            }
            for(OutputDescriptionType o : process.getOutput()) {
                Field field = null;
                for(Field f : clazz.getDeclaredFields()){
                    for(Annotation a : f.getDeclaredAnnotations()){
                        if(a instanceof DescriptionTypeAttribute){
                            DescriptionTypeAttribute descriptionTypeAttribute = (DescriptionTypeAttribute) a;
                            String id = descriptionTypeAttribute.identifier();
                            String outputId = o.getIdentifier().getValue();
                            String processId = process.getIdentifier().getValue();
                            if(outputId.equals(processId+":"+id) || outputId.equals(id) ||
                                    outputId.equals(processId+":"+f.getName())){
                                field = f;
                            }
                        }
                    }
                }
                if(field != null) {
                    field.setAccessible(true);
                    dataMap.put(URI.create(o.getIdentifier().getValue()), field.get(groovyObject));
                }
            }
        } catch (IllegalAccessException e) {
            LoggerFactory.getLogger(ProcessManager.class).error(e.getMessage());
        }
    }

    /**
     * Create a groovy object corresponding to the process with the given data.
     * @param process Process that will generate the groovy object.
     * @param dataMap Map of the data for the process.
     * @return A groovy object representing the process with the given data.
     */
    private GroovyObject createProcess(ProcessDescriptionType process, Class clazz, Map<URI, Object> dataMap){
        ProcessIdentifier pi = null;
        for(ProcessIdentifier proId : processIdList){
            if(proId.getProcessDescriptionType().getIdentifier().getValue().equals(process.getIdentifier().getValue())){
                pi = proId;
            }
        }
        if(pi == null){
            return null;
        }
        GroovyObject groovyObject;
        try {
            groovyObject = (GroovyObject) clazz.newInstance();
        } catch (InstantiationException|IllegalAccessException e) {
            LoggerFactory.getLogger(ProcessManager.class).error(e.getMessage());
            return null;
        }
        try {
            for(InputDescriptionType i : process.getInput()) {
                Field field = null;
                for(Field f : clazz.getDeclaredFields()){
                    for(Annotation a : f.getDeclaredAnnotations()){
                        if(a instanceof DescriptionTypeAttribute){
                            DescriptionTypeAttribute descriptionTypeAttribute = (DescriptionTypeAttribute) a;
                            String id = descriptionTypeAttribute.identifier();
                            String inputId = i.getIdentifier().getValue();
                            String processId = process.getIdentifier().getValue();
                            if(inputId.equals(processId+":"+id) || inputId.equals(id) ||
                                    inputId.equals(processId+":"+f.getName())){
                                field = f;
                            }
                        }
                    }
                }
                if(field != null) {
                    field.setAccessible(true);
                    Object data = dataMap.get(URI.create(i.getIdentifier().getValue()));
                    //If the descriptionType contains a JDBCValue, a JDBCColumn or an Enumeration, parse the value
                    // which is coma separated.
                    DataDescriptionType dataDescriptionType = i.getDataDescription().getValue();
                    if(dataDescriptionType instanceof JDBCValue ||
                            dataDescriptionType instanceof JDBCColumn ||
                            dataDescriptionType instanceof Enumeration ||
                            dataDescriptionType instanceof RawData){
                        if(data != null) {
                            data = data.toString().split("\\t");
                        }
                    }
                    if(dataDescriptionType instanceof LiteralDataType) {
                        if (Number.class.isAssignableFrom(field.getType()) && data != null) {
                            try {
                                Method valueOf = field.getType().getMethod("valueOf", String.class);
                                if (valueOf != null) {
                                    valueOf.setAccessible(true);
                                    data = valueOf.invoke(this, data.toString());
                                }
                            } catch (NoSuchMethodException | InvocationTargetException e) {
                                LOGGER.warn(I18N.tr("Unable to convert the LiteralData to the good script type."));
                            }
                        }
                        else if(data != null && (data.equals("true") || data.equals("false"))){
                            data = data.equals("true");
                        }
                    }
                    field.set(groovyObject, data);
                }
            }
            for(OutputDescriptionType o : process.getOutput()) {
                Field field = null;
                for(Field f : clazz.getDeclaredFields()){
                    for(Annotation a : f.getDeclaredAnnotations()){
                        if(a instanceof DescriptionTypeAttribute){
                            DescriptionTypeAttribute descriptionTypeAttribute = (DescriptionTypeAttribute) a;
                            String id = descriptionTypeAttribute.identifier();
                            String outputId = o.getIdentifier().getValue();
                            String processId = process.getIdentifier().getValue();
                            if(outputId.equals(processId+":"+id) || outputId.equals(id) ||
                                    outputId.equals(processId+":"+f.getName())){
                                field = f;
                            }
                        }
                    }
                }
                if(field != null) {
                    field.setAccessible(true);
                    field.set(groovyObject, dataMap.get(URI.create(o.getIdentifier().getValue())));
                }
            }
        } catch (IllegalAccessException e) {
            LoggerFactory.getLogger(ProcessManager.class).error(e.getMessage());
            return null;
        }
        return groovyObject;
    }

    /**
     * Return the process corresponding to the given identifier.
     * The identifier can the the one of the process or an input or an output.
     * @param identifier Identifier of the desired process.
     * @return The process.
     */
    public ProcessDescriptionType getProcess(CodeType identifier){
        for(ProcessIdentifier pi : processIdList){
            if(pi.getProcessDescriptionType().getIdentifier().getValue().equals(identifier.getValue())){
                return pi.getProcessDescriptionType();
            }
        }
        for(ProcessIdentifier pi : processIdList){
            for(InputDescriptionType input : pi.getProcessDescriptionType().getInput()) {
                if (input.getIdentifier().getValue().equals(identifier.getValue())) {
                    return pi.getProcessDescriptionType();
                }
            }
            for(OutputDescriptionType output : pi.getProcessDescriptionType().getOutput()) {
                if (output.getIdentifier().getValue().equals(identifier.getValue())) {
                    return pi.getProcessDescriptionType();
                }
            }
        }
        return null;
    }

    /**
     * Return the field of the given class corresponding to the given identifier.
     * @param clazz Class where is the field.
     * @param identifier Identifier of the field.
     * @return The field.
     */
    private Field getField(Class clazz, String identifier){
        for(Field f : clazz.getDeclaredFields()){
            for(Annotation a : f.getDeclaredAnnotations()){
                if(a instanceof DescriptionTypeAttribute){
                    if(((DescriptionTypeAttribute)a).identifier().equals(identifier)){
                        return f;
                    }
                    if(identifier.endsWith(":input:"+f.getName()) ||
                            identifier.endsWith(":output:"+f.getName())){
                        return f;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Remove the given process.
     * @param process Process to remove.
     */
    public void removeProcess(ProcessDescriptionType process) {
        ProcessIdentifier toRemove = null;
        for(ProcessIdentifier pi : processIdList){
            if(pi.getProcessDescriptionType().getIdentifier().getValue().equals(process.getIdentifier().getValue())){
                toRemove = pi;
            }
        }
        if(toRemove != null){
            processIdList.remove(toRemove);
        }
    }

    /**
     * Returns the ProcessIdentifier containing the process with the given CodeType.
     * @param identifier CodeType used as identifier of a process.
     * @return The process.
     */
    public ProcessIdentifier getProcessIdentifier(CodeType identifier){
        for(ProcessIdentifier pi : processIdList){
            if(pi.getProcessDescriptionType().getIdentifier().getValue().equals(identifier.getValue())){
                return pi;
            }
        }
        return null;
    }

    /**
     * Returns all the process identifiers.
     * @return All the process identifiers.
     */
    public List<ProcessIdentifier> getAllProcessIdentifier(){
        return processIdList;
    }

    /**
     * Returns a string containing all the sources add to the service with all the URI separated by a ;.
     * @return A string containing all the sources add to the service with all the URI separated by a ;.
     */
    public String getListSourcesAsString(){
        String str = "";
        for(ProcessIdentifier pi : processIdList){
            if(pi.isRemovable()) {
                if (str.isEmpty()) {
                    str += pi.getSourceFileURI();
                } else {
                    str += ";" + pi.getSourceFileURI();
                }
            }
        }
        return str;
    }

    /**
     * Cancel the job corresponding to the jobID.
     * @param jobId Id of the job to cancel.
     */
    public void cancelProcess(UUID jobId){
        closureMap.get(jobId).cancel();
    }

    public void filterProcessByDatabase(WpsServer.Database database) {
        List<ProcessDescriptionType> toRemove = new ArrayList<>();
        for(ProcessIdentifier pi : getAllProcessIdentifier()){
            boolean isAccepted = false;
            for(MetadataType metadata : pi.getProcessDescriptionType().getMetadata()){
                if(metadata.getRole().equalsIgnoreCase(ProcessMetadata.DBMS_TYPE_NAME) &&
                        metadata.getTitle().toLowerCase().equals(wpsService.getDatabase().name().toLowerCase())){
                    isAccepted = true;
                }
            }
            if(!isAccepted){
                toRemove.add(pi.getProcessDescriptionType());
            }
        }
        for(ProcessDescriptionType processDescriptionType : toRemove){
            this.removeProcess(processDescriptionType);
        }
    }
}
