/******************************************************************************
 * Copyright (C) 2009 Low Heng Sin                                            *
 * Copyright (C) 2009 Idalica Corporation                                     *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 *****************************************************************************/
package org.compiere.apps.form;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Vector;

import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;
import org.compiere.util.TimeUtil;
import org.compiere.util.Util;

/**
 * 
 * @author Michael McKay, 
 * 				<li>ADEMPIERE-72 VLookup and Info Window improvements
 * 					https://adempiere.atlassian.net/browse/ADEMPIERE-72
 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
 *		<a href="https://github.com/adempiere/adempiere/issues/407">
 * 		@see FR [ 407 ] Enhance visualization of allocation payment window</a>
 */
public class Allocation
{
	/**	Receivables & Payables	*/
	public static final String 	APAR_A = "A";
	/**	Payables only			*/
	public static final String 	APAR_P = "P";
	/**	Receivables only		*/
	public static final String 	APAR_R = "R";

	/**
	 *  Load Business Partner Info
	 *  - Payments
	 *  - Invoices
	 */
	public Vector<Vector<Object>> getPaymentData(boolean isMultiCurrency, Object date, int orgId, String apar, int currencyId, int bPartnerId)
	{		
		/********************************
		 *  Load unallocated Payments
		 *      1-TrxDate, 2-DocumentNo, (3-Currency, 4-PayAmt,)
		 *      5-ConvAmt, 6-ConvOpen, 7-Allocated
		 */
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		StringBuffer sql = new StringBuffer("SELECT p.DateTrx,p.DocumentNo,p.C_Payment_ID,"  //  1..3
				+ "c.ISO_Code,p.PayAmt,"                            //  4..5
				+ "currencyConvert(p.PayAmt,p.C_Currency_ID,?,?,p.C_ConversionType_ID,p.AD_Client_ID,p.AD_Org_ID) AS ConvertedAmt,"//  6   #1, #2
				+ "currencyConvert(paymentAvailable(C_Payment_ID),p.C_Currency_ID,?,?,p.C_ConversionType_ID,p.AD_Client_ID,p.AD_Org_ID) AS AvailableAmt,"  //  7   #3, #4
				+ "p.MultiplierAP, p.IsReceipt, p.AD_Org_ID, p.Description " // 8..11
				+ "FROM C_Payment_v p"		//	Corrected for AP/AR
				+ " INNER JOIN C_Currency c ON (p.C_Currency_ID=c.C_Currency_ID) "
				+ "WHERE p.IsAllocated='N' AND p.Processed='Y'"
				+ " AND p.C_Charge_ID IS NULL"		//	Prepayments OK
				+ " AND p.C_BPartner_ID=?");                   		//      #5
		if (!isMultiCurrency) {
			sql.append(" AND p.C_Currency_ID=?");				//      #6
		}
		if (orgId != 0 ) {
			sql.append(" AND p.AD_Org_ID=" + orgId);
		}
		if (apar != null
				&& !apar.equals(APAR_A)) {
			sql.append(" AND p.IsReceipt= '" + (apar.equals(APAR_R)? "Y": "N" ) +"'" );
		}
		sql.append(" ORDER BY p.DateTrx,p.DocumentNo");
		
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql.toString(), null);
			pstmt.setInt(1, currencyId);
			pstmt.setTimestamp(2, (Timestamp)date);
			pstmt.setInt(3, currencyId);
			pstmt.setTimestamp(4, (Timestamp)date);
			pstmt.setInt(5, bPartnerId);
			if (!isMultiCurrency)
				pstmt.setInt(6, currencyId);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				Vector<Object> line = new Vector<Object>();
				line.add(getInt("C_Payment_ID")); 	//  0-C_Payment_ID
				line.add(rs.getTimestamp("DateTrx"));       	//  1-TrxDate
				if(rs.getString("IsReceipt").equals("Y"))			//  Ar/Ap
					line.add("AR");
				else
					line.add("AP");
				int orgID = rs.getInt("AD_Org_ID"); 				// 10 AD_Org_ID
				if (orgID == 0) {
					line.add("*");
				} else {
					line.add("**");
				}
				Tuple pp = new Tuple(rs.getInt("C_Payment_ID"), rs.getString("DocumentNo"));
				line.add(pp);                       	//  4-DocumentNo
				line.add(rs.getString("Description"));  //  5-Description
				//	
				if (isMultiCurrency)
				{
					line.add(rs.getString("ISO_Code"));      	//  6-Currency
					line.add(rs.getBigDecimal("PayAmt"));  	//  7-PayAmt
				}
				line.add(rs.getBigDecimal("ConvertedAmt"));      	//  6/8-ConvertedAmt
				BigDecimal available = rs.getBigDecimal("AvailableAmt");
				if (available == null || available.signum() == 0)	//	nothing available
					continue;
				line.add(available);					//  7/9-ConvOpen/Available
				line.add(Env.ZERO);						//  8/10-PaymentAmt
				//
				data.add(line);
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{

		}
		
		return data;
	}
