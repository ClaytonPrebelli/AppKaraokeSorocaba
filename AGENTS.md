# AGENTS.md — Guia para agentes de IA e desenvolvedores

Este arquivo explica como continuar o projeto **sem quebrar** o aplicativo nem o fluxo de
atualização. Leia o `README.md` e o `docs/ARQUITETURA.md` antes de alterar código.

## 1. Objetivo do projeto

App Android nativo que replica o site **Karaoke Sorocaba**:

- lista músicas por categoria;
- busca por música ou cantor;
- agrupa por cantor em acordeões;
- mantém cache local de 15 dias;
- baixa e instala atualizações a partir de **GitHub Releases** do repositório
  `ClaytonPrebelli/AppKaraokeSorocaba`.

O APK é distribuído apenas pelo GitHub Releases. Não há Play Store.

## 2. Estado atual (v1.0.0)

- Kotlin 2.1.0, Compose BOM 2024.12.01, Material 3, AGP 8.7.3, Gradle 8.11.1.
- `compileSdk = 35`, `targetSdk = 35`, `minSdk = 26`.
- `applicationId = br.com.prebelli.appkaraokesorocaba`.
- Assinatura de release com `keystore/release.jks` + `keystore.properties` (locais, ignorados pelo
  Git) e secrets no GitHub Actions.
- Workflows:
  - `.github/workflows/ci.yml` — compila debug em push/PR na `main`.
  - `.github/workflows/release.yml` — em tag `v*`, compila release assinado e publica o APK.

## 3. Ambiente local desta máquina (Windows)

Ferramentas fora do repositório:

```powershell
$env:JAVA_HOME = "C:\Users\Clayton Prebelli\Projetos\.tools\jdk17\jdk-17.0.20.1+1"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

- JDK: `C:\Users\Clayton Prebelli\Projetos\.tools\jdk17\jdk-17.0.20.1+1`
- Android SDK: `C:\Users\Clayton Prebelli\Projetos\.tools\android-sdk`
  (apontado por `local.properties`, que não é versionado)
- `adb`: `C:\Users\Clayton Prebelli\Projetos\.tools\android-sdk\platform-tools\adb.exe`

Comandos principais (na raiz do projeto):

```powershell
.\gradlew.bat assembleDebug --no-daemon --console=plain
.\gradlew.bat assembleRelease --no-daemon --console=plain
.\gradlew.bat test --no-daemon
```

APKs:

```text
app/build/outputs/apk/debug/app-debug.apk
app/build/outputs/apk/release/app-release.apk
```

> O primeiro build baixa dependências e pode demorar. Builds seguintes usam cache.

## 4. Mapa do código

```text
app/src/main/java/br/com/prebelli/appkaraokesorocaba/
├─ MainActivity.kt                 # edge-to-edge, splash, tema, setContent
├─ data/
│  ├─ Categoria.kt                 # lista fixa de categorias + alfabeto
│  ├─ Musica.kt                    # modelo, parser JSON e agrupamento por cantor
│  ├─ MusicasApi.kt                # HTTP para a API do karaokê
│  ├─ MusicasCache.kt              # cache em arquivo, validade de 15 dias
│  └─ MusicasRepository.kt         # cache-first para categorias, direto para busca
├─ update/
│  ├─ GitHubReleaseClient.kt       # consulta releases/latest
│  ├─ UpdateState.kt               # máquina de estados da atualização
│  ├─ UpdateViewModel.kt           # verificação, download e instalação
│  └─ UpdateHost.kt                # diálogos da atualização
├─ ui/
│  ├─ KaraokeApp.kt                # NavHost e injeção do repositório
│  ├─ components/                  # header, busca, lista, loader, rodapé, FAB
│  ├─ screens/                     # ListaScreen, CategoriaScreen, BuscaScreen
│  └─ theme/                       # cores, tipografia (Inter/Jakarta) e MaterialTheme
└─ util/
   ├─ Intents.kt                   # abrir links externos
   └─ Version.kt                   # comparação semântica de versões
