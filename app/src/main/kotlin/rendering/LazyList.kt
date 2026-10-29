package com.adsbynimbus.android.sample.rendering

import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import com.adsbynimbus.AdSize
import com.adsbynimbus.Nimbus

class LazyListFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ItemList(
                        entries = listEntries,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = innerPadding
                    )
                }
            }
        }
    }
}

data class ItemModel(
    val id: Int,
    val title: String,
    val description: String
)

sealed interface ListEntry {
    data class Content(val item: ItemModel) : ListEntry
    data class Ad(val id: String, val position: String = "inline_mrec_ad") : ListEntry
}

val sampleItems = List(25) { index ->
    ItemModel(
        id = index + 1,
        title = "Item ${index + 1}",
        description = "This is the detailed description for item number ${index + 1}."
    )
}

val listEntries: List<ListEntry> = buildList {
    sampleItems.forEachIndexed { index, item ->
        add(ListEntry.Content(item))
        if (index == 11) { // Show only one Nimbus ad after 12 cards
            add(ListEntry.Ad(id = "ad_after_12_cards"))
        }
    }
}

@Composable
fun ItemList(
    entries: List<ListEntry>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val context = LocalContext.current
    val adContainers = remember { mutableMapOf<String, FrameLayout>() }

    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = entries,
            key = { entry ->
                when (entry) {
                    is ListEntry.Content -> "item_${entry.item.id}"
                    is ListEntry.Ad -> "ad_${entry.id}"
                }
            }
        ) { entry ->
            when (entry) {
                is ListEntry.Content -> {
                    ItemRow(
                        item = entry.item,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
                is ListEntry.Ad -> {
                    val adContainer = remember(entry.id) {
                        adContainers.getOrPut(entry.id) {
                            FrameLayout(context)
                        }
                    }
                    NimbusAdCard(
                        adContainer = adContainer,
                        position = entry.position,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ItemRow(
    item: ItemModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun NimbusAdCard(
    adContainer: FrameLayout,
    position: String,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(position) {
        Nimbus.bannerAd(position, size = AdSize.Mrec).show(adContainer)
    }
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = {
                    (adContainer.parent as? ViewGroup)?.removeView(adContainer)
                    adContainer
                },
                modifier = Modifier
                    .width(300.dp)
                    .height(250.dp)
            )
        }
    }
}

