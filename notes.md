---
typora-copy-images-to: assets
typora-root-url: assets
---



# 一、简介

## 分布式概念基础

集群：提供一组相同服务的机器，通过负载均衡将请求转发到不同的服务器上处理

分布式：不同的模块独立部署，共同组成一套系统对外提供服务。【用户不会感受到有多态服务器】

微服务：拒绝大型单体应用，基于业务边界进行服务微化拆分。1、松耦合；2、可使用不同语言开发；3、能够被小团队开发，更专注于业务逻辑



## 远程调用

springcloud：http+json

dubbo：rpc



## 负载均衡

算法：1、轮询；2、最小连接【优先选择连接数最小的】；3、散列【同一个用户的请求会被发送到同一个服务器】



## 服务注册/发现&注册中心

集群中，B服务上线会注册到注册中心，A服务可以发现可用的B服务【避免调用不可用的服务】

![image-20200724223125032](/image-20200724223125032.png)



## 配置中心

集群中每个服务的配置都从配置中心获取，而不需要单独配置【集中管理微服务的配置】

![image-20200724223314698](/image-20200724223314698.png)





## 服务熔断&服务降级

请求积压：底层服务响应慢，造成上层服务响应慢，造成整个服务响应慢

服务熔断：设置服务的超时，当被调用的服务经常失败到达某个阈值，我们可以开启断路保护机制，后来的请求不再去调用这个服务。**本地直接返回默认的数据**

服务降级：在运维期间，当系统处于高峰期，系统资源紧张，我们可以让非核心业务降级运行。降级:某些服务不处理，或者简单处理【抛异常、返回NULL.调用Mock数据、调用Fallback处理逻辑】。



![image-20200724223713910](/image-20200724223713910.png)



## API网关



在微服务架构中，APIGateway作为整体架构的重要组件，它抽象了徼服务中都需要的公共功能，同时提供了客户端负载均衡，服务自动熔断，灰度发布，统—认证，限流流控，日志统计等丰富的功能，帮助我们解决很多API管理难题。

![image-20200724224004479](/image-20200724224004479.png)



## 项目图解

![image-20200724224342496](/image-20200724224342496.png)

---



模块：

admin-vue：后台管理的前端

shop-vue：web的前端



![image-20200724224841360](/image-20200724224841360.png)

# 二、环境搭建

## 1、安装虚拟机

```
1、下载virtualbox：https://mirror.tuna.tsinghua.edu.cn/help/virtualbox/

2、下载Vagrant【可以在virtualbox中快速创建虚拟机】：https://www.vagrantup.com/downloads.html Vagrant下载，下载windows版本
	使用vagrant查看是否安装成功
	在cmd中使用命令vagrant安装centos7，命令后面的系统名参照下面的官方镜像系统名：vagrant init centos/7【但是不推荐，下面的vagrant up会下载镜像很久】
	vagrant up【在有Vagrantfile文件的目录下启动】
```



---

【进这个网站看下】https://app.vagrantup.com/boxes/search    Vagrant官方镜像仓库

中国镜像：http://mirrors.ustc.edu.cn/centos-cloud/centos/6/vagrant/x86_64/images/

```sh
##推荐vagrant 使用下方的步骤来：##
3、去中科大镜像下载 .box文件  http://mirrors.ustc.edu.cn/centos-cloud/centos

4、打开https://www.jianshu.com/p/7e8f61376053，里面是命令详解

5、下载好后直接cmd cd到下载目录
	vagrant box add centos7 CentOS-7-x86_64-Vagrant-2004_01.VirtualBox.box
	vagrant init centos7【会在当前文件夹下创建Vagrantfile文件】
6、打开Vagrantfile文件，修改  config.vm.box = "centos7"

7、vagrant up启动【在有Vagrantfile文件的目录下启动】

8、可以在当前目录下vagrant ssh【直接连入虚拟机，已启动好不需要输入密码】
```

![1596167299452](/1596167299452.png)

## 2、配置虚拟机网络

**网络配置**:

默认是转发，访问本机3333转发到虚拟机的3306才能访问到mysql。修改网络配置给虚拟机分配一个IP与外网在同一个网端【也可以修改网卡的方式根vmware修改网络的方式一样】

![1596173116039](/1596173116039.png)

