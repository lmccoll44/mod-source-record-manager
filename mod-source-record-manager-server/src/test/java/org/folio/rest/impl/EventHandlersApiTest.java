package org.folio.rest.impl;

import java.util.UUID;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.restassured.RestAssured;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class EventHandlersApiTest extends AbstractRestTest {

  private static final String HANDLERS_CREATED_INSTANCE_PATH = "/change-manager/handlers/created-inventory-instance";
  private static final String HANDLERS_DATA_IMPORT_PROCESSING_RESULT = "/change-manager/handlers/processing-result";

  private JsonObject eventCreatedInstance = new JsonObject()
    .put("id", UUID.randomUUID().toString())
    .put("eventType", "CREATED_INVENTORY_INSTANCE")
    .put("eventMetadata", new JsonObject()
      .put("tenantId", TENANT_ID)
      .put("eventTTL", 1)
      .put("publishedBy", "mod-inventory"));

  private JsonObject eventDataImportError = new JsonObject()
    .put("id", UUID.randomUUID().toString())
    .put("eventType", "DATA_IMPORT_ERROR")
    .put("eventMetadata", new JsonObject()
      .put("tenantId", TENANT_ID)
      .put("eventTTL", 1)
      .put("publishedBy", "mod-inventory"));

  @Test
  public void shouldReturnOkOnPostInstanceCreatedEvent() {
    RestAssured.given()
      .spec(spec)
      .when()
      .body(eventCreatedInstance.encode())
      .post(HANDLERS_CREATED_INSTANCE_PATH)
      .then()
      .statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @Test
  public void shouldReturnOkOnPostDataImportErrorEvent() {
    RestAssured.given()
      .spec(spec)
      .when()
      .body(eventDataImportError.encode())
      .post(HANDLERS_DATA_IMPORT_PROCESSING_RESULT)
      .then()
      .statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @Test
  public void shouldReturnOkIfEventPayloadIsInvalid() {
     JsonObject invalidEvent = new JsonObject()
      .put("id", UUID.randomUUID().toString())
      .put("eventType", "CREATED_INVENTORY_INSTANCE")
      .put("contexttt", "test")
      .put("eventMetadata", new JsonObject()
        .put("tenantId", TENANT_ID)
        .put("eventTTL", 1)
        .put("publishedBy", "mod-inventory"));

    RestAssured.given()
      .spec(spec)
      .when()
      .body(invalidEvent.encode())
      .post(HANDLERS_CREATED_INSTANCE_PATH)
      .then()
      .statusCode(HttpStatus.SC_NO_CONTENT);
  }
}
