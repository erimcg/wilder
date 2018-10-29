package net.n0code.wilder.obj;

import java.io.Serializable;

public class User implements Serializable {

    public static final String INVALID_USERNAME = "Invalid username";
    public static final String INVALID_PASSWORD = "Invalid password";
    public static final String INVALID_EMAIL = "Invalid email";

    public static final String INVALID_USERNAME_MSSG = "User name must have 3-5 characters and"
            + " contain only the characters a-z A-Z 0-9";
    public static final String INVALID_PASSWORD_MSSG = "Password must have at least 8 characters";
    public static final String INVALID_EMAIL_MSSG = "Invalid email address";

    private long uid = -1;
    private String username = null;
    private String password = null;
    private String email = null;

    /*
     *  This constructor should only be used by the databases.
     *  No error checking occurs.
     *  Password may be null (Outside users having excursions on this device).
     */
    public User(long uid, String username, String password, String email)
    {
        this.uid = uid;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    // Used when creating accounts.
    public User(String username, String password, String email)
            throws InvalidConstructionException
    {
        //TODO: remove this and uncomment later
        this.username = username;
        this.password = password;
        this.email = email;

        //setUserName(username);
        //setPassword(password);
        //setEmail(email);
    }

    public static boolean validUsername(String username)
    {
        if (username.length() >= 3 &&
                username.length() <= 15 &&
                username.matches("[a-zA-Z0-9_]*")) {
            return true;
        }
        else {
            return false;
        }
    }

    public static boolean validPassword(String password)
    {
        if (password.length() >= 8) {
            return true;
        }
        else {
            return false;
        }
    }

    public static boolean validEmail(String email)
    {
        if (email.contains("@")) {
            return true;
        }
        else {
            return false;
        }
    }

    public void setUserID(long uid)
    {
        this.uid = uid;
    }

    public void setUserName(String username) throws InvalidConstructionException
    {
        if (validUsername(username)) {
            this.username = username;
        }
        else {
            throw new InvalidConstructionException(INVALID_USERNAME);
        }
    }

    public void setPassword(String password) throws InvalidConstructionException
    {
        if (validPassword(password)) {
            this.password = password;
        }
        else {
            throw new InvalidConstructionException(INVALID_PASSWORD);
        }
    }

    public void setEmail(String email) throws InvalidConstructionException
    {
        if (validEmail(email)) {
            this.email = email;
            return;
        } else {
            throw new InvalidConstructionException(INVALID_EMAIL);
        }
    }

    public long getUserID() { return uid; }
    public String getUserName() { return username; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }

}