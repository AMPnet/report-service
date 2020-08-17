# Report Service

[![codecov](https://codecov.io/gh/AMPnet/wallet-service/branch/master/graph/badge.svg)](https://codecov.io/gh/AMPnet/wallet-service)

Report service is a part of the AMPnet crowdfunding project.

## Start

Application is running on port: `8129`. To change default port set configuration: `server.port`.

### Build

```sh
./gradlew build
```

### Run

```sh
./gradlew bootRun
```

After starting the application, API documentation is available at: `localhost:8129/docs/index.html`.
If documentation is missing generate it by running gradle task:

```sh
./gradlew copyDocs
```

### Test

```sh
./gradlew test
```
