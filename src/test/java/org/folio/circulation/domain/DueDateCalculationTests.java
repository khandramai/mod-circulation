package org.folio.circulation.domain;

import api.support.builders.LoanBuilder;
import api.support.builders.LoanPolicyBuilder;
import api.support.builders.Period;
import io.vertx.core.json.JsonObject;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.folio.circulation.support.HttpResult;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(JUnitParamsRunner.class)
public class DueDateCalculationTests {
  @Test
  @Parameters({
    "1",
    "2",
    "3",
    "4",
    "5"
  })
  public void shouldApplyWeeklyRollingPolicy(int duration) {

    JsonObject loanPolicy = new LoanPolicyBuilder()
      .rolling(Period.weeks(duration))
      .create();

    DateTime loanDate = new DateTime(2018, 3, 14, 11, 14, 54, DateTimeZone.UTC);

    JsonObject loan = new LoanBuilder()
      .open()
      .withLoanDate(loanDate)
      .create();

    final HttpResult<DateTime> calculationResult = new DueDateCalculation()
      .calculate(loan, loanPolicy);

    assertThat(calculationResult.value(), is(loanDate.plusWeeks(duration)));
  }

  @Test
  @Parameters({
    "1",
    "7",
    "14",
    "12",
    "30",
    "100"
  })
  public void shouldApplyDailyRollingPolicy(int duration) {

    JsonObject loanPolicy = new LoanPolicyBuilder()
      .rolling(Period.days(duration))
      .create();

    DateTime loanDate = new DateTime(2018, 3, 14, 11, 14, 54, DateTimeZone.UTC);

    JsonObject loan = new LoanBuilder()
      .open()
      .withLoanDate(loanDate)
      .create();

    final HttpResult<DateTime> calculationResult = new DueDateCalculation()
      .calculate(loan, loanPolicy);

    assertThat(calculationResult.value(), is(loanDate.plusDays(duration)));
  }

  @Test
  @Parameters({
    "2",
    "5",
    "30",
    "45",
    "60",
    "24"
  })
  public void shouldApplyHourlyRollingPolicy(int duration) {
    JsonObject loanPolicy = new LoanPolicyBuilder()
      .rolling(Period.hours(duration))
      .create();

    DateTime loanDate = new DateTime(2018, 3, 14, 11, 14, 54, DateTimeZone.UTC);

    JsonObject loan = new LoanBuilder()
      .open()
      .withLoanDate(loanDate)
      .create();

    final HttpResult<DateTime> calculationResult = new DueDateCalculation()
      .calculate(loan, loanPolicy);

    assertThat(calculationResult.value(), is(loanDate.plusHours(duration)));
  }

  @Test
  @Parameters({
    "1",
    "5",
    "30",
    "60",
    "200"
  })
  public void shouldApplyMinuteIntervalRollingPolicy(int duration) {
    JsonObject loanPolicy = new LoanPolicyBuilder()
      .rolling(Period.minutes(duration))
      .create();

    DateTime loanDate = new DateTime(2018, 3, 14, 11, 14, 54, DateTimeZone.UTC);

    JsonObject loan = new LoanBuilder()
      .open()
      .withLoanDate(loanDate)
      .create();

    final HttpResult<DateTime> calculationResult = new DueDateCalculation()
      .calculate(loan, loanPolicy);

    assertThat(calculationResult.value(), is(loanDate.plusMinutes(duration)));
  }

  @Test
  public void shouldFailForNonRollingProfile() {
    JsonObject loanPolicy = new LoanPolicyBuilder()
      .withLoansProfile("Fixed")
      .create();

    DateTime loanDate = new DateTime(2018, 3, 14, 11, 14, 54, DateTimeZone.UTC);

    JsonObject loan = new LoanBuilder()
      .open()
      .withLoanDate(loanDate)
      .create();

    final HttpResult<DateTime> calculationResult = new DueDateCalculation()
      .calculate(loan, loanPolicy);

    assertThat(calculationResult.failed(), is(true));
    //TODO: Figure out how to inspect failures
    assertThat(calculationResult.cause(), is(notNullValue()));
  }

  @Test
  public void shouldFailForUnrecognisedInterval() {
    JsonObject loanPolicy = new LoanPolicyBuilder()
      .rolling(new Period(5, "Unknown"))
      .create();

    DateTime loanDate = new DateTime(2018, 3, 14, 11, 14, 54, DateTimeZone.UTC);

    JsonObject loan = new LoanBuilder()
      .open()
      .withLoanDate(loanDate)
      .create();

    final HttpResult<DateTime> calculationResult = new DueDateCalculation()
      .calculate(loan, loanPolicy);

    assertThat(calculationResult.failed(), is(true));
    //TODO: Figure out how to inspect failures
    assertThat(calculationResult.cause(), is(notNullValue()));
  }
}
