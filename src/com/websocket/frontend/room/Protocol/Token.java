package com.websocket.frontend.room.Protocol;

/**
 * Created by Robin on 2015-12-22.
 * <p>
 * Used to request authentication by token.
 */
public class Token {
    public static final String ACTION = "token";
    private String key;
    private Long expiry;
    private String username;
    private Boolean accepted;
    private Header header;

    public Token() {
        header = new Header(ACTION);
    }

    public Token(Boolean accepted) {
        this();
        this.accepted = accepted;
    }

    public Boolean getAccepted() {
        return accepted;
    }

    public void setAccepted(Boolean accepted) {
        this.accepted = accepted;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Long getExpiry() {
        return expiry;
    }

    public void setExpiry(Long expiry) {
        this.expiry = expiry;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
