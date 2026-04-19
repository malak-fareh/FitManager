package main.java.com.example.Servlets;

import main.java.com.example.dao.StatsDAO;
import main.java.com.example.dao.StatsDAO.LocalStat;
import main.java.com.example.dao.StatsDAO.MonthlyStat;
import main.java.com.example.model.Abonnement;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "StatsServlets", urlPatterns = "/stats")
public class StatsServlets extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

    private String jdbcDriver;
    private String jdbcUrl;
    private String jdbcUser;
    private String jdbcPassword;
    private StatsDAO statsDAO;

    // -------------------------------------------------------------------------
    // Initialisation
    // -------------------------------------------------------------------------

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        jdbcDriver   = getInitParam(config, "jdbcDriver",   DEFAULT_JDBC_DRIVER);
        jdbcUrl      = getInitParam(config, "jdbcUrl",      null);
        jdbcUser     = getInitParam(config, "jdbcUser",     "");
        jdbcPassword = getInitParam(config, "jdbcPassword", "");

        if (jdbcUrl == null || jdbcUrl.isEmpty()) {
            throw new ServletException("StatsServlets : paramètre 'jdbcUrl' requis");
        }
        try {
            Class.forName(jdbcDriver);
        } catch (ClassNotFoundException e) {
            throw new ServletException("Impossible de charger le driver JDBC : " + jdbcDriver, e);
        }
        statsDAO = new StatsDAO();
    }

    // -------------------------------------------------------------------------
    // Routage GET
    // -------------------------------------------------------------------------

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            action = "summary";
        }

        try (Connection conn = getConnection()) {
            switch (action.toLowerCase()) {
                case "total":
                    writeJson(response, buildTotal(conn));
                    break;
                case "active":
                    writeJson(response, buildActive(conn));
                    break;
                case "bystatus":
                    writeJson(response, buildCountByStatus(conn, request));
                    break;
                case "bylocal":
                    writeJson(response, buildLocalStats(conn));
                    break;
                case "monthly":
                    writeJson(response, buildMonthlyStats(conn, request));
                    break;
                case "summary":
                default:
                    writeJson(response, buildSummary(conn, request));
                    break;
            }
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Erreur serveur : " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Constructeurs de réponse
    // -------------------------------------------------------------------------

    private Object buildTotal(Connection conn) throws SQLException {
        return new SimpleResponse("total", statsDAO.countTotal(conn));
    }

    private Object buildActive(Connection conn) throws SQLException {
        return new SimpleResponse("active", statsDAO.countActive(conn));
    }

    private Object buildCountByStatus(Connection conn, HttpServletRequest request)
            throws SQLException {
        Abonnement.Statut statut = parseStatut(request.getParameter("statut"));
        return new SimpleResponse("countByStatus", statsDAO.countByStatus(conn, statut));
    }

    private List<LocalStat> buildLocalStats(Connection conn) throws SQLException {
        return statsDAO.countByLocal(conn);
    }

    private List<MonthlyStat> buildMonthlyStats(Connection conn, HttpServletRequest request)
            throws SQLException {
        Integer monthsBack = parseInteger(request.getParameter("monthsBack"));
        if (monthsBack == null || monthsBack <= 0) monthsBack = 6;
        return statsDAO.subscriptionsPerMonth(conn, monthsBack);
    }

    /**
     * CORRECTION : parseInteger appelé une seule fois (variable locale)
     * au lieu de deux fois avec une expression ternaire imbriquée.
     */
    private Object buildSummary(Connection conn, HttpServletRequest request) throws SQLException {
        Integer monthsBack = parseInteger(request.getParameter("monthsBack"));
        if (monthsBack == null || monthsBack <= 0) monthsBack = 6;

        StatsSummary summary = new StatsSummary();
        summary.total   = statsDAO.countTotal(conn);
        summary.active  = statsDAO.countActive(conn);
        summary.byLocal = statsDAO.countByLocal(conn);
        summary.byMonth = statsDAO.subscriptionsPerMonth(conn, monthsBack);
        return summary;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);
    }

    private static Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static Abonnement.Statut parseStatut(String value) {
        if (value == null || value.trim().isEmpty()) return Abonnement.Statut.ACTIF;
        try {
            return Abonnement.Statut.fromString(value.trim());
        } catch (IllegalArgumentException ignored) {
            return Abonnement.Statut.ACTIF;
        }
    }

    private void writeJson(HttpServletResponse response, Object object) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.print(toJson(object));
        }
    }

    private String toJson(Object object) {
        if (object == null) return "null";

        if (object instanceof SimpleResponse) {
            SimpleResponse r = (SimpleResponse) object;
            return "{\"type\":\"" + escape(r.type) + "\",\"value\":" + r.value + "}";
        }
        if (object instanceof List) {
            StringBuilder sb = new StringBuilder("[");
            List<?> list = (List<?>) object;
            for (int i = 0; i < list.size(); i++) {
                sb.append(toJson(list.get(i)));
                if (i < list.size() - 1) sb.append(',');
            }
            sb.append("]");
            return sb.toString();
        }
        if (object instanceof LocalStat) {
            LocalStat stat = (LocalStat) object;
            return "{\"localId\":" + stat.getLocalId()
                    + ",\"nom\":\"" + escape(stat.getNom()) + "\""
                    + ",\"count\":" + stat.getCount() + "}";
        }
        if (object instanceof MonthlyStat) {
            MonthlyStat stat = (MonthlyStat) object;
            return "{\"year\":"   + stat.getYear()
                    + ",\"month\":" + stat.getMonth()
                    + ",\"count\":" + stat.getCount() + "}";
        }
        if (object instanceof StatsSummary) {
            StatsSummary s = (StatsSummary) object;
            return "{\"total\":"    + s.total
                    + ",\"active\":" + s.active
                    + ",\"byLocal\":" + toJson(s.byLocal)
                    + ",\"monthly\":" + toJson(s.byMonth)
                    + "}";
        }
        return quote(object.toString());
    }

    private String quote(String value) {
        if (value == null) return "null";
        return "\"" + escape(value) + "\"";
    }

    private String escape(String value) {
        if (value == null) return "";
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private String getInitParam(ServletConfig config, String name, String defaultValue) {
        String value = config.getInitParameter(name);
        return (value == null || value.trim().isEmpty()) ? defaultValue : value.trim();
    }

    // -------------------------------------------------------------------------
    // Classes internes
    // -------------------------------------------------------------------------

    private static class SimpleResponse {
        private final String type;
        private final int    value;

        SimpleResponse(String type, int value) {
            this.type  = type;
            this.value = value;
        }
    }

    private static class StatsSummary {
        int total;
        int active;
        List<LocalStat>   byLocal;
        List<MonthlyStat> byMonth;
    }
}