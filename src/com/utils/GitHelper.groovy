package com.utils

class GitHelper implements Serializable {
    def script

    GitHelper(script) {
        this.script = script
    }

    List<String> getRemoteBranches(String repoUrl, String gitCredentialsId = null) {
        def branches = ''
        if (gitCredentialsId) {
            script.withCredentials([
                script.usernamePassword(credentialsId: gitCredentialsId, usernameVariable: 'GIT_USER', passwordVariable: 'GIT_PASS')
            ]) {
                branches = script.sh(
                    script: """
                        git ls-remote --heads https://\$GIT_USER:\$GIT_PASS@${repoUrl.replaceFirst('https://', '')} |
                        awk '{print \$2}' | sed 's|refs/heads/||'
                    """,
                    returnStdout: true
                ).trim()
            }
        } else {
            branches = script.sh(
                script: """
                    git ls-remote --heads ${repoUrl} |
                    awk '{print \$2}' | sed 's|refs/heads/||'
                """,
                returnStdout: true
            ).trim()
        }

        return branches ? branches.split("\n") : []
    }

    String chooseBranch(List<String> branches) {
        return script.input(
            message: 'Выберите ветку для тестирования:',
            parameters: [
                [$class: 'ChoiceParameterDefinition', choices: branches.join('\n'), description: '', name: 'Branch']
            ]
        )
    }

    void cloneBranch(String repoUrl, String branch, String gitCredentialsId, boolean recurseSubmodules = false) {
        if (gitCredentialsId != 'false') {
            script.withCredentials([gitUsernamePassword(credentialsId: "${gitCredentialsId}", gitToolName: 'git')]) {
                script.sh "git clone --branch ${branch} ${repoUrl}"
            }
        } else {
            script.sh "git clone --branch ${branch} ${repoUrl}"
        }
    }

    void initAndUpdateSubmodules(String repoDir = "source") {
        script.dir(repoDir) {
            script.sh 'git submodule init'
            script.sh 'git submodule update --recursive'
        }
    }
}
