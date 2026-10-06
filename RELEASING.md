# リリース手順

タグ `v<バージョン>` をpushすると、GitHub Actions（`.github/workflows/release.yml`）がビルド・署名してMaven Centralへ公開する。座標は `io.github.iineineno03k:aaa-kt:<バージョン>`。

## 初回リリースまでに残っている作業

済んでいるもの:

- ライブラリ本体・テスト・CI（`main` のCIは成功）
- 署名用GPG鍵の作成（フィンガープリント `CD3289CC32BEC9112976C954A716DAC097E9F779`、有効期限3年）
- 公開鍵の `keyserver.ubuntu.com` への登録
- 秘密鍵のGitHub Secrets `SIGNING_KEY` への登録（パスフレーズなし）

残っているもの（上から順に行う）:

### 1. Central Portalにアカウントを作る

Central PortalはMaven Centralにライブラリを登録するための管理サイト。

1. https://central.sonatype.com を開き、「Continue with GitHub」で `iineineno03k` としてログインする。
2. 右上のアカウントメニューから Namespaces を開き、`io.github.iineineno03k` が Verified になっていることを確認する。GitHubでログインすると自動で認証される。

### 2. Portalのトークンを発行してSecretsに入れる

1. Portal右上のアカウントメニューから「Generate User Token」を押す。ユーザー名とパスワードの2つが表示される（閉じると再表示できないので、その場で次へ進む）。
2. それぞれをSecretsに登録する。コマンドを実行すると値の入力を求められるので貼り付ける。

```bash
gh secret set MAVEN_CENTRAL_USERNAME -R iineineno03k/aaa-kt
gh secret set MAVEN_CENTRAL_PASSWORD -R iineineno03k/aaa-kt
```

`gh` を使わない場合は、GitHubのリポジトリ画面の Settings → Secrets and variables → Actions から同じ名前で登録する。

登録後、Secretsが3つ（`SIGNING_KEY`・`MAVEN_CENTRAL_USERNAME`・`MAVEN_CENTRAL_PASSWORD`）あることを確認する。

```bash
gh secret list -R iineineno03k/aaa-kt
```

### 3. リポジトリを公開にする

Maven Centralに出すライブラリはソースの公開先（POMの `scm`）が見える必要がある。

```bash
gh repo edit iineineno03k/aaa-kt --visibility public --accept-visibility-change-consequences
```

ライブラリ名（`aaa-kt`）を変えるならこの前に行う。Maven Centralは一度公開した座標を変更・削除できない。変える場合は `build.gradle.kts` の `coordinates`・`pom`、`settings.gradle.kts`、パッケージ名、README、GitHubのリポジトリ名を揃えて直す。

### 4. 最初のタグを打つ

```bash
git clone git@github.com:iineineno03k/aaa-kt.git
cd aaa-kt
git tag v0.1.0
git push origin v0.1.0
```

Actions の Release ワークフローが動く。このワークフローはまだ一度も実行していないので、失敗したらログを見て直す。起こりやすいのは次の2つ。

- 署名の失敗: `SIGNING_KEY` の形式が合っていない。鍵を作ったマシンで下のコマンドを実行して登録し直す。
  ```bash
  gpg --export-secret-keys --armor CD3289CC32BEC9112976C954A716DAC097E9F779 \
    | grep -v '\-\-' | grep -v '^=.' | tr -d '\n' \
    | gh secret set SIGNING_KEY -R iineineno03k/aaa-kt
  ```
- 認証の失敗（401）: `MAVEN_CENTRAL_USERNAME`・`MAVEN_CENTRAL_PASSWORD` がPortalのログイン情報ではなく、発行したUser Tokenの値になっているか確認する。

失敗したタグをやり直すときは、タグを消してから打ち直す。

```bash
git push origin :refs/tags/v0.1.0
git tag -d v0.1.0
```

### 5. 公開を確認する

ワークフロー成功後、Portalの Deployments で `PUBLISHED` になっていることを確認する。Maven Centralから取れるようになるまで数十分かかることがある。

```bash
curl -sI https://repo1.maven.org/maven2/io/github/iineineno03k/aaa-kt/0.1.0/aaa-kt-0.1.0.pom | head -1
```

`200` が返れば、利用側で次のように入れられる。

```kotlin
dependencies {
    testImplementation("io.github.iineineno03k:aaa-kt:0.1.0")
}
```

## 2回目以降のリリース

`main` に変更を入れ、新しいバージョンのタグをpushするだけでよい。

```bash
git tag v0.2.0
git push origin v0.2.0
```

## 署名鍵について

- 秘密鍵の原本は、鍵を作成したマシンのGPGキーリングにある。別のマシンからリリースするだけなら鍵は不要（署名はGitHub Actionsが行う）。
- 有効期限が切れる前に延長し、公開鍵を鍵サーバーへ送り直す。
  ```bash
  gpg --quick-set-expire CD3289CC32BEC9112976C954A716DAC097E9F779 3y
  gpg --export --armor CD3289CC32BEC9112976C954A716DAC097E9F779 \
    | curl --data-urlencode keytext@- https://keyserver.ubuntu.com/pks/add
  ```
