package org.nsu.authorization.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.properties")
@Getter
@Setter
public class AppProperties {

    private String timezone;
}
