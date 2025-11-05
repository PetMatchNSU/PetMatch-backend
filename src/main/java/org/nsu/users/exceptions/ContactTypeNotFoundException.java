package org.nsu.users.exceptions;

public class ContactTypeNotFoundException extends RuntimeException {
    public ContactTypeNotFoundException(String type) {
        super("Contact type not found: " + type);
    }
}