/*
 * Copyright Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags and
 * the COPYRIGHT.txt file distributed with this work.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.teiid.deployers;

import static org.teiid.odbc.PGUtil.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.teiid.CommandContext;

public class PgCatalogMetadataStore extends MetadataFactory {

        public static String formatType(org.teiid.CommandContext cc, Integer oid, Integer typmod) throws SQLException {
            if (oid == null) {
                return null;
            }
            Connection c = cc.getConnection();
            try {
                PreparedStatement ps = c.prepareStatement("select typname from pg_catalog.pg_type where oid = ?"); //$NON-NLS-1$
                ps.setInt(1, oid);
                ps.execute();
                ResultSet rs = ps.getResultSet();
                if (rs.next()) {
                    String name = rs.getString(1);
                    boolean isArray = name.startsWith("_"); //$NON-NLS-1$
                    if (isArray) {
                        name = name.substring(1);
                    }
                    switch (name) {
                    case "bool": //$NON-NLS-1$
                        name = "boolean"; //$NON-NLS-1$
                        break;
                    case "varchar": //$NON-NLS-1$
                        name = "character varying"; //$NON-NLS-1$
                        break;
                    case "int2": //$NON-NLS-1$
                        name = "smallint"; //$NON-NLS-1$
                        break;
                    case "int4": //$NON-NLS-1$
                        name = "integer"; //$NON-NLS-1$
                        break;
                    case "int8": //$NON-NLS-1$
                        name = "bigint"; //$NON-NLS-1$
                        break;
                    case "float4": //$NON-NLS-1$
                        name = "real"; //$NON-NLS-1$
                        break;
                    case "float8": //$NON-NLS-1$
                        name = "double precision"; //$NON-NLS-1$
                        break;
                    case "bpchar": //$NON-NLS-1$
                        name = "character"; //$NON-NLS-1$
                        break;
                    }
                    if (typmod != null && typmod > 4) {
                        if (name.equals("numeric")) {  //$NON-NLS-1$
                            name += "("+((typmod-4)>>16)+","+((typmod-4)&0xffff)+")";  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        } else if (name.equals("character") || name.equals("varchar")) { //$NON-NLS-1$ //$NON-NLS-2$
                            name += "("+(typmod-4)+")";  //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    }
                    if (isArray) {
                        name += "[]"; //$NON-NLS-1$
                    }
                    return name;
                }
                return "???"; //$NON-NLS-1$
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }
}
