# genesis-notify

This project holds the code for the genesis notification 

# Introduction

## Project Structure

This project contains **client** and **server/jvm** sub-project which contain respectively the frontend and the backend code

### Server

The server code for this project can be found [here](./server/jvm/README.md).
It is built using a DSL-like definition based on the Kotlin language: GPAL.

## GSF Version

This project __in master__ relies on the latest `-SNAPSHOT` of GSF. This project is built nightly, and this nightly
version will be picked up on build. In case this behaviour needs to be overwritten, developers can add `USE_MVN_LOCAL`
as a gradle properties (either in ~/.gradle/gradle.properties, or in gradle.properties).

## Clients

Seeds may provide one or more clients, ie. web, mobile, desktop etc.

### Web Client

The web client for this project can be found [here](./client/web/README.md). It is built using Genesis's next
generation web development framework, which is based on Web Components.

# License

This is free and unencumbered software released into the public domain.

For full terms, see [LICENSE](./LICENSE)

**NOTE** This project uses licensed components listed in the next section, thus licenses for those components are required during development.

## Licensed components
Genesis low-code platform
