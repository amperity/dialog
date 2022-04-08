Change Log
==========

All notable changes to this project will be documented in this file, which
follows the conventions of [keepachangelog.com](http://keepachangelog.com/).
This project adheres to [Semantic Versioning](http://semver.org/).


## [Unreleased]

### Changed
- Remove shell call to `hostname` to infer the default computer hostname. This
  caused the Clojure Agent send-off pool to be started at logging init time,
  which was an unacceptable side-effect.

### Added
- The default hostname can now be modified with `dialog.util/set-hostname!` if
  necessary.


## [0.3.1] - 2022-02-25

### Fixed
- Use SLF4J message formatter to correctly interpret formatting anchors (`{}`)
  and throwables in the `DialogLogger` implementation.
  [#6](https://github.com/amperity/dialog/issues/6)
  [PR#7](https://github.com/amperity/dialog/pull/7)


## [0.3.0] - 2022-02-22

### Added
- Add support for output-specific middleware.
  [PR#4](https://github.com/amperity/dialog/pull/4)

### Changed
- The JSON formatter is much more defensive about unknown types and will
  default to stringifying them when found.
  [PR#5](https://github.com/amperity/dialog/pull/5)


## [0.2.0] - 2022-01-14

### Added
- Added `:padding` option for the `:simple` and `:pretty` formatters to allow
  control of the fixed-width printing for the thread, level, and logger names.
  [PR#2](https://github.com/amperity/dialog/pull/2)

### Changed
- The canonical configuration file is now `dialog.edn` instead of
  `dialog/config.edn`, though the original path is still loaded as a fallback.

### Fixed
- Ensure that dialog is initialized when events are logged. This fixes
  standalone usage without SLF4J.
- Logger instances are now serializable, to support cases where a class being
  serialized declares a non-static logger field.
  [PR#3](https://github.com/amperity/dialog/pull/3)


## 0.1.0 - 2021-12-29

Initial release.


[Unreleased]: https://github.com/amperity/ken/compare/0.3.1...HEAD
[0.3.1]: https://github.com/amperity/ken/compare/0.3.0...0.3.1
[0.3.0]: https://github.com/amperity/ken/compare/0.2.0...0.3.0
[0.2.0]: https://github.com/amperity/ken/compare/0.1.0...0.2.0
