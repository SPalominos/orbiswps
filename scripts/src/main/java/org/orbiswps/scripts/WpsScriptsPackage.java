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
package org.orbiswps.scripts;

import org.apache.commons.io.IOUtils;
import org.orbiswps.client.api.WpsClient;
import org.orbiswps.server.WpsServer;
import org.orbiswps.server.controller.process.ProcessIdentifier;
import org.orbiswps.server.utils.ProcessMetadata;
import org.orbiswps.server.utils.WpsScriptUtils;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;

/**
 * In the WpsService, the script are organized in a tree, which has the WpsService as root.
 *
 * Scripts can be add to the tree under a specific node path with custom icon with the following method :
 *      localWpsService.addLocalScript('processFile', 'icon', 'boolean', 'nodePath');
 * with the following parameter :
 *      processFile : The File object corresponding to the script. Be careful, the plugin resource files can't be
 *              accessed from the outside of the plugin. So you have to copy it (in a temporary file as example) before
 *              adding it to the WpsService.
 *      icon : Array of Icon object to use for the WpsClient tree containing the processes. The first icon will be used
 *              for the first node of the path, the second icon for the second node ... If the node already exists,
 *              its icon won't be changed. If there is less icon than node, the last icon will be used for the others.
 *              If no icon are specified, the default one from the WpsClient will be used.
 *      boolean : it SHOULD be true. Else the used will be able to remove the process from the WpsClient without
 *              deactivating the plugin.
 *      nodePath : Path to the node where the process should be add. If nodes of the path doesn't exists, they will be
 *              created.
 * This add method return a ProcessIdentifier object which give all the information needed to identify a process. It
 * should be kept to be able to remove it later.
 *
 *
 * The 'customLoadScript()' method load the scripts one by one under different file path with  different icons.
 *
 *
 * When the plugin is launched , the 'activate()' method is call. This method load the scripts in the
 * WpsService and refresh the WpsClient.
 * When the plugin is stopped or uninstalled, the 'deactivate()' method is called. This method removes the loaded script
 * from the WpsService and refreshes the WpsClient.
 *
 */
public class WpsScriptsPackage {

    /** I18N object */
    private static final I18n I18N = I18nFactory.getI18n(WpsScriptsPackage.class);

    /**
     * Logger instance.
     */
    protected static final Logger LOGGER = LoggerFactory.getLogger(WpsScriptsPackage.class);

    /**
     * The WPS service.
     * The WPS service contains all the declared processes available for the client.
     */
    protected WpsServer wpsServer;

    /**
     * The WPS client.
     */
    protected WpsClient wpsClient;

    /**
     * List of identifier of the processes loaded by this plusgin.
     */
    protected List<URI> listIdProcess;

    /**
     * Adds to the WpsServer all the script contained in the 'scripts' resource folder.
     */
    protected void loadAllScripts(){
        String[] icons = new String[]{loadIcon("orbisgis.png")};
        String resourceName = "scripts";
        String nodePath = I18N.tr("OrbisGIS");
        URL resourceUrl = this.getClass().getResource(resourceName);
        List<URL> urls = new ArrayList<>();
        if(resourceUrl.getProtocol().equalsIgnoreCase("bundle")){
            addAllGroovyUrls(resourceUrl, icons, resourceName);
        }
        else if(resourceUrl.getProtocol().equalsIgnoreCase("file")){
            addAllGroovyUrls(new File(resourceUrl.getFile()), icons, resourceName);
        }
        for(URL url : urls){
            loadScript(url, icons, nodePath);
        }
    }

    /**
     * Adds recursively all the script contained in a folder.
     * @param resourceUrl Url of the directory to explore.
     */
    private void addAllGroovyUrls(URL resourceUrl, String[] icons, String nodePath){
        //Get the URL of all the files contained in the 'script' folder.
        Enumeration<URL> enumUrl = FrameworkUtil.getBundle(this.getClass()).findEntries(resourceUrl.getFile(), "*", false);
        //For each url, if it is a file, load it, if it is a directory, check its content.
        while(enumUrl.hasMoreElements()) {
            URL scriptUrl = enumUrl.nextElement();
            //If the url if a groovy file,
            if(scriptUrl.getFile().endsWith(".groovy")) {
                loadScript(scriptUrl, icons, nodePath);
            }
            //If the url is a folder,
            else {
                //Recursively add the scripts.
                addAllGroovyUrls(scriptUrl, icons, nodePath+"/"+ I18N.tr(new File(scriptUrl.getFile()).getName()));
            }
        }
    }

