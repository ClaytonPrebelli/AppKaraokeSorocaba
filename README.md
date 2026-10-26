# Karaoke Sorocaba — Aplicativo Android

Aplicativo Android **nativo** (Kotlin + Jetpack Compose) que reproduz o comportamento do site
[Karaoke Sorocaba](https://karaokesorocaba.com.br): consulta de músicas por categoria, busca por
música/cantor e lista organizada por cantor, com cache offline de 15 dias.

- Repositório: <https://github.com/ClaytonPrebelli/AppKaraokeSorocaba>
- Releases/APK: <https://github.com/ClaytonPrebelli/AppKaraokeSorocaba/releases>
- Aplicação web de referência: <https://github.com/Prebelli-Solucoes/KaraokeSorocaba>
- API consumida: `https://apikaraoke.prebellisolucoes.com/Musicas/`

> Este aplicativo foi feito para ser distribuído fora da Play Store, via APK no GitHub Releases.
> Ele verifica sozinho se há uma versão nova e conduz o usuário até o instalador do Android.

---

## 1. O que o aplicativo faz

### Tela inicial (`ListaScreen`)
- Hero com logo do Don Petisco, título **Karaoke Sorocaba** e o texto
  “Encontre a música certa para a sua vez no microfone.”
- Campo de busca com botão **Buscar** (a busca exige pelo menos **4 caracteres**).
- Grade com as 6 categorias:
  - Nacionais (BRA)
  - Inglês (ING)
  - Italiano (ITA)
  - Espanhol (ESP)
  - Gospel (GOS)
  - Infantil (INF)
- Rodapé com o crédito “Desenvolvido por Clayton Prebelli”, versão instalada e o botão
  **Verificar atualização**.

### Tela de categoria (`CategoriaScreen`)
- Cabeçalho com voltar, logo, nome da categoria e atalho para a tela inicial.
- **Nacionais** e **Inglês** têm filtro por letra (A–Z e `#`).
  - O `#` é enviado para a API como `0` (assim como no site).
- **Italiano, Espanhol, Gospel e Infantil** não têm filtro por letra.
- Lista em acordeões: um cartão por cantor, mostrando a quantidade de faixas.
- Ao expandir, cada faixa mostra código, nome e trecho da letra.
- Botão flutuante para voltar ao topo (fecha os acordeões).
- Estado vazio e estado de erro com botão **Tentar novamente**.

### Tela de busca (`BuscaScreen`)
- Cabeçalho “Resultados para {termo}”.
- Campo de busca fixo para refazer a busca.
- Resultados agrupados por cantor nos mesmos acordeões.
- Estado vazio com a mensagem “Nenhuma música encontrada para ‘{termo}’.”.
- Botão flutuante para voltar ao topo.

### Cache offline
- O site guarda as listas no `localStorage` e limpa tudo a cada 15 dias.
- O app reproduz a mesma ideia em arquivos internos (`filesDir/musicas`), com validade de 15 dias.
- A busca **não** é cacheada (igual ao site); as categorias são.
- Ao abrir uma categoria já visitada, a lista aparece instantaneamente do cache. Se o cache expirou
  ou não existe, os dados são buscados na API e regravados.

### Atualização automática
- Ao abrir o app, uma chamada é feita a
  `https://api.github.com/repos/ClaytonPrebelli/AppKaraokeSorocaba/releases/latest`.
- A versão do `tag_name` do release é comparada com o `BuildConfig.VERSION_NAME` instalado.
- Se for mais nova, o app mostra as notas do release e o botão **Atualizar agora**.
- O APK é baixado para o cache do app, com barra de progresso.
- Depois do download, o instalador do Android é aberto para substituir o app.
- Na primeira vez, o Android exige que o usuário permita **instalar apps desconhecidos** para o
  Karaoke Sorocaba. O app detecta isso e oferece um atalho para a tela de configuração.
- **Limitação do Android:** um APK distribuído fora da Play Store não pode ser instalado 100% sem
  interação. A confirmação final é sempre do sistema (isso só seria possível com um app
  “device owner” ou distribuição pela Play Store). O download e a abertura do instalador são
  automáticos; o toque final é do usuário.

---

## 2. Requisitos

### Para usar
- Android 8.0 (API 26) ou superior.
- Conexão com a internet para carregar listas novas e verificar atualizações.

### Para compilar
- JDK 17.
- Android SDK com:
  - `platforms;android-35`
  - `build-tools;35.0.0` (o Gradle também baixa o 34.0.0 automaticamente se necessário)
  - `platform-tools`
- Gradle é fornecido pelo wrapper (`./gradlew`).

---

## 3. Compilar localmente (Windows PowerShell)

O projeto já contém o Gradle Wrapper. Nesta máquina, o toolchain usado no desenvolvimento está em
`C:\Users\Clayton Prebelli\Projetos\.tools` (fora do repositório):

```powershell
$env:JAVA_HOME = "C:\Users\Clayton Prebelli\Projetos\.tools\jdk17\jdk-17.0.20.1+1"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

cd "C:\Users\Clayton Prebelli\Projetos\AppKaraokeSorocaba"
.\gradlew.bat assembleDebug
```

O `local.properties` (não versionado) aponta para o SDK:

```properties
sdk.dir=C:/Users/Clayton Prebelli/Projetos/.tools/android-sdk
```

APK de debug gerado em:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Instalar em um aparelho conectado:

```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Compilar release assinado localmente

O release usa a keystore `keystore/release.jks` e as senhas de `keystore.properties`
(ambos **ignorados pelo Git**). Sem eles, o Gradle gera um APK sem assinatura, que **não** pode ser
publicado no release.

```powershell
.\gradlew.bat assembleRelease
```

APK gerado em:

```text
app/build/outputs/apk/release/app-release.apk
```

Conferir a assinatura:

```powershell
& "C:\Users\Clayton Prebelli\Projetos\.tools\android-sdk\build-tools\35.0.0\apksigner.bat" `
  verify --print-certs app\build\outputs\apk\release\app-release.apk
```

> **Guarde a keystore e as senhas.** Todas as versões futuras precisam ser assinadas com a mesma
> chave. Se a keystore for perdida, os aparelhos que já têm o app instalado não conseguirão
> atualizar; será necessário desinstalar e instalar de novo. Faça backup de `keystore/release.jks` e
> de `keystore.properties`.

---

## 4. Publicar uma nova versão (GitHub Actions)

O workflow `.github/workflows/release.yml` compila e publica o APK automaticamente quando uma tag
`v*` é enviada.

### Passo a passo

1. Altere `version.properties`:
   ```properties
   VERSION_CODE=2
   VERSION_NAME=1.0.1
   ```
   - `VERSION_CODE` **sempre aumenta** (é o que o Android usa para aceitar a substituição do app).
   - `VERSION_NAME` deve ser igual à tag sem o `v`.
2. Commit e push na `main`.
3. Crie e envie a tag:
   ```powershell
   git tag v1.0.1
   git push origin v1.0.1
   ```
4. O GitHub Actions:
   - valida se a tag (`v1.0.1`) é igual a `VERSION_NAME` (`1.0.1`);
   - descriptografa a keystore a partir dos secrets;
   - compila o APK release assinado;
   - confere a assinatura com `apksigner`;
   - cria o release com o arquivo `karaoke-sorocaba-v1.0.1.apk`.

O release fica em:

```text
https://github.com/ClaytonPrebelli/AppKaraokeSorocaba/releases
```

### Secrets necessários no GitHub

| Secret | Conteúdo |
| --- | --- |
| `KEYSTORE_BASE64` | Conteúdo de `keystore/release.jks` em Base64 |
| `KEYSTORE_PASSWORD` | Senha da keystore |
| `KEY_ALIAS` | Alias da chave (`karaoke`) |
| `KEY_PASSWORD` | Senha da chave (igual à da keystore neste projeto) |

Gerar o Base64 no Windows:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("keystore\release.jks"))
```

Se a tag for enviada sem os secrets, o workflow falha antes de gerar o APK.

### Regras de release

- Não reutilize uma tag já publicada para uma versão nova. Prefira aumentar o patch
  (`1.0.1` → `1.0.2`).
- Se o workflow falhar **antes** de criar o release, corrija o código, faça commit, apague a tag
  local/remota e crie a tag de novo.
- Se o release já foi criado mas o APK está quebrado, publique uma versão seguinte; não mova a tag.
- O asset publicado **precisa** terminar com `.apk`; o app procura o primeiro asset com essa
  extensão no release mais recente.

---

## 5. Estrutura do projeto

```text
AppKaraokeSorocaba/
├─ app/
│  ├─ build.gradle.kts
│  └─ src/main/
│     ├─ AndroidManifest.xml
│     ├─ java/br/com/prebelli/appkaraokesorocaba/
│     │  ├─ MainActivity.kt
│     │  ├─ data/            # API, modelos, cache e repositório
│     │  ├─ update/          # verificação/download/instalação do APK
│     │  ├─ ui/              # tema, navegação, componentes e telas
│     │  └─ util/            # versão e abertura de links
│     └─ res/                # ícones, fontes, cores, splash e FileProvider
├─ .github/workflows/        # CI e publicação do APK
├─ docs/                     # documentação técnica
├─ gradle/wrapper/           # Gradle Wrapper
├─ keystore/                 # NÃO versionado: keystore de release
├─ keystore.properties       # NÃO versionado: senhas da keystore
├─ version.properties        # versão atual (versionCode e versionName)
├─ README.md                 # este arquivo
└─ AGENTS.md                 # guia para agentes de IA continuarem o projeto
```

---

## 6. API consumida

Base: `https://apikaraoke.prebellisolucoes.com/Musicas/`

