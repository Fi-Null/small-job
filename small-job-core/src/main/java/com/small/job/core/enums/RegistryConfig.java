package com.small.job.core.enums;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/27/19 2:20 PM
 */
public class RegistryConfig {

    public static final int BEAT_TIMEOUT = 30;
    public static final int DEAD_TIMEOUT = BEAT_TIMEOUT * 3;

    public enum RegistType {EXECUTOR, ADMIN}

}