```sh
1、打开Vagrantfile文件下的 config.vm.network "private_network", ip: "192.168.33.10"配置

2、cmd ipconfig 查看ipv4地址-->以太网适配器 VirtualBox Host-Only Network: 192.168.56.1

3、将步骤1的IP修改成步骤2网端下的IP例：192.168.56.10

4、重启虚拟机vagrant reload【在Vagrantfile文件目录下使用该命令】

5、vagrant ssh连入虚拟机
```

## 3、xshell连接虚拟机

```sh
1、vagrant ssh连入虚拟机
    
2、切换su root，密码vagrant

3、vi /etc/ssh/sshd_config
	修改PasswordAuthentication yes

4、reboot 重启虚拟机

5、xshell连接，192.168.56.10 -- root + vagrant
```

## 4、虚拟机内安装docker

**简介docker**：虚拟容器技术，docker基于镜像。秒级启动各种容器。每一种容器都是一个完整的运行环境，容器之间互相隔离

docker类似于ghost工具，获取软件的镜像，docker可以根据镜像启动一个运行时环境

**每个容器都是一套单独运行的环境**

![1596180861811](/1596180861811.png)



---

docker安装可以参考官方文档：

​	https://www.docker.com->Resources->Docs->Get Docker->Docker Engine-Community ->Linux ->centOS 

路径可能修改了，直接访问这个： https://docs.docker.com/engine/install/centos/ 



然后可以参考该文档介绍了

```sh
1、所有软件镜像可以在该网站找到：hub.docker.com
2、卸载旧版本uninstall old versions
	yum remove docker \
                  docker-client \
                  docker-client-latest \
                  docker-common \
                  docker-latest \
                  docker-latest-logrotate \
                  docker-logrotate \
                  docker-engine
3、yum相关包
sudo yum install -y yum-utils
4、docker镜像地址，这里可以修改成aliyun镜像【https://developer.aliyun.com/article/110806】
sudo yum-config-manager \
    --add-repo \
    https://download.docker.com/linux/centos/docker-ce.repo
    【使用aliyun：sudo yum-config-manager --add-repo http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo】
    
5、安装docker--INSTALL DOCKER ENGINE
	sudo yum install docker-ce docker-ce-cli containerd.io

6、Start Docker.
 	sudo systemctl start docker 
 	
7、测试
	sudo docker run hello-world
	
8、docker images：查看镜像

9、开机自启：sudo systemctl enable docker
```

![1596176185250](/1596176185250.png)

## 5、添加docker镜像加速

1、默认从hub.docker下载软件镜像【很慢】

2、修改成aliyun的镜像加速器



阿里云加速： https://cr.console.aliyun.com/cn-hangzhou/instances/mirrors 

别人的文档： https://juejin.im/post/6844904181497757704 

```sh
1、执行：sudo mkdir -p /etc/docker
2、执行
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": ["https://7zi5cb9i.mirror.aliyuncs.com"]
}
EOF
3、重启：sudo systemctl daemon-reload
4、重启：sudo systemctl restart docker
```

## 6、安装mysql

去hub.docker.com查看版本，然后:加上版本，否则会下载最新版本

```sh
1、sudo docker pull mysql:5.7

2、
docker run -p 3306:3306 --name mysql \
-v /mydata/mysql/log:/var/log/mysql \
-v /mydata/mysql/data:/var/ib/mysql \
-v /mydata/mysql/conf:/etc/mysql \
-e MYSQL_ROOT_PASSWORD=root \
-d mysql:5.7

    参数说明
    -p 3306:3306:将容器的3306端口映射到主机的3306端口
    -v /mydata/mysql/conf:/etc/mysql:将配置文件夹挂载到主机【linux文件与容器内部的文件挂载】
    -v /mydata/mysql/log:/var/log/mysgl:将日志文件夹挂载到主机【不用进入到容器内部就能查看日志】
    -v /mydata/mysql/data:/var/ib/mysql/:将配置文件夹挂载到主机【相当于快捷方式】
    -e MYSQL_ROOT_PASSWORD=root: 初始化root用户的密码
   	-d 后台启动

3、查看正在运行中的docker容器：docker ps

4、进入容器内部
docker exec -it  mysql /bin/bash
whereis mysql

5、退出容器：exit

6、重启mysql：
	docker ps -a：查看id或name
	docker restart 1b4671904bfa：重启restart id或name

7、远程无法连接：https://blog.csdn.net/scarecrow__/article/details/81556845

8、在linux的 mydata/mysql/conf/my.cnf配置文件下加入下
[client]
default-character-set=utf8

[mysql]
default-character-set=utf8
[mysqld]
init_connect='SET collation_connection = utf8_unicode_ci'
init_connect='SET NAMES utf8'
character-set-server=utf8
collation-server=utf8_unicode_ci
skip-character-set-client-handshake
skip-name-resolve

9、重启：docker restart mysql
```

