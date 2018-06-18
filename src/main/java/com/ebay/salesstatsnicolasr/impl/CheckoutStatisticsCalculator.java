package com.ebay.salesstatsnicolasr.impl;

import com.ebay.salesstatsnicolasr.model.Statistics;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * Calculates the checkout statistics (order count, order amount total) for the last 60 seconds.
 *
 */
@Component
public class CheckoutStatisticsCalculator {

    private static final int STATISTICS_PERIOD_SECONDS = 60;

    private long checkoutAmountSum = 0;
    private long orderCount = 0;

    //key: seconds since 1970, value: checkout statistics for that second
    private SortedMap<Long, PerSecondStatisticsEntry> checkoutSecondToStatistics =
            new TreeMap<>();

    public synchronized void add(long checkoutAmount, long currentTimeSeconds) {
        checkoutAmountSum += checkoutAmount;
        ++orderCount;
        PerSecondStatisticsEntry perSecondStatisticsEntry = checkoutSecondToStatistics.get(currentTimeSeconds);
        if (perSecondStatisticsEntry == null) { //first checkout in this second
            perSecondStatisticsEntry = new PerSecondStatisticsEntry(checkoutAmount);
        } else {
            perSecondStatisticsEntry.checkoutAmount += checkoutAmount;
            ++perSecondStatisticsEntry.count;
        }
        checkoutSecondToStatistics.put(currentTimeSeconds, perSecondStatisticsEntry);
    }

    public synchronized Statistics getStatistics(long currentTimeSeconds) {
        removeOldEntries(currentTimeSeconds);
        return new Statistics(checkoutAmountSum, orderCount);
    }
    
    /**
     * Ensures that even if "getStatistics" isn't called regularly, the statistics map doesn't grow indefinitely.
     */
    @Scheduled(fixedDelay = 10000)
    private void removeOldEntriesScheduled() {
        long currentTimeSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        removeOldEntries(currentTimeSeconds);
    }

    /**
     * Removes all entries from the map which are at least 60 seconds older than currentTimeSeconds.
     * Updates the global statistics accordingly.
     */
    private void removeOldEntries(long currentTimeSeconds) {
        long threshold = currentTimeSeconds - STATISTICS_PERIOD_SECONDS;
        SortedMap<Long, PerSecondStatisticsEntry> toRemove =
                checkoutSecondToStatistics.headMap(threshold);
        
        toRemove.entrySet().stream().forEach(entry -> {
            checkoutAmountSum -= entry.getValue().checkoutAmount;
            orderCount -= entry.getValue().count;
        });
        toRemove.clear();
    }

    private static class PerSecondStatisticsEntry {

        public PerSecondStatisticsEntry(long checkoutAmount) {
            this.count = 1;
            this.checkoutAmount = checkoutAmount;
        }

        private long count;
        private long checkoutAmount;
    }
}
