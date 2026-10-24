# IA no Brigadeiro

O Brigadeiro é **AI-assisted**, não um chat. A IA aparece em três pontos: fala → texto,
texto → ingredientes estruturados e (opcional) geração de receitas. Todo o resto é software
determinístico.

## 1. Speech-to-text

Contrato: `com.brigadeiro.app.audio.SpeechToTextService` (áudio cru → `TranscriptionResult`).

Caminho ativo no MVP: o reconhecedor **on-device da plataforma**, encapsulado em
`ui/voice/VoiceInput.kt`:

- cria um `SpeechRecognizer` novo a cada sessão e destrói no fim;
- usa `createOnDeviceSpeechRecognizer()` quando disponível (API 31+), com fallback para
  `createSpeechRecognizer()`;
- verifica `SpeechRecognizer.isRecognitionAvailable()` antes de começar;
- define `EXTRA_LANGUAGE = "pt-BR"`; se o engine on-device não tiver o modelo de pt-BR
  (`ERROR_LANGUAGE_UNAVAILABLE`/`ERROR_LANGUAGE_NOT_SUPPORTED`), abre a atividade de voz
  do sistema (`ACTION_RECOGNIZE_SPEECH`), que funciona na maioria dos aparelhos;
- tem watchdog de 15 s e reporta qualquer erro com mensagem acionável — nunca falha em
  silêncio;
- pede a permissão `RECORD_AUDIO` **somente quando o microfone é tocado**;
- se a permissão for negada, mostra uma mensagem e o usuário continua digitando.

O áudio não é enviado a nenhum LLM. A voz converge para o mesmo pipeline do texto: a
transcrição vira o argumento de navegação da tela de confirmação.

**Whisper (opcional).** `WhisperSpeechToTextService` implementa a mesma interface, mas
está **não integrado nesta build**. Integrar de verdade exige:

1. instalar o NDK e compilar o whisper.cpp (ou consumir um AAR pré-compilado mantido);
2. baixar um modelo multilíngue (`ggml-base` ou maior — **nunca** `.en`) em runtime;
3. colocar o arquivo em `filesDir/models/` (gerenciado por `ModelManager`).

Enquanto isso não acontece, o serviço se declara indisponível e o reconhecedor da
plataforma permanece ativo. Nada é mascarado como pronto.

## 2. Extração de ingredientes

Dois extratores, ambos atrás de `IngredientExtractor`:

- **`HeuristicIngredientExtractor` (padrão, offline).** Regras PT-BR:
  - distingue posse ("tenho", "comprei", "sobrou") de desejo ("queria fazer", "daria
    para", "que tal") — uma sugestão **não** vira inventário;
  - reconhece números ("dois", "meia", "500g") e unidades (kg, g, ml, xícara, dente, ...);
  - normaliza nomes (minúsculas, sem acento, singular) e usa um léxico de alimentos para
    canonicalizar e categorizar;
  - mescla duplicatas e marca itens incertos.
- **`LlmIngredientExtractor` (opcional).** Usa um `AIProvider` + `IngredientResponseParser`.
  É acionado por `CompositeIngredientExtractor` apenas quando o heurístico não acha nada
  ou sinaliza incerteza.

### Schema de saída

```json
{"ingredients":[{"name":"tomate","quantity":2,"unit":"unidade","confidence":0.9}]}
```

Regras do extrator: extrair só o que foi mencionado, não inventar quantidade nem
ingrediente, preservar quantidade explícita, retornar JSON válido no schema. Quando há
ambiguidade, o item é marcado para confirmação.

### Validação

`IngredientResponseParser` extrai o primeiro objeto `{...}` balanceado, decodifica com
`kotlinx.serialization` tolerante, valida schema e domínio (nome ≥ 2 caracteres,
quantidade finita ≥ 0) e normaliza. Entradas inválidas são descartadas — nada corrompido
é persistido. Coberto por testes em `IngredientResponseParserTest`.

## 3. Modelo local (opcional)

`LocalAIProvider` é o gancho para um runtime on-device (LiteRT-LM / MediaPipe GenAI).
Ele expõe `OnDeviceTextEngine`; a dependência do runtime fica fora do núcleo.

- **Onde fica o modelo:** `filesDir/models/gemma-3-1b-it-int4.task` (via `ModelManager`).
- **Como obter:** download manual/instalação a partir do Hugging Face (Gemma 3 1B INT4).
  Não há download automático nesta build.
- **Se não existir:** `isAvailable()` retorna `false`, `complete()` retorna `Unavailable`,
  e o app segue com a extração determinística.
- **Nunca commitar** arquivos `.gguf`, `.bin`, `.task`, `.litertlm` (já no `.gitignore`).

Obs.: o MediaPipe LLM Inference está em modo de manutenção; o sucessor é o LiteRT-LM.
O Gemma 3 1B INT4 tem ~1 GB e exige um aparelho razoável; emuladores não são confiáveis.

## 4. Provedor remoto (opcional)

`RemoteAIProvider` é um cliente de chat compatível com OpenAI (`POST {baseUrl}/chat/completions`).
Configurável nos Ajustes (URL base, chave, modelo) e guardado no DataStore. **Nenhuma
credencial é hardcoded** e nenhuma requisição é feita até haver configuração.

## 5. Dieta

O perfil alimentar (`DietProfile`) é sempre respeitado, tanto offline quanto online:

- `DietClassifier` (determinístico, PT-BR) rotula ingredientes com `FoodTag`s;
- `DietPolicy` calcula o que a dieta proíbe (dieta = regra rígida; restrições removem uma
  tag cada). Pescatariano = peixe/frutos do mar + ovos + laticínios; vegetariano = ovos +
  laticínios; vegano = nada de origem animal.
- No catálogo, receitas com tag proibida são descartadas antes de pontuar; quando o foco
  em congelar está ativo, as congeláveis sobem ao topo.
- No gerador remoto, a dieta vai no system prompt **e** o resultado é pós-filtrado pelo
  classificador — o modelo não pode sobrepor uma restrição.

Não há cálculo nutricional nem metas de calorias/macros no MVP: apenas padrão alimentar,
restrições e preferência por congelar.

## 6. OCR de nota (ML Kit)

Adicionar itens pela foto da nota roda 100% no aparelho:

- `ai/receipt/ReceiptOcr.kt` encapsula o **ML Kit Text Recognition v2** (modelo
  Latin empacotado, `com.google.mlkit:text-recognition`), sem enviar a imagem para
  lugar nenhum.
- `ai/receipt/ReceiptParser.kt` é determinístico: quebra em linhas, descarta
  cabeçalho/rodapé e totais (CNPJ, cupom, total, cartão...), extrai produto +
  quantidade/unidade e casa com o `IngredientLexicon`; o que não reconhece entra
  marcado como incerto.
- Fluxo: foto → OCR → parser → **tela de revisão** (`ui/screens/scan`) → edição do
  usuário → `ConfirmIngredientsUseCase`. Regra do SDD respeitada: nada entra no
  inventário sem confirmação.

## 7. Receitas- **Padrão offline:** `CatalogRecipeGenerator` pontua um catálogo embutido de receitas
  brasileiras pela quantidade de ingredientes disponíveis e lista o que falta.
- **Remoto opcional:** `RemoteRecipeGenerator` gera via `AIProvider` quando configurado e
  cai para o catálogo em caso de falha/offline.
- **Local opcional:** a mesma interface permitiria gerar com o modelo on-device no futuro.

## 8. Offline

Funciona sem internet: abrir, ver/adicionar/editar/remover/pesquisar ingredientes,
extração heurística, sugestões do catálogo e **OCR da nota** (ML Kit empacotado).
Dependem de rede apenas a transcrição em nuvem (o MVP prefere o reconhecedor on-device)
e a geração remota de receitas.

## 9. Limitações conhecidas

- Whisper e o modelo local estão **atrás de interfaces, mas não integrados** nesta build.
- O léxico heurístico cobre os alimentos comuns; nomes muito incomuns entram como "Outros".
- O catálogo de receitas é curado e finito.
- Reconhecimento on-device depende do pacote de idioma instalado no aparelho; sem ele, a
  plataforma pode falhar offline e o app orienta o usuário a digitar.