| Uso | Requisição | Observações |
| --- | --- | --- |
| Categoria com letra | `GET verCategoria?cat=BRA&letra=A` | Usado por Nacionais e Inglês |
| Categoria sem letra | `GET verCategoria?cat=ITA` | Usado por Italiano, Espanhol, Gospel e Infantil |
| Busca | `GET Busca?termo=amor` | O app só envia termos com 4+ caracteres |
| Letra `#` | `letra=0` | Conversão feita no app |

Formato da resposta (UTF-8):

```json
[
  {
    "id": 24,
    "nome": "CANÇÃO PRA NÃO VOLTAR",
    "categoria": "BRA",
    "cantor": "A BANDA MAIS BONITA DA CIDADE",
    "trecho": "Não volte pra casa meu amor que aqui",
    "codigo": 15450
  }
]
```

Categorias: `BRA`, `ING`, `ITA`, `ESP`, `GOS`, `INF`.

O filtro por letra da API é aplicado pelo **início do nome do cantor** (ex.: `letra=A` retorna
cantores que começam com A), exatamente como no site.

---

## 7. Tecnologias e decisões

- **Kotlin + Jetpack Compose** e **Material 3**.
- **Single Activity** + **Navigation Compose**.
- **Sem banco de dados**: as listas são JSON em arquivos internos, com validade de 15 dias.
- **HttpURLConnection + org.json** para evitar dependências extras.
- **Fontes empacotadas**: Inter e Plus Jakarta Sans (variáveis) em `res/font`.
- **Cores e medidas** replicam o CSS do site (paleta violeta, cartões brancos, cantos arredondados).
- **Minificação R8** ativada no release.
- `applicationId`: `br.com.prebelli.appkaraokesorocaba`.
  - Debug usa o sufixo `.debug`, então debug e release podem ficar instalados ao mesmo tempo.

