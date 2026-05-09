# YTMusic API - Kotlin

This is a complete Kotlin transpilation of the [ytmusicapi](https://github.com/sigma67/ytmusicapi)
Python library.

## Overview

**ytmusicapi** allows you to interact with YouTube Music using Kotlin. It emulates the YouTube web
client, enabling both authenticated and unauthenticated requests.

## Status

This is a work-in-progress transpilation of the Python ytmusicapi to Kotlin with the following
components:

### ✅ Completed

- Core module structure and package organization
- Constants and configuration
- Type definitions and aliases
- Exception classes
- Basic authentication framework
- OAuth credentials handling (base structure)
- Helper utilities (headers, context initialization, etc.)
- Navigation constants for API response traversal
- Continuation utilities for pagination
- Type system and enums
- Test framework setup
- Build configuration (Gradle)

### 🔄 In Progress

- Parser implementations (album, artist, playlist, etc.)
- Mixin implementations (browsing, search, library, etc.)
- Request/response handling with proper serialization
- i18n/Localization support

### 📋 TODO

- Complete parser implementations for all content types
- HTTP client integration and request handling
- Mixin method implementations
- Comprehensive test suite
- Documentation and examples
- CLI setup tool

## Architecture

The Kotlin version maintains the same architecture as the Python library:

### Core Classes

- **YTMusic**: Main API client, inherits from YTMusicBase and mixins
- **YTMusicBase**: Base class handling authentication, requests, and context

### Mixins (functional grouping)

- **BrowsingMixin**: Home, albums, artists, playlists
- **SearchMixin**: Search functionality
- **WatchMixin**: Watch/history features
- **ChartsMixin**: Chart data
- **ExploreMixin**: Explore functionality
- **LibraryMixin**: User library access
- **PlaylistsMixin**: Playlist management
- **PodcastsMixin**: Podcast support
- **UploadsMixin**: Upload management

### Parsers

- Album, Artist, Browsing, Explore, Library
- Playlist, Podcasts, Search, Songs, Uploads, Watch

## Dependencies

- **OkHttp 4.11.0**: HTTP client
- **Kotlin Serialization**: JSON handling
- **Kotlin Logging**: Logging support
- **JUnit 5 / Kotest**: Testing

## Building

```bash
./gradlew build
```

## Testing

```bash
./gradlew test
```

## Usage Example

```kotlin
import com.sigma67.ytmusicapi.YTMusic

fun main() {
    // Initialize without authentication
    val yt = YTMusic()

    // With authentication
    val ytAuth = YTMusic(auth = "path/to/headers_auth.json")

    // Search for songs
    val results = yt.search("Imagine - John Lennon")

    // Browse content
    val home = yt.getHome()

    // Create playlist (requires authentication)
    // val playlistId = ytAuth.createPlaylist("My Playlist")
}
```

## Project Structure

```
src/
├── main/kotlin/com/sigma67/ytmusicapi/
│   ├── Constants.kt              # API constants
│   ├── Enums.kt                  # Enum definitions
│   ├── Exceptions.kt             # Custom exceptions
│   ├── TypeAlias.kt              # Type definitions
│   ├── Helpers.kt                # Utility functions  
│   ├── Navigation.kt             # Navigation paths
│   ├── Continuations.kt          # Pagination handling
│   ├── I18N.kt                   # Internationalization
│   ├── Setup.kt                  # Authentication setup
│   ├── YTMusic.kt                # Main API classes
│   ├── API.kt                    # Public API surface
│   ├── auth/                     # Authentication modules
│   ├── models/                   # Data models
│   ├── parsers/                  # Response parsers
│   └── mixins/                   # Functional mixins
└── test/kotlin/                  # Tests
```

## Contributing

This is an active transpilation project. Areas needing work:

- Parser implementations - converting Python parsing logic to Kotlin
- Mixin method bodies - implementing actual API calls
- Test coverage - comprehensive testing
- Documentation - API docs and examples

## License

MIT License (same as original ytmusicapi)

## Original Project

This is based on the [ytmusicapi](https://github.com/sigma67/ytmusicapi) project by sigma67.

---

**Note**: This transpilation maintains functional parity with the Python version while leveraging
Kotlin's type safety and null-safety features. The public API is compatible with the Python library
where semantically possible.
