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

package org.orbiswps.server.controller.parser;

import net.opengis.ows._2.CodeType;
import net.opengis.wps._2_0.Format;
import net.opengis.wps._2_0.InputDescriptionType;
import net.opengis.wps._2_0.OutputDescriptionType;
import org.orbiswps.groovyapi.attributes.DescriptionTypeAttribute;
import org.orbiswps.groovyapi.attributes.InputAttribute;
import org.orbiswps.groovyapi.attributes.RawDataAttribute;
import org.orbiswps.server.controller.utils.FormatFactory;
import org.orbiswps.server.controller.utils.ObjectAnnotationConverter;
import org.orbiswps.server.model.MalformedScriptException;
import org.orbiswps.server.model.ObjectFactory;
import org.orbiswps.server.model.RawData;

import javax.xml.bind.JAXBElement;
import java.lang.reflect.Field;
import java.net.URI;

/**
 * Parser dedicated to the RawData parsing.
 *
 * @author Sylvain PALOMINOS
 **/

public class RawDataParser implements Parser {

    @Override
    public InputDescriptionType parseInput(Field f, Object defaultValue, URI processId) throws MalformedScriptException {
        //Instantiate the RawData
        Format format = FormatFactory.getFormatFromExtension(FormatFactory.TEXT_EXTENSION);
        RawData rawData = ObjectAnnotationConverter.annotationToObject(f.getAnnotation(RawDataAttribute.class), format);
        if(defaultValue != null && defaultValue instanceof String[]) {
            rawData.setDefaultValues((String[])defaultValue);
        }

        InputDescriptionType input = new InputDescriptionType();
        JAXBElement<RawData> jaxbElement = new ObjectFactory().createRawData(rawData);
        input.setDataDescription(jaxbElement);

        ObjectAnnotationConverter.annotationToObject(f.getAnnotation(InputAttribute.class), input);
        ObjectAnnotationConverter.annotationToObject(f.getAnnotation(DescriptionTypeAttribute.class), input,
                processId.toString());

        if(input.getIdentifier() == null){
            CodeType codeType = new CodeType();
            codeType.setValue(processId+":"+f.getName());
            input.setIdentifier(codeType);
        }

        return input;
    }

    @Override
    public OutputDescriptionType parseOutput(Field f, Object defaultValue, URI processId) throws MalformedScriptException {
        //Instantiate the RawData
        Format format = FormatFactory.getFormatFromExtension(FormatFactory.TEXT_EXTENSION);
        RawData rawData = ObjectAnnotationConverter.annotationToObject(f.getAnnotation(RawDataAttribute.class), format);
        if(defaultValue != null && defaultValue instanceof String[]) {
            rawData.setDefaultValues((String[])defaultValue);
        }

        OutputDescriptionType output = new OutputDescriptionType();
        JAXBElement<RawData> jaxbElement = new ObjectFactory().createRawData(rawData);
        output.setDataDescription(jaxbElement);

        ObjectAnnotationConverter.annotationToObject(f.getAnnotation(DescriptionTypeAttribute.class), output,
                processId.toString());

        if(output.getIdentifier() == null){
            CodeType codeType = new CodeType();
            codeType.setValue(processId+":"+f.getName());
            output.setIdentifier(codeType);
        }

        return output;
    }

    @Override
    public Class getAnnotation() {
        return RawDataAttribute.class;
    }
}
