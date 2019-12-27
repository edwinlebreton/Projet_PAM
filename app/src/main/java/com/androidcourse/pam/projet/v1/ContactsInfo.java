package com.androidcourse.pam.projet.v1;

public class ContactsInfo {
    private String contactId;
    private String displayName;
    private String phoneNumber;
    private Boolean isAddedNumber;

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Boolean getIsAddedNumber() {
        return isAddedNumber;
    }

    public void setIsAddedNumber(Boolean isAddedNumber) {
        this.isAddedNumber = isAddedNumber;
    }
}
