package api.support.fixtures;

import static org.folio.circulation.support.JsonPropertyFetcher.getProperty;

import java.util.UUID;
import java.util.function.UnaryOperator;

import org.folio.circulation.support.http.client.IndividualResource;

import api.support.builders.LostItemFeePolicyBuilder;
import api.support.http.ResourceClient;
import io.vertx.core.json.JsonObject;

public class LostItemFeePoliciesFixture {
  private final RecordCreator lostItemFeePolicyRecordCreator;

  public LostItemFeePoliciesFixture(ResourceClient lostItemFeePoliciesClient) {
    lostItemFeePolicyRecordCreator = new RecordCreator(lostItemFeePoliciesClient,
      reason -> getProperty(reason, "name"));
  }

  public IndividualResource facultyStandard() {
    return facultyStandard(UnaryOperator.identity());
  }

  public IndividualResource facultyStandard(UnaryOperator<LostItemFeePolicyBuilder> builder) {
    JsonObject itemAgedLostOverdue = new JsonObject();
    itemAgedLostOverdue.put("duration", 12);
    itemAgedLostOverdue.put("intervalId", "Months");

    JsonObject patronBilledAfterAgedLost = new JsonObject();
    patronBilledAfterAgedLost.put("duration", 12);
    patronBilledAfterAgedLost.put("intervalId", "Months");

    JsonObject chargeAmountItem = new JsonObject();
    chargeAmountItem.put("chargeType", "Actual cost");
    chargeAmountItem.put("amount", 5.00);

    JsonObject lostItemChargeFeeFine = new JsonObject();
    lostItemChargeFeeFine.put("duration", "6");
    lostItemChargeFeeFine.put("intervalId", "Months");

    final LostItemFeePolicyBuilder undergradStandard = new LostItemFeePolicyBuilder()
      .withName("Undergrad standard")
      .withDescription("This is description for undergrad standard")
      .withItemAgedLostOverdue(itemAgedLostOverdue)
      .withPatronBilledAfterAgedLost(patronBilledAfterAgedLost)
      .withChargeAmountItem(chargeAmountItem)
      .withLostItemProcessingFee(5.00)
      .withChargeAmountItemPatron(true)
      .withChargeAmountItemSystem(true)
      .withLostItemChargeFeeFine(lostItemChargeFeeFine)
      .withReturnedLostItemProcessingFee(true)
      .withReplacedLostItemProcessingFee(true)
      .withReplacementAllowed(true)
      .withLostItemReturned("Charge");

    return lostItemFeePolicyRecordCreator.createIfAbsent(builder.apply(undergradStandard));
  }

  public IndividualResource create(UUID id, String name) {
    return lostItemFeePolicyRecordCreator.createIfAbsent(new LostItemFeePolicyBuilder()
      .withId(id)
      .withName(name));
  }

  public IndividualResource create(LostItemFeePolicyBuilder lostItemFeePolicyBuilder) {
    return lostItemFeePolicyRecordCreator.createIfAbsent(lostItemFeePolicyBuilder);
  }

  public void cleanUp() {
    lostItemFeePolicyRecordCreator.cleanUp();
  }
}
