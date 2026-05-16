<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Два</title>
</head>
<body>

<jsp:useBean id="calc" scope="session" class="jspappl.Calculator" />

<%! int counter = 0; %>
<% counter++; %>

<h1>Два</h1>

<h3>Счётчик посещений Главной страницы: <%= counter %></h3>

<h2>Введите последовательность целых чисел</h2>
<p>Например: <code>10, 20, 30, 40, 50</code></p>

<form method="post" action="jsp_3.jsp">
    <p>
        <label>Числа (через запятую или пробел):</label><br>
        <input type="text" name="nums" size="50"
               value="<jsp:getProperty name='calc' property='input' />">
    </p>
    <p>
        <input type="submit" value="Вычислить и перейти на Финишную страницу">
    </p>
</form>

<hr>
<p><a href="jsp_1.jsp">Назад на Стартовую страницу</a></p>

</body>
</html>
