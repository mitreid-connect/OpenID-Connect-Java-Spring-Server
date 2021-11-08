# MITREid Connect

This project has been forked from the [MITREid Connect repository](https://github.com/mitreid-connect/OpenID-Connect-Java-Spring-Server). However, due to split between the paths we want to take with the project, we have decided to remove the fork relationship. All the original code will stay licensed to the original project. We would like to thank the developers of the original project for their work and wish them all the best in its continuation.

## Contribution

This repository uses [Conventional Commits](https://www.npmjs.com/package/@commitlint/config-conventional).
Any change that significantly changes behavior in a backward-incompatible way or requires a configuration change must be marked as BREAKING CHANGE.

# Building

Project is built with `mvn clean package` command. Following parameters can be passed to modify the final build:

### General properties
- location of the configuration files (path to the containing dir) : `-Dconfig.location=/etc/oidc/config`
    - default: `/etc/perun`
- final build name: `-Dfinal.name=name`
    - default: `oidc`

### Logging configuration
Following are the options for customization of logging when building

- logging style: `-Dlog.to=FILE`
    - default: `FILE`
    - available: `SYSLOG,FILE,ROLLING_FILE`
- logging level: `-Dlog.level=level`
    - default: `info`
    - available: `error,warn,info,debug,trace` 
- logging to SYSLOG
    - logging contextName (program name in syslog): `-Dlog.contextName=contextName`
        - default: `oidc`
    - logging facility: `-Dlog.facility=facility`
        - default: `LOCAL7`
- logging to file
    - file path specification: `-Dlog.file=/var/log/oidc`
      - default: `${catalina.base}/logs/${CONTEXT_NAME}`
    - file extension: `-Dlog.file-extension=debug`
      - default: `log`
- logging to rolling-file
    - file path specification: `-Dlog.rolling-file=/var/log/oidc`
      - default: `${catalina.base}/logs/${CONTEXT_NAME}`
    - file extension: `-Dlog.file-extension=debug`
      - default: `log`

## WAR file

The result war-file is located under the `perun-oidc-server-webapp/target/{NAME}.war`. The WAR should be deployed into a tomcat.