```

## 5. Contratos que NÃO devem ser quebrados

### 5.1 Identidade do app
- **Não altere** `applicationId` nem a keystore entre versões. O Android só substitui um app se o
  pacote e a assinatura forem os mesmos.
- O debug tem sufixo `.debug` de propósito (permite conviver com o release).

### 5.2 Versão
- `version.properties` é a fonte da verdade:
  ```properties
  VERSION_CODE=1
  VERSION_NAME=1.0.0
  ```
- O workflow de release falha se a tag `vX.Y.Z` não for exatamente igual a `VERSION_NAME`.
- `VERSION_CODE` deve sempre aumentar.
- O updater compara `releases/latest.tag_name` com `BuildConfig.VERSION_NAME` usando
  `util/Version.kt` (aceita `v` e compara partes numéricas).

### 5.3 Release
- O release precisa ter **um asset terminado em `.apk`**. O updater escolhe o primeiro.
- O release mais recente deve ser estável (não draft e não prerelease).
- Nome sugerido do asset: `karaoke-sorocaba-vX.Y.Z.apk` (o workflow já renomeia).
- O repositório usado pelo updater está em `GitHubReleaseClient.URL_ULTIMO_RELEASE`. Se o
  repositório mudar, atualize também o workflow e o README.

### 5.4 API
- Base: `https://apikaraoke.prebellisolucoes.com/Musicas/`
- Categorias: `BRA`, `ING`, `ITA`, `ESP`, `GOS`, `INF`.
- `verCategoria?cat=X` e `verCategoria?cat=X&letra=Y`.
- `Busca?termo=...`.
- `#` no filtro de letras vira `0` antes de chamar a API.
- Respostas são JSON UTF-8; o parser está em `data/Musica.kt`.
- A API pode retornar listas grandes (centenas de itens por letra). Não bloqueie a main thread.

### 5.5 Cache
- Validade: **15 dias** (`MusicasCache.VALIDADE_MS`).
- Chaves: `cat_<CAT>_<LETRA>` (com letra) ou `cat_<CAT>` (sem letra).
- O cache fica em `filesDir/musicas/*.json`.
- A busca não é cacheada, por decisão de produto (igual ao site).
- Não use `SharedPreferences` para listas grandes; use arquivos.

### 5.6 Atualização
- A checagem acontece uma vez por processo em `verificarAoAbrir()` e também manualmente pelo
  rodapé.
- Estados em `UpdateState`: `Idle`, `Checking`, `Available`, `UpToDate`, `Downloading`,
  `ReadyToInstall`, `NeedsPermission`, `Error`.
- Download vai para `cacheDir/updates/app-update.apk` e é liberado via `FileProvider`
  (authority `${applicationId}.fileprovider`).
- O Android exige `REQUEST_INSTALL_PACKAGES` e a permissão de “instalar apps desconhecidos”.
  **Não tente** instalar silenciosamente: não é possível para um APK comum.
- Validações mínimas do APK baixado: tamanho > 1 KB e assinatura ZIP (`PK`). Não remova.

## 6. Regras para alterações de UI

- Use os tokens de `ui/theme/Color.kt` e `ui/theme/Type.kt`; evite cores hardcoded fora do tema.
- A fidelidade visual é intencional. O layout de referência é o app Angular em
  `Prebelli-Solucoes/KaraokeSorocaba` (`src/app/templates/*`).
- O `HomeComponent` do site não é usado (a rota inicial é a lista); o app começa em `ListaScreen`.
- Mantenha textos em português com acentuação correta.
- O app usa fontes Inter (corpo) e Plus Jakarta Sans (títulos) empacotadas em `res/font`.
- Não adicione dependências de imagem remota: bandeiras e logo são recursos locais.
- Se adicionar uma tela, registre a rota em `ui/KaraokeApp.kt` e documente no README.

## 7. Regras para dados/erros

- Erros de rede devem cair em `erro` e mostrar o cartão com **Tentar novamente**.
- Se houver cache válido, ele tem prioridade e um erro de rede não deve apagar a tela.
- Mensagens de erro em português, sem stack trace.
- Não logue dados pessoais; não há dados pessoais no app.

