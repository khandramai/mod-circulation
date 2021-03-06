package org.folio.circulation.domain.notice.schedule;

import static org.folio.circulation.domain.notice.schedule.TriggeringEvent.DUE_DATE;
import static org.folio.circulation.support.results.Result.succeeded;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.folio.circulation.domain.Loan;
import org.folio.circulation.domain.LoanAndRelatedRecords;
import org.folio.circulation.domain.notice.NoticeConfiguration;
import org.folio.circulation.domain.notice.NoticeEventType;
import org.folio.circulation.domain.notice.NoticeTiming;
import org.folio.circulation.domain.notice.PatronNoticePolicy;
import org.folio.circulation.infrastructure.storage.notices.PatronNoticePolicyRepository;
import org.folio.circulation.infrastructure.storage.notices.ScheduledNoticesRepository;
import org.folio.circulation.resources.context.RenewalContext;
import org.folio.circulation.support.Clients;
import org.folio.circulation.support.results.Result;
import org.joda.time.DateTime;
import org.joda.time.Period;

public class DueDateScheduledNoticeService {
  public static DueDateScheduledNoticeService using(Clients clients) {
    return new DueDateScheduledNoticeService(
      ScheduledNoticesRepository.using(clients),
      new PatronNoticePolicyRepository(clients));
  }

  private final ScheduledNoticesRepository scheduledNoticesRepository;
  private final PatronNoticePolicyRepository noticePolicyRepository;

  public DueDateScheduledNoticeService(
    ScheduledNoticesRepository scheduledNoticesRepository,
    PatronNoticePolicyRepository noticePolicyRepository) {
    this.scheduledNoticesRepository = scheduledNoticesRepository;
    this.noticePolicyRepository = noticePolicyRepository;
  }

  public Result<LoanAndRelatedRecords> scheduleNoticesForLoanDueDate(
    LoanAndRelatedRecords relatedRecords) {
    scheduleNoticesForLoanDueDate(relatedRecords.getLoan());
    return succeeded(relatedRecords);
  }

  private Result<Loan> scheduleNoticesForLoanDueDate(Loan loan) {
    noticePolicyRepository.lookupPolicy(loan)
      .thenAccept(r -> r.next(policy -> scheduleDueDateNoticesBasedOnPolicy(loan, policy)));
    return succeeded(loan);
  }

  private Result<PatronNoticePolicy> scheduleDueDateNoticesBasedOnPolicy(
    Loan loan, PatronNoticePolicy noticePolicy) {

    List<ScheduledNotice> scheduledNotices = noticePolicy.getNoticeConfigurations().stream()
      .filter(c -> c.getNoticeEventType() == NoticeEventType.DUE_DATE)
      .map(c -> createDueDateScheduledNotice(c, loan))
      .collect(Collectors.toList());

    scheduledNotices.forEach(scheduledNoticesRepository::create);
    return succeeded(noticePolicy);
  }

  private ScheduledNotice createDueDateScheduledNotice(NoticeConfiguration configuration, Loan loan) {
    return new ScheduledNoticeBuilder()
      .setId(UUID.randomUUID().toString())
      .setLoanId(loan.getId())
      .setRecipientUserId(loan.getUserId())
      .setNextRunTime(determineNextRunTime(configuration, loan))
      .setNoticeConfig(createScheduledNoticeConfig(configuration))
      .setTriggeringEvent(DUE_DATE)
      .build();
  }

  private DateTime determineNextRunTime(NoticeConfiguration configuration, Loan loan) {
    if (configuration.getTiming() == NoticeTiming.UPON_AT) {
      return loan.getDueDate();
    }
    DateTime dueDate = loan.getDueDate();
    Period timingPeriod = configuration.getTimingPeriod().timePeriod();

    switch (configuration.getTiming()) {
      case BEFORE:
        return dueDate.minus(timingPeriod);
      case AFTER:
        return dueDate.plus(timingPeriod);
      default:
        return dueDate;
    }
  }

  private ScheduledNoticeConfig createScheduledNoticeConfig(NoticeConfiguration configuration) {
    return new ScheduledNoticeConfigBuilder()
      .setTemplateId(configuration.getTemplateId())
      .setTiming(configuration.getTiming())
      .setFormat(configuration.getNoticeFormat())
      .setRecurringPeriod(configuration.getRecurringPeriod())
      .setSendInRealTime(configuration.sendInRealTime())
      .build();
  }

  public Result<LoanAndRelatedRecords> rescheduleDueDateNotices(LoanAndRelatedRecords relatedRecords) {
    return rescheduleDueDateNotices(relatedRecords.getLoan(), relatedRecords);
  }

  public Result<RenewalContext> rescheduleDueDateNotices(RenewalContext renewalContext) {
    return rescheduleDueDateNotices(renewalContext.getLoan(), renewalContext);
  }

  private <T> Result<T> rescheduleDueDateNotices(Loan loan, T mapTo) {
    if (!loan.isClosed()) {
      scheduledNoticesRepository.deleteByLoanIdAndTriggeringEvent(loan.getId(), DUE_DATE)
        .thenAccept(r -> r.next(v -> scheduleNoticesForLoanDueDate(loan)));
    }

    return succeeded(mapTo);
  }
}
