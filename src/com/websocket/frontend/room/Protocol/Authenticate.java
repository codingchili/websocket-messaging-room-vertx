package com.websocket.frontend.room.Protocol;

/**
 * Created by Robin on 2015-12-16.
 * <p>
 * Transfer object for Authentication requests/replies.
 */
public class Authenticate {
    public static final String ACTION = "authenticate";
    private Header header;
    private String username;
    private String password;
    private String token;
    private Long expiry;
    private Boolean authenticated = false;
    private Boolean created = false;

    public Authenticate() {
    }

    public Authenticate(String username, String password) {
        this(username, password, null);
    }

    public Authenticate(String username, String password, String actor) {
        header = new Header(ACTION, actor);
        this.password = password;
        this.username = username;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Long getExpiry() {
        return expiry;
    }

    public void setExpiry(Long expiry) {
        this.expiry = expiry;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean isAuthenticated() {
        return authenticated;
    }

    public Authenticate setAuthenticated(Boolean authenticated) {
        this.authenticated = authenticated;
        return null;
    }

    public Boolean isCreated() {
        return created;
    }

    public void setCreated(Boolean created) {
        this.created = created;
    }
}
