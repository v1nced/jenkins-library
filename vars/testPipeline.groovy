def call(Map config) {
    pipeline {
        agent any
        
        parameters {
            choice(
                name: 'BRANCH_NAME',
                choices: getBranchList(config.repoUrl),
                description: 'Выберите ветку для тестирования'
            )
            booleanParam(
                name: 'SKIP_CACHE',
                defaultValue: false,
                description: 'Пропустить кеш зависимостей'
            )
        }
        
        environment {
            PROJECT_NAME = "${config.projectName}"
            PROJECT_TYPE = "${config.projectType}" // 'dotnet' или 'npm'
            REPO_URL = "${config.repoUrl}"
            TELEGRAM_CHAT_ID = "${config.telegramChatId}"
            BUILD_USER = "${currentBuild.getBuildCauses('hudson.model.Cause$UserIdCause')[0]?.userId ?: 'Jenkins'}"
        }
        
        stages {
            stage('Подготовка среды') {
                steps {
                    script {
                        setupEnvironment([
                            projectName: env.PROJECT_NAME,
                            projectType: env.PROJECT_TYPE,
                            branch: params.BRANCH_NAME,
                            repoUrl: env.REPO_URL,
                            skipCache: params.SKIP_CACHE
                        ])
                    }
                }
            }
            
            stage('Запуск тестов') {
                steps {
                    script {
                        if (env.PROJECT_TYPE == 'dotnet') {
                            dotnetTest([
                                projectPath: env.WORKSPACE,
                                configuration: config.testConfiguration ?: 'Release'
                            ])
                        } else if (env.PROJECT_TYPE == 'npm') {
                            npmTest([
                                projectPath: env.WORKSPACE,
                                testCommand: config.testCommand ?: 'test:e2e'
                            ])
                        }
                    }
                }
            }
            
            stage('Сбор результатов') {
                steps {
                    script {
                        collectTestResults([
                            projectName: env.PROJECT_NAME,
                            projectType: env.PROJECT_TYPE,
                            buildUser: env.BUILD_USER,
                            branch: params.BRANCH_NAME
                        ])
                    }
                }
            }
        }
        
        post {
            always {
                script {
                    sendTelegramNotification([
                        chatId: env.TELEGRAM_CHAT_ID,
                        projectName: env.PROJECT_NAME,
                        buildStatus: currentBuild.currentResult,
                        buildUser: env.BUILD_USER,
                        branch: params.BRANCH_NAME,
                        buildUrl: env.BUILD_URL
                    ])
                }
                
                // Очистка workspace
                cleanWs()
            }
        }
    }
}