# Arquitetura

## Camadas

```
UI (Compose)  →  ViewModel  →  Use case  →  Repository / RecipeGenerator  →  Room / rede
```

- **ui/** – telas, componentes reutilizáveis, tema, navegação e o controlador de voz.
  Nenhuma lógica de negócio ou chamada de rede mora aqui.
- **domain/** – modelos (`Ingredient`, `IngredientDraft`, `Recipe`, ...), interfaces de
  repositório e casos de uso (`ParseIngredients`, `ConfirmIngredients`, `SuggestRecipes`,
  `GenerateRecipe`).
- **data/** – implementações: Room (`IngredientEntity`, `IngredientDao`, `AppDatabase`),
  `IngredientRepositoryImpl`, `SettingsRepositoryImpl` (DataStore), `ModelManager`
  (diretório de modelos on-device) e o cliente remoto OkHttp.
- **ai/** – `AIProvider`, extratores de ingredientes e `RecipeGenerator`.
- **audio/** – contrato `SpeechToTextService` e o backend opcional do Whisper.
- **di/** – `AppContainer` (injeção manual) e `BrigadeiroViewModelFactory`.

## Fluxo de dados

1. A Home recebe texto (digitado ou transcrito) e navega para a tela de confirmação
   passando o texto como argumento de navegação (`Routes.confirm(text)`).
2. `ConfirmViewModel` chama `ParseIngredientsUseCase` → `IngredientExtractor`.
3. O resultado são `IngredientDraft`s (com flag `uncertain`). A tela permite editar,
   remover e adicionar itens.
4. Ao confirmar, `ConfirmIngredientsUseCase` normaliza, mescla por `normalizedName` e
   grava no Room através de `IngredientRepository`.
5. `Minha Cozinha` e `Ideias` observam o repositório como fonte de verdade.

Nada vindo do modelo é persistido diretamente: o pipeline é
`extractor → parser JSON → validação de schema → validação de domínio → normalização →
draft → confirmação → repositório`.

## Room

- `ingredients`: `id`, `name`, `normalizedName` (índice único), `quantity`, `unit`,
  `category`, `createdAt`, `updatedAt`.
- Banco `brigadeiro.db`, versão 1.
- O repositório é a única porta de entrada; a UI nunca acessa o DAO.

## Voz

`ui/voice/VoiceInput.kt` encapsula o reconhecedor da plataforma:

- cria um `SpeechRecognizer` novo por sessão e o destrói ao fim (evita listener
  obsoleto e `ERROR_CLIENT`);
- verifica `SpeechRecognizer.isRecognitionAvailable` e prefere o engine on-device
  (API 31+); se o idioma pt-BR não estiver disponível no aparelho, cai para a
  atividade de voz do sistema (`ACTION_RECOGNIZE_SPEECH`), que funciona na maioria
  dos aparelhos;
- tem watchdog de 15 s e sempre mostra uma mensagem acionável em vez de falhar em
  silêncio; a permissão de microfone é pedida só ao tocar no botão.

O contrato `audio/SpeechToTextService` e o `WhisperSpeechToTextService` continuam
disponíveis para um backend local futuro.

## Providers de IA

`AIProvider` é a abstração base (`complete(prompt, systemPrompt): AIResult`):

- `FakeAIProvider` – respostas fixas para desenvolvimento e testes. Nunca é apresentado
  como modelo real.
- `LocalAIProvider` – hook para runtime on-device (LiteRT-LM / MediaPipe GenAI). Fica
  indisponível quando o modelo não está instalado ou nenhum engine foi injetado.
- `RemoteAIProvider` – cliente compatível com OpenAI (OpenAI, OpenRouter, Groq, Ollama).
  Só ativa quando o usuário configura URL/chave/modelo nos Ajustes. Nada é hardcoded.

O restante do app nunca sabe qual provider está ativo.

## OCR de nota

- `ai/receipt/ReceiptOcr.kt` – ML Kit Text Recognition (bundled, on-device) → linhas.
- `ai/receipt/ReceiptParser.kt` – determinístico: descarta ruído, casa com o
  `IngredientLexicon` e gera `IngredientDraft`s (incertos marcados).
- `ui/screens/scan/ScanScreen.kt` + `ScanViewModel` – captura (câmera do sistema via
  `FileProvider` ou galeria via `PickVisualMedia`), OCR, revisão e confirmação com o
  mesmo `ConfirmIngredientsUseCase`. Câmera usa `ACTION_IMAGE_CAPTURE` (não exige
  permissão `CAMERA`); a imagem fica no cache do app.

## Extração de ingredientes- `HeuristicIngredientExtractor` (offline, determinístico) é o padrão. Detecta posse vs.
  desejo ("tenho X" ≠ "queria fazer X"), quantidades, unidades, sinônimos e sinaliza
  incerteza.
- `LlmIngredientExtractor` usa um `AIProvider` e o `IngredientResponseParser` (JSON seguro
  + validação). É consultado apenas quando o heurístico não encontra nada ou marca
  incerteza.

## Perfil alimentar

- `DietProfile` (dieta + restrições + foco em congelar + flag `onboarded`) é persistido
  pelo `DietRepositoryImpl` (DataStore `brigadeiro_diet`).
- `DietClassifier` mapeia nomes de ingredientes para `FoodTag`s (carne, peixe, laticínio,
  ovo, glúten, castanhas, soja) e `DietPolicy` decide, de forma pura, o que o perfil
  proíbe. Laticínios vegetais e pasta de amendoim são tratados como casos especiais.
- O `BrigadeiroRoot` observa o perfil: mostra o onboarding (ou splash) na primeira
  abertura e o app depois de respondido/pulado. O mesmo wizard é reaberto pela Home e
  pelos Ajustes para edição.
- `SuggestRecipesUseCase`/`GenerateRecipeUseCase` injetam o perfil no `RecipeGenerator`;
  ViewModels e telas não mudam. O catálogo descarta receitas incompatíveis e o gerador
  remoto recebe a dieta no prompt e ainda pós-filtra o resultado.
- A cozinha marca com um selo discreto itens fora da dieta, sem impedir o cadastro.

## Receitas

`RecipeGenerator` (interface) define `suggestRecipes(ingredients, profile, maxTotalMinutes)`
e `generateRecipe(suggestion, ingredients, profile)`:

- `CatalogRecipeGenerator` – catálogo offline embutido, pontua receitas pelos ingredientes
  disponíveis, esconde as que exigem equipamento indisponível e aplica o filtro de tempo.
  É o padrão e não exige rede, chave ou modelo. Cada receita carrega `activeTimeMinutes`,
  `costPerServing` (estimativa), `imageEmoji`, `requiredEquipment` e um placeholder de
  imagem (`RecipeImage` procura `R.drawable.recipe_<slug>` e cai para um tile ilustrado).
- `SubstitutionCatalog` – mapa determinístico ingrediente → alternativas, cruzado por
  `TextNormalizer`; a UI mostra as trocas como opção, nunca como regra.
- `RemoteRecipeGenerator` – gera via `AIProvider` remoto quando configurado, cai para o
  catálogo em caso de falha/offline e pós-filtra pelo tempo/dieta/equipamento.

## Perfil da cozinha

- `Equipment` (fogão, forno, micro-ondas, air fryer, liquidificador, sanduicheira, panela
  de pressão, mixer) vive no `DietProfile` e no DataStore (`diet_equipment`). O padrão é o
  conjunto comum, então nada é escondido até o usuário editar.
- O onboarding ganha um passo de equipamentos e os Ajustes permitem alternar cada item,
  persistindo na hora.
- `CatalogRecipeGenerator` descarta receitas cujo `requiredEquipment` não está no perfil.

## Navegação

Navigation Compose com três destinos de topo (`Home`, `Minha Cozinha`, `Ideias`) exibidos
em `TopLevelScaffold`, que usa **bottom navigation** em telefones e **navigation rail** em
janelas largas (tablets). As telas de confirmação, receita e ajustes são destinos de pilha.

## Decisões

- **Injeção manual** em vez de Hilt: dependências poucas e centralizadas; menos build.
- **Versões espelhadas do app `mvp_cards`** (AGP 8.6.1, Kotlin 2.0.20, Compose BOM
  2024.09.03, Room 2.6.1), já validadas em SDK 34.
- **Offline por padrão**: o app entrega os três cenários de sucesso sem rede.
- **Tema orientado pela marca** (site `../brigadeiro`): marrom, sálvia, laranja e rosa,
  fontes Eczar + Varta, mapeados para papéis do Material 3.
