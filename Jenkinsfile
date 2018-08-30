node("${NODE_DEFINED}"){
    timestamps{
        cleanWs()
        stage("Checkout"){
            git branch: "${BRANCH}",credentialsId: "${CREDENTIAL_ID}", url: 'http://git.fzyun.io/huyh/ids-parent.git'
        }
        stage("Maven"){
            sh'''
                apt-get update
                apt-get install -y --no-install-recommends maven
            '''
        }
        stage("Build"){
            sh './cmd/build.sh'
        }
        stage("PaaS"){
            sh './cmd/paas.sh'
        }
    }
}