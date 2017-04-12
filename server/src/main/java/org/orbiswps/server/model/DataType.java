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

package org.orbiswps.server.model;

import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Types;


/**
 * Enumeration of the LiteralData type.
 *
 * For more information : http://www.w3.org/TR/2004/REC-xmlschema-2-20041028/datatypes.html
 *
 * @author Sylvain PALOMINOS
 */

public enum DataType {
    //LiteralData types
    NUMBER("number"),
    INTEGER("http://www.w3.org/TR/2004/REC-xmlschema-2-20041028/datatypes.html#integer"),
    DOUBLE("http://www.w3.org/TR/2004/REC-xmlschema-2-20041028/datatypes.html#double"),
    FLOAT("http://www.w3.org/TR/2004/REC-xmlschema-2-20041028/datatypes.html#float"),
    SHORT("http://www.w3.org/TR/2004/REC-xmlschema-2-20041028/datatypes.html#short"),
    BYTE("http://www.w3.org/TR/2004/REC-xmlschema-2-20041028/datatypes.html#byte"),
    UNSIGNED_BYTE("http://www.w3.org/TR/2004/REC-xmlschema-2-20041028/datatypes.html#unsignedByte"),
    LONG("http://www.w3.org/TR/2004/REC-xmlschema-2-20041028/datatypes.html#long"),
    STRING("http://www.w3.org/TR/2004/REC-xmlschema-2-20041028/datatypes.html#string"),
    BOOLEAN("http://www.w3.org/TR/2004/REC-xmlschema-2-20041028/datatypes.html#boolean"),

    //Other types
    OTHER("other"),
    GEOMETRY("geometry"),
    POINT("point"),
    LINESTRING("linestring"),
    POLYGON("polygon"),
    MULTIPOINT("multipolygon"),
    MULTILINESTRING("multilinestring"),
    MULTIPOLYGON("multipolygon"),
    RASTER("raster"),
    NONE("none");

    /** URI for the data type. */
    private URI uri;

    /**
     * Main constructor.
     * @param uriString String of the URI to the reference of the type.
     */
    DataType(String uriString) {
        try {
            this.uri = new URI(uriString);
        } catch (URISyntaxException e) {
            LoggerFactory.getLogger(DataType.class).error(e.getMessage());
        }
    }

    public static boolean isSpatialType(DataType dataType){
        return dataType.equals(GEOMETRY) ||
                dataType.equals(POINT) ||
                dataType.equals(LINESTRING) ||
                dataType.equals(POLYGON) ||
                dataType.equals(MULTIPOINT) ||
                dataType.equals(MULTILINESTRING) ||
                dataType.equals(MULTIPOLYGON);
    }

    public static DataType getDataTypeFromFieldType(String fieldType){
        return DataType.valueOf(fieldType);
    }

    /**
     * Returns the URI of the type.
     * @return The URI of the type.
     */
    public URI getUri() {
        return uri;
    }

    public static boolean testDBType(DataType dataType, String dbTypeName){
        dbTypeName = dbTypeName.toUpperCase();
        switch(dataType) {
            case INTEGER:
                return (dbTypeName.equals("INT") || dbTypeName.equals("INTEGER") ||
                        dbTypeName.equals("MEDIUMINT") || dbTypeName.equals("INT4") || dbTypeName.equals("SIGNED"));
            case BOOLEAN:
                return (dbTypeName.equals("BOOLEAN") || dbTypeName.equals("BIT") || dbTypeName.equals("BOOL"));
            case BYTE:
                return (dbTypeName.equals("TINYINT"));
            case SHORT:
                return (dbTypeName.equals("SMALLINT") || dbTypeName.equals("INT2") || dbTypeName.equals("YEAR"));
            case LONG:
                return (dbTypeName.equals("BIGINT") || dbTypeName.equals("INT8") || dbTypeName.equals("IDENTITY") ||
                        dbTypeName.equals("BIGSERIAL") || dbTypeName.equals("SERIAL8"));
            case DOUBLE:
                return (dbTypeName.equals("DOUBLE") || dbTypeName.equals("FLOAT") || dbTypeName.equals("FLOAT8"));
            case FLOAT:
                return (dbTypeName.equals("REAL") || dbTypeName.equals("FLOAT4"));
            case STRING:
                return (dbTypeName.equals("VARCHAR") || dbTypeName.equals("LONGVARCHAR") ||
                        dbTypeName.equals("VARCHAR2") || dbTypeName.equals("NVARCHAR") ||
                        dbTypeName.equals("NVARCHAR2") || dbTypeName.equals("VARCHAR_CASESENSITIVE") ||
                        dbTypeName.equals("VARCHAR_IGNORECASE") || dbTypeName.equals("CHAR") ||
                        dbTypeName.equals("CHARACTER") || dbTypeName.equals("NCHAR"));

            case NUMBER:
                return (dbTypeName.equals("INT") || dbTypeName.equals("INTEGER") || dbTypeName.equals("MEDIUMINT") ||
                        dbTypeName.equals("INT4") || dbTypeName.equals("SIGNED") || dbTypeName.equals("TINYINT") ||
                        dbTypeName.equals("SMALLINT") || dbTypeName.equals("INT2") || dbTypeName.equals("YEAR") ||
                        dbTypeName.equals("BIGINT") || dbTypeName.equals("INT8") || dbTypeName.equals("IDENTITY") ||
                        dbTypeName.equals("DOUBLE") || dbTypeName.equals("FLOAT") || dbTypeName.equals("FLOAT8") ||
                        dbTypeName.equals("REAL") || dbTypeName.equals("FLOAT4"));

            case OTHER:
                return (dbTypeName.equals("OTHER"));
            case GEOMETRY:
                return (dbTypeName.equals("POINT") || dbTypeName.equals("POLYGON") || dbTypeName.equals("LINESTRING") ||
                        dbTypeName.equals("MULTIPOINT") || dbTypeName.equals("MULTILINESTRING") ||
                        dbTypeName.equals("GEOMETRY") || dbTypeName.equals("MULTIPOLYGON") ||
                        dbTypeName.equals("GEOMETRYCOLLECTION"));
            case POINT:
                return (dbTypeName.equalsIgnoreCase(POINT.name()));
            case LINESTRING:
                return (dbTypeName.equalsIgnoreCase(LINESTRING.name()));
            case POLYGON:
                return (dbTypeName.equalsIgnoreCase(POLYGON.name()));
            case MULTIPOINT:
                return (dbTypeName.equalsIgnoreCase(MULTIPOINT.name()));
            case MULTILINESTRING:
                return (dbTypeName.equalsIgnoreCase(MULTILINESTRING.name()));
            case MULTIPOLYGON:
                return (dbTypeName.equalsIgnoreCase(MULTIPOLYGON.name()));
            default: return false;
        }
    }

