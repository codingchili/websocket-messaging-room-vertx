package com.websocket.frontend.room.Protocol;

import com.websocket.frontend.room.Configuration;

/**
 * Created by Robin on 2015-12-28.
 *
 * Sent to the logging service.
 */
public class LogUserCount {
    private String name = Configuration.REGISTER_NAME;
    private String type = "logging.users";
    private Integer count;

    public LogUserCount() {
    }

    public LogUserCount(Integer count) {
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
