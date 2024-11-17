## 制作Docker镜像

### 制作镜像 
```
docker build -f Dockerfile -t "enba-integrate-wechat-native" . --no-cache
```

### 运行
```
docker run -d -p 80:80 -v /usr/local/project/enba-integrate-wechat-native-logs:/home/logs --name enba-integrate-wechat-native enba-integrate-wechat-native:latest
```

### 查看运行日志
```
docker logs -f enba-integrate-wechat-native
```

### 进入容器
```
docker exec -it enba-integrate-wechat-native sh
```