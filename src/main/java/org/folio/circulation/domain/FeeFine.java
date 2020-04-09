package org.folio.circulation.domain;

import io.vertx.core.json.JsonObject;

public class FeeFine {
  public static final String OVERDUE_FINE_TYPE = "Overdue fine";
  public static final String LOST_ITEM_FEE = "Lost item fee";
  public static final String LOST_ITEM_PROCESSING_FEE = "Lost item processing fee";

  private final String id;
  private final String ownerId;
  private final String feeFineType;

  public FeeFine(String id, String ownerId, String feeFineType) {
    this.id = id;
    this.ownerId = ownerId;
    this.feeFineType = feeFineType;
  }

  public static FeeFine from(JsonObject jsonObject) {
    return new FeeFine(jsonObject.getString("id"), jsonObject.getString("ownerId"),
      jsonObject.getString("feeFineType"));
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();

    jsonObject.put("id", this.id);
    jsonObject.put("ownerId", this.ownerId);
    jsonObject.put("feeFineType", this.feeFineType);

    return jsonObject;
  }

  public String getId() {
    return id;
  }

  public String getFeeFineType() {
    return feeFineType;
  }
}