    /**
     * Adds recursively all the script contained in a folder.
     * @param directory Directory to explore.
     */
    private void addAllGroovyUrls(File directory, String[] icons, String nodePath){
        for(File f : directory.listFiles()) {
            if (f.isDirectory()) {
                addAllGroovyUrls(f, icons, nodePath+"/"+ I18N.tr(f.getName()));
            }
            else {
                try {
                    loadScript(f.toURI().toURL(), icons, nodePath);
                } catch (MalformedURLException e) {
                    LOGGER.warn("Unable to get the URL of the script : {0}", directory.getName());
                }
            }
        }
    }

    /**
     * Load a script by adding it to the wpsServer and saving the resulting ProcessIdentifier.
     * @param scriptUrl Url of the script to add.
     * @param icons Array of icons to use in the UI.
     * @param path Path to use for this script for the UI representation
     */
    private void loadScript(URL scriptUrl, String[] icons, String path) {
        String tempFolderPath = wpsServer.getScriptFolder();
        File tempFolder = new File(tempFolderPath, "wpsscripts");
        if (!tempFolder.exists()) {
            if (!tempFolder.mkdirs()) {
                LOGGER.error(I18N.tr("Unable to create the OrbisGIS temporary folder."));
                return;
            }
        }
        File tempFile = WpsScriptUtils.copyResourceFile(scriptUrl, tempFolder);
        List<ProcessIdentifier> piList = wpsServer.addProcess(tempFile);
        if (piList != null) {
            for (ProcessIdentifier pi : piList) {
                if (pi == null || pi.getProcessDescriptionType() == null || pi.getProcessDescriptionType().getInput() == null) {
                    LOGGER.error(I18N.tr("Error, the ProcessIdentifier get is malformed."));
                    return;
                }
                URI uri = URI.create(pi.getProcessDescriptionType().getIdentifier().getValue());
                if (wpsClient != null) {
                    Map<ProcessMetadata.INTERNAL_METADATA, Object> metadataMap = new HashMap<>();
                    metadataMap.put(ProcessMetadata.INTERNAL_METADATA.IS_REMOVABLE, false);
                    metadataMap.put(ProcessMetadata.INTERNAL_METADATA.NODE_PATH, path);
                    metadataMap.put(ProcessMetadata.INTERNAL_METADATA.ICON_ARRAY, icons);
                    wpsClient.addProcessMetadata(uri, metadataMap);
                }
                listIdProcess.add(uri);
            }
        }
    }

    /**
     * This method removes all the scripts contained in the 'listIdProcess' list. (Be careful before any modification)
     */
    protected void removeAllScripts(){
        for(URI idProcess : listIdProcess){
            wpsServer.removeProcess(idProcess);
        }
    }

    /**
     * This method copy the an icon into the temporary system folder to make it accessible by the WpsClient
     * @param iconName
     * @return 
     */
    protected String loadIcon(String iconName){
        URL iconUrl = this.getClass().getResource("icons/"+iconName);
        if(iconUrl == null){
            LOGGER.error(I18N.tr("Unable to get the URL of the icon {0}", iconName));
            return null;
        }
        String tempFolderPath = wpsServer.getScriptFolder();
        File tempFolder = new File(tempFolderPath, "wpsscripts");
        if(!tempFolder.exists()) {
            if(!tempFolder.mkdirs()){
                LOGGER.error(I18N.tr("Unable to create the OrbisGIS temporary folder."));
                return null;
            }
        }
        //Create a temporary File object
        final File tempFile = new File(tempFolder.getAbsolutePath(), iconName);
        if(!tempFile.exists()) {
            try{
                if(!tempFile.createNewFile()){
                    LOGGER.error(I18N.tr("Unable to create the icon file."));
                    return null;
                }
            } catch (IOException e) {
                LOGGER.error(I18N.tr("Unable to create the icon file.\n Error : {0}", e.getMessage()));
            }
        }
        //Copy the content of the resource file in the temporary file.
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            IOUtils.copy(iconUrl.openStream(), out);
        }
        catch (Exception e){
            LOGGER.error(I18N.tr("Unable to copy the content of the icon to the temporary file."));
            return null;
        }
        return tempFile.getAbsolutePath();
    }
}
