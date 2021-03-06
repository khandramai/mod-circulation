package api.support.matchers;

import static api.support.matchers.EventTypeMatchers.isItemCheckedInEventType;
import static api.support.matchers.EventTypeMatchers.isItemCheckedOutEventType;
import static api.support.matchers.EventTypeMatchers.isItemClaimedReturnedEventType;
import static api.support.matchers.EventTypeMatchers.isItemDeclaredLostEventType;
import static api.support.matchers.EventTypeMatchers.isLoanDueDateChangedEventType;
import static api.support.matchers.EventTypeMatchers.isLogRecordEventType;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.folio.circulation.support.json.JsonPropertyFetcher.getBooleanProperty;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.Is.is;

import org.hamcrest.Matcher;

import io.vertx.core.json.JsonObject;

public class EventMatchers {

  public static Matcher<JsonObject> isValidItemCheckedOutEvent(JsonObject loan) {
    return allOf(JsonObjectMatcher.allOfPaths(
      hasJsonPath("eventPayload", allOf(
        hasJsonPath("userId", is(loan.getString("userId"))),
        hasJsonPath("loanId", is(loan.getString("id"))),
        hasJsonPath("dueDate", is(loan.getString("dueDate")))
      ))),
      isItemCheckedOutEventType());
  }

  public static Matcher<JsonObject> isValidCheckOutLogEvent(JsonObject checkedOutLoan) {
    return allOf(JsonObjectMatcher.allOfPaths(
      hasJsonPath("eventPayload", allOf(
        hasJsonPath("logEventType", is("CHECK_OUT_EVENT")),
        hasJsonPath("servicePointId", is(checkedOutLoan.getString("checkoutServicePointId"))),
        hasJsonPath("loanId", is(checkedOutLoan.getString("id"))),
        hasJsonPath("isLoanClosed", is(checkedOutLoan.getJsonObject("status").getString("name").equals("Closed"))),
        hasJsonPath("dueDate", is(checkedOutLoan.getString("dueDate"))),
        hasJsonPath("userId", is(checkedOutLoan.getString("userId"))),
        hasJsonPath("itemId", is(checkedOutLoan.getString("itemId"))),
        hasJsonPath("itemBarcode", is(checkedOutLoan.getJsonObject("item").getString("barcode"))),
        hasJsonPath("itemStatusName", is(checkedOutLoan.getJsonObject("item").getJsonObject("status").getString("name")))
      ))),
      isLogRecordEventType());
  }

  public static Matcher<JsonObject> isValidItemCheckedInEvent(JsonObject loan) {
    return allOf(JsonObjectMatcher.allOfPaths(
      hasJsonPath("eventPayload", allOf(
        hasJsonPath("userId", is(loan.getString("userId"))),
        hasJsonPath("loanId", is(loan.getString("id"))),
        hasJsonPath("returnDate", is(loan.getString("returnDate")))
      ))),
      isItemCheckedInEventType());
  }

  public static Matcher<JsonObject> isValidCheckInLogEvent(JsonObject checkedInLoan) {
    return allOf(JsonObjectMatcher.allOfPaths(
      hasJsonPath("eventPayload", allOf(
        hasJsonPath("logEventType", is("CHECK_IN_EVENT")),
        hasJsonPath("servicePointId", is(checkedInLoan.getString("checkinServicePointId"))),
        hasJsonPath("returnDate", is(checkedInLoan.getString("returnDate"))),
        hasJsonPath("loanId", is(checkedInLoan.getString("id"))),
        hasJsonPath("isLoanClosed", is(checkedInLoan.getJsonObject("status").getString("name").equals("Closed"))),
        hasJsonPath("systemReturnDate", is(checkedInLoan.getString("systemReturnDate"))),
        hasJsonPath("returnDate", is(checkedInLoan.getString("returnDate"))),
        hasJsonPath("dueDate", is(checkedInLoan.getString("dueDate"))),
        hasJsonPath("userId", is(checkedInLoan.getString("userId"))),
        hasJsonPath("itemId", is(checkedInLoan.getString("itemId"))),
        hasJsonPath("itemBarcode", is(checkedInLoan.getJsonObject("item").getString("barcode"))),
        hasJsonPath("itemStatusName", is(checkedInLoan.getJsonObject("item").getJsonObject("status").getString("name")))
      ))),
      isLogRecordEventType());
  }

  public static Matcher<JsonObject> isValidItemClaimedReturnedEvent(JsonObject loan) {
    return allOf(JsonObjectMatcher.allOfPaths(
      hasJsonPath("eventPayload", allOf(
        hasJsonPath("userId", is(loan.getString("userId"))),
        hasJsonPath("loanId", is(loan.getString("id")))
      ))),
      isItemClaimedReturnedEventType());
  }

  public static Matcher<JsonObject> isValidItemDeclaredLostEvent(JsonObject loan) {
    return allOf(JsonObjectMatcher.allOfPaths(
      hasJsonPath("eventPayload", allOf(
        hasJsonPath("userId", is(loan.getString("userId"))),
        hasJsonPath("loanId", is(loan.getString("id")))
      ))),
      isItemDeclaredLostEventType());
  }

  public static Matcher<JsonObject> isValidLoanDueDateChangedEvent(JsonObject loan) {
    return allOf(JsonObjectMatcher.allOfPaths(
      hasJsonPath("eventPayload", allOf(
        hasJsonPath("userId", is(loan.getString("userId"))),
        hasJsonPath("loanId", is(loan.getString("id"))),
        hasJsonPath("dueDate", is(loan.getString("dueDate"))),
        hasJsonPath("dueDateChangedByRecall",
          is(getBooleanProperty(loan, "dueDateChangedByRecall")))
      ))),
      isLoanDueDateChangedEventType());
  }

}
