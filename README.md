# SIGNOMIX

**IoT & data management platform**

The next versions and the target architecture of Signomix will be available in a separate repository: signomix-ta

Services forming the platform:

- [signomix](https://github.com/signomix/signomix) - this repository
- [signomix-ta-ps](https://github.com/signomix/signomix-ta-ps)
- [signomix-database](https://github.com/signomix/signomix-database)
- [signomix-proxy](https://github.com/signomix/signomix-proxy)

## Building

- Build `signomix-ta-ps`
- Build `signomix-database`
- Build `signomix-proxy`
- Build `signomix` with:

```
$ mvn package
$ docker build -t signomix-main:latest .
```

## Running

The services that make up the platform are launched using [Docker Compose](https://docs.docker.com/compose/):

```
$ docker-compose up -d
```

To stop the platform:

```
$ docker-compose down
```

