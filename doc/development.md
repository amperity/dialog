Development
===========

Dialog uses Clojure's CLI tooling, `deps.edn`, and `tools.build` for development.


## REPL

To start a basic REPL, use `clj`:

```bash
clj -M:repl
```


## Run Tests

To test-compile the code and find any reflection warnings:

```bash
bin/check
```

Tests are run with [kaocha](https://github.com/lambdaisland/kaocha) via a bin script:

```bash
# run tests once
bin/test

# watch and rerun tests
bin/test --watch
```
