package com.brigadeiro.app.ui.screens.scan

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brigadeiro.app.di.BrigadeiroViewModelFactory
import com.brigadeiro.app.ui.components.AIConfirmationCard
import com.brigadeiro.app.ui.components.BrigadeiroButton
import com.brigadeiro.app.ui.components.BrigadeiroSecondaryButton
import com.brigadeiro.app.ui.components.LoadingState
import com.brigadeiro.app.ui.theme.Spacing
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    onBack: () -> Unit,
    onDone: () -> Unit,
    viewModel: ScanViewModel = viewModel(factory = BrigadeiroViewModelFactory),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    var pendingUri by remember { mutableStateOf<Uri?>(null) }

    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { success ->
        val uri = pendingUri
        pendingUri = null
        if (success && uri != null) viewModel.process(context, uri)
    }

    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) viewModel.process(context, uri)
    }

    LaunchedEffect(state.saved) {
        if (state.saved) onDone()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Adicionar por foto") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (state.phase) {
                ScanPhase.Choosing -> ChooseContent(
                    error = state.error,
                    onCamera = {
                        val uri = createReceiptUri(context)
                        pendingUri = uri
                        takePicture.launch(uri)
                    },
                    onGallery = {
                        pickMedia.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly,
                            ),
                        )
                    },
                )

                ScanPhase.Processing -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    LoadingState(message = "Lendo a nota...")
                }

                ScanPhase.Review -> ReviewContent(
                    state = state,
                    onNameChange = viewModel::updateName,
                    onQuantityChange = viewModel::updateQuantity,
                    onUnitChange = viewModel::updateUnit,
                    onRemove = viewModel::remove,
                    onAddEmpty = viewModel::addEmpty,
                    onConfirm = viewModel::confirm,
                    onRetry = viewModel::retry,
                )
            }
        }
    }
}

@Composable
private fun ChooseContent(
    error: String?,
    onCamera: () -> Unit,
    onGallery: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.screenHorizontal),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(Spacing.xl))
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(96.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.ReceiptLong,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(44.dp),
                )
            }
        }
        Spacer(Modifier.height(Spacing.lg))
        Text(
            text = "Adicione pela nota do mercado",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Spacing.xs))
        Text(
            text = "Fotografe a nota fiscal e eu encontro os itens de comida. " +
                "Você confere tudo antes de salvar.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (error != null) {
            Spacer(Modifier.height(Spacing.md))
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.height(Spacing.xl))
        BrigadeiroButton(
            text = "Tirar foto",
            onClick = onCamera,
            leadingIcon = Icons.Outlined.PhotoCamera,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(Spacing.sm))
        BrigadeiroSecondaryButton(
            text = "Escolher da galeria",
            onClick = onGallery,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ReviewContent(
    state: ScanUiState,
    onNameChange: (Int, String) -> Unit,
    onQuantityChange: (Int, String) -> Unit,
    onUnitChange: (Int, String) -> Unit,
    onRemove: (Int) -> Unit,
    onAddEmpty: () -> Unit,
    onConfirm: () -> Unit,
    onRetry: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                start = Spacing.screenHorizontal,
                end = Spacing.screenHorizontal,
                top = Spacing.sm,
                bottom = Spacing.lg,
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            item {
                Column {
                    Text(
                        text = "Encontramos ${state.itemCount} itens",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        text = "Confira antes de adicionar. Itens com aviso podem estar errados.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(Spacing.md))
                }
            }
            itemsIndexed(state.drafts) { index, draft ->
                AIConfirmationCard(
                    draft = draft,
                    onNameChange = { onNameChange(index, it) },
                    onQuantityChange = { onQuantityChange(index, it) },
                    onUnitChange = { onUnitChange(index, it) },
                    onRemove = { onRemove(index) },
                )
            }
            item {
                Spacer(Modifier.height(Spacing.xs))
                BrigadeiroSecondaryButton(
                    text = "Adicionar outro",
                    onClick = onAddEmpty,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            BrigadeiroSecondaryButton(
                text = "Outra foto",
                onClick = onRetry,
                modifier = Modifier.weight(1f),
            )
            BrigadeiroButton(
                text = "Adicionar",
                onClick = onConfirm,
                enabled = state.drafts.any { it.name.trim().length >= 2 },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private fun createReceiptUri(context: Context): Uri {
    val dir = File(context.cacheDir, "receipts").apply { mkdirs() }
    val file = File(dir, "receipt_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
