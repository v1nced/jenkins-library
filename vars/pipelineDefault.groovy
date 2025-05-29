def call (Map args = [
        git_repo: '',
        git_creds: '',
        dotnet_test: '',
        npm_build: '',
        send_notification: '',
        debug_mode: ''
]) {
    pipeline{
        agent any
        environment{
            GIT_REPO = "${args.git_repo ?: env.GIT_REPO ?: 'false'}"
            GIT_CREDS = "${args.git_creds ?: env.GIT_CREDS ?: ''}"
            DOTNET_TEST = "${args.dotnet_test ?: env.DOTNET_TEST ?: 'false'}"
            NPM_BUILD = "${args.npm_build ?: env.NPM_BUILD ?: 'false'}"
            SEND_NOTIFICATION = "${args.send_notification ?: env.SEND_NOTIFICATION ?: 'false'}"
            DEBUG_MODE = "${args.debug_mode ?: env.DEBUG_MODE ?: 'false'}"
        }
        stages{
            stage('Prep'){
                when{
                    expression{env.DEBUG_MODE == "true"}
                }
                steps{
                    script{
                        cloneGitRepo("$GIT_REPO", "main", "$GIT_CREDS", false)
                    }
                }
            }
            stage('Dotnet Test'){
                when{
                    expression{env.DEBUG_MODE == "true"}
                }
                steps{
                    script{
                        echo "running DOTNET test"
                        echo "Test FAILED" | grep "FAILED" && unstable: "error"

                    }
                }
            }
            stage('NPM Test'){
                when{
                    expression{env.DEBUG_MODE == "true"}
                }
                steps{
                    script{
                        echo "running NPM test"
                    }
                }
            }
        }
    }
}
