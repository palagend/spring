node {
   def mvnHome
   stage('Preparation') { // for display purposes
    git branch: 'develop', credentialsId: 'huyh', url: 'http://git.fzyun.io/ids/ids-service'
   }
   stage('Build') {
    sh '''  sudo apt-get update
            sudo apt-get install -y --no-install-recommends maven
            mvn clean package'''

   }
   stage('Results') {
      archiveArtifacts '**/target/*.jar'
   }
   stage('BuildDocker'){
       sh '''
        export VERSION=1.0.0-SNAPSHOT TAG=dev${BUILD_NUMBER}
        mv -v file-server/target/ids-file-server-$VERSION.jar dockerfiles/ids-file-server/ids-file-server.jar
        mv -v federation/target/ids-user-federation-$VERSION.jar dockerfiles/ids-user-federation/ids-user-federation.jar
        mv -v user/target/ids-service-user-$VERSION.jar dockerfiles/ids-service-user/ids-service-user.jar
        cd dockerfiles && ./build.sh
       '''
   }
}