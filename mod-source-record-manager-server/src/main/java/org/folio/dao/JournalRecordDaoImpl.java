package org.folio.dao;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.UpdateResult;
import org.folio.dao.util.PostgresClientFactory;
import org.folio.rest.jaxrs.model.ActionLog;
import org.folio.rest.jaxrs.model.JobExecutionLogDto;
import org.folio.rest.jaxrs.model.JournalRecord;
import org.folio.rest.jaxrs.model.JournalRecord.ActionStatus;
import org.folio.rest.jaxrs.model.JournalRecord.ActionType;
import org.folio.rest.jaxrs.model.JournalRecord.EntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.ws.rs.BadRequestException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.folio.rest.persist.PostgresClient.convertToPsqlStandard;

@Repository
@SuppressWarnings("squid:CallToDeprecatedMethod")
public class JournalRecordDaoImpl implements JournalRecordDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(JournalRecordDaoImpl.class);
  private final Set<String> sortableFields = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("source_record_order", "action_type", "error")));

  private static final String JOURNAL_RECORDS_TABLE = "journal_records";
  private static final String INSERT_SQL = "INSERT INTO %s.%s (id, job_execution_id, source_id, source_record_order, entity_type, entity_id, entity_hrid, action_type, action_status, error, action_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
  private static final String SELECT_BY_JOB_EXECUTION_ID_QUERY = "SELECT * FROM %s.%s WHERE job_execution_id = ?";
  private static final String ORDER_BY_PATTERN = " ORDER BY %s %s";
  private static final String DELETE_BY_JOB_EXECUTION_ID_QUERY = "DELETE FROM %s.%s WHERE job_execution_id = ?";
  private static final String GET_JOB_LOG_BY_JOB_EXECUTION_ID_QUERY = "SELECT job_execution_id, entity_type, action_type, " +
                                                                        "COUNT(*) FILTER (WHERE action_status = 'COMPLETED') AS total_completed, " +
                                                                        "COUNT(*) FILTER (WHERE action_status = 'ERROR') AS total_failed " +
                                                                      "FROM %s.%s WHERE job_execution_id = ? AND action_type != 'ERROR' " +
                                                                      "GROUP BY (job_execution_id, entity_type, action_type)";

  @Autowired
  private PostgresClientFactory pgClientFactory;

  @Override
  public Future<String> save(JournalRecord journalRecord, String tenantId) {
    Future<UpdateResult> future = Future.future();
    try {
      journalRecord.withId(UUID.randomUUID().toString());
      String query = format(INSERT_SQL, convertToPsqlStandard(tenantId), JOURNAL_RECORDS_TABLE);
      JsonArray queryParams = new JsonArray();
      prepareInsertQueryParameters(journalRecord, queryParams);
      pgClientFactory.createInstance(tenantId).execute(query, queryParams, future.completer());
    } catch (Exception e) {
      LOGGER.error("Error saving JournalRecord entity", e);
      future.fail(e);
    }
    return future.map(journalRecord.getId());
  }

  private void prepareInsertQueryParameters(JournalRecord journalRecord, JsonArray queryParams) {
    queryParams.add(journalRecord.getId())
      .add(journalRecord.getJobExecutionId())
      .add(journalRecord.getSourceId())
      .add(journalRecord.getSourceRecordOrder())
      .add(journalRecord.getEntityType())
      .add(journalRecord.getEntityId())
      .add(journalRecord.getEntityHrId() != null ? journalRecord.getEntityHrId() : EMPTY)
      .add(journalRecord.getActionType())
      .add(journalRecord.getActionStatus())
      .add(journalRecord.getError() != null ? journalRecord.getError() : EMPTY)
      .add(Timestamp.from(journalRecord.getActionDate().toInstant()).toString());
  }

  @Override
  public Future<List<JournalRecord>> getByJobExecutionId(String jobExecutionId, String sortBy, String order, String tenantId) {
    Future<ResultSet> future = Future.future();
    try {
      StringBuilder queryBuilder = new StringBuilder(format(SELECT_BY_JOB_EXECUTION_ID_QUERY, convertToPsqlStandard(tenantId), JOURNAL_RECORDS_TABLE));
      if (sortBy != null) {
        queryBuilder.append(prepareSortingClause(sortBy, order));
      }
      JsonArray queryParams = new JsonArray()
        .add(jobExecutionId != null ? jobExecutionId : EMPTY);
      pgClientFactory.createInstance(tenantId).select(queryBuilder.toString(), queryParams, future.completer());
    } catch (Exception e) {
      LOGGER.error("Error getting JournalRecords by jobExecution id", e);
      future.fail(e);
    }
    return future.map(this::mapResultSetToJournalRecordsList);
  }

  @Override
  public Future<Boolean> deleteByJobExecutionId(String jobExecutionId, String tenantId) {
    Future<UpdateResult> future = Future.future();
    String query = format(DELETE_BY_JOB_EXECUTION_ID_QUERY, convertToPsqlStandard(tenantId), JOURNAL_RECORDS_TABLE);
    JsonArray queryParams = new JsonArray()
      .add(jobExecutionId != null ? jobExecutionId : EMPTY);
    pgClientFactory.createInstance(tenantId).execute(query, queryParams, future.completer());
    return future.map(updateResult -> updateResult.getUpdated() >= 1);
  }

  @Override
  public Future<JobExecutionLogDto> getJobExecutionLogDto(String jobExecutionId, String tenantId) {
    Future<ResultSet> future = Future.future();
    String query = format(GET_JOB_LOG_BY_JOB_EXECUTION_ID_QUERY, convertToPsqlStandard(tenantId), JOURNAL_RECORDS_TABLE);
    JsonArray queryParams = new JsonArray()
      .add(jobExecutionId != null ? jobExecutionId : EMPTY);
    pgClientFactory.createInstance(tenantId).select(query, queryParams, future.completer());
    return future.map(this::mapResultSetToJobExecutionLogDto);
  }

  private List<JournalRecord> mapResultSetToJournalRecordsList(ResultSet resultSet) {
    return resultSet.getRows().stream()
      .map(this::mapRowJsonToJournalRecord)
      .collect(Collectors.toList());
  }

  private JournalRecord mapRowJsonToJournalRecord(JsonObject rowAsJson) {
    return new JournalRecord()
      .withId(rowAsJson.getString("id"))
      .withJobExecutionId(rowAsJson.getString("job_execution_id"))
      .withSourceId(rowAsJson.getString("source_id"))
      .withSourceRecordOrder(rowAsJson.getInteger("source_record_order"))
      .withEntityType(EntityType.valueOf(rowAsJson.getString("entity_type")))
      .withEntityId(rowAsJson.getString("entity_id"))
      .withEntityHrId(rowAsJson.getString("entity_hrid"))
      .withActionType(ActionType.valueOf(rowAsJson.getString("action_type")))
      .withActionStatus(ActionStatus.valueOf(rowAsJson.getString("action_status")))
      .withError(rowAsJson.getString("error"))
      .withActionDate(Date.from(LocalDateTime.parse(rowAsJson.getString("action_date")).toInstant(ZoneOffset.UTC)));
  }

  private JobExecutionLogDto mapResultSetToJobExecutionLogDto(ResultSet resultSet) {
    JobExecutionLogDto jobExecutionSummary = new JobExecutionLogDto();
    resultSet.getRows().forEach(rowAsJson -> {
      ActionLog actionLog = new ActionLog()
        .withEntityType(rowAsJson.getString("entity_type"))
        .withActionType(rowAsJson.getString("action_type"))
        .withTotalCompleted(rowAsJson.getInteger("total_completed"))
        .withTotalFailed(rowAsJson.getInteger("total_failed"));

      jobExecutionSummary.withJobExecutionId(rowAsJson.getString("job_execution_id"));
      jobExecutionSummary.getJobExecutionResultLogs().add(actionLog);
    });
    return jobExecutionSummary;
  }

  private String prepareSortingClause(String sortBy, String order) {
    if (!sortableFields.contains(sortBy)) {
      throw new BadRequestException(format("The specified field for sorting journal records is invalid: '%s'", sortBy));
    }
    return format(ORDER_BY_PATTERN, sortBy, order);
  }

}
