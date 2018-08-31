node("${NODE_DEFINED}"){
    timestamps{
        cleanWs()
        stage("Checkout"){
            checkout scm
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
        stage("Deploy"){
            sh './cmd/deploy.sh'
        }
    }
}
