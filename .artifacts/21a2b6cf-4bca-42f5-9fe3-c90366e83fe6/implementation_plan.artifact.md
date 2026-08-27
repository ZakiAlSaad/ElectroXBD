# Fix corrupted root build.gradle.kts

The root `build.gradle.kts` file is corrupted (contains null bytes). I will restore it with the standard plugin declarations based on the `libs.versions.toml` file.

## User Review Required

> [!IMPORTANT]
> The root `build.gradle.kts` was found to be corrupted. I am restoring it with standard boilerplate. If you had custom logic there, it might be lost.

## Proposed Changes

### Root Project

#### [MODIFY] [build.gradle.kts](file:///A:/ElectroXBD/build.gradle.kts)
Replace corrupted content with standard plugin declarations.

## Verification Plan

### Automated Tests
- Run `./gradlew help` to ensure the project can be synchronized and the build files are valid.
