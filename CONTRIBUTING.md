# Contributing to OnlyMusic

Contributions, suggestions, and bug reports are welcome. OnlyMusic is an Android music player built with Kotlin and Jetpack Compose, organized as a Gradle multi-module project. These guidelines aim to keep changes focused, reviewable, and integrated frequently using trunk-based development (TBD).

## Trunk-based workflow

`main` is the trunk and the integration point for ongoing work. Use a short-lived branch for each focused change, then open a pull request against `main` as soon as the change is ready for review.

1. Start from the latest `main`:

   ```bash
   git switch main
   git pull --ff-only
   ```

2. Create a small, task-specific branch. Follow the repository's existing prefixes where practical, such as `feat/` for features and `docs/` for documentation:

   ```bash
   git switch -c feat/describe-your-change
   ```

3. Keep the branch focused on one issue or cohesive improvement. Prefer small commits and integrate promptly; avoid long-lived branches and broad, unrelated refactors.
4. Open a pull request against `main`, link the relevant issue when there is one, and explain the user-visible or technical impact. Keep the pull request small enough to review and address feedback promptly.
5. After merge, remove the branch. If work needs more time, split it into smaller increments that can be reviewed and merged independently. Coordinate incomplete or user-facing changes with reviewers rather than leaving a branch diverged for a long time.

Do not push directly to `main`; it is protected. A fork-based workflow is also fine: create the short-lived branch in your fork and open a pull request from it to this repository's `main`.

## Project-specific guidance

- **Choose the module that owns the change.** `features/search` and `features/player` contain feature UI and view-model code; `data` contains repositories and Room persistence; `core` contains shared UI and core components; `app` contains application setup, navigation, and the player service. Avoid putting feature-specific behavior in shared modules unless it is genuinely shared.
- **Preserve module boundaries.** If a change crosses modules, keep dependencies flowing through the existing Gradle/module structure and make the smallest necessary interface changes.
- **Test behavior at the right level.** Add or update unit tests alongside the affected module's `src/test` code. For UI or device-dependent behavior, consider the module's `src/androidTest` tests. Playback and search depend on network services and NewPipe Extractor, so prefer deterministic tests with fakes over relying on live upstream services.
- **Keep persistence changes deliberate.** When changing Room entities or database structure in `data`, review the checked-in schemas under `data/schemas` and update/add the generated schema as appropriate. Consider migration and existing user data behavior.
- **Follow existing Kotlin and Compose conventions.** Use the project's Detekt configuration (`detekt.yml`); keep composables focused and preserve existing UI behavior, accessibility, and loading/error states.
- **Be mindful of upstream services.** NewPipe Extractor-backed search and playback can be affected by service changes. Handle failures gracefully and avoid presenting upstream availability as guaranteed.

## Build and verification

The project uses the Gradle wrapper and Java 21 in CI. Before opening a pull request, run the checks relevant to your change; for Kotlin or application code, run:

```bash
./gradlew detekt --continue
./gradlew testDebugUnitTest --continue
```

For changes that affect app assembly or resources, also run:

```bash
./gradlew assembleDebug
```

The CI workflow runs Detekt and unit tests for changes under `app/`, `core/`, `data/`, `features/`, and selected Gradle files. Documentation-only changes may not trigger that workflow, so review Markdown carefully before submitting.

## Reporting bugs

Include the Android version, device or emulator model, application version or commit, steps to reproduce, expected and actual behavior, and relevant logs or screenshots. For search or playback issues, mention whether the problem is reproducible with another query or track and whether the upstream service appears available. Do not include private data, credentials, or copyrighted media files in reports.
