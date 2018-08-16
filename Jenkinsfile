node("${NODE_DEFINED}"){
    timestamps{
        cleanWs()
        stage("Checkout"){
            git branch: "develop",credentialsId: "${CREDENTIAL_ID}", url: 'http://git.fzyun.io/ids/ids-service.git'
        }
        //编译微服务代码
        stage("Start"){
            sh'''
                apt-get update
                apt-get install -y --no-install-recommends maven
                mvn clean package
            '''
        }
        //生成并推送 Dokcer 镜像
        stage("Build Docker"){
            sh'''
                export TAG=${PAAS_ENV}-build-${BUILD_NUMBER}
                export REGISTRY=${HUB} VERSION=1.0.0-SNAPSHOT
                mv -v file-server/target/ids-file-server-$VERSION.jar dockerfiles/ids-file-server/ids-file-server.jar
                mv -v federation/target/ids-user-federation-$VERSION.jar dockerfiles/ids-user-federation/ids-user-federation.jar
                mv -v user/target/ids-service-user-$VERSION.jar dockerfiles/ids-service-user/ids-service-user.jar
                cd dockerfiles && ./build.sh
            '''
        }
        //获取部署代码
        stage("Checkout Deploy"){
             git branch: 'develop', credentialsId: "${CREDENTIAL_ID}", url: 'http://git.fzyun.io/ids/ids-paas-deploy.git'
        }
        //部署至云平台
        stage("Deploy"){
            sh'''
                export VOLUME_DRIVER=rancher-nfs MYSQL_ROOT_PASSWORD=Founder123
                export TAG1=${PAAS_ENV}-build-$BUILD_NUMBER TAG2=${PAAS_ENV}-build-${BUILD_NUMBER} TAG3=${PAAS_ENV}-build-${BUILD_NUMBER}
                export IDS_KEYCLOAK_SERVERURL=${IDS_KEYCLOAK_SERVERURL:="http://kc.kc.fzyun.io/auth"}
                rancher-compose -p 'ids-backbone' -f ids-backbone/0/docker-compose.yml up -d -p --force-recreate --confirm-upgrade
            '''
        }
    }
}