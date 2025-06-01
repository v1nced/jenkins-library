def call(Map config) {
    echo "📊 Сбор результатов тестирования"
    
    def testResults = [:]
    
    try {
        // Получение статистики тестов
        def testResultAction = currentBuild.getAction(hudson.tasks.test.AbstractTestResultAction.class)
        
        if (testResultAction != null) {
            testResults = [
                totalTests: testResultAction.totalCount,
                failedTests: testResultAction.failCount,
                skippedTests: testResultAction.skipCount,
                passedTests: testResultAction.totalCount - testResultAction.failCount - testResultAction.skipCount,
                testDuration: testResultAction.totalCount > 0 ? 
                    String.format("%.2f", testResultAction.getTotalDuration() / 1000) + "s" : "0s"
            ]
        } else {
            testResults = [
                totalTests: 0,
                failedTests: 0,
                skippedTests: 0,
                passedTests: 0,
                testDuration: "0s"
            ]
        }
        
        // Сохранение результатов в файл для дальнейшего использования
        writeJSON file: "${env.ARTIFACTS_PATH}/test-summary.json", json: [
            project: config.projectName,
            projectType: config.projectType,
            branch: config.branch,
            buildNumber: env.BUILD_NUMBER,
            buildUser: config.buildUser,
            timestamp: new Date().format("yyyy-MM-dd HH:mm:ss"),
            results: testResults,
            buildStatus: currentBuild.currentResult ?: 'SUCCESS'
        ]
        
        // Архивирование артефактов
        archiveArtifacts artifacts: "${env.ARTIFACTS_PATH}/**/*", allowEmptyArchive: true
        
        echo "✅ Результаты собраны: Всего тестов: ${testResults.totalTests}, Прошло: ${testResults.passedTests}, Провалено: ${testResults.failedTests}"
        
    } catch (Exception e) {
        echo "⚠️ Ошибка при сборе результатов: ${e.getMessage()}"
        // Не прерываем выполнение, продолжаем с базовой информацией
    }
}