# jre基础环境
FROM openjdk:8-jre-alpine

# 维护者信息
MAINTAINER enba

#设置时区
ENV TZ=Asia/Chongqing

# 添加jar包到容器中
ADD target/enba-integrate-wechat-native-1.0.0-SNAPSHOT.jar /home/
#复制证书
COPY apiclient_key.pem /home/apiclient_key.pem

# 对外暴漏的端口号,只是声明，实际以服务器启动的端口为准
EXPOSE 80

# RUN
CMD nohup java -jar /home/enba-integrate-wechat-native-1.0.0-SNAPSHOT.jar >> /home/logs/app.log 2>&1 & \
    echo "****** 查看日志..." & \
    tail -f /home/logs/app.log
