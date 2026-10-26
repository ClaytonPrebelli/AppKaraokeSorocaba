# Arquitetura

Documento técnico do aplicativo **Karaoke Sorocaba**. Complementa o `README.md` e o `AGENTS.md`.

## 1. Visão geral

O app é um cliente Android nativo do site Karaoke Sorocaba. Não há backend próprio: ele consome a
mesma API pública do site.

```text
┌─────────────────────────────────────────────────────────────┐
│                        MainActivity                         │
│  splash → edge-to-edge → KaraokeTheme → KaraokeApp          │
└───────────────────────────┬─────────────────────────────────┘
                            │
             ┌──────────────┴───────────────┐
             │        NavHost (Compose)      │
             │  lista → categoria / busca    │
             └──────────────┬───────────────┘
                            │
        ┌───────────────────┼────────────────────┐
        │                   │                    │
  ListaScreen        CategoriaScreen       BuscaScreen
        │                   │                    │
        └───────────────────┼────────────────────┘
                            │
                   MusicasRepository
                    ┌───────┴────────┐
                    │                │
               MusicasCache     MusicasApi
             (filesDir/15d)   (HTTP/UTF-8)
```

Em paralelo, `UpdateViewModel` consulta o GitHub Releases e conduz o download/instalação do APK.

## 2. Camada de dados

### 2.1 Modelo

`data/Musica.kt`:

```kotlin
data class Musica(
    val id: Long,
    val nome: String,
    val cantor: String,
    val trecho: String,
    val codigo: Int,
    val categoria: String
)
```

- `parseMusicas(json)` lê um array JSON com `org.json`.
- Itens sem nome e sem cantor são ignorados.
- Cantor vazio vira `"Sem cantor"`.
- `agruparPorCantor(musicas)` ordena com `Collator` pt-BR e agrupa em `CantorFaixas`.

### 2.2 API

`data/MusicasApi.kt` usa `HttpURLConnection` em `Dispatchers.IO`:

- timeout de conexão: 15 s;
- timeout de leitura: 25 s;
- `User-Agent: AppKaraokeSorocaba`;
- leitura como UTF-8;
- qualquer status fora de 2xx vira `IOException`.
- `URLEncoder` codifica `cat`, `letra` e `termo`.

Endpoints:

```text
GET {base}verCategoria?cat={BRA|ING|ITA|ESP|GOS|INF}[&letra={A..Z|0}]
GET {base}Busca?termo={texto}
```

Base: `https://apikaraoke.prebellisolucoes.com/Musicas/`.

### 2.3 Cache

`data/MusicasCache.kt`:

- diretório: `filesDir/musicas`;
- um arquivo por chave: `cat_BRA_A.json`, `cat_ITA.json`, etc.;
- validade: 15 dias (mesma ideia do `localStorage` do site);
- ao iniciar, `limparSeExpirado()` apaga tudo se passaram 15 dias desde a última limpeza;
- leitura expirada apaga o arquivo e retorna `null`;
- gravação silenciosa com `runCatching`.

### 2.4 Repositório

`data/MusicasRepository.kt`:

- `categoria(cat, letra)`:
  1. tenta cache;
  2. se válido, parseia e retorna;
  3. se não, chama a API, parseia e grava o JSON bruto;
- `buscar(termo)`: sempre chama a API (sem cache).

O repositório é criado uma vez em `KaraokeApp` com `remember` e passado às telas.

## 3. UI

### 3.1 Tema

`ui/theme`:

- paleta violeta idêntica à do site (`Brand950..100`, `Surface`, `Border`, `Muted`, `Gold`);
- tipografia:
  - **Inter** para corpo/labels;
  - **Plus Jakarta Sans** para títulos;
  - ambas variáveis, com `FontVariation.Settings`;
- Material 3 com `lightColorScheme` fixo (sem dynamic color);
- `MainActivity` usa `enableEdgeToEdge` com barras transparentes.

### 3.2 Navegação

`ui/KaraokeApp.kt`:

```text
lista
categoria/{cat}
busca?termo={termo}
```

- `Uri.encode` no termo; argumento de query evita problemas com `/`.
- `onHome` volta para `lista` usando `popBackStack("lista", false)`.
- `UpdateHost` é global e fica fora do `NavHost`, para os diálogos sobreviverem à navegação.

### 3.3 Telas

**ListaScreen**

- `Column` com scroll vertical.
- Hero com gradiente desenhado por `heroBackground()` (`drawBehind` com gradiente linear e dois
  radiais), recorte inferior arredondado, logo em cartão branco.
- `SearchForm` reutilizado (input + botão).
- Grade de categorias montada com `chunked(2)` (duas colunas).
- Rodapé com versão, crédito e verificação de atualização.

**CategoriaScreen**

- Cabeçalho fixo (`PageHeader`).
- `LetterNav` horizontal fixo apenas para `filtraPorLetra = true`.
- `CantorList` com `LazyColumn` de acordeões.
- FAB para topo; fecha os acordeões.
- Loader em overlay sobre o conteúdo, com logo pulsando.
- `LaunchedEffect(categoria.key, letraSelecionada, tentativa)` recarrega a lista.

