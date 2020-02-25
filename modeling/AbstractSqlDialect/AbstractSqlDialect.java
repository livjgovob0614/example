/*
 * Copyright 2002-2015 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.copperengine.core.persistent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.*;
import java.util.Date;

import org.copperengine.core.*;
import org.copperengine.core.batcher.BatchCommand;
import org.copperengine.core.common.WorkflowRepository;
import org.copperengine.core.db.utility.JdbcUtils;
import org.copperengine.core.internal.WorkflowAccessor;
import org.copperengine.core.monitoring.NullRuntimeStatisticsCollector;
import org.copperengine.core.monitoring.RuntimeStatisticsCollector;
import org.copperengine.core.monitoring.StmtStatistic;
import org.copperengine.core.util.FunctionWithException;
import org.copperengine.management.DatabaseDialectMXBean;
import org.copperengine.management.model.AuditTrailInfo;
import org.copperengine.management.model.AuditTrailInstanceFilter;
import org.copperengine.management.model.WorkflowInstanceFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.copperengine.core.util.StateMapper;


/**
 * Abstract base implementation of the {@link DatabaseDialect} for SQL databases
 * 
 * @author austermann
 */
public abstract class AbstractSqlDialect implements DatabaseDialect, DatabaseDialectMXBean {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSqlDialect.class);
    private WorkflowRepository wfRepository;
    /**
     * if multiple engines could be running together, you MUST turn it on
     */
    protected Serializer serializer = new StandardJavaSerializer();
    private WorkflowPersistencePlugin workflowPersistencePlugin = WorkflowPersistencePlugin.NULL_PLUGIN;


    protected PreparedStatement createReadStmt(final Connection c, final String workflowId) throws SQLException {
        PreparedStatement dequeueStmt = c.prepareStatement("select id,priority,data,object_state,creation_ts,PPOOL_ID,state,last_mod_ts from COP_WORKFLOW_INSTANCE where id = ?");
        dequeueStmt.setString(1, workflowId);
        return dequeueStmt;
    }


    @Override
    public Workflow<?> read(String workflowInstanceId, Connection con) throws Exception {
        logger.trace("read({})", workflowInstanceId);

        PreparedStatement readStmt = null;
        PreparedStatement selectResponsesStmt = null;
        try {
            readStmt = createReadStmt(con, workflowInstanceId);

            final ResultSet rs = readStmt.executeQuery();
            if (!rs.next()) {
                rs.close();
                return null;
            }
            PersistentWorkflow<?> wf;
            final String id = rs.getString(1);
            final int prio = rs.getInt(2);

            SerializedWorkflow sw = new SerializedWorkflow();
            sw.setData(rs.getString(3));
            sw.setObjectState(rs.getString(4));
            wf = (PersistentWorkflow<?>) serializer.deserializeWorkflow(sw, wfRepository);
            wf.setId(id);
            wf.setPriority(prio);
            wf.setProcessorPoolId(rs.getString(6));
            WorkflowAccessor.setCreationTS(wf, new Date(rs.getTimestamp(5).getTime()));
            WorkflowAccessor.setLastActivityTS(wf, new Date(rs.getTimestamp(8).getTime()));
            DBProcessingState dbProcessingState = DBProcessingState.getByOrdinal(rs.getInt(7));
            ProcessingState state = DBProcessingState.getProcessingStateByState(dbProcessingState);
            WorkflowAccessor.setProcessingState(wf, state);
            rs.close();
            readStmt.close();

            selectResponsesStmt = con.prepareStatement("select w.WORKFLOW_INSTANCE_ID, w.correlation_id, w.timeout_ts, r.response from (select WORKFLOW_INSTANCE_ID, correlation_id, timeout_ts from COP_WAIT where WORKFLOW_INSTANCE_ID = ?) w LEFT OUTER JOIN COP_RESPONSE r ON w.correlation_id = r.correlation_id");
            selectResponsesStmt.setString(1, workflowInstanceId);
            ResultSet rsResponses = selectResponsesStmt.executeQuery();
            while (rsResponses.next()) {
                String cid = rsResponses.getString(2);
                final Timestamp timeoutTS = rsResponses.getTimestamp(3);
                boolean isTimeout = timeoutTS != null ? timeoutTS.getTime() <= System.currentTimeMillis() : false;
                String response = rsResponses.getString(4);
                Response<?> r = null;
                if (response != null) {
                    r = serializer.deserializeResponse(response);
                    wf.addResponseId(r.getResponseId());
                } else if (isTimeout) {
                    // timeout
                    r = new Response<Object>(cid);
                }
                if (r != null) {
                    wf.putResponse(r);
                }
                wf.addWaitCorrelationId(cid);
            }
            workflowPersistencePlugin.onWorkflowsLoaded(con, Arrays.<PersistentWorkflow<?>>asList(wf));

            return wf;
        } finally {
            JdbcUtils.closeStatement(readStmt);
            JdbcUtils.closeStatement(selectResponsesStmt);
        }
    }
