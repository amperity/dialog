Dialog
======

[![CircleCI](https://circleci.com/gh/amperity/dialog.svg?style=shield&circle-token=33a22acf23de36febc517ba16a26d33fefec0a7c)](https://circleci.com/gh/amperity/dialog)
[![codecov](https://codecov.io/gh/amperity/dialog/branch/main/graph/badge.svg)](https://codecov.io/gh/amperity/dialog)
[![cljdoc badge](https://cljdoc.org/badge/com.amperity/dialog)](https://cljdoc.org/d/com.amperity/dialog/CURRENT)

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

Releases are published on Clojars; to use the latest version with Leiningen,
add the following to your project dependencies:

[![Clojars Project](http://clojars.org/com.amperity/dialog/latest-version.svg)](http://clojars.org/com.amperity/dialog)

By default, this will pull in the SLF4J API package as well as redirecting
`java.util.logging` and `commons-logging` classes to SLF4J. If you're using
libraries developed against log4j, you may also want to add a dependency on
`org.slf4j/log4j-over-slf4j` as well. For more information, see the
[SLF4J legacy bridging](https://www.slf4j.org/legacy.html) documentation.

Within your code, you can use the standard `clojure.tools.logging` approach to
logging and things should Just Work. If for some reason you don't want to pull
in `org.clojure/tools.logging`, the `dialog.logger` namespace contains a set of
compatible macros such as `logp`, `debug`, `infof`, etc.

The `dialog.logger` namespace also contains a number of utility functions which
can be used to inspect and dynamically adjust the logging configuration at
runtime.


## Configuration

At initialization time (when SLF4J looks on the classpath for the
`StaticLoggerBinder` class), dialog will try to read configuration from a
resource named `dialog.edn`. This is an [aero](https://github.com/juxt/aero/)
file which should contain a map telling dialog how to behave. The profile used
to read the configuration can be set with the `dialog.profile` system
property or `DIALOG_PROFILE` environment variable at runtime.

Check out the [sample config](dev/dialog.edn) in this repo for a comprehensive
example.

### Initialization Hook

Aero is quite expressive, but in the event that you need to run some logic to
pre-configure the logging system (or perform init-time side-effects), you can
specify a fully-qualified symbol under the `:init` key in the config. This will
be resolved to a function and called with the raw config map. The result of the
function will be used as a modified version of the config.

### Logger Levels

Dialog supports the standard set of log levels, specified with keywords:
`:trace`, `:debug`, `:info`, `:warn`, `:error`, and `:fatal` in ascending order
of severity. Each logger may have a threshold and will only report events
logged at that threshold or higher. These thresholds can be controlled with a
few keys in the config map:

- `:level`

  This sets the level of the root logger, which is used if no other
  more-specific threshold is configured. This may be overridden at runtime by
  setting the `dialog.level` system property or the `DIALOG_LEVEL` environment
  variable.

- `:levels`

  This key should contain a map of logger prefixes to a configured level
  keyword. Loggers will use the _most specific_ prefix which matches when
  determining their level. These may also be overridden at runtime with
  `dialog.level.{logger.ns}` system properties or `DIALOG_LEVEL_{LOGGER_NS}`
  environment variables.

- `:blocked`

  An optional set of logger prefixes which should **never** be logged. This can
  be useful as a security mechanism, to prevent output from loggers like
  `org.apache.http.headers`, which can leak `Authorization: Bearer ...`
  secrets.

### Middleware

The `:middleware` entry in the config can contain a vector of "middleware
functions", which will be called in order on incoming events to process them
before they are output. This allows for local extensions to the logging system,
such as collecting additional callsite context, applying custom formatting,
filtering out unwanted events, and more.

Each symbol in the vector is resolved to a var when the config is loaded, and
will be called with the full config map and the event being processed. The
function should return an updated event if logging should proceed, or nil if
the event should be dropped. A middleware call which throws an exception will
be ignored, and the original event will continue being processed.

Top-level middleware will be applied to all events; middleware may also be
applied to individual outputs for more specific processing.

### Event Outputs

Finally, dialog needs to know how and where to send the logged events. These
are configured under the `:outputs` key, which should contain a map from an
output-id keyword to a configuration for that output.

There are two significant configurations for an output - its `:type` and its
`:format`. The output format is used to translate events into message strings,
and the output type is used to write the formatted message to some destination.
As a shorthand, an output with no other configuration can be specified just by
its type keyword.

#### Output Types

There are four supported outputs:

- `:null`

  A null output which drops all events logged to it. Most useful for testing.

- `:print`

  An output which writes log messages to an output stream. By default, this is
  the process' standard output, but may be explicitly set by adding a `:stream`
  key to the output with a value of `:stdout` or `:stderr`.

- `:file`

  An output which writes log messages to a local file. The output must contain
  a `:path` entry which specifies the location of the log file. Dialog will try
  to pre-create the parent directories of this file when it is initialized.

- `:syslog`

  This output writes log messages to a [syslog](https://en.wikipedia.org/wiki/Syslog)
  daemon using UDP. By default messages will be sent to the local host on port
  514, but this may be controlled with the `:address` and `:port` keys on the
  output. The syslog output sends the `:time`, `:host`, `:sys`, `:proc`, and
  `:level` event attributes as special fields, in addition to the formatted
  message.

#### Message Formats

There are four supported formats:

- `:message`

  A trivial formatter which just uses the event's `:message` value, verbatim.

- `:simple`

  The simple formatter is the default, and produces a log message from several
  attributes of the event, including the time, level, logger name, and message.
  This is a good format to use for deployed systems which need to log to stdout
  or a file.

- `:pretty`

  This formatter is a lot like `:simple`, but it adds ANSI color coding to the
  messages, making them much easier for humans to consume. This is most useful
  in interactive situations like the REPL. For a slightly nicer experience, you
  can also add `:timestamp :short` to the output map to only show the time
  portion instead of the full date-time string.

- `:json`

  This renders the event data directly as JSON, which can be useful for outputs
  which will be read by structured logging tools. If you need custom formatting
  for certain types, you can transform them with middleware before they are
  output.

To support further light customization, both the `:simple` and `:pretty`
formatters will look for some additional metadata on events:
- If an event has a `:dialog.format/tail` string metadata, it will be appended
  to the message produced by the formatters.
- If the event has `:dialog.format/extra`, it will also be printed as a data
  structure at the end of the message.


## License

Copyright © 2021 Amperity, Inc.

Distributed under the MIT License.
