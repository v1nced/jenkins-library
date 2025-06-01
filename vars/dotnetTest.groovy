def call(Map config) {
    echo "🧪 Запуск .NET тестов"
    
    try {
        // Восстановление пакетов
        if (!env.SKIP_CACHE?.toBoolean()) {
            sh "dotnet restore ${config.projectPath}"
        }
        
        // Сборка проекта
        sh "dotnet build ${config.projectPath} --configuration ${config.configuration} --no-restore"
        
        // Запуск тестов с генерацией отчетов
        sh """
            dotnet test ${config.projectPath} \
                --configuration ${config.configuration} \
                --no-build \
                --logger "trx;LogFileName=test-results.trx" \
                --logger "html;LogFileName=test-results.html" \
                --results-directory ${env.TEST_RESULTS_PATH} \
                --collect:"XPlat Code Coverage"
        """
        
        // Публикация результатов тестов
        publishTestResults([
            testResultsPattern: "${env.TEST_RESULTS_PATH}/**/*.trx",
            allowEmptyResults: false
        ])
        
        // Публикация покрытия кода
        publishCoverage([
            adapters: [
                istanbulCoberturaAdapter("${env.TEST_RESULTS_PATH}/**/coverage.cobertura.xml")
            ],
            sourceFileResolver: sourceFiles('STORE_LAST_BUILD')
        ])
        
        echo "✅ .NET тесты выполнены успешно"
        
    } catch (Exception e) {
        echo "❌ Ошибка при выполнении .NET тестов: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        throw e
    }
}