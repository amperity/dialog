Change Log
==========

All notable changes to this project will be documented in this file, which
follows the conventions of [keepachangelog.com](http://keepachangelog.com/).
This project adheres to [Semantic Versioning](http://semver.org/).


## [Unreleased]

...


## [1.1.103] - 2023-01-05

### Added
- Custom output writers and formatters can be used by providing a symbol for
  the output's `:type` and `:format` keys. The symbol will be resolved to a var
  at config time and called with the output map.
  [PR#20](https://github.com/amperity/dialog/pull/20)
- Outputs support customized logger levels via the `:level` and `:levels`
  options, mirroring the global config levels.
  [PR#31](https://github.com/amperity/dialog/pull/31)

### Changed
- Patch version numbers now count the total number of commits to the repository.
- Logger level matching rewritten to be approximately 6x faster.

### Fixed
- Cached logger levels are reset when configuration is re-initialized.
  [#26](https://github.com/amperity/dialog/issues/26)
  [PR#28](https://github.com/amperity/dialog/pull/28)


## [1.0.1] - 2022-06-01

No major behavioral changes in this release, but it does mark the "officially
stable" version of the library after much production usage.

### Changed
- Updated a handful of dependencies.
  [PR#9](https://github.com/amperity/dialog/pull/9)

### Fixed
- Fixed a `ClassCastException` when setting the root logger level with an
  environment or system property override.
  [PR#10](https://github.com/amperity/dialog/pull/10)


## [0.3.2] - 2022-04-08

### Changed
- Remove shell call to `hostname` to infer the default computer hostname. This
  caused the Clojure Agent send-off pool to be started at logging init time,
  which was an unacceptable side-effect.
  [PR#8](https://github.com/amperity/dialog/pull/8)

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


[Unreleased]: https://github.com/amperity/dialog/compare/1.1.103...HEAD
[1.1.103]: https://github.com/amperity/dialog/compare/1.0.1...1.1.103
[1.0.1]: https://github.com/amperity/dialog/compare/0.3.2...1.0.1
[0.3.2]: https://github.com/amperity/dialog/compare/0.3.1...0.3.2
[0.3.1]: https://github.com/amperity/dialog/compare/0.3.0...0.3.1
[0.3.0]: https://github.com/amperity/dialog/compare/0.2.0...0.3.0
[0.2.0]: https://github.com/amperity/dialog/compare/0.1.0...0.2.0
