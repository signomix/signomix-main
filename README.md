# SIGNOMIX

**IoT & data management platform**

## Quick start

### Building the platform

Signomix project uses [Apache Maven](https://maven.apache.org/) as a build tool. You also need [npm](https://www.npmjs.com/) 
to build Signomix web applications.

Before building included web application for the first time, 
you need to install required node packages:

```
   $ cd src/main/webapp/blog
   $ npm install
```

Then you can build Signomix:

```
   $ mvn package
   $ mvn assembly:single
```

As the result you will get the platform distribution package `signomix-distribution.zip`
located in the `target` folder. 

### Running your local distribution

Unpack the distribution archive in target location and run:

```
   $ mkdir tmp
   $ unzip signomix.zip -d tmp
   $ cd tmp
   $ sh run.sh
```

The service can be stopped by pressing `Ctrl-C`.

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
