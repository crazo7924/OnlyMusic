# Test Cases Suggestions for OnlyMusic

This document outlines the suggested test cases for the various modules in the OnlyMusic application. As requested, we focus on suggesting tests first, organized by their relevant components. The plan assumes using MockK for Unit Tests (mocking dependencies) and Espresso/Compose Test Rule for UI testing.

---

## 1. Module: `:data`

### `MusicRepository` Implementations
**Component: `NewPipeMusicRepository` (Remote Data Source)**
*   **Test Case:** `search_validQuery_returnsFlowOfResults`: Verify that `search()` returns a flow emitting `Result.success` containing `MediaListItem`s when the downstream extractor returns valid items. Mock the inner `YoutubeMusicSearchExtractor` (or wrap it) if possible, or perform an integration test with a real simple query.
*   **Test Case:** `search_emptyResults_returnsEmptyFlowOrHandledError`: Verify behavior when a query yields no results.
*   **Test Case:** `loadMediaUri_validStreamUri_returnsResultSuccess`: Verify that parsing a valid stream URI returns the correct `MediaListItem` with `InfoType.STREAM`.
*   **Test Case:** `loadPlaylistUri_validPlaylistUri_returnsFlowOfResults`: Verify that extracting a playlist returns a flow of multiple `MediaListItem` elements.
*   **Test Case:** `loadAutoPlaylistUri_validVideoId_returnsFlowOfResults`: Verify extracting the "mix" playlist for a given track ID returns related media items.

**Component: `CachingMusicRepository` (Decorator/Repository)**
*   **Test Case:** `search_cachesResultsInDatabase`: Verify that calling `search()` fetches results from the `remoteRepository`, and for every successful item, it calls `songDao.insertSong` and `playlistDao.insertSongToPlaylist` (adding it to the "recent" playlist). Use MockK to mock `remoteRepository`, `songDao`, and `playlistDao`.
*   **Test Case:** `search_emitsLocalRecentSongs`: Verify that after fetching from remote, it also calls `playlistDao.getRecentSongs()` and emits those local items in the Flow.

**Component: Database DAOs (`PlaylistDao`, `SongDao`)**
*   **Note:** Usually tested via Robolectric or Android Instrumented tests using Room's in-memory database.
*   **Test Case:** `insertSong_and_retrieveIt`: Verify a `Song` entity can be inserted into `SongDao` and retrieved.
*   **Test Case:** `createPlaylist_and_getPlaylist`: Verify inserting a playlist sets its type to 'LOCAL' and it can be fetched.
*   **Test Case:** `insertSongToPlaylist_getRecentSongs`: Verify inserting a cross-reference between a song and the 'recent' playlist allows retrieval via `getRecentSongs()`.

---

## 2. Module: `:features:search`

### `SearchViewModel`
*   **Test Case:** `updateQueryFrom_updatesUiStateQuery`: Verify that calling `updateQueryFrom("test")` correctly updates the `uiState.query`.
*   **Test Case:** `search_queryLengthLessThanMin_resetsState`: Verify that if `search()` is called and `query.length < minQueryLength`, `searchState` becomes `INITIAL` and `suggestions` list is cleared.
*   **Test Case:** `search_validQuery_updatesStateToLoadingThenSuccess`: Verify that with a valid query, the state changes to `SEARCHING`, triggers `musicRepository.search(query)` (mocked with MockK), updates to `LOADING` as items are emitted, and finally updates to `SUCCESS` after collection finishes.
*   **Test Case:** `search_noResults_updatesStateToError`: Verify that if `musicRepository.search(query)` emits nothing (empty suggestions), the `searchState` transitions to `ERROR`.

### `SearchUI` (Compose UI Tests / Espresso)
*   **Test Case:** `SearchUI_initialState_showsPlaceholderText`: When `SearchUiState.searchState == INITIAL`, verify the text "Search for music you love" is displayed.
*   **Test Case:** `SearchUI_searchingState_showsShimmerLoading`: When `SearchUiState.searchState == SEARCHING`, verify the shimmer loading components are visible.
*   **Test Case:** `SearchUI_successState_displaysSuggestions`: When `SearchUiState.searchState == SUCCESS` with mock suggestions, verify the `SearchList` renders list items with correct title and artist text.
*   **Test Case:** `SearchUI_clickOnSuggestion_triggersOnItemClicked`: Using Compose test rule, perform a click on a suggestion item and verify the `onItemClicked` callback is invoked with the correct `MediaListItem`.
*   **Test Case:** `SearchUI_longClickOnSuggestion_showsDropdownMenu`: Verify that long-clicking an item opens the DropdownMenu with "Enqueue", "Enqueue Next", and "Enqueue Radio" options.

