package com.bth.running.util;

/**
 * @author Martin Macheiner
 *         Date: 08.09.2017.
 */

public class AppParams {

    public static final int REQ_CODE_PERM_LOCATION = 0x1245;

    public static final int HELP_SHOW_DELAY = 2000;
    public static final int LOCATIONS_FOR_CURRENT_PACE = 10;

    public static final int REALM_SCHEMA_VERSION = 1;

    public static final int NOTIFICATION_ID = 0x1726;
    public static final String NOTIFICATION_CHANNEL_ID = "def_channel";
    public static String START_FROM_NOTIFICATION_ACTION = "start_from_notification";

    public class ServiceConnection {

        public static final String SUBJECT = "calling_subject";
        public static final int SUBJECT_START = 0;
        public static final int SUBJECT_STOP = 1;
        public static final int SUBJECT_REQ_CUR1RENT = 2;

        public static final String EXTRA_FINISHED_RUN = "extra_finished_run";
        public static final String EXTRA_CURRENT_RUN = "extra_finished_run";

        public static final String SERVICE_SUBJECT = "service_subject";
        public static final int SERVICE_SUBJECT_FINISHED_RUN = 0;
        public static final int SERVICE_SUBJECT_CURRENT_RUN = 1;
        public static final int SERVICE_SUBJECT_RUN_UPDATE = 2;
        public static final String EXTRA_RUN_UPDATE = "extra_run_update";
        public static final String EXTRA_START_MILLIS = "extra_start_millis";
    }
}
