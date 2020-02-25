/*
 * Copyright (c) 2004, 2005 TADA AB - Taby Sweden
 * Distributed under the terms shown in the file COPYRIGHT
 * found in the root folder of this project or at
 * http://eng.tada.se/osprojects/COPYRIGHT.html
 */
package org.postgresql.pljava.test;

import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.io.PrintStream;

/**
 * Some fairly crude tests. All tests are confided to the schema
 * &quot;javatest&quot;
 * 
 * @author Thomas Hallgren
 */
public class Tester
{
	private final Connection m_connection;

	public void testDatabaseMetaData() throws SQLException
	{
		Statement stmt = m_connection.createStatement();
		ResultSet rs = null;

		try
		{

/////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////
			executeMetaDataFunction(stmt,
				"getProcedureColumns((String)null,\"sqlj\",\"install_jar\",(String)null)");
		}
		finally
		{
			if(rs != null)
			{
				try
				{
					rs.close();
				}
				catch(SQLException e)
				{
				}
				rs = null;
			}
		}
	}

	private void executeMetaDataFunction(Statement stmt, String functionCall)
	throws SQLException
	{
		ResultSet rs = null;
		try
		{
			System.out.println("*** " + functionCall + ":");
			rs = stmt
				.executeQuery("SELECT * FROM javatest.callMetaDataMethod('"
					+ functionCall + "')");
			while(rs.next())
			{
				System.out.println(rs.getString(1));
			}
			rs.close();

		}
		catch(Exception e)
		{
			System.out.println("  Failed: " + e.getMessage());
		}
		finally
		{
			if(rs != null)
			{
				try
				{
					rs.close();
				}
				catch(SQLException e)
				{
				}
				rs = null;
			}
		}
	}
}