**BuscaScreen**

- Formulário fixo abaixo do cabeçalho.
- `consulta` é o termo efetivamente pesquisado; `termo` é o texto do input.
- Pesquisar o mesmo termo incrementa `tentativa` para forçar recarga.
- Mesma `CantorList` e mesmos estados de erro/vazio.

**Componentes**

- `PageHeader`: seta voltar, logo, eyebrow, título, casa.
- `SearchForm`: input branco de 54 dp + botão gradiente; vira linha em telas ≥ 560 dp.
- `CantorList`: acordeões, linhas de música, vazio, erro, rodapé.
- `LoadingOverlay`: scrim + cartão branco + logo pulsante.
- `AppFooter`: crédito, versão e “Verificar atualização”.
- `BotaoTopo`: FAB circular com gradiente.

## 4. Atualização

### 4.1 Checagem

`GitHubReleaseClient.buscarUltima(versaoAtual)`:

- `GET https://api.github.com/repos/ClaytonPrebelli/AppKaraokeSorocaba/releases/latest`;
- ignora draft/prerelease;
- compara `tag_name` (sem `v`) com `BuildConfig.VERSION_NAME`;
- escolhe o primeiro asset terminado em `.apk`;
- retorna `ReleaseInfo` ou `null` se não houver versão mais nova.

`util/Version.kt` compara partes numéricas (`1.2.3`), então `1.0.10 > 1.0.9`.

### 4.2 Máquina de estados

```text
Idle
 └─ verificarAoAbrir()/verificar(manual)
      └─ Checking
           ├─ sem versão nova → Idle (ou UpToDate se manual)
           ├─ erro → Idle (ou Error se manual)
           └─ Available
                ├─ "Depois" → Idle
                └─ "Atualizar agora" → Downloading
                     ├─ erro → Error
                     └─ ReadyToInstall
                          ├─ sem permissão → NeedsPermission
                          │    ├─ abrir configurações
                          │    └─ "Já autorizei" → tenta de novo
                          └─ com permissão → abre instalador → Idle
```

### 4.3 Download

- pasta `cacheDir/updates`;
- arquivo `app-update.apk` (apagado antes de baixar);
- buffer de 64 KB;
- progresso atualizado a cada ~120 ms;
- valida tamanho mínimo (> 1 KB) e assinatura ZIP (`PK`);
- `FileProvider` configurado em `res/xml/file_paths.xml` (`cache-path updates/`);
- intent `ACTION_VIEW` com MIME `application/vnd.android.package-archive` e
  `FLAG_GRANT_READ_URI_PERMISSION`.

### 4.4 Limitação de instalação silenciosa

Para instalar sem toque seria necessário:

- ser **device owner** / ter privilégios de sistema; ou
- distribuir pela Play Store com in-app updates.

Como o app é sideloaded via GitHub, o Android sempre exibe a confirmação final. O app automatiza o
máximo permitido: detecta a versão, baixa e abre o instalador.

## 5. Build e release

### 5.1 Gradle

- `version.properties` define `versionCode`/`versionName`.
- `app/build.gradle.kts` lê a keystore de:
  - variáveis de ambiente (`KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`), ou
  - `keystore.properties` na raiz (local).
- Se a keystore não existir, o release fica sem assinatura (não publicar).
- Release: R8 + shrink de recursos.

### 5.2 CI

`.github/workflows/ci.yml`:

- push na `main` e pull requests;
- instala JDK 17 e Gradle;
- roda `assembleDebug`.

`.github/workflows/release.yml`:

- tag `v*`;
- valida tag = `VERSION_NAME`;
- decodifica `KEYSTORE_BASE64` em `keystore/release.jks`;
- compila `assembleRelease`;
- confere a assinatura com `apksigner`;
- copia para `dist/karaoke-sorocaba-<tag>.apk`;
- publica o release via `softprops/action-gh-release@v2`.

## 6. Decisões de projeto

| Decisão | Motivo |
| --- | --- |
| Kotlin + Compose nativo | App real, sem WebView, melhor performance e offline |
| Sem banco | Listas são JSON imutáveis por 15 dias; arquivos bastam |
| `HttpURLConnection` + `org.json` | Menos dependências, APK menor |
| Cache com validade de 15 dias | Replica o comportamento do site |
| Fontes empacotadas | Visual consistente e offline |
| Atualização via GitHub Releases | Requisito do produto e distribuição gratuita |
| `applicationId` com sufixo `.debug` | Permite debug e release lado a lado |

## 7. Possíveis evoluções

- Testes instrumentados com Compose UI Test (precisa de emulador/aparelho).
- Cache da busca e “favoritos”.
- Download em segundo plano com `WorkManager` e notificação.
- Verificação de assinatura do APK baixado contra uma chave pública embutida.
- Ícone monocromático temático para Android 13+.
- Tela “Sobre” com links das redes sociais do site.
