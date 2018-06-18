package com.ebay.salesstatsnicolasr.impl;

import com.ebay.salesstatsnicolasr.model.Statistics;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Calculates the checkout statistics (order count, order amount total) for the last 60 seconds.
 *
 */
@Component
public class CheckoutStatisticsCalculator {

    private static final int STATISTICS_PERIOD_SECONDS = 60;

    private AtomicLong checkoutAmountSum = new AtomicLong(0);

    private AtomicLong orderCount = new AtomicLong(0);

    private SortedMap<Long, PerSecondStatisticsEntry> checkoutSecondToStatistics =
            Collections.synchronizedSortedMap(new TreeMap<>());

    public void add(long checkoutAmount, long currentTimeSeconds) {
        checkoutAmountSum.addAndGet(checkoutAmount);
        orderCount.incrementAndGet();
        PerSecondStatisticsEntry perSecondStatisticsEntry = checkoutSecondToStatistics.get(currentTimeSeconds);
        if (perSecondStatisticsEntry == null) {
            perSecondStatisticsEntry = new PerSecondStatisticsEntry(checkoutAmount);
        } else {
            perSecondStatisticsEntry.checkoutAmount.addAndGet(checkoutAmount);
            perSecondStatisticsEntry.count.incrementAndGet();
        }
        checkoutSecondToStatistics.put(currentTimeSeconds, perSecondStatisticsEntry);
    }

    public Statistics getStatistics(long currentTimeSeconds) {
        removeOldEntries(currentTimeSeconds);
        return new Statistics(checkoutAmountSum.longValue(), orderCount.longValue());
    }
    
    /**
     * Ensures that even if "getStatistics" isn't called regularly, the statistics map doesn't grow indefinitely.
     */
    @Scheduled(fixedDelay = 10000)
    private void removeOldEntriesScheduled() {
        long currentTimeSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        removeOldEntries(currentTimeSeconds);
    }

    private void removeOldEntries(long currentTimeSeconds) {
        long threshold = currentTimeSeconds - STATISTICS_PERIOD_SECONDS;
        SortedMap<Long, PerSecondStatisticsEntry> toRemove =
                checkoutSecondToStatistics.headMap(threshold);
        
        toRemove.entrySet().stream().forEach(entry -> {
            checkoutAmountSum.addAndGet(-entry.getValue().checkoutAmount.longValue());
            orderCount.addAndGet(-entry.getValue().count.longValue());
        });
        toRemove.clear();
    }

    private static class PerSecondStatisticsEntry {

        public PerSecondStatisticsEntry(long checkoutAmount) {
            this.count = new AtomicLong(1);
            this.checkoutAmount = new AtomicLong(checkoutAmount);
        }

        private AtomicLong count;
        private AtomicLong checkoutAmount;
    }
}
