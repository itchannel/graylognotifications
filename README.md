# Notifications Plugin for Graylog

A Graylog plugin to allow for pushover intergration. This plugin is designed to allow quick sending of alarm notifications to pushover users or groups.

## Beta support for 4.0
Plugin works with 4.0 but I have fully rewritten the codebase to no longer be a "Legacy" callback so there may be small bugs. 

**Required Graylog version:** 3.0 and later

Installation
------------

[Download the plugin](https://github.com/itchannel/graylognotifications/releases)
and place the `.jar` file in your Graylog plugin directory. The plugin directory
is the `plugins/` folder relative from your `graylog-server` directory by default
and can be configured in your `graylog.conf` file.

Restart `graylog-server` and you are done.

Usage
-----

The following information is required in order to use the plugin:

* Pushover API Key
* Pushover Group ID or User ID



Getting started
---------------

This project is using Maven 3 and requires Java 8 or higher.

* Clone this repository.
* Run `mvn package` to build a JAR file.
* Optional: Run `mvn jdeb:jdeb` and `mvn rpm:rpm` to create a DEB and RPM package respectively.
* Copy generated JAR file in target directory to your Graylog plugin directory.
* Restart the Graylog.

