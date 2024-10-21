package com.purna.eval.config;

public enum SysPropNames {

    USERNAME("username"),
    PASSWORD("password"),
    LOG_API_CALLS("logApiCalls");

    public final String label;

    SysPropNames(String label) {
        this.label = label;
    }

}
