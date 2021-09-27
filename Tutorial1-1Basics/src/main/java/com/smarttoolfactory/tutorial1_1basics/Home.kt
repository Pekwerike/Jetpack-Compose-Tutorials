package com.smarttoolfactory.tutorial1_1basics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.*
import com.smarttoolfactory.tutorial1_1basics.model.TutorialSectionModel
import com.smarttoolfactory.tutorial1_1basics.ui.components.TutorialSectionCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal val tabList = listOf("Components", "Layout", "State", "Gestures", "Theming")

/**
 * This is Home tab for this tutorials
 */
@OptIn(ExperimentalPagerApi::class)
@ExperimentalAnimationApi
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    navigateToTutorial: (String) -> Unit,
    state: SearchState = rememberSearchState()
) {

    println("🤔 HomeScreen() state:\n$state")

    Column(
        modifier = modifier.fillMaxSize()
    ) {

        SearchBar(
            query = state.query,
            onQueryChange = { state.query = it },
            onSearchFocusChange = { state.focused = it },
            onClearQuery = { state.query = TextFieldValue("") },
            searching = state.searching,
            focused = state.focused,
            modifier = modifier
        )

        LaunchedEffect(state.query.text) {
            state.searching = true
            println("⚠️ HomeScreen() LaunchedEffect query: ${state.query.text}, searching: ${state.searching}")
            delay(100)
            state.searchResults = viewModel.getTutorials(state.query.text)
            state.searching = false
        }

        when (state.searchDisplay) {
            SearchDisplay.InitialResults -> {
                HomeContent(tabList, modifier, viewModel.componentTutorialList, navigateToTutorial)
            }
            SearchDisplay.NoResults -> {
                Text("No Results")
            }

            SearchDisplay.Suggestions -> {
                Text("Suggestions")
            }

            SearchDisplay.Results -> {
                TutorialListContent(modifier, state.searchResults, navigateToTutorial)
            }
        }
    }
}

@ExperimentalPagerApi
@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun HomeContent(
    pages: List<String>,
    modifier: Modifier,
    tutorialList: List<TutorialSectionModel>,
    navigateToTutorial: (String) -> Unit
) {

    val pagerState: PagerState = rememberPagerState(
        pageCount = pages.size,
        initialOffscreenLimit = 2
    )
    val coroutineScope = rememberCoroutineScope()

    ScrollableTabRow(
        backgroundColor = MaterialTheme.colors.surface,
        contentColor = MaterialTheme.colors.onSurface,
        edgePadding = 8.dp,
        // Our selected tab is our current page
        selectedTabIndex = pagerState.currentPage,
        // Override the indicator, using the provided pagerTabIndicatorOffset modifier
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
            )
        }
    ) {
        // Add tabs for all of our pages
        pages.forEachIndexed { index, title ->
            Tab(
                text = { Text(title) },
                selected = pagerState.currentPage == index,
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
            )
        }
    }

    HorizontalPager(state = pagerState) { page: Int ->
        when (page) {
            0 -> TutorialListContent(modifier, tutorialList, navigateToTutorial)
            else -> ComingSoonScreen()
        }
    }
}


@Composable
fun ComingSoonScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "⚠️ Under Construction!", fontSize = 28.sp, fontWeight = FontWeight.Bold)
    }
}

@ExperimentalAnimationApi
@Composable
fun TutorialListContent(
    modifier: Modifier = Modifier,
    tutorialList: List<TutorialSectionModel>,
    navigateToTutorial: (String) -> Unit
) {

    val scrollState = rememberLazyListState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xffEEEEEE)
    ) {
        // List of Tutorials
        LazyColumn(
            state = scrollState,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = {

                items(tutorialList) { item: TutorialSectionModel ->

                    var isExpanded by remember(key1 = item.title) { mutableStateOf(item.expanded) }

                    TutorialSectionCard(
                        model = item,
                        onClick = {
                            navigateToTutorial(item.title)
                        },
                        onExpandClicked = {
                            item.expanded = !item.expanded
                            isExpanded = item.expanded
                        },
                        expanded = isExpanded
                    )
                }
            }
        )
    }
}