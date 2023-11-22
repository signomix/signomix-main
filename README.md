On November 22, 2023, I changed the name of the GitHub repository "signomix" to "signomix-main". This change was intended to organize the naming in a way that better reflects the purpose of each repository in the Signomix platform. 

You can find more information about the platform and the changes in the project in the "signomix-ta" repository.

# SIGNOMIX

**IoT & data management platform**

A hosted, cloud-based Signomix is available at https://signomix.com

Services forming the platform:

- [signomix](https://github.com/signomix/signomix) - this repository
- [signomix-ta-ps](https://github.com/signomix/signomix-ta-ps)
- [signomix-database](https://github.com/signomix/signomix-database)
- [signomix-proxy](https://github.com/signomix/signomix-proxy)

## Quickstart

Signomix demo instance can be started using [Docker Compose](https://docs.docker.com/compose/). The configuration file `docker-compose.yml` creates multi-container Signomix application using packages from GitHub repositories.

```shell
$ docker-compose -f docker-compose.yml up
```

Then browse to http://localhost:8080

## Building local images

- Build `signomix-ta-ps`
- Build `signomix-database`
- Build `signomix-proxy`
- Build `signomix` with:

```
$ mvn package
$ docker build -t signomix:local .
```

## Running local images

The services that make up the platform are launched using [Docker Compose](https://docs.docker.com/compose/):

```
$ docker-compose -f docker-compose.yml -f docker-compose.local.yml up -d
```

To stop the platform:

```
$ docker-compose -f docker-compose.yml -f docker-compose.local.yml down
```

> The next versions and the target architecture of Signomix will be available in a separate repository: `signomix-ta`.

