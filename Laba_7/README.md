

### Настройка порта 9090 (П.11 = 1)

Изменить порт в конфиге Tomcat:

```bash
# Файл: /opt/homebrew/opt/tomcat@9/libexec/conf/server.xml
# Заменить port="8080" на port="9090":
```

```xml
<Connector port="9090" protocol="HTTP/1.1"
           connectionTimeout="20000"
           redirectPort="8443" />
```

### Сборка WAR и деплой

```bash
cd Laba_7

# Компиляция
cd src/main/java
javac -encoding UTF-8 -cp ../../../lib/javax.servlet-api-4.0.1.jar org/example/*.java
cd ../../..

# Сборка WAR
mkdir -p build/WEB-INF/classes/org/example
cp src/main/java/org/example/Servlet1.class build/WEB-INF/classes/org/example/
cd build && jar cvf ../lab7.war . && cd ..

# Деплой в Tomcat
cp lab7.war /opt/homebrew/opt/tomcat@9/libexec/webapps/
```

### Запуск Tomcat

```bash
# Запуск (в терминале, с логами)
/opt/homebrew/opt/tomcat@9/bin/catalina run

# Или в фоне
brew services start tomcat@9
```

### Открыть в браузере

```
http://localhost:9090/lab7/ServletAppl?fio=Иванов И.И.&group=4233&nums=10,20,30,40,50
```

### Остановка Tomcat

```bash
/opt/homebrew/opt/tomcat@9/bin/catalina stop
# или
brew services stop tomcat@9
```

