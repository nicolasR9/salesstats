package com.ebay.salesstatsnicolasr.impl;
import com.ebay.salesstatsnicolasr.model.Statistics;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Queue;

@Component
public class CheckoutStatisticsCalculator {
    
    private static final int STATISTICS_PERIOD_SECONDS = 60;
    
    private long checkoutAmountSum = 0;
    private Queue<Entry> queue = new LinkedList<Entry>();
    
    public synchronized void add(long checkoutAmount) {
        checkoutAmountSum += checkoutAmount;
        queue.add(new Entry(LocalDateTime.now(), checkoutAmount));
    }
    
    public synchronized Statistics getStatistics() {
        removeOldEntries();
        return new Statistics(checkoutAmountSum, queue.size());
    }
    
    private void removeOldEntries() {
        Entry entry = queue.peek();
        LocalDateTime now = LocalDateTime.now();
        while (entry != null && Duration.between(entry.checkoutTime, now).getSeconds() > STATISTICS_PERIOD_SECONDS) {
            queue.remove();
            checkoutAmountSum -= entry.checkoutAmount;
            entry = queue.peek();
        }
    }
    
    private static class Entry {
        private LocalDateTime checkoutTime;
        private double checkoutAmount;
        
        public Entry(LocalDateTime checkoutTime, double checkoutAmount) {
            this.checkoutTime = checkoutTime;
            this.checkoutAmount = checkoutAmount;
        }
    }

}
