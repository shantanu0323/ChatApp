package com.chat.bridge;

/**
 * Created by Shaan on 28-11-2017.
 */

class Friends {
    public String sinceDate;

    public Friends (){}

    public Friends (String sinceDate) {
        this.sinceDate = sinceDate;
    }

    public String getSinceDate() {
        return sinceDate;
    }

    public void setSinceDate(String sinceDate) {
        this.sinceDate = sinceDate;
    }
}
