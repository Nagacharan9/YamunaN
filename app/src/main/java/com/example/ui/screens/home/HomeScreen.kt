package com.example.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.PicsumImage
import com.example.ui.components.AuthorFilterChips
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ErrorStateView
import com.example.ui.components.GalleryImageCard
import com.example.ui.components.SearchBarComponent
import com.example.ui.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onImageClick: (PicsumImage) -> Unit
) {
    val filteredImages by viewModel.filteredImages.collectAsState()
    val allImages by viewModel.allImages.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val authorFilter by viewModel.authorFilter.collectAsState()
    val isLoadingFirstPage by viewModel.isLoadingFirstPage.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val galleryError by viewModel.galleryError.collectAsState()

    var searchInput by remember { mutableStateOf("") }
    val gridState = rememberLazyGridState()
    val refreshState = rememberPullToRefreshState()

    // Detect when user scrolls near the end to trigger infinite pagination
    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItemCount = gridState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItemCount > 0 && lastVisibleItemIndex >= totalItemCount - 4
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && searchInput.isBlank()) {
            viewModel.loadMoreImages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Explore Gallery",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (allImages.isNotEmpty()) "${filteredImages.size} photos available" else "Picsum curation",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshImages() },
                        enabled = !isRefreshing && !isLoadingFirstPage,
                        modifier = Modifier.testTag("gallery_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh Gallery"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshImages() },
            state = refreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Search and Filter Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    SearchBarComponent(
                        query = searchInput,
                        onQueryChange = {
                            searchInput = it
                            viewModel.setSearchQuery(it)
                        },
                        placeholder = "Search by author (e.g. Alejandro)...",
                        testTag = "home_search_input"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    AuthorFilterChips(
                        selectedFilter = authorFilter,
                        onFilterSelected = { viewModel.setAuthorFilter(it) }
                    )
                }

                // Error State
                if (galleryError != null && allImages.isEmpty()) {
                    ErrorStateView(
                        message = galleryError ?: "Network error",
                        onRetry = { viewModel.refreshImages() },
                        modifier = Modifier.padding(16.dp)
                    )
                } else if (isLoadingFirstPage) {
                    // Initial Loading Skeleton
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("gallery_loading_indicator"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(strokeWidth = 3.dp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Loading beautiful imagery...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else if (filteredImages.isEmpty()) {
                    EmptyStateView(
                        title = "No Images Found",
                        description = if (searchInput.isNotEmpty()) "No photos found matching '$searchInput'. Try another search term or filter."
                        else "No images match the selected filter.",
                        icon = Icons.Filled.ImageSearch,
                        actionButtonText = if (searchInput.isNotEmpty()) "Clear Search" else "Reset Filter",
                        onActionClick = {
                            searchInput = ""
                            viewModel.setSearchQuery("")
                            viewModel.setAuthorFilter(com.example.data.repository.AuthorFilter.ALL)
                        }
                    )
                } else {
                    // Image Grid with 2 columns
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        state = gridState,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("gallery_grid")
                    ) {
                        items(
                            items = filteredImages,
                            key = { it.id }
                        ) { image ->
                            GalleryImageCard(
                                image = image,
                                isFavorite = favoriteIds.contains(image.id),
                                onFavoriteToggle = { viewModel.toggleFavorite(image) },
                                onCardClick = { onImageClick(image) }
                            )
                        }

                        // Infinite scrolling footer loader
                        if (isLoadingMore && searchInput.isBlank()) {
                            item(span = { GridItemSpan(2) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Loading more photos...",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
