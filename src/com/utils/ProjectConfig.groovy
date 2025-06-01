package com.company.jenkins

class ProjectConfig implements Serializable {
    
    static Map getDefaultConfig(String projectType) {
        switch(projectType) {
            case 'dotnet':
                return [
                    testConfiguration: 'Release',
                    buildTool: 'dotnet',
                    testPattern: '**/*Tests.csproj',
                    coverageThreshold: 80
                ]
            case 'npm':
                return [
                    testCommand: 'test',
                    buildTool: 'npm',
                    testPattern: 'test/**/*.spec.js',
                    coverageThreshold: 70
                ]
            default:
                return [:]
        }
    }
    
    static void validateConfig(Map config) {
        def required = ['projectName', 'projectType', 'repoUrl']
        def missing = required.findAll { !config.containsKey(it) }
        
        if (missing) {
            throw new IllegalArgumentException("Отсутствуют обязательные параметры: ${missing.join(', ')}")
        }
        
        if (!['dotnet', 'npm'].contains(config.projectType)) {
            throw new IllegalArgumentException("Неподдерживаемый тип проекта: ${config.projectType}")
        }
    }
}