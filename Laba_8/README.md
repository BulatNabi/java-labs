## Сборка и развёртывание

Предполагается что Tomcat 9 уже установлен и настроен на порт 9090 (см. Laba_7).

### Компиляция Bean

```bash
cd Laba_8/src/main/java
javac -encoding UTF-8 -cp ../../../lib/javax.servlet-api-4.0.1.jar jspappl/Calculator.java
```

### Сборка WAR

```bash
cd Laba_8
rm -rf build
mkdir -p build/WEB-INF/classes/jspappl
cp src/main/java/jspappl/Calculator.class build/WEB-INF/classes/jspappl/
cp src/main/webapp/*.jsp build/
cd build && jar cvf ../lab8.war . && cd ..
```

### Деплой

```bash
cp lab8.war /opt/homebrew/opt/tomcat@9/libexec/webapps/
```

Tomcat автоматически распаковывает WAR. Если Tomcat не запущен:

```bash
/opt/homebrew/opt/tomcat@9/bin/catalina run
```

---

## Использование

Открыть в браузере:

```
http://localhost:9090/lab8/jsp_1.jsp
```
