package org.folio.circulation.domain;

import static org.folio.circulation.support.http.ResponseMapping.forwardOnFailure;
import static org.folio.circulation.support.http.ResponseMapping.mapUsingJson;

import java.util.concurrent.CompletableFuture;

import org.folio.circulation.support.Clients;
import org.folio.circulation.support.CollectionResourceClient;
import org.folio.circulation.support.Result;
import org.folio.circulation.support.http.client.CqlQuery;
import org.folio.circulation.support.http.client.PageLimit;
import org.folio.circulation.support.http.client.Response;
import org.folio.circulation.support.http.client.ResponseInterpreter;

public class FeeFineRepository {
  private final CollectionResourceClient feeFineStorageClient;

  public FeeFineRepository(Clients clients) {
    feeFineStorageClient = clients.feeFineStorageClient();
  }

  public CompletableFuture<Result<FeeFine>> getOverdueFine(String feeFineOwnerId) {
    return getFeeFine(feeFineOwnerId, FeeFine.OVERDUE_FINE_TYPE);
  }

  private CompletableFuture<Result<FeeFine>> getFeeFine(String feeFineOwnerId, String feeFineType) {
    Result<CqlQuery> typeQuery = CqlQuery.exactMatch("feeFineType", feeFineType);
    Result<CqlQuery> ownerQuery = CqlQuery.exactMatch("ownerId", feeFineOwnerId);

    return typeQuery.combine(ownerQuery, CqlQuery::and)
      .after(q -> feeFineStorageClient.getMany(q, PageLimit.limit(1)))
      .thenApply(r -> r.next(this::mapResponseToFeeFines))
      .thenApply(r -> r.map(MultipleRecords::getRecords))
      .thenApply(r -> r.map(col -> col.stream().findFirst().orElse(null)));
  }

  private Result<MultipleRecords<FeeFine>> mapResponseToFeeFines(Response response) {
    return MultipleRecords.from(response, FeeFine::from, "feefines");
  }

  public CompletableFuture<Result<FeeFine>> create(FeeFine feeFine) {
    final ResponseInterpreter<FeeFine> interpreter = new ResponseInterpreter<FeeFine>()
      .flatMapOn(201, mapUsingJson(FeeFine::from))
      .otherwise(forwardOnFailure());

    return feeFineStorageClient.post(feeFine.toJson())
      .thenApply(interpreter::flatMap);
  }
}
