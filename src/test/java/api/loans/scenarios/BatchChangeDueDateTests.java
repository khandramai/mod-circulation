///*
//package api.loans.scenarios;
//
//import static api.support.http.InterfaceUrls.loansUrl;
//import static api.support.matchers.PatronNoticeMatcher.hasEmailNoticeProperties;
//import static api.support.matchers.TextDateTimeMatcher.isEquivalentTo;
//import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
//import static org.folio.circulation.support.JsonPropertyWriter.write;
//import static org.hamcrest.CoreMatchers.hasItems;
//import static org.hamcrest.core.Is.is;
//import static org.hamcrest.junit.MatcherAssert.assertThat;
//
//import org.folio.circulation.support.http.client.IndividualResource;
//import org.folio.circulation.support.http.client.Response;
//import org.folio.circulation.support.http.client.ResponseHandler;
//
//import api.support.APITests;
//import api.support.builders.BatchChangeDueDateRequestBuilder;
//import api.support.builders.ItemBuilder;
//import api.support.builders.LoanPolicyBuilder;
//import api.support.builders.NoticeConfigurationBuilder;
//import api.support.builders.NoticePolicyBuilder;
//import api.support.builders.RequestBuilder;
//import api.support.fixtures.ItemExamples;
//import api.support.fixtures.TemplateContextMatchers;
//import api.support.http.InventoryItemResource;
//import io.vertx.core.json.JsonArray;
//import io.vertx.core.json.JsonObject;
//import java.net.MalformedURLException;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.TimeoutException;
//import org.apache.commons.lang3.StringUtils;
//import org.awaitility.Awaitility;
//import org.hamcrest.Matcher;
//import org.hamcrest.MatcherAssert;
//import org.hamcrest.Matchers;
//import org.joda.time.DateTime;
//import org.joda.time.Period;
//import org.junit.Assert;
//import org.junit.Test;
//
//public class BatchChangeDueDateTests extends APITests {
//
//  private IndividualResource loan;
//  private InventoryItemResource item;
//
//  @Override
//  public void beforeEach()
//    throws MalformedURLException, InterruptedException, ExecutionException, TimeoutException {
//    super.beforeEach();
//
//    item = itemsFixture.basedUponNod();
//    loan = loansFixture.checkOutByBarcode(item);
//  }
//
//  @Test
//  public void canManuallyChangeTheDueDateOfLoan()
//    throws InterruptedException,
//    MalformedURLException,
//    TimeoutException,
//    ExecutionException {
//
//    Response fetchedLoan = loansClient.getById(loan.getId());
//
//    JsonObject loanToChange = fetchedLoan.getJson().copy();
//
//    DateTime dueDate = DateTime.parse(loanToChange.getString("dueDate"));
//    DateTime newDueDate = dueDate.plus(Period.days(14));
//
//    write(loanToChange, "action", "dueDateChange");
//    write(loanToChange, "dueDate", newDueDate);
//
//    CompletableFuture<Response> putCompleted = new CompletableFuture<>();
//
//    client.put(loansUrl(String.format("/%s", loan.getId())), loanToChange,
//      ResponseHandler.any(putCompleted));
//
//    Response putResponse = putCompleted.get(5, TimeUnit.SECONDS);
//
//    assertThat(String.format("Failed to update loan: %s",
//      putResponse.getBody()), putResponse.getStatusCode(), is(HTTP_NO_CONTENT));
//
//    Response updatedLoanResponse = loansClient.getById(loan.getId());
//
//    verifyLoanAfterChangingDueDate(updatedLoanResponse.getJson(), newDueDate);
//
//    verifyLoanInStorageAfterChangingDueDate(loansStorageClient.getById(loan.getId()).getJson());
//
//    JsonObject fetchedItem = itemsClient.getById(item.getId()).getJson();
//
//    assertThat("item status is not checked out",
//      fetchedItem.getJsonObject("status").getString("name"), is("Checked out"));
//  }
//
//  @Test
//  public void canChangeDueDateOfLoanWithOpenRequest()
//    throws InterruptedException,
//    MalformedURLException,
//    TimeoutException,
//    ExecutionException {
//
//    requestsFixture.place(new RequestBuilder()
//      .hold()
//      .forItem(item)
//      .by(usersFixture.steve())
//      .fulfilToHoldShelf(servicePointsFixture.cd1()));
//
//    Response fetchedLoan = loansClient.getById(loan.getId());
//
//    JsonObject loanToChange = fetchedLoan.getJson().copy();
//
//    DateTime dueDate = DateTime.parse(loanToChange.getString("dueDate"));
//    DateTime newDueDate = dueDate.plus(Period.days(14));
//
//    write(loanToChange, "action", "dueDateChange");
//    write(loanToChange, "dueDate", newDueDate);
//
//    CompletableFuture<Response> putCompleted = new CompletableFuture<>();
//
//    client.put(loansUrl(String.format("/%s", loan.getId())), loanToChange,
//      ResponseHandler.any(putCompleted));
//
//    Response putResponse = putCompleted.get(5, TimeUnit.SECONDS);
//
//    assertThat(String.format("Failed to update loan: %s",
//      putResponse.getBody()), putResponse.getStatusCode(), is(HTTP_NO_CONTENT));
//
//    Response updatedLoanResponse = loansClient.getById(loan.getId());
//
//    verifyLoanAfterChangingDueDate(updatedLoanResponse.getJson(), newDueDate);
//
//    verifyLoanInStorageAfterChangingDueDate(loansStorageClient.getById(loan.getId()).getJson());
//
//    JsonObject fetchedItem = itemsClient.getById(item.getId()).getJson();
//
//    assertThat("item status is not checked out",
//      fetchedItem.getJsonObject("status").getString("name"), is("Checked out"));
//  }
//
//
//  @Test
//  public void manualDueDateChangeNoticeIsSentWhenPolicyDefinesManualDueDateChangeNoticeConfiguration()
//    throws InterruptedException,
//    MalformedURLException,
//    TimeoutException,
//    ExecutionException {
//
//    UUID manualDueDateChangeTemplateId = UUID.randomUUID();
//    JsonObject manualDueDateChangeNoticeConfiguration = new NoticeConfigurationBuilder()
//      .withTemplateId(manualDueDateChangeTemplateId)
//      .withManualDueDateChangeEvent()
//      .create();
//    JsonObject checkInNoticeConfiguration = new NoticeConfigurationBuilder()
//      .withTemplateId(UUID.randomUUID())
//      .withCheckInEvent()
//      .create();
//
//    IndividualResource noticePolicy = noticePoliciesFixture.create(
//      new NoticePolicyBuilder()
//        .withName("Policy with manual due date change notice")
//        .withLoanNotices(Arrays.asList(
//          manualDueDateChangeNoticeConfiguration, checkInNoticeConfiguration)));
//
//    int renewalLimit = 3;
//    IndividualResource loanPolicyWithLimitedRenewals = loanPoliciesFixture.create(
//      new LoanPolicyBuilder()
//        .withName("Limited renewals loan policy")
//        .rolling(org.folio.circulation.domain.policy.Period.months(1))
//        .limitedRenewals(renewalLimit));
//
//    useFallbackPolicies(
//      loanPolicyWithLimitedRenewals.getId(),
//      requestPoliciesFixture.allowAllRequestPolicy().getId(),
//      noticePolicy.getId(),
//      overdueFinePoliciesFixture.facultyStandard().getId(),
//      lostItemFeePoliciesFixture.facultyStandard().getId());
//
//    ItemBuilder itemBuilder = ItemExamples.basedUponSmallAngryPlanet(
//      materialTypesFixture.book().getId(),
//      loanTypesFixture.canCirculate().getId(),
//      StringUtils.EMPTY,
//      "ItemPrefix",
//      "ItemSuffix",
//      Collections.singletonList(""));
//    InventoryItemResource smallAngryPlanet =
//      itemsFixture.basedUponSmallAngryPlanet(itemBuilder, itemsFixture.thirdFloorHoldings());
//
//    IndividualResource steve = usersFixture.steve();
//
//
//    IndividualResource loan = loansFixture.checkOutByBarcode(smallAngryPlanet, steve);
//    JsonObject loanToChange = loan.getJson().copy();
//
//    DateTime dueDate = DateTime.parse(loanToChange.getString("dueDate"));
//    DateTime newDueDate = dueDate.plus(Period.weeks(2));
//
//    write(loanToChange, "dueDate", newDueDate);
//
//    loansClient.replace(loan.getId(), loanToChange);
//
//    IndividualResource loanAfterUpdate = loansClient.get(loan);
//
//    Awaitility.await()
//      .atMost(1, TimeUnit.SECONDS)
//      .until(patronNoticesClient::getAll, Matchers.hasSize(1));
//    List<JsonObject> sentNotices = patronNoticesClient.getAll();
//
//    Map<String, Matcher<String>> noticeContextMatchers = new HashMap<>();
//    noticeContextMatchers.putAll(TemplateContextMatchers.getUserContextMatchers(steve));
//    noticeContextMatchers.putAll(TemplateContextMatchers.getItemContextMatchers(smallAngryPlanet, true));
//    noticeContextMatchers.putAll(TemplateContextMatchers.getLoanContextMatchers(loanAfterUpdate));
//    noticeContextMatchers.putAll(TemplateContextMatchers.getLoanPolicyContextMatchers(renewalLimit, renewalLimit));
//    MatcherAssert.assertThat(sentNotices,
//      hasItems(
//        hasEmailNoticeProperties(steve.getId(), manualDueDateChangeTemplateId, noticeContextMatchers)));
//  }
//
//  @Test
//  public void manualBatchDueDateChangeNoticeIsSentWhenPolicyDefinesManualDueDateChangeNoticeConfiguration()
//    throws InterruptedException,
//    MalformedURLException,
//    TimeoutException,
//    ExecutionException {
//
//    UUID manualDueDateChangeTemplateId = UUID.randomUUID();
//    JsonObject manualDueDateChangeNoticeConfiguration = new NoticeConfigurationBuilder()
//      .withTemplateId(manualDueDateChangeTemplateId)
//      .withManualDueDateChangeEvent()
//      .create();
//    JsonObject checkInNoticeConfiguration = new NoticeConfigurationBuilder()
//      .withTemplateId(UUID.randomUUID())
//      .withCheckInEvent()
//      .create();
//
//    IndividualResource noticePolicy = noticePoliciesFixture.create(
//      new NoticePolicyBuilder()
//        .withName("Policy with manual due date change notice")
//        .withLoanNotices(Arrays.asList(
//          manualDueDateChangeNoticeConfiguration, checkInNoticeConfiguration)));
//
//    int renewalLimit = 3;
//    IndividualResource loanPolicyWithLimitedRenewals = loanPoliciesFixture.create(
//      new LoanPolicyBuilder()
//        .withName("Limited renewals loan policy")
//        .rolling(org.folio.circulation.domain.policy.Period.months(1))
//        .limitedRenewals(renewalLimit));
//
//    useFallbackPolicies(
//      loanPolicyWithLimitedRenewals.getId(),
//      requestPoliciesFixture.allowAllRequestPolicy().getId(),
//      noticePolicy.getId(),
//      overdueFinePoliciesFixture.facultyStandard().getId(),
//      lostItemFeePoliciesFixture.facultyStandard().getId());
//
//    ItemBuilder itemBuilder = ItemExamples.basedUponSmallAngryPlanet(
//      materialTypesFixture.book().getId(),
//      loanTypesFixture.canCirculate().getId(),
//      StringUtils.EMPTY,
//      "ItemPrefix",
//      "ItemSuffix",
//      Collections.singletonList(""));
//    InventoryItemResource smallAngryPlanet =
//      itemsFixture.basedUponSmallAngryPlanet(itemBuilder, itemsFixture.thirdFloorHoldings());
//
//    InventoryItemResource basedUponDunkirk =
//      itemsFixture.basedUponDunkirk();
//
//    IndividualResource steve = usersFixture.steve();
//
//    IndividualResource firstLoan = loansFixture.checkOutByBarcode(smallAngryPlanet, steve);
//    JsonObject firstLoanToChange = firstLoan.getJson().copy();
//    DateTime dueDate = DateTime.parse(firstLoanToChange.getString("dueDate"));
//    DateTime newDueDate = dueDate.plus(Period.weeks(2));
//
//    IndividualResource secondLoan = loansFixture.checkOutByBarcode(basedUponDunkirk, steve);
//
//    String firstLoanId = firstLoan.getId().toString();
//    String secondLoanId = secondLoan.getId().toString();
//
//    Response response = loansFixture
//      .batchChangeDueDate(new BatchChangeDueDateRequestBuilder()
//        .forLoanId(firstLoanId)
//        .forLoanId(secondLoanId)
//        .on(newDueDate)
//      );
//
//    assertThat(response.getStatusCode(), is(204));
//
//    IndividualResource firstLoanAfterUpdate = loansClient.get(firstLoan);
//    IndividualResource secondLoanAfterUpdate = loansClient.get(secondLoan);
//
//    Awaitility.await()
//      .atMost(1, TimeUnit.SECONDS)
//      .until(patronNoticesClient::getAll, Matchers.hasSize(2));
//    List<JsonObject> sentNotices = patronNoticesClient.getAll();
//
//    Map<String, Matcher<String>> noticeContextMatchers = new HashMap<>();
//    noticeContextMatchers.putAll(TemplateContextMatchers.getUserContextMatchers(steve));
//    noticeContextMatchers.putAll(TemplateContextMatchers.getItemContextMatchers(smallAngryPlanet, true));
//    noticeContextMatchers.putAll(TemplateContextMatchers.getLoanContextMatchers(firstLoanAfterUpdate));
//    noticeContextMatchers.putAll(TemplateContextMatchers.getLoanPolicyContextMatchers(renewalLimit, renewalLimit));
//    MatcherAssert.assertThat(sentNotices,
//      hasItems(
//        hasEmailNoticeProperties(steve.getId(), manualDueDateChangeTemplateId, noticeContextMatchers)));
//
//    Map<String, Matcher<String>> secondNoticeContextMatchers = new HashMap<>();
//    noticeContextMatchers.putAll(TemplateContextMatchers.getUserContextMatchers(steve));
//    noticeContextMatchers.putAll(TemplateContextMatchers.getItemContextMatchers(basedUponDunkirk, true));
//    noticeContextMatchers.putAll(TemplateContextMatchers.getLoanContextMatchers(secondLoanAfterUpdate));
//    noticeContextMatchers.putAll(TemplateContextMatchers.getLoanPolicyContextMatchers(renewalLimit, renewalLimit));
//    MatcherAssert.assertThat(sentNotices,
//      hasItems(
//        hasEmailNoticeProperties(steve.getId(), manualDueDateChangeTemplateId, secondNoticeContextMatchers)));
//  }
//
//  @Test
//  public void canManuallyChangeTheDueDateOfLoanByBatch()
//    throws InterruptedException,
//    MalformedURLException,
//    TimeoutException,
//    ExecutionException {
//
//    final InventoryItemResource basedUponDunkirkItem = itemsFixture.basedUponDunkirk();
//
//    IndividualResource secondLoan = loansFixture.checkOutByBarcode(basedUponDunkirkItem);
//
//    Response firstFetchedLoan = loansClient.getById(loan.getId());
//
//    JsonObject firstLoanToChange = firstFetchedLoan.getJson().copy();
//
//    DateTime dueDate = DateTime.parse(firstLoanToChange.getString("dueDate"));
//
//    DateTime newDueDate = dueDate.plus(Period.days(14));
//
//    String firstLoanId = loan.getId().toString();
//    String secondLoanId = secondLoan.getId().toString();
//
//    Response response = loansFixture
//      .batchChangeDueDate(new BatchChangeDueDateRequestBuilder()
//        .forLoanId(firstLoanId)
//        .forLoanId(secondLoanId)
//        .on(newDueDate)
//      );
//
//    assertThat(response.getStatusCode(), is(204));
//
//    Response firstUpdatedLoanResponse = loansClient.getById(loan.getId());
//
//    Response secondUpdatedLoanResponse = loansClient.getById(loan.getId());
//
//    verifyLoanAfterChangingDueDate(firstUpdatedLoanResponse.getJson(), newDueDate);
//
//    verifyLoanAfterChangingDueDate(secondUpdatedLoanResponse.getJson(), newDueDate);
//
//    verifyLoanInStorageAfterChangingDueDate(loansStorageClient
//      .getById(loan.getId()).getJson());
//
//    verifyLoanInStorageAfterChangingDueDate(loansStorageClient
//      .getById(secondLoan.getId()).getJson());
//
//    JsonObject fetchedItem = itemsClient.getById(item.getId()).getJson();
//
//    assertThat("item status is not checked out",
//      fetchedItem.getJsonObject("status").getString("name"), is("Checked out"));
//
//    JsonObject secondFetchedItem = itemsClient.getById(basedUponDunkirkItem.getId()).getJson();
//
//    assertThat("item status is not checked out",
//      secondFetchedItem.getJsonObject("status").getString("name"), is("Checked out"));
//  }
//
//  private void verifyLoanAfterChangingDueDate(JsonObject updatedLoan, DateTime newDueDate) {
//    assertThat("status is not open",
//      updatedLoan.getJsonObject("status").getString("name"), is("Open"));
//
//    assertThat("action is not change due date",
//      updatedLoan.getString("action"), is("dueDateChange"));
//
//    assertThat("should not contain a return date",
//      updatedLoan.containsKey("returnDate"), is(false));
//
//    assertThat("due date does not match",
//      updatedLoan.getString("dueDate"), isEquivalentTo(newDueDate));
//
//    assertThat("renewal count should not have changed",
//      updatedLoan.containsKey("renewalCount"), is(false));
//  }
//
//  private void verifyLoanInStorageAfterChangingDueDate(JsonObject storedLoan) {
//    assertThat("item status snapshot in storage is not checked out",
//      storedLoan.getString("itemStatus"), is("Checked out"));
//
//    assertThat("Should not contain check in service point summary",
//      storedLoan.containsKey("checkinServicePoint"), is(false));
//
//    assertThat("Should not contain check out service point summary",
//      storedLoan.containsKey("checkoutServicePoint"), is(false));
//  }
//
//  @Test
//  public void batchChangeDueDateShouldReturnFailedLoanIds() {
//
//    DateTime newDueDate = DateTime.now().plusDays(14);
//
//    String randomUUID = UUID.randomUUID().toString();
//    Response response = loansFixture
//      .batchChangeDueDate(new BatchChangeDueDateRequestBuilder()
//        .forLoanId(loan.getId().toString())
//        .forLoanId(randomUUID)
//        .on(newDueDate)
//      );
//
//    JsonObject errors = response.getJson().getJsonArray("errors")
//      .getJsonObject(0);
//
//    JsonArray failedLoanIds = new JsonArray(
//      errors.getJsonArray("parameters").getJsonObject(0)
//        .getString("value"));
//
//    Assert.assertTrue(failedLoanIds.contains(randomUUID));
//    Assert.assertFalse(failedLoanIds.contains(loan.getId()));
//
//    assertThat(response.getStatusCode(), is(422));
//  }
//
//  @Test
//  public void canNotBatchChangeDueDateWithEmptyRequestBody() {
//
//    Response response = loansFixture
//      .batchChangeDueDate(new BatchChangeDueDateRequestBuilder()
//        .withEmptyBody()
//      );
//    assertThat(response.getStatusCode(), is(422));
//  }
//
//  @Test
//  public void canNotBatchChangeDueDateWithEmptyLoanIds() {
//
//    Response response = loansFixture
//      .batchChangeDueDate(new BatchChangeDueDateRequestBuilder()
//        .on(DateTime.now())
//      );
//    assertThat(response.getStatusCode(), is(422));
//  }
//
//  @Test
//  public void canNotBatchChangeDueDateWithEmptyDueDate() {
//
//    Response response = loansFixture
//      .batchChangeDueDate(new BatchChangeDueDateRequestBuilder()
//        .forLoanId(loan.getId().toString())
//      );
//    assertThat(response.getStatusCode(), is(422));
//  }
//
//}
//*/