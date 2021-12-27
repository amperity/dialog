Dialog
======

[![CircleCI](https://circleci.com/gh/amperity/dialog.svg?style=shield&circle-token=XXX)](https://circleci.com/gh/amperity/dialog)
[![codecov](https://codecov.io/gh/amperity/dialog/branch/main/graph/badge.svg)](https://codecov.io/gh/amperity/dialog)
[![cljdoc badge](https://cljdoc.org/badge/com.amperity/dialog)](https://cljdoc.org/d/com.amperity/dialog/CURRENT)

Dialog is a simple, opinionated, just-enough logging library that implements an
SLF4J-compatible logging backend in Clojure. This means it is compatible with a
wide variety of logging APIs, including `clojure.tools.logging`.

Inspired by the fallout from the [log4shell](https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2021-44228)
vulnerability, and the question "what if we wrote _just enough_ code to do the
kinds of logging we want?". This is an attempt to produce a
minimally-configurable logging backend that is nonetheless still extensible in
a few ways - in particular, it aims to be easy to integrate with
[ken](https://github.com/amperity/ken) as an extension to observability
tooling.


## Usage

Releases are published on Clojars; to use the latest version with Leiningen,
add the following to your project dependencies:

[![Clojars Project](http://clojars.org/com.amperity/dialog/latest-version.svg)](http://clojars.org/com.amperity/dialog)

**TODO:** more usage information


## License

Copyright © 2021 Amperity, Inc.

Distributed under the MIT License.