## 7、安装redis

```sh
1、docker pull redis

2、mkdir -p /mydata/redis/conf

3、touch /mydata/redis/conf/redis.conf

4、docker run -p 6379:6379 --name redis \
-v/mydata/redis/data:/data \
-v/mydata/redis/conf/redis.conf:/etc/redis/redis.conf \
-d redis redis-server /etc/redis/redis.conf

5、终止容器：docker stop redis

6、删除容器：docker rm redis

7、连接：
	方式1：
	docker exec -it redis /bin/bash
	redis-cli -p 6379
	方式2：
	docker exec -it redis redis-cli 

8、windows可视化客户端直接连接6379端口

```

---

---

默认情况：持久化未开启

```sh
9、开启持久化
vim /mydata/redis/conf/redis.conf
添加：appendonly yes

```

## 8、安装jdk、maven

1、配置环境变量jdk8

2、配置maven环境变量

​	 下载：https://maven.apache.org/download.cgi 

​	 配置： https://www.runoob.com/maven/maven-setup.html 

```sh
MAVEN_HOME，变量值：E:\Maven\apache-maven-3.3.9

Path，添加变量值：;%MAVEN_HOME%\bin
```

3、配置maven阿里云仓库

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">

  <localRepository>E:\java\apache\repository</localRepository>
    <mirrors>
	<mirror>
	    <id>aliyun</id>
	    <name>aliyun Maven</name>
	    <mirrorOf>central</mirrorOf>
	    <url>http://maven.aliyun.com/nexus/content/groups/public</url>
	   <!-- <url>http://maven.oschina.net/content/groups/public</url> -->
        </mirror>
    </mirrors>
	
  <profiles>
    <profile>
      <id>jdk-1.8</id>
      <activation>
		<activeByDefault>true</activeByDefault>
        <jdk>1.8</jdk>
      </activation>
	  <properties>
	    <maven.compiler.source>1.8</maven.compiler.source>
	    <maven.compiler.target>1.8</maven.compiler.target>
	    <maven.compiler.compilerVersion>1.8</maven.compiler.compilerVersion>
      </properties>
    </profile>
  </profiles>
</settings>
```

## 9、配置IDEA

1、configure配置maven和jdk

![1596195480839](/1596195480839.png)![1596195492134](/1596195492134.png)

2、下载lombok、mybatisx



![1596195629030](/1596195629030.png)

## 10、安装+配置vscode

1、下载： https://code.visualstudio.com/ 

2、安装插件

![1596197062491](/1596197062491.png)

![1596197257218](/1596197257218.png)

## 11、配置git

```sh
1、用户名
git config --global user.name "lemon"
2、邮箱
git config --global user.email "lemon_wan@aliyun.com"
3、配置ssh登录，不需要账号密码
ssh-keygen -t rsa -C "lemon_wan@aliyun.com"【三次回车】
4、查看
cat ~/.ssh/id_rsa.pub【C:\Users\Administrator\.ssh\id_rsa.pub】

5、复制内容：


6、复制内容，打开github添加ssh-key
settings->SSH and GPK keys
New ssh key

ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQC4TBUaL8w4yIq+NHAhnes5dctUemWr9i3Q4gbg8JvBA0dKxoZNeXf1Moi8rEU8uoxpISG5x47UCJ2cKop3Y1Zt7jpFk/qYjEp69QUaIXAlwDRtmcevTJ2bPnxofPT/oUDS99S/4ZZ10mBrjT4IttRU0E4gQddYZbRtd/X6vRIOY4fslzUoZhulNfXVeC5f66fPCdTHtMg7lTByrMWDFFr1pJMDQqOqPe9VVweKuP5wnp/JlEcg6zQT0K9bQGTtGrDsS9xKU8jxn9v+1mwysExZ0s9RdOtaqCoM5wjOuIEwqK6gMss46Su7pu7AG+Xmv7u0gL5T5D6hZlnJgR9m28JV lemon_wan@aliyun.com

7、测试
ssh -T git@github.com
```

8、IDEA创建Git项目
https://www.liaoxuefeng.com/wiki/896043488029600/900375748016320
