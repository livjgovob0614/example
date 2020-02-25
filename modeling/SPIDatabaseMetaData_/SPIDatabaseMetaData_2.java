/*
 * Copyright (c) 2005-2019 Tada AB and other contributors, as listed below.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the The BSD 3-Clause License
 * which accompanies this distribution, and is available at
 * http://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 *   Filip Hrbek
 *   PostgreSQL Global Development Group
 *   Chapman Flack
 */

package org.postgresql.pljava.jdbc;

/**
 * @author Filip Hrbek
 */
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.ArrayList;
import static java.util.Arrays.sort;
import java.util.HashMap;

public class SPIDatabaseMetaData implements DatabaseMetaData
{
	public SPIDatabaseMetaData(SPIConnection conn)
	{
		m_connection = conn;
	}

	private final SPIConnection m_connection; // The connection association

	private static String escapeQuotes(String s)
	{
        if (s == null)
        {
            return null;
        }

		StringBuffer sb = new StringBuffer();
		int length = s.length();
		char prevChar = ' ';
		char prevPrevChar = ' ';
		for(int i = 0; i < length; i++)
		{
			char c = s.charAt(i);
			sb.append(c);
			if(c == '\''
				&& (prevChar != '\\' || (prevChar == '\\' && prevPrevChar == '\\')))
			{
				sb.append("'");
			}
			prevPrevChar = prevChar;
			prevChar = c;
		}
		return sb.toString();
	}

	/**
	 * Creates a condition with the specified operator
     * based on schema specification:<BR>
     * <UL>
     * <LI>schema is specified => search in this schema only</LI>
     * <LI>schema is equal to "" => search in the 'public' schema</LI>
     * <LI>schema is null =>  search in all schemas</LI>
	 * </UL>
	 */
	private static String resolveSchemaConditionWithOperator(
        String expr, String schema, String operator)
	{
        //schema is null => search in current_schemas(true)
        if (schema == null)
        {
			//This means that only "visible" schemas are searched.
			//It was approved to change to *all* schemas.
            //return expr + " " + operator + " ANY (current_schemas(true))";
			return "1 OPERATOR(pg_catalog.=) 1";
        }
        //schema is specified => search in this schema
	else if(!"".equals(schema))
	{
		return expr + " " + operator + " '" + escapeQuotes(schema) + "' ";
	}
        //schema is "" => search in the 'public' schema
        else
        {
            return expr + " " + operator + " 'public' ";
        }
    }

	/**
	 * Creates a pattern condition based on schema specification:<BR>
     * <UL>
     * <LI>schema is specified => search in this schema only</LI>
     * <LI>schema is equal to "" => search in the 'public' schema</LI>
     * <LI>schema is null =>  search in all schemas</LI>
	 * </UL>
	 */
	private static String resolveSchemaPatternCondition(
        String expr, String schema)
	{
        return resolveSchemaConditionWithOperator(expr, schema, "LIKE");
    }

