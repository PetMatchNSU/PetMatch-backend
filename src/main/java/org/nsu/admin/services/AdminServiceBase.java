package org.nsu.admin.services;

import org.nsu.authorization.core.security.PersonDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class AdminServiceBase {

    protected Long getCurrentModeratorId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof PersonDetails) {
            PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
            return personDetails.getUserId();
        }
        throw new RuntimeException("Unable to get current moderator ID from security context");
    }
}
