import com.utils.GitHelper

def call(String repoUrl, String branch, String gitCredentialsId, boolean recurseSubmodules = false) {
    def git = new GitHelper(this)
    git.cloneBranch(repoUrl, branch, gitCredentialsId, recurseSubmodules)
}
