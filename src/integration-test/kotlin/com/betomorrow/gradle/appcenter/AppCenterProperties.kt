package com.betomorrow.gradle.appcenter

/**
 * AppCenter API properties used for integration tests.
 */
object AppCenterProperties {

    /** AppCenter API token. */
    val API_TOKEN = ""

    /** AppCenter owner name. */
    val OWNER_NAME = ""

    /** AppCenter app name. */
    val APP_NAME = "GradleSample"

    /** Path to apk file that gets uploaded to AppCenter. */
    val APK_PATH = "./src/integration-test/resources/test.apk"

    /** Path to mapping file that gets uploaded to AppCenter. */
    val MAPPING_PATH = "./src/integration-test/resources/mapping.txt"
}