---

## 3. Module: `:features:player`

### `PlayerViewModel`
*   **Test Case:** `updatePosition_updatesUiState`: Verify calling `updatePosition(1000)` updates `uiState.position` to `1000L`.
*   **Test Case:** `updateDuration_updatesUiState`: Verify calling `updateDuration(5000)` updates `uiState.duration` to `5000L`.
*   **Test Case:** `setError_updatesUiStatePlaybackStateAndMessage`: Verify calling `setError("Network Error")` sets `playbackState` to `ERROR` and `errorMessage` to "Network Error".
*   **Test Case:** `updateStateFromPlayer_mapsPlayerStateCorrectly`: Create a mock `androidx.media3.common.Player` (using MockK). Set its `playbackState` to `Player.STATE_READY` and `playWhenReady` to `true`. Verify `updateStateFromPlayer` maps `playbackState` to `PlaybackState.PLAYING`. Repeat for `STATE_BUFFERING` -> `LOADING`, `STATE_ENDED` -> `STOPPED`.
*   **Test Case:** `updateStateFromPlayer_extractsMediaItemsAndPosition`: Mock a `Player` with a current `Timeline`, `currentMediaItem`, and `currentPosition`. Verify the ViewModel's state correctly copies these values into the `PlayerUiState`.

### `PlayerUI` (Compose UI Tests / Espresso)
*   **Test Case:** `PlayerUI_rendersTitleAndArtist`: Given a mock `PlayerUiState` with a loaded media item, verify the title and artist text are visible.
*   **Test Case:** `PlayerUI_playbackControls_dispatchCallbacks`: Verify clicking the "Play/Pause" button invokes `onPlayPause()`. Verify clicking "Next" invokes `onPlayNext()`, and "Previous" invokes `onPlayPrevious()`.
*   **Test Case:** `PlayerUI_sliderSeeks_dispatchesOnSeekTo`: Interact with the slider and verify `onSeekTo` is triggered with the correct expected float value representing the duration position.

---

## 4. Module: `:app` (Main Application & Service)

### `PlayerService` (Media3 Service)
*   **Note:** Testing Android Services can be complex. Often best tested via an integration test using a `MediaController`.
*   **Test Case:** `onCustomCommand_LOAD_STREAM_URI_processesCorrectly`: Mock a `MediaSession` and `ControllerInfo`. Send a `COMMAND_LOAD_STREAM_URI` with a bundle containing a URI. Verify that `musicRepository.loadMediaUri` is called (using MockK) and the `exoPlayer` receives `setMediaItem`.
*   **Test Case:** `onCustomCommand_ENQUEUE_PLAYLIST_URI_addsItemsToExoPlayer`: Send `COMMAND_ENQUEUE_PLAYLIST_URI`. Mock `musicRepository.loadPlaylistUri` to return a flow of items. Verify `exoPlayer.addMediaItems` is invoked with the resulting list.
*   **Test Case:** `onCustomCommand_SEEK_TO_PERCENTAGE_calculatesPositionCorrectly`: Send `COMMAND_SEEK_TO_PERCENTAGE` with `0.5f`. Mock `exoPlayer.duration` to return `10000L`. Verify `exoPlayer.seekTo(5000L)` is called.

### `MediaControllerManager`
*   **Test Case:** `initialize_connectsToMediaSessionAndAddsListener`: Verify that calling `initialize()` creates a `MediaController` future, and once resolved, adds a listener that observes `Player` events.
*   **Test Case:** `PlayerListener_onEvents_isPlayingChanged_startsPositionUpdates`: Verify that when the `PlayerListener` receives `EVENT_IS_PLAYING_CHANGED` with `isPlaying = true`, it begins periodic calls to `playerViewModel.updatePosition`.

### `MainScreen` / `MainActivity` (Integration/UI Tests)
*   **Test Case:** `MainScreen_bottomSheet_expandsAndCollapses`: Verify the interaction between the bottom bar (mini-player) and the expanding BottomSheetScaffold containing the full `PlayerUI`.
*   **Test Case:** `MainScreen_searchToPlayerFlow`: (Integration) Type a query in `SearchUI`, mock a successful response, click an item, and verify the `MediaControllerManager` sends the `COMMAND_LOAD_STREAM_URI` to the service.
