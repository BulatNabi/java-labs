package org.example;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {"/ServletAppl"})
public class Servlet1 extends HttpServlet {

    private static boolean trigger = false;
    private static long counter = 0;

    private static final int DEFAULT_SIZE = 1;
    private static final int MIN_SIZE = 6;
    private static int textSize = DEFAULT_SIZE;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        String fio   = request.getParameter("fio");
        String group = request.getParameter("group");
        String nums  = request.getParameter("nums");
        String reset = request.getParameter("reset");

        boolean wasReset = reset != null && (reset.equals("1") || reset.equalsIgnoreCase("true"));
        if (wasReset) {
            textSize = DEFAULT_SIZE;
        }

        trigger = !trigger;
        counter++;

        boolean cannotShrink = false;
        if (!wasReset) {
            if (textSize < MIN_SIZE) {
                textSize++;
            } else {
                cannotShrink = true;
            }
        }

        List<String> oddPositions  = new ArrayList<>();
        List<String> evenPositions = new ArrayList<>();
        List<String> bad = new ArrayList<>();
        if (nums != null && !nums.isEmpty()) {
            String[] tokens = nums.split("[,\\s]+");
            int pos = 1;
            for (String token : tokens) {
                if (token.isEmpty()) continue;
                try {
                    Integer.parseInt(token);
                } catch (NumberFormatException ex) {
                    bad.add(token);
                    continue;
                }
                if (pos % 2 != 0) {
                    oddPositions.add(token);
                } else {
                    evenPositions.add(token);
                }
                pos++;
            }
        }

        try (PrintWriter out = response.getWriter()) {
            String safeFio   = (fio   == null ? "не указано" : escape(fio));
            String safeGroup = (group == null ? "не указана" : escape(group));

            out.println("<!DOCTYPE html>");
            out.println("<html lang=\"ru\">");
            out.println("<head>");
            out.println("<meta charset=\"UTF-8\">");
            out.println("<title>" + safeFio + ", группа " + safeGroup + "</title>");
            out.println("</head>");
            out.println("<body>");

            out.println("<h2>ServletAppl" + request.getServletPath() + "</h2>");

            String openH  = "<h" + textSize + ">";
            String closeH = "</h" + textSize + ">";

            out.println("<table>");

            out.println("<tr><td>" + openH + "Триггер: " + trigger + closeH + "</td></tr>");

            out.println("<tr><td>" + openH + "Счётчик обращений: " + counter + closeH + "</td></tr>");

            out.println("<tr><td>" + openH + "Введённые числа: "
                    + escape(nums == null ? "—" : nums) + closeH + "</td></tr>");

            if (!bad.isEmpty()) {
                out.println("<tr><td>" + openH + "Не удалось распознать: "
                        + escape(bad.toString()) + closeH + "</td></tr>");
            }

            out.println("<tr><td>" + openH
                    + "Нечётные позиции: " + oddPositions + closeH + "</td></tr>");
            out.println("<tr><td>" + openH
                    + "Чётные позиции: " + evenPositions + closeH + "</td></tr>");
            out.println("<tr><td>" + openH
                    + "Текущий размер текста: h" + textSize + closeH + "</td></tr>");

            out.println("</table>");

            if (cannotShrink) {
                out.println("<p><b>Дальнейшее уменьшение размера текста невозможно "
                        + "(достигнут минимум h" + MIN_SIZE + ").</b></p>");
                out.println("<p>Для сброса добавьте параметр <code>reset=1</code>.</p>");
            }

            out.println("<hr>");
            out.println("<p>Пример: <code>/ServletAppl?fio=Иванов И.И."
                    + "&amp;group=4233&amp;nums=10,20,30,40,50&amp;reset=1</code></p>");
            out.println("<p><i>Порт сервера: 9090 (п.11 = 1)</i></p>");

            out.println("</body>");
            out.println("</html>");
        }
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    @Override
    public String getServletInfo() {
        return "Lab 7, variant 2";
    }
}
