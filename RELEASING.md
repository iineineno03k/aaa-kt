# リリース手順

タグ `v<バージョン>` をpushすると、GitHub Actions（`.github/workflows/release.yml`）がビルド・署名してMaven Centralへ公開する。座標は `io.github.iineineno03k:aaa-kt:<バージョン>`。

## リリースする

`main` に変更を入れ、新しいバージョンのタグをpushする。以下のコマンドは、最初に `VERSION` へ出したいバージョンを入れてから実行する。

```bash
VERSION=0.2.0   # 出したいバージョンに書き換える
git tag "v$VERSION"
git push origin "v$VERSION"
```

ワークフロー成功後、Central Portal（https://central.sonatype.com ）の Deployments で `PUBLISHED` になっていることを確認する。Maven Centralから取れるようになるまで20分ほどかかる（0.1.0の実績）。

```bash
curl -sI "https://repo1.maven.org/maven2/io/github/iineineno03k/aaa-kt/$VERSION/aaa-kt-$VERSION.pom" | head -1
```

`200` が返ったら、READMEのInstall欄のバージョンを新しい番号に直す。

Maven Centralは一度公開したバージョンを変更・削除できない。直すときは新しいバージョンを出す。

## リリース前の確認

READMEは「Kotlin 2.0以降」で使えるとしている。公開物のPOMは `build.gradle.kts` の `coreLibrariesVersion` で指定した `kotlin-stdlib` に依存するので、この値を上げると古いKotlinの利用側でコンパイルできなくなる（0.1.0はこの指定がなく、Kotlin 2.0で使えなかった）。上げるときはREADMEの対応バージョンと、CI（`.github/workflows/ci.yml`）のPOM検査の値も合わせる。

## 失敗したとき

- 署名の失敗: `SIGNING_KEY` の形式が合っていない。鍵を作ったマシンで下のコマンドを実行して登録し直す。
  ```bash
  gpg --export-secret-keys --armor CD3289CC32BEC9112976C954A716DAC097E9F779 \
    | grep -v '\-\-' | grep -v '^=.' | tr -d '\n' \
    | gh secret set SIGNING_KEY -R iineineno03k/aaa-kt
  ```
- 認証の失敗（401）: Central PortalのUser Tokenが切れているか、値が違う。下の「Central Portalのトークンについて」の手順で入れ直す。

ワークフローが失敗して何も公開されなかったタグは、消してから打ち直せる。

```bash
git push origin ":refs/tags/v$VERSION"
git tag -d "v$VERSION"
```

## Central Portalのトークンについて

- リリースに使うUser Token（名前 `aaa-kt-github-actions`）は **2027-10-07に期限が切れる**。それ以降のリリースは、入れ直すまで401で失敗する。
- 入れ直すときは、https://central.sonatype.com に `iineineno03k` のGitHubアカウントでログインし、アカウントメニューの「Generate User Token」で発行する。表示されたユーザー名とパスワードをそれぞれSecretsに登録する。
  ```bash
  gh secret set MAVEN_CENTRAL_USERNAME -R iineineno03k/aaa-kt
  gh secret set MAVEN_CENTRAL_PASSWORD -R iineineno03k/aaa-kt
  ```

## 署名鍵について

- 秘密鍵の原本は、鍵を作成したマシンのGPGキーリングにある。別のマシンからリリースするだけなら鍵は不要（署名はGitHub Actionsが行う）。
- 有効期限が切れる前に延長し、公開鍵を鍵サーバーへ送り直す。
  ```bash
  gpg --quick-set-expire CD3289CC32BEC9112976C954A716DAC097E9F779 3y
  gpg --export --armor CD3289CC32BEC9112976C954A716DAC097E9F779 \
    | curl --data-urlencode keytext@- https://keyserver.ubuntu.com/pks/add
  ```
