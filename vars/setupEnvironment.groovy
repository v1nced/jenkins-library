def call(Map config) {
    echo "🔧 Настройка среды для проекта ${config.projectName}"
    
    // Установка переменных окружения
    env.PROJECT_PATH = "${env.WORKSPACE}/${config.projectName}"
    env.TEST_RESULTS_PATH = "${env.WORKSPACE}/test-results"
    env.ARTIFACTS_PATH = "${env.WORKSPACE}/artifacts"
    
    // Создание необходимых директорий
    sh """
        mkdir -p ${env.TEST_RESULTS_PATH}
        mkdir -p ${env.ARTIFACTS_PATH}
    """
    
    // Клонирование репозитория с рекурсивными подмодулями
    checkout([
        $class: 'GitSCM',
        branches: [[name: "*/${config.branch}"]],
        doGenerateSubmoduleConfigurations: false,
        extensions: [
            [$class: 'SubmoduleOption',
             disableSubmodules: false,
             parentCredentials: true,
             recursiveSubmodules: true,
             reference: '',
             trackingSubmodules: false],
            [$class: 'CleanBeforeCheckout']
        ],
        submoduleCfg: [],
        userRemoteConfigs: [[
            credentialsId: 'git-credentials',
            url: config.repoUrl
        ]]
    ])
    
    echo "✅ Среда настроена успешно"
}