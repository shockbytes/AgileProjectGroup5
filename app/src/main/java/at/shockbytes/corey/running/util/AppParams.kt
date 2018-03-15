package at.shockbytes.corey.running.util

/**
 * @author  Martin Macheiner
 * Date:    08.09.2017.
 */

object AppParams {

    object ServiceConnection {

        val SUBJECT = "calling_subject"
        val SUBJECT_START = 0
        val SUBJECT_STOP = 1
        val SUBJECT_REQ_CUR1RENT = 2

        val EXTRA_FINISHED_RUN = "extra_finished_run"
        val EXTRA_CURRENT_RUN = "extra_finished_run"

        val SERVICE_SUBJECT = "service_subject"
        val SERVICE_SUBJECT_FINISHED_RUN = 0
        val SERVICE_SUBJECT_CURRENT_RUN = 1
        val SERVICE_SUBJECT_RUN_UPDATE = 2
        val EXTRA_RUN_UPDATE = "extra_run_update"
        val EXTRA_START_MILLIS = "extra_start_millis"
    }

    const val REQ_CODE_PERM_LOCATION = 0x1245

    const val HELP_SHOW_DELAY = 2000L
    const val LOCATIONS_FOR_CURRENT_PACE = 10

    const val REALM_VERSION_INIT_VERSION = 1
    const val REALM_VERSION_STATS_UPDATE_VERSION = 2
    const val REALM_VERSION_RUN_START_TIME_UPDATE = 3
    const val REALM_VERSION_BUGFIX = 4
    const val REALM_SCHEMA_VERSION = 5

    const val NOTIFICATION_ID = 0x1726
    const val NOTIFICATION_CHANNEL_ID = "def_channel"
    const val START_FROM_NOTIFICATION_ACTION = "start_from_notification"

}
