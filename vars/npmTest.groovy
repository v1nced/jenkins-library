// ===== vars/npmTest.groovy =====
def call(Map config) {
    echo "🧪 Запуск npm тестов"
    
    try {
        dir(config.projectPath) {
            // Установка зависимостей
            if (!env.SKIP_CACHE?.toBoolean()) {
                sh "npm ci"
            }
            
            // Запуск тестов
            sh "npm run ${config.testCommand}"
            
            // Публикация результатов если есть файлы отчетов
            if (fileExists('test-results.xml')) {
                publishTestResults([
                    testResultsPattern: 'test-results.xml',
                    allowEmptyResults: false
                ])
            }
            
            // Публикация покрытия кода
            if (fileExists('coverage/lcov.info')) {
                publishCoverage([
                    adapters: [
                        coberturaAdapter('coverage/cobertura-coverage.xml')
                    ],
                    sourceFileResolver: sourceFiles('STORE_LAST_BUILD')
                ])
            }
        }
        
        echo "✅ npm тесты выполнены успешно"
        
    } catch (Exception e) {
        echo "❌ Ошибка при выполнении npm тестов: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        throw e
    }
}