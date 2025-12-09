package org.nsu.users.utils;

import java.time.LocalTime;
import java.util.List;

/**
 * Utility class for validating bond time intervals
 */
public final class BondTimeValidationUtil {
    
    private BondTimeValidationUtil() {}

    /**
     * Validates that start time is before end time
     */
    public static boolean isValidInterval(LocalTime start, LocalTime end) {
        if (start == null || end == null) {
            return true; // Let @NotNull handle null values
        }
        return start.isBefore(end);
    }

    /**
     * Checks if two time intervals overlap
     */
    public static boolean intervalsOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            return false;
        }
        // Two intervals overlap if: start1 < end2 AND start2 < end1
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    /**
     * Checks if any intervals in the list overlap with each other
     */
    public static <T> boolean hasOverlappingIntervals(List<T> intervals, TimeExtractor<T> extractor) {
        if (intervals == null || intervals.size() < 2) {
            return false;
        }
        
        for (int i = 0; i < intervals.size(); i++) {
            T first = intervals.get(i);
            if (first == null) continue;
            
            LocalTime start1 = extractor.getStart(first);
            LocalTime end1 = extractor.getEnd(first);
            
            for (int j = i + 1; j < intervals.size(); j++) {
                T second = intervals.get(j);
                if (second == null) continue;
                
                LocalTime start2 = extractor.getStart(second);
                LocalTime end2 = extractor.getEnd(second);
                
                if (intervalsOverlap(start1, end1, start2, end2)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Functional interface for extracting start and end time from bond time objects
     */
    public interface TimeExtractor<T> {
        LocalTime getStart(T obj);
        LocalTime getEnd(T obj);
    }
}
