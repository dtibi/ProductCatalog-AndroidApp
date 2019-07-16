package com.example.android.productcatalog;

import java.util.Objects;

public class User {

    String email;
    String password;

    public User() {
        this.email="";
        this.password="";
    }
    public User(String email,String password) {
        this.email=email;
        this.password=password;
    }

    public String getEmail(){return this.email;}
    public String getPassword(){return this.password;}
    public void setEmail(String email){this.email=email;}
    public void setPassword(String password){this.password=password;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User product = (User) o;
        return  getEmail().equals(product.getEmail()) &&
                getPassword().equals(product.getPassword());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEmail(), getPassword());
    }

    @Override
    public String toString() {
        return "User{" +
                "Email='" + email + '\'' +
                ", Password='" + password + '\'' +
                '}';
    }
}
