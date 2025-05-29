@Library('my-shared-lib') _

node {
    stage('Select branch') {
        def repoUrl = 'https://github.com/your-org/your-repo.git'
        def gitCreds = 'my-git-creds-id'
        branch = selectGitBranch(repoUrl, gitCreds)
    }

    stage('Clone') {
        cloneGitRepo('https://github.com/your-org/your-repo.git', branch, 'source', 'my-git-creds-id', true)
    }

    stage('Test') {
        dir('source') {
            dotnetTest()
        }
    }
}