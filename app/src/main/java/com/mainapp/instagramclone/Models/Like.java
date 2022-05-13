// 13.05.2022 - Reviewed. All Done.
package com.mainapp.instagramclone.Models;

import androidx.annotation.NonNull;

public class Like {
    private String user_id;

    public Like(String user_id) {
        this.user_id = user_id;
    }

    public Like() {}

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    @NonNull
    @Override
    public String toString() {
        return "Like{" +
                "user_id='" + user_id + '\'' +
                '}';
    }
}