---

## 8. Limitações conhecidas

- A instalação de atualização sempre mostra a confirmação final do Android (segurança do sistema).
- O app precisa de internet para buscar categorias que ainda não estão no cache e para buscar.
- Não há cache da busca.
- A API é externa; se ela mudar, o app precisa ser atualizado.
- Não há analytics (o site usa Google Analytics; o app não).
- Os testes automatizados ainda não cobrem a interface (não há emulador no ambiente de build).

---

## 9. Checklist de validação antes de publicar

- [ ] `VERSION_CODE` aumentado.
- [ ] `VERSION_NAME` igual à tag (`vX.Y.Z`).
- [ ] `.\gradlew.bat assembleDebug` passou.
- [ ] `.\gradlew.bat assembleRelease` passou.
- [ ] O APK abre em um aparelho Android 8+.
- [ ] Navegação: inicial → categoria → busca → voltar → início.
- [ ] Cache: abrir categoria, fechar o app e abrir de novo (lista aparece sem rede).
- [ ] Verificação de atualização manual no rodapé.
- [ ] Release publicado com o asset `.apk`.
- [ ] Instalar a versão anterior e testar o fluxo de atualização para a nova.

---

## 10. Documentação adicional

- [`AGENTS.md`](AGENTS.md) — guia para agentes de IA (e pessoas) continuarem o projeto.
- [`docs/ARQUITETURA.md`](docs/ARQUITETURA.md) — fluxo de dados, telas, cache e atualização.
- [`CHANGELOG.md`](CHANGELOG.md) — histórico de versões.

---

Desenvolvido por **Clayton Prebelli**.
