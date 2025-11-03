package org.nsu.users.core.services;

import org.nsu.authorization.core.config.AppProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class TimezoneService {

    private final AppProperties appProperties;
    private static TimezoneService instance;

    @Autowired
    public TimezoneService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @PostConstruct
    public void init() {
        instance = this;
    }

    public String getConfiguredTimezone() {
        return appProperties.getTimezone();
    }

    public static OffsetDateTime convertLocalTimeToOffsetDateTimeStatic(LocalTime localTime) {
        if (localTime == null) {
            return null;
        }
        
        String timezone = instance != null ? instance.getConfiguredTimezone() : "Europe/Moscow";
        ZoneId configuredZone = ZoneId.of(timezone);
        ZonedDateTime zdt = ZonedDateTime.now(configuredZone).with(localTime);
        return zdt.toOffsetDateTime();
    }

}