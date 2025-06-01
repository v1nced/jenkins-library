// ===== vars/sendTelegramNotification.groovy =====
def call(Map config) {
    echo "📱 Отправка уведомления в Telegram"
    
    try {
        def testSummary = [:]
        
        // Попытка прочитать результаты тестов
        if (fileExists("${env.ARTIFACTS_PATH}/test-summary.json")) {
            testSummary = readJSON file: "${env.ARTIFACTS_PATH}/test-summary.json"
        }
        
        def status = config.buildStatus ?: 'UNKNOWN'
        def statusEmoji = getStatusEmoji(status)
        def statusColor = getStatusColor(status)
        
        def message = """
🚀 *Тестирование завершено*

📦 *Проект:* ${config.projectName}
🌿 *Ветка:* ${config.branch}
👤 *Запустил:* ${config.buildUser}
🏗️ *Сборка:* #${env.BUILD_NUMBER}
${statusEmoji} *Статус:* ${status}

${testSummary.results ? """
📊 *Результаты тестов:*
• Всего: ${testSummary.results.totalTests}
• Прошло: ✅ ${testSummary.results.passedTests}
• Провалено: ❌ ${testSummary.results.failedTests}
• Пропущено: ⏭️ ${testSummary.results.skippedTests}
• Время: ⏱️ ${testSummary.results.testDuration}
""" : ""}

🔗 [Детали сборки](${config.buildUrl})
""".trim()

        // Отправка через HTTP API Telegram
        def telegramToken = env.TELEGRAM_BOT_TOKEN
        def chatId = config.chatId
        
        if (telegramToken && chatId) {
            def payload = [
                chat_id: chatId,
                text: message,
                parse_mode: "Markdown",
                disable_web_page_preview: true
            ]
            
            httpRequest(
                httpMode: 'POST',
                url: "https://api.telegram.org/bot${telegramToken}/sendMessage",
                contentType: 'APPLICATION_JSON',
                requestBody: groovy.json.JsonOutput.toJson(payload),
                validResponseCodes: '200'
            )
            
            echo "✅ Уведомление отправлено в Telegram"
        } else {
            echo "⚠️ Не настроены параметры для Telegram (TELEGRAM_BOT_TOKEN или chatId)"
        }
        
    } catch (Exception e) {
        echo "❌ Ошибка при отправке уведомления: ${e.getMessage()}"
        // Не прерываем выполнение пайплайна из-за ошибки уведомления
    }
}

// Вспомогательные функции
def getStatusEmoji(status) {
    switch(status) {
        case 'SUCCESS': return '✅'
        case 'FAILURE': return '❌'
        case 'UNSTABLE': return '⚠️'
        case 'ABORTED': return '🛑'
        default: return '❓'
    }
}

def getStatusColor(status) {
    switch(status) {
        case 'SUCCESS': return 'good'
        case 'FAILURE': return 'danger'
        case 'UNSTABLE': return 'warning'
        default: return '#439FE0'
    }
}

def getBranchList(repoUrl) {
    // Получение списка веток (упрощенная версия)
    return ['main', 'develop', 'staging', 'feature/*']
}