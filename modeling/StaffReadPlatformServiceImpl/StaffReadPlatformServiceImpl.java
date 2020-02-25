/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.organisation.staff.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.organisation.staff.exception.StaffNotFoundException;
import org.apache.fineract.portfolio.client.domain.ClientStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountStatusType;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class StaffReadPlatformServiceImpl implements StaffReadPlatformService {

	public Object[] hasAssociatedItems(final Long staffId){
        ArrayList<String> params = new ArrayList<String>();        
                
        String sql =  "select c.display_name as client, g.display_name as grp,l.loan_officer_id as loan, s.field_officer_id as sav"+
        			  " from m_staff staff "+
        			  " left outer join m_client c on staff.id = c.staff_id  AND c.status_enum < "+ ClientStatus.CLOSED.getValue() +
        			  " left outer join m_group g on staff.id = g.staff_id " +
                      " left outer join m_loan l on staff.id = l.loan_officer_id and l.loan_status_id < " +  LoanStatus.WITHDRAWN_BY_CLIENT.getValue() +
                      " left outer join m_savings_account s on c.staff_id = s.field_officer_id and s.status_enum < "+ SavingsAccountStatusType.WITHDRAWN_BY_APPLICANT.getValue() +
                      " where  staff.id  =  " + staffId +
                      " group by staff.id";
        
       
        List<Map<String, Object>> result =  this.jdbcTemplate.queryForList(sql);
        if (result != null) {
       		for (Map<String, Object> map : result) {
       			if (map.get("client") != null) {
       				params.add("client");
       			}
       			if (map.get("grp") != null) {
       				params.add("group");
       			}
       			if (map.get("loan")  != null) {
       				params.add("loan");
       			}
       			if (map.get("sav")  != null) {
       				params.add("savings account");
       			}
       		}		
        }
        return params.toArray();
        
    }
}
