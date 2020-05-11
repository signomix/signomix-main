# SIGNOMIX

**IoT & data management platform**

## Quick start

### Building the platform

Signomix project uses Apache Ant as a build tool. You can get the most recent version from [https://ant.apache.org/bindownload](https://ant.apache.org/bindownload)

First of all download required libraries by running Ant target:

    ant get-dependencies

Then to build Signomix run:

    ant dist

The build script asks for release type (select 'standard') and the environment name (select 'dev').

As the result you will get: 

* the selected environment structure (inside the `dist` folder)
* the platform distribution package - `signomix.zip`

### Running the platform locally

There are 2 options how to start the platform:

1. With the `run.sh` script directly in the 'dist' folder

    ```
    cd dist
    sh run.sh
    ```

2. By unpacking the distribution archive in target location.

    ```
    mkdir tmp
    unzip signomix.zip -d tmp
    cd tmp
    sh run.sh
    ```

The service can be stopped by pressing Ctrl-C.

### Preconfigured accounts

Signomix contains two preconfigured user accounts:

1. Signomix admin, used for content and user management [http://localhost:8080/admin](http://localhost:8080/admin)

    login: admin

    password: test123

2. Example user of the Signomix application, able to register and manage IoT devices and data at [http://localhost:8080/app](http://localhost:8080/app)

    login: tester1

    password: signomix

## Platform features

* user and content management
* IoT devices management
* REST API
* integration API for LoRaWAN networks
* data processing
* data visualisation (dashboards)
* notifications

For details go to [https://signomix.com](https://signomix.com)

## Documentation

The documentation will be successively improved on the [wiki pages](https://github.com/gskorupa/signomix/wiki). It will take some time to provide good quality documentation for the platform, but the project maintainer will answer any questions that may arise. 

At the same time, user documentation is available online at [https://signomix.com](https://signomix.com)
