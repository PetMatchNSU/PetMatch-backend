package org.nsu.users.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.nsu.users.utils.BondTimeValidationUtil;

import java.time.LocalTime;
import java.util.List;

/**
 * Validator for bond time intervals.
 * Uses BondTimeValidationUtil for actual validation logic.
 */
public class ValidBondTimeIntervalsValidator implements ConstraintValidator<ValidBondTimeIntervals, List<?>> {

    private final BondTimeValidationUtil.TimeExtractor<Object> extractor = new BondTimeValidationUtil.TimeExtractor<>() {
        @Override
        public LocalTime getStart(Object obj) {
            return getTimeByMethod(obj, "getBondTimeStart");
        }

        @Override
        public LocalTime getEnd(Object obj) {
            return getTimeByMethod(obj, "getBondTimeEnd");
        }

        private LocalTime getTimeByMethod(Object obj, String methodName) {
            try {
                return (LocalTime) obj.getClass().getMethod(methodName).invoke(obj);
            } catch (Exception e) {
                return null;
            }
        }
    };

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
                continue;
            }

            LocalTime start = extractor.getStart(bondTime);
            LocalTime end = extractor.getEnd(bondTime);
            
            if (!BondTimeValidationUtil.isValidInterval(start, end)) {
                context.buildConstraintViolationWithTemplate("Start time must be before end time")
                        .addPropertyNode("bondTime")
                        .addPropertyNode("[" + i + "]")
                        .addConstraintViolation();
                return false;
            }
        }

        // Check for overlapping intervals using utility method
        @SuppressWarnings("unchecked")
        List<Object> intervals = (List<Object>) bondTimes;
        if (BondTimeValidationUtil.hasOverlappingIntervals(intervals, extractor)) {
            context.buildConstraintViolationWithTemplate("Bond time intervals cannot overlap")
                    .addPropertyNode("bondTime")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}