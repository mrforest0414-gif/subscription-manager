# subscription-manager

契約中サブスクリプションの一元管理アプリ(blog-system と同じ構成)。

- Spring Boot 4.1.1 / Spring Web MVC / Spring Data JPA / Bean Validation
- SQLite(`submanager.db`、初回起動で自動作成。空のときだけサンプル6件を投入)
- Thymeleaf + Bootstrap 5 + Bootstrap Icons + Chart.js(CDN読み込み。Node.js不要)
- Gradle 9.7.1 (Wrapper) / Java 17 以上

## 起動

Eclipse/STS: File > Import > Gradle > Existing Gradle Project でこのフォルダを選択し、
`SubManagerApplication` を Spring Boot App として実行。

コマンドライン:

    gradlew.bat bootRun     -> http://localhost:8080
    gradlew.bat test

## 構成

    com.example.submanager
      SubManagerApplication
      subscription/   Subscription(Entity), SubscriptionRepository, SubscriptionService,
                      SubscriptionForm(入力DTO+検証), Summary(集計結果), 各enum, DataSeeder
      web/            SubscriptionController, AssetVersionAdvice
    templates/        index.html(ダッシュボード), admin/subscription-edit.html,
                      error/404.html, fragments/(header, footer)

## 仕様メモ

- 外貨は `Currency` enum の固定レートで円換算(USD=150, EUR=163)。
- 年額は12分割して月換算。「解約済み」は集計から除外。
- 登録済みの次回更新日が過去なら、課金サイクル分だけ先へ進めて表示。
- 7日以内の更新は行をハイライト(`SubscriptionService.REMINDER_DAYS`)。
- 認証なし(blog-system の /admin と同様、ローカル利用前提)。
