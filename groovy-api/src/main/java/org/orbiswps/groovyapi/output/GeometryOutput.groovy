/**
 * OrbisToolBox is an OrbisGIS plugin dedicated to create and manage processing.
 *
 * OrbisToolBox is distributed under GPL 3 license. It is produced by CNRS <http://www.cnrs.fr/> as part of the
 * MApUCE project, funded by the French Agence Nationale de la Recherche (ANR) under contract ANR-13-VBDU-0004.
 *
 * OrbisToolBox is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * OrbisToolBox is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with OrbisToolBox. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/> or contact directly: info_at_orbisgis.org
 */

package org.orbiswps.groovyapi.output

import groovy.transform.AnnotationCollector
import groovy.transform.Field
import org.orbiswps.groovyapi.attributes.DescriptionTypeAttribute
import org.orbiswps.groovyapi.attributes.OutputAttribute
import org.orbiswps.groovyapi.attributes.GeometryAttribute

/**
 * Geometry output annotation.
 * The Geometry is a complex data that represents a geometry.
 * As an output, this annotation should be placed just before the variable.
 *
 * The following fields must be defined (mandatory) :
 *  - title : String[]
 *       Title of a process, input, and output. Normally available for display to a human. It is composed either a
 *       unique title or a translated title, its language, another title, its language ...
 *       i.e. title = "title" or tittle = ["titleFr", "fr", "titleEn", "en"]
 *
 * The following fields can be defined (optional) :
 *  - description : String[]
 *      Brief narrative description of a process, input, and output. Normally available for display to a human.It is
 *      composed either a unique description or a translated description, its language, another description, its language ...
 *      i.e. description = "description" or description = ["descriptionFr", "fr", "descriptionEn", "en"]
 *
 *  - keywords : String[]
 *      Array of keywords that characterize a process, its inputs, and outputs. Normally available for display to a
 *      human. It is composed of a succession of two String : the human readable keyword list coma
 *      separated and its language.
 *      i.e. keywords = ["the keyword 1,the keyword 2", "en",
 *                       "le mot clef 1, le mot clef 2", "fr"]
 *  - identifier : String
 *      Unambiguous identifier of a process, input, and output. It should be a valid URI.
 *
 *  - metadata : String[]
 *      Reference to additional metadata about this item. It is composed of a succession of three String : the metadata
 *      role, the metadata title and the href, coma separated.
 *      i.e. metadata = ["role1,title,href1",
 *                       "role2,title,href2"]
 *
 *  - geometryTypes : String[]
 *      Array of geometry type allowed. If no types are specified, accept all.
 *
 *  - excludedTypes : String[]
 *      Array of the type not allowed for the geometry.
 *
 *  - dimension : int
 *      Dimension of the geometry (can be 2 or 3).
 *
 * Usage example can be found at https://github.com/orbisgis/orbisgis/wiki/
 *
 * @author Sylvain PALOMINOS
 */
@AnnotationCollector([Field, GeometryAttribute, OutputAttribute, DescriptionTypeAttribute])
@interface GeometryOutput {}