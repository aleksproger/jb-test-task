**Setup**

- The CLI is created for usage in macOS environment with arm64 architecture. In order to run it on x86-64 architecture, please fix DependencyManager build.gradle.kts.
- In order to run tool for default dependency `org.jfrog.buildinfo:build-info-extractor-gradle:4.20.0` just run it wit no arguments
- To provide custom dependency, please run tool with following arguments: `--dependency <dependency>`
- Only supports .jar dependencies