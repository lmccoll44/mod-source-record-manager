package org.folio.dao;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.UpdateResult;
import org.folio.dao.util.PostgresClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static java.lang.String.format;
import static org.folio.rest.persist.PostgresClient.convertToPsqlStandard;

@Repository
@SuppressWarnings("squid:CallToDeprecatedMethod")
public class MappingRuleDaoImpl implements MappingRuleDao {
  private static final Logger LOGGER = LoggerFactory.getLogger(MappingRuleDaoImpl.class);

  private static final String TABLE_NAME = "mapping_rules";
  private static final String RULES_JSON_FIELD = "mappingRules";
  private static final String SELECT_QUERY = "SELECT jsonb FROM %s.%s limit 1";
  private static final String UPDATE_QUERY = "UPDATE %s.%s SET jsonb = jsonb_set(jsonb, '{mappingRules}', '%s')";

  @Autowired
  private PostgresClientFactory pgClientFactory;

  @Override
  public Future<Optional<JsonObject>> get(String tenantId) {
    Future<ResultSet> future = Future.future();
    try {
      String query = format(SELECT_QUERY, convertToPsqlStandard(tenantId), TABLE_NAME);
      pgClientFactory.createInstance(tenantId).select(query, future.completer());
    } catch (Exception e) {
      LOGGER.error("Error getting mapping rules", e);
      future.fail(e);
    }
    return future.map(resultSet -> {
      if (resultSet.getRows().isEmpty()) {
        return Optional.empty();
      } else {
        JsonObject rules = new JsonObject(resultSet.getRows().get(0).getString("jsonb"))
          .getJsonObject(RULES_JSON_FIELD);
        return Optional.of(rules);
      }
    });
  }

  @Override
  public Future<String> save(JsonObject rules, String tenantId) {
    Future<String> future = Future.future();
    pgClientFactory.createInstance(tenantId).save(TABLE_NAME, new JsonObject().put(RULES_JSON_FIELD, rules), future);
    return future;
  }

  @Override
  public Future<JsonObject> update(JsonObject rules, String tenantId) {
    Future<UpdateResult> future = Future.future();
    String query = format(UPDATE_QUERY, convertToPsqlStandard(tenantId), TABLE_NAME, rules);
    pgClientFactory.createInstance(tenantId).execute(query, future.completer());
    return future.map(rules);
  }
}
