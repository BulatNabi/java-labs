<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Раз</title>
</head>
<body>

<jsp:useBean id="calc" scope="session" class="jspappl.Calculator" />
<jsp:setProperty name="calc" property="fio" value="Набиуллин Булат Раязович" />

<h1>Раз</h1>

<h2>Задание на лабораторную работу</h2>
<p>
    Web-приложение из трёх JSP-страниц со связкой через Bean-компонент.
    На <b>Главной странице</b> вычисляется функция из лабораторной работы №1 —
    разделение последовательности целых чисел на две подпоследовательности:
    на нечётных позициях (1, 3, 5, ...) и на чётных позициях (2, 4, 6, ...).
    Результаты выводятся на <b>Финишной странице</b>.
</p>

<h3>Студент: <jsp:getProperty name="calc" property="fio" /></h3>

<p><a href="jsp_2.jsp">Перейти на Главную страницу</a></p>

</body>
</html>
