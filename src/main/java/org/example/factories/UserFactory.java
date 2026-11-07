package org.example.factories;

import org.example.annotation.Cumponent;
import org.example.models.User;

@Cumponent
public class UserFactory {
    public User create(String fullName, String contactDetails) {
        User user = new User();
        user.setFullName(fullName);
        user.setContactDetails(contactDetails);
        return user;
    }
}
