package org.folio.circulation.resources;

import static org.folio.circulation.resources.RenewalValidator.errorWhenEarlierOrSameDueDate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.joda.time.DateTime.now;
import static org.joda.time.DateTimeZone.UTC;
import static org.junit.Assert.assertTrue;

import org.folio.circulation.domain.Item;
import org.folio.circulation.domain.Loan;
import org.folio.circulation.support.ValidationErrorFailure;
import org.joda.time.DateTime;
import org.junit.Test;

import io.vertx.core.json.JsonObject;
import lombok.val;

public class RenewalValidatorTest {
  @Test
  public void shouldAllowRenewalWhenDueDateIsEarlierOrSameForDeclaredLostItem() {
    val dueDate = now(UTC);
    val proposedDueDate = dueDate.minusWeeks(2);
    val loan = loanWithItemInStatus("Declared lost", dueDate);

    val validationResult = errorWhenEarlierOrSameDueDate(loan, proposedDueDate);

    assertTrue(validationResult.succeeded());
  }

  @Test
  public void shouldDisallowRenewalWhenDueDateIsEarlierOrSame() {
    val dueDate = now(UTC);
    val proposedDueDate = dueDate.minusWeeks(2);
    val loan = loanWithItemInStatus("Checked out", dueDate);

    val validationResult = errorWhenEarlierOrSameDueDate(loan, proposedDueDate);

    assertTrue(validationResult.failed());
    assertThat(validationResult.cause(), instanceOf(ValidationErrorFailure.class));
  }

  private Loan loanWithItemInStatus(String itemStatus, DateTime dueDate) {
    val itemRepresentation = new JsonObject()
      .put("status", new JsonObject().put("name", itemStatus));
    val loanRepresentation = new JsonObject()
      .put("dueDate", dueDate.toString());

    val item = Item.from(itemRepresentation);
    return Loan.from(loanRepresentation).withItem(item);
  }
}
