package com.example.android.productcatalog.data;

import com.example.android.productcatalog.MainActivity;
import com.example.android.productcatalog.data.model.LoggedInUser;

import java.io.IOException;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    public Result<LoggedInUser> login(String username, String password) {

        try {
            // FIX: handle loggedInUser authentication
            if (username.contentEquals("yossi") && password.contentEquals("123456")) {
                LoggedInUser fakeUser =
                        new LoggedInUser(
                                java.util.UUID.randomUUID().toString(),
                                "Yossi");
                MainActivity.userisadmin = true;
                return new Result.Success<>(fakeUser);
            }
        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
        return new Result.Error(new IOException("Error logging in"));
    }

    public void logout() {
        MainActivity.userisadmin = false;
    }
}
