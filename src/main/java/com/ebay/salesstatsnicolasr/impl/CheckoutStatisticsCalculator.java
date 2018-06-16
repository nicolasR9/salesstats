package com.ebay.salesstatsnicolasr.impl;
import com.ebay.salesstatsnicolasr.model.Statistics;

import org.springframework.stereotype.Component;

import java.util.SortedMap;
import java.util.TreeMap;

@Component
public class CheckoutStatisticsCalculator {
    
    private static final int STATISTICS_PERIOD_SECONDS = 60;
    
    private long checkoutAmountSum = 0;
    
    private long orderCount = 0;
    
    private TreeMap<Long, Entry> checkoutSecondToStatistics = new TreeMap<>();

    //TODO are seconds enough?
    public void add(long checkoutAmount, long currentTimeMillis) {
        checkoutAmountSum += checkoutAmount;
        ++orderCount;
        //TODO just use first entry
        long key = currentTimeMillis / 1000;
        Entry entry = checkoutSecondToStatistics.get(key);
        if (entry == null) {
            entry = new Entry(1, checkoutAmount);
        } else {
            entry.checkoutAmount += checkoutAmount;
            ++entry.count;
        }
        checkoutSecondToStatistics.put(key, entry);
    }
    
    public Statistics getStatistics(long currentTimeMillis) {
        removeOldEntries(currentTimeMillis);
        return new Statistics(checkoutAmountSum, orderCount);
    }
    
    //TODO call every x seconds
    private void removeOldEntries(long currentTimeMillis) {
        SortedMap<Long, Entry> toRemove =
                checkoutSecondToStatistics.headMap(currentTimeMillis / 1000 - STATISTICS_PERIOD_SECONDS);
        for (java.util.Map.Entry<Long, Entry> entry : toRemove.entrySet()) {
            checkoutAmountSum -= entry.getValue().checkoutAmount;
            orderCount -= entry.getValue().count;
        }
        toRemove.clear();
    }

    private static class Entry {
        
        public Entry(long count, double checkoutAmount) {
            this.count = count;
            this.checkoutAmount = checkoutAmount;
        }
        private long count;
        private double checkoutAmount;
    }


}
