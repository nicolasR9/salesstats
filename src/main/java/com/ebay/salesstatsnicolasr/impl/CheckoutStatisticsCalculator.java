package com.ebay.salesstatsnicolasr.impl;
import com.ebay.salesstatsnicolasr.model.Statistics;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class CheckoutStatisticsCalculator {
    
    private static final int STATISTICS_PERIOD_SECONDS = 60;
    
    private AtomicLong checkoutAmountSum = new AtomicLong(0);
    
    private AtomicLong orderCount = new AtomicLong(0);
    
    private SortedMap<Long, Entry> checkoutSecondToStatistics = Collections.synchronizedSortedMap(new TreeMap<>());

    //TODO are seconds enough?
    public void add(long checkoutAmount, long currentTimeMillis) {
        checkoutAmountSum.addAndGet(checkoutAmount);
        orderCount.incrementAndGet();
        //TODO just use first entry
        long key = currentTimeMillis / 1000;
        Entry entry = checkoutSecondToStatistics.get(key);
        if (entry == null) {
            entry = new Entry(checkoutAmount);
        } else {
            entry.checkoutAmount.addAndGet(checkoutAmount);
            entry.count.incrementAndGet();
        }
        checkoutSecondToStatistics.put(key, entry);
    }
    
    public Statistics getStatistics(long currentTimeMillis) {
        removeOldEntries(currentTimeMillis);
        return new Statistics(checkoutAmountSum.longValue(), orderCount.longValue());
    }
    
    //TODO call every x seconds
    private void removeOldEntries(long currentTimeMillis) {
        SortedMap<Long, Entry> toRemove =
                checkoutSecondToStatistics.headMap(currentTimeMillis / 1000 - STATISTICS_PERIOD_SECONDS);
        for (java.util.Map.Entry<Long, Entry> entry : toRemove.entrySet()) {
            checkoutAmountSum.addAndGet(-entry.getValue().checkoutAmount.longValue());
            orderCount.addAndGet(-entry.getValue().count.longValue());
        }
        toRemove.clear();
    }

    private static class Entry {
        
        public Entry(long checkoutAmount) {
            this.count = new AtomicLong(1);
            this.checkoutAmount = new AtomicLong(checkoutAmount);
        }
        private AtomicLong count;
        private AtomicLong checkoutAmount;
    }


}