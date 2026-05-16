<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Три</title>
</head>
<body>

<jsp:useBean id="calc" scope="session" class="jspappl.Calculator" />
<jsp:setProperty name="calc" property="input" param="nums" />

<h1>Три</h1>

<table>
    <tr><td><h3>Студент: <jsp:getProperty name="calc" property="fio" /></h3></td></tr>
    <tr><td><h3>Исходные числа: <jsp:getProperty name="calc" property="input" /></h3></td></tr>
    <tr><td><h3>Нечётные позиции: <%= calc.getOddPositions() %></h3></td></tr>
    <tr><td><h3>Чётные позиции: <%= calc.getEvenPositions() %></h3></td></tr>
    <%
        List<String> bad = calc.getBad();
        if (!bad.isEmpty()) {
    %>
        <tr><td><h3>Не удалось распознать: <%= bad %></h3></td></tr>
    <%
        }
    %>
</table>

<hr>
<p><a href="jsp_2.jsp">Возврат на Главную страницу</a></p>

</body>
</html>
