package com.example.ui.screens.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.PicsumImage
import com.example.ui.components.EmptyStateView
import com.example.ui.components.GalleryImageCard
import com.example.ui.components.SearchBarComponent
import com.example.ui.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: AppViewModel,
    onImageClick: (PicsumImage) -> Unit,
    onNavigateToHome: () -> Unit
) {
    val filteredFavorites by viewModel.filteredFavorites.collectAsState()
    val allFavorites by viewModel.allFavorites.collectAsState()
    var searchInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Favorites",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${allFavorites.size} saved favorites",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (allFavorites.isNotEmpty()) {
                // Search bar within favorites
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    SearchBarComponent(
                        query = searchInput,
                        onQueryChange = {
                            searchInput = it
                            viewModel.setFavoritesSearchQuery(it)
                        },
                        placeholder = "Search saved favorites by author...",
                        testTag = "favorites_search_input"
                    )
                }
            }

            if (allFavorites.isEmpty()) {
                EmptyStateView(
                    title = "No Favorites Yet",
                    description = "When you like photos in the gallery, they'll appear here for quick access offline.",
                    icon = Icons.Filled.FavoriteBorder,
                    actionButtonText = "Browse Gallery",
                    onActionClick = onNavigateToHome,
                    modifier = Modifier.weight(1f)
                )
            } else if (filteredFavorites.isEmpty()) {
                EmptyStateView(
                    title = "No Matching Favorites",
                    description = "No favorites match '$searchInput'.",
                    icon = Icons.Filled.Favorite,
                    actionButtonText = "Clear Filter",
                    onActionClick = {
                        searchInput = ""
                        viewModel.setFavoritesSearchQuery("")
                    },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("favorites_grid")
                ) {
                    items(
                        items = filteredFavorites,
                        key = { it.id }
                    ) { fav ->
                        val picsumImage = fav.toPicsumImage()
                        GalleryImageCard(
                            image = picsumImage,
                            isFavorite = true,
                            onFavoriteToggle = { viewModel.removeFavorite(fav.id) },
                            onCardClick = { onImageClick(picsumImage) }
                        )
                    }
                }
            }
        }
    }
}
