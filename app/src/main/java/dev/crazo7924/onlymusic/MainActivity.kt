package dev.crazo7924.onlymusic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.media3.exoplayer.ExoPlayer
import dev.crazo7924.onlymusic.player.PlayerViewModel
import dev.crazo7924.onlymusic.player.PlayerViewModelFactory
import dev.crazo7924.onlymusic.player.ui.PlayerUI
import dev.crazo7924.onlymusic.search.SearchViewModel
import dev.crazo7924.onlymusic.search.SearchViewModelFactory
import dev.crazo7924.onlymusic.search.data.NewPipeSearchRepository
import dev.crazo7924.onlymusic.search.ui.TopSearchBar
import dev.crazo7924.onlymusic.theme.OnlyMusicTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val player = ExoPlayer.Builder(this@MainActivity).build()
        val playerViewModel: PlayerViewModel by viewModels { PlayerViewModelFactory(player) }
        val searchViewModel: SearchViewModel by viewModels {
            SearchViewModelFactory(
                searchRepository = NewPipeSearchRepository(),
                minQueryLength = 3
            )
        }
        setContent {
            OnlyMusicTheme {
                val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
                BottomSheetScaffold(
                    modifier = Modifier.fillMaxSize(),
                    sheetContent = {
                        PlayerUI(
                            viewModel = playerViewModel,
                            modifier = Modifier
                                .fillMaxSize()
                                .systemBarsPadding()
                        )
                    },
                    sheetShape = RectangleShape,
                    sheetDragHandle = {
                        val expanded: Boolean =
                            when (bottomSheetScaffoldState.bottomSheetState.targetValue) {
                                SheetValue.Hidden -> true
                                SheetValue.Expanded -> false
                                SheetValue.PartiallyExpanded -> true
                            }
                        if (expanded) {
                            MediaListItem()
                        }
                    },
                    sheetPeekHeight = 64.dp,
                    scaffoldState = bottomSheetScaffoldState,
                ) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        val uiState = searchViewModel.uiState.collectAsState()
                        TopSearchBar(
                            query = uiState.value.query,
                            minQueryLength = searchViewModel.minQueryLength,
                            onQueryChange = { searchViewModel.updateQueryFrom(it) },
                            suggestions = uiState.value.suggestions,
                            onSearch = { searchViewModel.search() },
                            searchState = uiState.value.searchState
                        )
                    }
                }
            }
        }
    }
}