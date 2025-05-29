import com.utils.GitHelper

def call(String repoUrl, String gitCredentialsId = null) {
    def git = new GitHelper(this)

    def branches = git.getRemoteBranches(repoUrl, gitCredentialsId)
    if (branches.isEmpty()) {
        error "Нет доступных веток в репозитории: ${repoUrl}"
    }

    return git.chooseBranch(branches)
}
