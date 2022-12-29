Configuration
=============

At initialization time, dialog will try to read configuration from a resource
named `dialog.edn` or `dialog/config.edn`. This is an
[aero](https://github.com/juxt/aero/) file which should contain a map telling
dialog how to behave. The profile used to read the configuration can be set
with the `dialog.profile` system property or `DIALOG_PROFILE` environment
variable at runtime.

Check out the [sample config](../dev/dialog.edn) in this repo for a comprehensive
example.


## Logger Levels

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


## Initialization Hook

Aero is quite expressive, but in the event that you need to run some logic to
pre-configure the logging system (or perform init-time side-effects), you can
specify a fully-qualified symbol under the `:init` key in the config. This will
be resolved to a function and called with the raw config map. The result of the
function will be used as a modified version of the config.


## Middleware

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


## Event Outputs

Finally, dialog needs to know how and where to send the logged events. These
are configured under the `:outputs` key, which should contain a map from an
output-id keyword to a configuration for that output.

There are two significant configurations for an output - its `:type` and its
`:format`. The output format is used to translate events into message strings,
and the output type is used to write the formatted message to some destination.
As a shorthand, an output with no other configuration can be specified just by
its type keyword.

### Output Types

There are four built-in outputs:

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

If you need an output that is not already provided, you can write your own by
specifying a qualified symbol for the output `:type`. During initialization,
this will be resolved to a var and called with the output map to construct the
writer function. Check out the existing output types for examples.

### Message Formats

There are four built-in formats:

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

Similar to the output writers, if you want more control over the format you can
provide your own function by specifying a qualified symbol as the `:format`.
This will be resolved to a var during initialization and called on events to
produce the message string.

### Output Levels

If you need to override the global levels for events on specific outputs, you
can provide the same `:level` and `:levels` settings [described above](#logger-levels).
These will let you adjust the thresholds for events being written to each
specific output, but note the following caveats:

- You can only raise logger thresholds, not lower them - if an event is not
  logged because it doesn't meet the global threshold for that logger, it will
  never be sent to the outputs for evaluation.
- Output levels do not have the same performance optimizations as the global
  levels, because the results are not cached and the event has to have made it
  from the SLF4J framework into a Clojure event map and handed to Dialog.
- You cannot adjust output levels dynamically, short of reloading the whole
  config. (This is why there is no `:blocked` equivalent.)
