package org.shareat.feature.home.ui.home.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.shareat.shared.designsystem.theme.ShareatTheme

@Composable
internal fun HomeSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search restaurants") },
        leadingIcon = {
            Icon(imageVector = Icons.Filled.Search, contentDescription = null)
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
    )
}

@Preview
@Composable
private fun HomeSearchBarPreview() {
    ShareatTheme {
        HomeSearchBar(query = "", onQueryChange = {})
    }
}

@Preview
@Composable
private fun HomeSearchBarWithQueryPreview() {
    ShareatTheme {
        HomeSearchBar(query = "Casa Naranja", onQueryChange = {})
    }
}
