// src/com/company/jenkins/TestRunner.groovy
package com.company.jenkins

class TestRunner implements Serializable {
    
    def script
    
    TestRunner(script) {
        this.script = script
    }
    
    def runTests(Map config) {
        script.echo "🚀 Запуск тестов для ${config.projectType} проекта"
        
        switch(config.projectType) {
            case 'dotnet':
                return runDotNetTests(config)
            case 'npm':
                return runNpmTests(config)
            default:
                throw new UnsupportedOperationException("Неподдерживаемый тип проекта: ${config.projectType}")
        }
    }
    
    private def runDotNetTests(Map config) {
        def commands = [
            "dotnet restore ${config.projectPath}",
            "dotnet build ${config.projectPath} --configuration ${config.configuration} --no-restore",
            "dotnet test ${config.projectPath} --configuration ${config.configuration} --no-build --logger trx --results-directory test-results"
        ]
        
        commands.each { command ->
            script.sh command
        }
        
        return collectDotNetResults()
    }
    
    private def runNpmTests(Map config) {
        script.dir(config.projectPath) {
            script.sh "npm ci"
            script.sh "npm run ${config.testCommand}"
        }
        
        return collectNpmResults()
    }
    
    private def collectDotNetResults() {
        // Логика сбора результатов .NET тестов
        return [
            testFramework: 'MSTest/NUnit/xUnit',
            resultsPath: 'test-results'
        ]
    }
    
    private def collectNpmResults() {
        // Логика сбора результатов npm тестов
        return [
            testFramework: 'Jest/Mocha/Cypress',
            resultsPath: 'test-results'
        ]
    }
}