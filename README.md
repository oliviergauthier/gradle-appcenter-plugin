# AppCenter Gradle Plugin

[![Build Status](https://travis-ci.com/oliviergauthier/gradle-appcenter-plugin.svg?branch=master)](https://travis-ci.com/oliviergauthier/gradle-appcenter-plugin.svg?branch=master)

## Summary

This plugin allow you to upload android application to AppCenter. You can declare several applications, the plugin will take care of build variant to upload the apk on the right AppCenter application

Find last version on [Gradle Repository](https://plugins.gradle.org/plugin/com.betomorrow.appcenter)

## Quick Start

File : `build.gradle`

```groovy
buildscript {
    repositories {
        maven {
            url "https://plugins.gradle.org/m2/"
        }
    }
    dependencies {
        classpath "com.betomorrow.gradle:appcenter-plugin:1.1.1"
    }
}

```

File : `app/build.gradle`

```groovy

apply plugin: "com.betomorrow.appcenter"

android {
    // ...
    productFlavors {
        alpha {
            dimension "environment"
            applicationIdSuffix ".alpha"
            versionNameSuffix "-alpha"
        }
        beta {
            dimension "environment"
            applicationIdSuffix ".beta"
            versionNameSuffix "-beta"
        }
        prod {
            dimension "environment"
        }
    }
    
    buildTypes {
        release {
            signingConfig android.signingConfigs.release
        }
    }
}

appcenter {
    apiToken = "XXXXXXXX"                       // Api Token from AppCenter user profile
    ownerName = "ACME"                          // Owner Name of the AppCenter Application
    distributionGroups = ["Beta"]               // Name of the AppCenter Distribution Group
    releaseNotes = file("../changelog.md")      // Can be a file or text
    apps {                                      // Here we manage 3 AppCenter applications : alpha, beta and prod
        alpha {                                 // When dimension is provided, this name match the productFlavor name
            dimension = "environment"           // This dimension match the flavor dimension
            appName = "GradleSample-Alpha"      // The AppCenter application name
        }
        beta {
            dimension = "environment"
            appName = "GradleSample-Beta"
        }
        prodRelease {                           // When no dimension is provided, this name match the full variant name
            appName = "GradleSample"
        }
    }
}

```

The plugin will create 3 tasks :

- appCenterUploadAlphaRelease
- appCenterUploadBetaRelease
- appCenterUploadProdRelease


## Override properties

Each `apps` nodes inherit properties from `appcenter` global properties. You can override those properties by defining properties on the target application node like in the following sample with `alpha` node

```groovy
appcenter {
    apiToken = "XXXXXXXX"
    ownerName = "ACME"
    distributionGroups = ["Beta"]
    releaseNotes = file("../changelog.md")
    apps {      
        alpha {
            dimension = "environment"
            apiToken = "YYYYYYYY"
            ownerName = "AnotherOwner"
            distributionGroups = ["Alpha"]
            releaseNotes = "No Changes"
            appName = "GradleSample-Alpha"
        }
        prodRelease {           
            appName = "GradleSample"
        }
    }
}
```

## Use Environment Variables (for CI use)
- `APPCENTER_API_TOKEN` : AppCenter API token
- `APPCENTER_OWNER_NAME` : Owner name
- `APPCENTER_DISTRIBUTION_GROUPS` : Comma separated list of distribution groups 
- `APPCENTER_RELEASE_NOTES` :  Release notes in Markdown format