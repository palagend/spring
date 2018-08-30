# IDS API Server说明文档
## 环境变量
启动程序是必须指定的环境变量：  
1. CONFIG_SERVER_URI
2. KEYCLOAK_USER
3. KEYCLOAK_PASSWORD
4. RABBITMQ_USERNAME
5. RABBITMQ_PASSWORD

如果开启了Keycloak的Authorization功能，则需要额外指定的环境变量：

## 开发工作流
Localhost + ( ConfigServer + Git ) -> Docker -> PaaS >> Jenkins

## 依赖的(微)服务
* RabbitMQ
* Logstash
* Keycloak
* Database
* ConfigServer
* EurekaServer

## 其他
程序端口： 8880