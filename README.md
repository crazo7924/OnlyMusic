# OnlyMusic

OnlyMusic is a modern music player with a beautiful, responsive UI built using Jetpack Compose. It provides fast song search and playback, and integrates TeamNewPipe's extractor library to source audio — a big shoutout to TeamNewPipe for their work.

## Key features
- Beautiful, Compose-driven UI and smooth animations
- Search for songs and play them directly
- Start a "song radio" from the search results list (instant related-track radio)
- Uses TeamNewPipe's extractor library (available on JitPack) for content extraction
- Lightweight, easy to use, and designed with extensibility in mind
- Many more features planned — stay tuned!

## Screenshots
TODO: Place screenshots here — add images from `/assets` or from a screenshot URL

## Getting started

Notes
- The extractor library is provided by TeamNewPipe and distributed via JitPack. Please consult their repository or JitPack page for the most up-to-date artifact coordinates and licensing.
- Playback implementation details (e.g., ExoPlayer) are internal — if you plan to extend playback, follow established Android media playback patterns.

## How to use
- Open the app
- Use the search bar to find songs
- Tap a result to play the track
- From the search results list, you can start a "song radio" by long pressing for the context menu options to play related tracks continuously

## Contributing
Contributions, ideas, and feature requests are welcome. Planned improvements include:
- Improved offline playback and caching
- Playlists and queue management
- User preferences and theming options
- Enhanced discovery and recommendations

If you'd like to contribute:
1. Fork the repo
2. Create a branch for your feature/fix
3. Open a pull request describing your changes

## Acknowledgements
- TeamNewPipe — for the extractor library (available on JitPack). Huge thanks for their open work.
- Jetpack Compose — for making beautiful UI development on Android so much easier.

## License
This project is available under the GNU GPLv3 License. See the LICENSE file for details.