    public static boolean testGeometryType(DataType dataType, int type){
        switch(dataType) {
            case GEOMETRY:
                return (type == GeometryTypeCodes.GEOMETRY) ||
                        (type == GeometryTypeCodes.POINT) ||
                        (type == GeometryTypeCodes.LINESTRING) ||
                        (type == GeometryTypeCodes.POLYGON) ||
                        (type == GeometryTypeCodes.MULTIPOINT) ||
                        (type == GeometryTypeCodes.MULTILINESTRING) ||
                        (type == GeometryTypeCodes.MULTIPOLYGON);
            case POINT:
                return type == GeometryTypeCodes.POINT;
            case LINESTRING:
                return type == GeometryTypeCodes.LINESTRING;
            case POLYGON:
                return type == GeometryTypeCodes.POLYGON;
            case MULTIPOINT:
                return type == GeometryTypeCodes.MULTIPOINT;
            case MULTILINESTRING:
                return type == GeometryTypeCodes.MULTILINESTRING;
            case MULTIPOLYGON:
                return type == GeometryTypeCodes.MULTIPOLYGON;
            default: return false;
        }
    }

    public static DataType getDataType(int sqlTypeId) {
        switch (sqlTypeId) {
            case Types.BOOLEAN:
            case Types.BIT:
                return BOOLEAN;
            case Types.DOUBLE:
                return DOUBLE;
            case Types.NUMERIC:
                return NUMBER;
            case Types.DECIMAL:
            case Types.REAL:
            case Types.FLOAT:
                return FLOAT;
            case Types.BIGINT:
            case Types.INTEGER:
                return INTEGER;
            case Types.SMALLINT:
                return SHORT;
            case Types.VARCHAR:
            case Types.NCHAR:
            case Types.CHAR:
                return STRING;
            default:
                return OTHER;
        }
    }

    /**
     * Copy of the GeometryTypeCodes interface from the H2GIS project.
     */
    private interface GeometryTypeCodes{
            public static final int GEOMETRY = 0;
            public static final int POINT = 1;
            public static final int LINESTRING = 2;
            public static final int POLYGON = 3;
            public static final int MULTIPOINT = 4;
            public static final int MULTILINESTRING = 5;
            public static final int MULTIPOLYGON = 6;
            public static final int GEOMCOLLECTION = 7;
            public static final int CURVE = 13;
            public static final int SURFACE = 14;
            public static final int POLYHEDRALSURFACE = 15;
            public static final int GEOMETRYZ = 1000;
            public static final int POINTZ = POINT + 1000;
            public static final int LINESTRINGZ = LINESTRING + 1000;
            public static final int POLYGONZ = POLYGON + 1000;
            public static final int MULTIPOINTZ = MULTIPOINT + 1000;
            public static final int MULTILINESTRINGZ = MULTILINESTRING + 1000;
            public static final int MULTIPOLYGONZ = MULTIPOLYGON + 1000;
            public static final int GEOMCOLLECTIONZ = GEOMCOLLECTION + 1000;
            public static final int CURVEZ = CURVE + 1000;
            public static final int SURFACEZ = SURFACE + 1000;
            public static final int POLYHEDRALSURFACEZ = POLYHEDRALSURFACE + 1000;
            public static final int GEOMETRYM = 2000;
            public static final int POINTM = POINT + 2000;
            public static final int LINESTRINGM = LINESTRING + 2000;
            public static final int POLYGONM = POLYGON + 2000;
            public static final int MULTIPOINTM = MULTIPOINT + 2000;
            public static final int MULTILINESTRINGM = MULTILINESTRING + 2000;
            public static final int MULTIPOLYGONM = MULTIPOLYGON + 2000;
            public static final int GEOMCOLLECTIONM = GEOMCOLLECTION + 2000;
            public static final int CURVEM = CURVE + 2000;
            public static final int SURFACEM = SURFACE + 2000;
            public static final int POLYHEDRALSURFACEM = POLYHEDRALSURFACE + 2000;
            public static final int GEOMETRYZM = 3000;
            public static final int POINTZM = POINT + 3000;
            public static final int LINESTRINGZM = LINESTRING + 3000;
    }
}
