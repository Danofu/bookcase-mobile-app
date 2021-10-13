package com.danofu.bookcase.databaseobject;

import androidx.annotation.NonNull;

public class Reader {

    private String id, firstName, lastName, address, email, photoLink;

    public Reader() {
    }

    public Reader(String id, String firstName, String lastName, String address, String email,
                  String photoLink) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.email = email;
        this.photoLink = photoLink;
    }

    public String getFullName() {
        if (firstName == null && lastName == null)
            return null;

        String fullName = "";

        if (lastName != null)
            fullName += lastName + " ";
        if (firstName != null)
            fullName += firstName;

        return fullName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoLink() {
        return photoLink;
    }

    public void setPhotoLink(String photoLink) {
        this.photoLink = photoLink;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reader reader = (Reader) o;

        return compareFields(firstName, reader.firstName) &&
                compareFields(lastName, reader.lastName) &&
                compareFields(address, reader.address) &&
                compareFields(email, reader.email) &&
                compareFields(photoLink, reader.photoLink);
    }

    private boolean compareFields(String field1, String field2) {
        if (field1 == null) return field2 == null;
        else return field1.equals(field2);
    }

    @NonNull
    @Override
    public String toString() {
        return "Reader{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", address='" + address + '\'' +
                ", email='" + email + '\'' +
                ", photoLink='" + photoLink + '\'' +
                '}';
    }

}
