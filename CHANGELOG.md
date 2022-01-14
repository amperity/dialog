Change Log
==========

All notable changes to this project will be documented in this file, which
follows the conventions of [keepachangelog.com](http://keepachangelog.com/).
This project adheres to [Semantic Versioning](http://semver.org/).


## Unreleased

### Added
- Added `:padding` option for the `:simple` and `:pretty` formatters to allow
  disabling fixed-width printing for the thread, level, and logger names.

### Changed
- The canonical configuration file is now `dialog.edn` instead of
  `dialog/config.edn`, though the original path is still loaded as a fallback.

### Fixed
- Ensure that dialog is initialized when events are logged. This fixes
  standalone usage without SLF4J.


## 0.1.0 - 2021-12-29

Initial release.


[Unreleased]: https://github.com/amperity/ken/compare/0.1.0...HEAD
