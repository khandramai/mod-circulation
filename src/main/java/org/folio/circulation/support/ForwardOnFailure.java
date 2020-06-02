package org.folio.circulation.support;

import io.vertx.core.http.HttpServerResponse;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.folio.circulation.support.http.client.Response;
import org.folio.circulation.support.http.server.ForwardResponse;

public class ForwardOnFailure implements HttpFailure {
  private final Response failureResponse;

  public ForwardOnFailure(Response failureResponse) {
    this.failureResponse = failureResponse;
  }

  public Response getFailureResponse() {
    return failureResponse;
  }

  @Override
  public void writeTo(HttpServerResponse response) {
    ForwardResponse.forward(response, failureResponse);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
      .append("failureResponse", failureResponse)
      .toString();
  }
}
