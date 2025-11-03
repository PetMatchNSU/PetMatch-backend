package org.nsu.users.core.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class TimezoneService {

    private final ZoneId appTimezoneId;

    public TimezoneService(@Value("${app.timezone}") String appTimezoneId) {
        this.appTimezoneId = ZoneId.of(appTimezoneId);
    }

    public OffsetDateTime convertLocalTimeToOffsetDateTime(LocalTime localTime) {
        if (localTime == null) {
            return null;
        }

        ZonedDateTime zdt = ZonedDateTime.now(appTimezoneId).with(localTime);
        return zdt.toOffsetDateTime();
    }
}
