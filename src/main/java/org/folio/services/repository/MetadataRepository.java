package org.folio.services.repository;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Metadata Repository interface, performs CRUD operations on JobExecution and Log entities
 */
@ProxyGen
public interface MetadataRepository {

  static MetadataRepository create(Vertx vertx) {
    return new MetadataRepositoryImpl(vertx);
  }

  static MetadataRepository createProxy(Vertx vertx, String address) {
    return new MetadataRepositoryVertxEBProxy(vertx, address);
  }

  /**
   * Returns logs of executed jobs
   *
   * @param tenantId            tenant id
   * @param query               query string to filter logs based on matching criteria in fields
   * @param offset              starting index in a list of results
   * @param limit               maximum number of results to return
   * @param landingPage         flag that indicates whether requested collection of entities have to be prepared for rendering on the landing page
   * @param asyncResultHandler  result handler parametrized with JsonObject containing {@link org.folio.rest.jaxrs.model.LogCollection}
   */
  void getLogs(String tenantId, String query, int offset, int limit, boolean landingPage, Handler<AsyncResult<JsonObject>> asyncResultHandler);

  /**
   * Returns jobExecutions
   *
   * @param tenantId            tenant id
   * @param query               query string to filter jobExecutions based on matching criteria in fields
   * @param offset              starting index in a list of results
   * @param limit               maximum number of results to return
   * @param asyncResultHandler  result handler parametrized with JsonObject containing {@link org.folio.rest.jaxrs.model.JobExecutionCollection}
   */
  void getJobExecutions(String tenantId, String query, int offset, int limit, Handler<AsyncResult<JsonObject>> asyncResultHandler);

  void createLog(String tenantId, JsonObject log, Handler<AsyncResult<JsonObject>> asyncResultHandler);
  void updateLog(String tenantId, JsonObject log, Handler<AsyncResult<JsonObject>> asyncResultHandler);
  void getLogById(String tenantId, String logId, Handler<AsyncResult<JsonObject>> asyncResultHandler);
  void deleteLogById(String tenantId, String logId, Handler<AsyncResult<JsonObject>> asyncResultHandler);

  void createJobExecution(String tenantId, JsonObject jobExecution, Handler<AsyncResult<JsonObject>> asyncResultHandler);
  void updateJobExecution(String tenantId, JsonObject jobExecution, Handler<AsyncResult<JsonObject>> asyncResultHandler);
  void getJobExecutionById(String tenantId, String jobExecutionId, Handler<AsyncResult<JsonObject>> asyncResultHandler);
  void deleteJobExecutionById(String tenantId, String jobExecutionId, Handler<AsyncResult<JsonObject>> asyncResultHandler);
}