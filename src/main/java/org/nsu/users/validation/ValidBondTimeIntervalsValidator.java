package org.nsu.users.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalTime;
import java.util.List;

public class ValidBondTimeIntervalsValidator implements ConstraintValidator<ValidBondTimeIntervals, List<?>> {

    @Override
    public void initialize(ValidBondTimeIntervals constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(List<?> bondTimes, ConstraintValidatorContext context) {
        if (bondTimes == null || bondTimes.isEmpty()) {
            return true; // Let @NotNull handle null/empty validation
        }

        context.disableDefaultConstraintViolation();

        // Check each interval individually
        for (int i = 0; i < bondTimes.size(); i++) {
            Object bondTime = bondTimes.get(i);
            if (bondTime == null) {
                continue; // Let @NotNull handle null values
            }

            LocalTime start = getStartTime(bondTime);
            LocalTime end = getEndTime(bondTime);
            
            if (start == null || end == null) {
                continue; // Let @NotNull handle null values
            }

            // Check if start time is before end time
            if (!start.isBefore(end)) {
                context.buildConstraintViolationWithTemplate("Start time must be before end time")
                        .addPropertyNode("bondTime")
                        .addPropertyNode("[" + i + "]")
                        .addConstraintViolation();
                return false;
            }
        }

        // Check for overlapping intervals
        for (int i = 0; i < bondTimes.size(); i++) {
            Object first = bondTimes.get(i);
            if (first == null) {
                continue;
            }

            LocalTime start1 = getStartTime(first);
            LocalTime end1 = getEndTime(first);
            
            if (start1 == null || end1 == null) {
                continue;
            }

            for (int j = i + 1; j < bondTimes.size(); j++) {
                Object second = bondTimes.get(j);
                if (second == null) {
                    continue;
                }

                LocalTime start2 = getStartTime(second);
                LocalTime end2 = getEndTime(second);
                
                if (start2 == null || end2 == null) {
                    continue;
                }

                if (intervalsOverlap(start1, end1, start2, end2)) {
                    context.buildConstraintViolationWithTemplate("Bond time intervals cannot overlap")
                            .addPropertyNode("bondTime")
                            .addConstraintViolation();
                    return false;
                }
            }
        }

        return true;
    }

    private LocalTime getStartTime(Object bondTime) {
        try {
            // Handle RegistrationRequest.BondTime
            if (bondTime.getClass().getSimpleName().equals("BondTime")) {
                return (LocalTime) bondTime.getClass().getMethod("getBondTimeStart").invoke(bondTime);
            }
            // Handle UpdateUserRequest.BondTimeDto
            else if (bondTime.getClass().getSimpleName().equals("BondTimeDto")) {
                return (LocalTime) bondTime.getClass().getMethod("getBondTimeStart").invoke(bondTime);
            }
        } catch (Exception e) {
            // Fallback - return null to let other validations handle it
        }
        return null;
    }

    private LocalTime getEndTime(Object bondTime) {
        try {
            // Handle RegistrationRequest.BondTime
            if (bondTime.getClass().getSimpleName().equals("BondTime")) {
                return (LocalTime) bondTime.getClass().getMethod("getBondTimeEnd").invoke(bondTime);
            }
            // Handle UpdateUserRequest.BondTimeDto
            else if (bondTime.getClass().getSimpleName().equals("BondTimeDto")) {
                return (LocalTime) bondTime.getClass().getMethod("getBondTimeEnd").invoke(bondTime);
            }
        } catch (Exception e) {
            // Fallback - return null to let other validations handle it
        }
        return null;
    }

    private boolean intervalsOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        // Two intervals overlap if: start1 < end2 AND start2 < end1
        return start1.isBefore(end2) && start2.isBefore(end1);
    }
}