	/*
	 * Get a description of a catalog's stored procedure parameters and result
	 * columns. <p>Only descriptions matching the schema, procedure and
	 * parameter name criteria are returned. They are ordered by PROCEDURE_SCHEM
	 * and PROCEDURE_NAME. Within this, the return value, if any, is first. Next
	 * are the parameter descriptions in call order. The column descriptions
	 * follow in column number order. <p>Each row in the ResultSet is a
	 * parameter description or column description with the following fields:
	 * <ol> <li><b>PROCEDURE_CAT</b> String => procedure catalog (may be null)
	 * <li><b>PROCEDURE_SCHE</b>M String => procedure schema (may be null)
	 * <li><b>PROCEDURE_NAME</b> String => procedure name <li><b>COLUMN_NAME</b>
	 * String => column/parameter name <li><b>COLUMN_TYPE</b> Short => kind of
	 * column/parameter: <ul><li>procedureColumnUnknown - nobody knows <li>procedureColumnIn -
	 * IN parameter <li>procedureColumnInOut - INOUT parameter <li>procedureColumnOut -
	 * OUT parameter <li>procedureColumnReturn - procedure return value <li>procedureColumnResult -
	 * result column in ResultSet </ul> <li><b>DATA_TYPE</b> short => SQL type
	 * from java.sql.Types <li><b>TYPE_NAME</b> String => Data source specific
	 * type name <li><b>PRECISION</b> int => precision <li><b>LENGTH</b> int =>
	 * length in bytes of data <li><b>SCALE</b> short => scale <li><b>RADIX</b>
	 * short => radix <li><b>NULLABLE</b> short => can it contain NULL? <ul><li>procedureNoNulls -
	 * does not allow NULL values <li>procedureNullable - allows NULL values
	 * <li>procedureNullableUnknown - nullability unknown <li><b>REMARKS</b>
	 * String => comment describing parameter/column </ol> @param catalog This
	 * is ignored in org.postgresql, advise this is set to null @param
	 * schemaPattern @param procedureNamePattern a procedure name pattern @param
	 * columnNamePattern a column name pattern, this is currently ignored
	 * because postgresql does not name procedure parameters. @return each row
	 * is a stored procedure parameter or column description @exception
	 * SQLException if a database-access error occurs
	 * 
	 * @see #getSearchStringEscape
	 */
	// Implementation note: This is required for Borland's JBuilder to work
	public ArrayList getProcedureColumns(String catalog,
		String schemaPattern, String procedureNamePattern,
		String columnNamePattern) throws SQLException
	{
		ArrayList v = new ArrayList(); // The new ResultSet tuple stuff

		String sql =
			"SELECT"
			+ "  n.nspname, p.proname, p.prorettype, p.proargtypes,"
			+ "  t.typtype::pg_catalog.varchar, t.typrelid "
			+ " FROM"
			+ "  pg_catalog.pg_proc p, pg_catalog.pg_namespace n,"
			+ "  pg_catalog.pg_type t"
			+ " WHERE p.pronamespace OPERATOR(pg_catalog.=) n.oid"
			+ " AND p.prorettype OPERATOR(pg_catalog.=) t.oid "
            + " AND " + resolveSchemaPatternCondition(
                            "n.nspname", schemaPattern);
		if(procedureNamePattern != null)
		{
			sql += " AND p.proname LIKE '"
				+ escapeQuotes(procedureNamePattern) + "' ";
		}
		sql += " ORDER BY n.nspname, p.proname ";

		ResultSet rs = m_connection.createStatement().executeQuery(sql);
		String schema = null;
		String procedureName = null;
		String returnType = null;
		String returnTypeType = null;
		String returnTypeRelid = null;

		String[] argTypes = null;
		while(rs.next())
		{
			schema = rs.getString("nspname");
			procedureName = rs.getString("proname");
			returnType = (String)rs.getObject("prorettype");
			returnTypeType = rs.getString("typtype");
			returnTypeRelid = (String)rs.getObject("typrelid");
			argTypes = (String[])rs.getObject("proargtypes");

			// decide if we are returning a single column result.
			if(!returnTypeType.equals("c"))
			{
				Object[] tuple = new Object[4];
				tuple[0] = null;
				tuple[1] = schema;
				tuple[2] = procedureName;
				tuple[3] = "returnValue";
				v.add(tuple);
			}

			// Add a row for each argument.
			for(int i = 0; i < argTypes.length; i++)
			{
				String argString = argTypes[i];
				Object[] tuple = new Object[4];
				tuple[0] = null;
				tuple[1] = schema;
				tuple[2] = procedureName;
				tuple[3] = "$" + (i + 1);
				v.add(tuple);
			}

			// if we are returning a multi-column result.
			if(returnTypeType.equals("c"))
			{
				String columnsql = "SELECT a.attname,a.atttypid FROM pg_catalog.pg_attribute a WHERE a.attrelid = ? ORDER BY a.attnum ";
				PreparedStatement stmt = m_connection.prepareStatement(columnsql);
				stmt.setObject(1, returnTypeRelid);
				ResultSet columnrs = stmt.executeQuery(columnsql);

				while(columnrs.next())
				{
					String columnTypeString = (String)columnrs.getObject("atttypid");
					Object[] tuple = new Object[4];
					tuple[0] = null;
					tuple[1] = schema;
					tuple[2] = procedureName;
					tuple[3] = columnrs.getString("attname");
					v.add(tuple);
				}
				columnrs.close();
				stmt.close();
			}
		}
		rs.close();

		return v;
	}
