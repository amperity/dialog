Dialog
======

[![CircleCI](https://circleci.com/gh/amperity/dialog.svg?style=shield&circle-token=33a22acf23de36febc517ba16a26d33fefec0a7c)](https://circleci.com/gh/amperity/dialog)
[![codecov](https://codecov.io/gh/amperity/dialog/branch/main/graph/badge.svg)](https://codecov.io/gh/amperity/dialog)
[![cljdoc badge](https://cljdoc.org/badge/com.amperity/dialog)](https://cljdoc.org/d/com.amperity/dialog/CURRENT)
[![Clojars Project](https://img.shields.io/clojars/v/com.amperity/dialog.svg)](https://clojars.org/com.amperity/dialog)

Dialog is a simple and opinionated logging library that implements an
[SLF4J](https://www.slf4j.org/)-compatible backend in Clojure. This means it is
compatible with a wide variety of logging APIs, including
`clojure.tools.logging` and most Java libraries.

Inspired by the fallout from the [log4shell](https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2021-44228)
vulnerability, and the question "what if we wrote _just enough_ code to do the
kinds of logging we want?". This is an attempt to produce a
minimally-configurable logging backend that is nonetheless still extensible in
a few ways - in particular, it aims to be easy to integrate with
[ken](https://github.com/amperity/ken) as an extension to observability
tooling.


## Usage

Releases are published on Clojars. You can use the latest Maven coordinate, or
point at the main branch using `deps.edn`.

By default, this will pull in the SLF4J API package as well as redirecting
`java.util.logging`, `commons-logging`, and `log4j` classes to SLF4J. For more
information, see the [SLF4J legacy bridging](https://www.slf4j.org/legacy.html)
documentation.

Within your code, you can use the standard `clojure.tools.logging` approach to
logging and things should Just Work. If for some reason you don't want to pull
in `org.clojure/tools.logging`, the `dialog.logger` namespace contains a set of
compatible macros such as `logp`, `debug`, `infof`, etc.

The `dialog.logger` namespace also contains a number of utility functions which
can be used to inspect and dynamically adjust the logging configuration at
runtime.


## Configuration

At initialization time, dialog will try to read configuration from a resource
named `dialog.edn`. This is an [aero](https://github.com/juxt/aero/) file which
should contain a map telling dialog how to behave. See the
[configuration docs](doc/configuration.md) for more information, or check out
the [sample config](dev/dialog.edn) in this repo for a comprehensive example.


## Development

This library uses Clojure's CLI and `deps.edn` to manage and build the project.
See the [development docs](doc/development.md) for detailed instructions.


## License

Copyright © 2023 Amperity, Inc.

Distributed under the MIT License.