## 8. Segurança e segredos

**Nunca versione:**

- `keystore/release.jks`
- `keystore.properties`
- `local.properties`
- qualquer senha/token

Eles estão no `.gitignore`. A keystore do release está nos secrets do GitHub
(`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`).

Se precisar recriar a keystore, **pare**: isso quebra atualizações de quem já instalou o app.
Só recrie se aceitar que todos desinstalem e reinstalem o aplicativo.

## 9. Fluxo de release (passo a passo)

1. Editar `version.properties` (`VERSION_CODE + 1`, `VERSION_NAME` novo).
2. Rodar build local (debug e, se possível, release).
3. Commit na `main` e push.
4. Criar tag anotada ou leve com o mesmo nome:
   ```powershell
   git tag v1.0.1
   git push origin main
   git push origin v1.0.1
   ```
5. Acompanhar o workflow **Publicar APK** no GitHub Actions.
6. Conferir o release e o asset `.apk`.
7. Instalar a versão anterior e validar o fluxo de atualização no aparelho.
8. Atualizar o `CHANGELOG.md`.

Se o workflow falhar antes do release: corrigir, commitar, apagar a tag remota/local e recriar.
Se o release já saiu quebrado: **aumentar a versão** e publicar de novo; não mover a tag.

## 10. Como diagnosticar problemas

| Sintoma | Onde olhar |
| --- | --- |
| Lista não carrega | `MusicasApi` (HTTP), `MusicasRepository` (cache), log do app |
| Cache não expira | `MusicasCache.VALIDADE_MS`, `limparSeExpirado()` |
| App não vê atualização | `GitHubReleaseClient`, tag vs `VERSION_NAME`, asset `.apk`, rate limit da API |
| Atualização baixa mas não instala | permissão “instalar apps desconhecidos”, `FileProvider`, assinatura diferente |
| Instalação recusada | mesma `applicationId`? `versionCode` maior? mesma keystore? |
| Workflow falha | tag ≠ `VERSION_NAME`, secrets ausentes, assinatura/APK inválido |
| Fonte estranha | `res/font`, `ui/theme/Type.kt`, variações de peso |

Comandos úteis:

```powershell
# logcat do app
adb logcat --pid=$(adb shell pidof -s br.com.prebelli.appkaraokesorocaba)

# verificar assinatura
& "C:\Users\Clayton Prebelli\Projetos\.tools\android-sdk\build-tools\35.0.0\apksigner.bat" `
  verify --print-certs app\build\outputs\apk\release\app-release.apk

# versão do APK
& "C:\Users\Clayton Prebelli\Projetos\.tools\android-sdk\build-tools\35.0.0\aapt2.exe" `
  dump badging app\build\outputs\apk\release\app-release.apk
```

## 11. Definition of done

Antes de considerar uma tarefa concluída:

- [ ] `.\gradlew.bat assembleDebug` compila sem erros.
- [ ] `.\gradlew.bat assembleRelease` compila (quando houver keystore).
- [ ] Fluxo principal navegável: lista → categoria → busca → voltar → início.
- [ ] Cache de 15 dias preservado.
- [ ] Nenhum segredo foi adicionado ao Git.
- [ ] README/AGENTS/CHANGELOG atualizados quando o comportamento mudar.
- [ ] Versão e tag consistentes quando o objetivo for publicar.

## 12. O que NÃO fazer

- Não trocar o `applicationId` nem a keystore.
- Não remover `REQUEST_INSTALL_PACKAGES` ou o `FileProvider`.
- Não usar APIs de instalação silenciosa (não funcionam sem root/device owner).
- Não commitar `keystore/`, `keystore.properties`, `local.properties` ou tokens.
- Não mover/apagar tags já publicadas que geraram release.
- Não introduzir analytics, rastreamento ou permissões desnecessárias.
- Não substituir o cache por um banco sem necessidade; o requisito é simples e offline.
