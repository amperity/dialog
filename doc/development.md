Development
===========

Dialog uses Clojure's CLI tooling, `deps.edn`, and `tools.build` for development.


## REPL

To start a basic REPL, use the bin script:

```bash
bin/repl
```


## Run Tests

To test-compile the code and find any reflection warnings:

```bash
bin/test check
```

Tests are run with [kaocha](https://github.com/lambdaisland/kaocha) via a bin script:

```bash
# run tests once
bin/test

# watch and rerun tests
bin/test --watch
```

To compute test coverage with [cloverage](https://github.com/cloverage/cloverage):

```bash
bin/test coverage
```


## Build Jar

For compiling code and building a JAR file, dialog uses `tools.build`. The
various commands can be found in the [`build.clj`](../build.clj) file and
invoked with the `-T:build` alias or the bin script:

```bash
# clean artifacts
bin/build clean

# create a jar
bin/build jar

# install to local repo
bin/build install

# deploy to Clojars
bin/build deploy
```
