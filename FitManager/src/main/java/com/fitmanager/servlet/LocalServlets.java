package com.fitmanager.servlet;

import com.fitmanager.dao.LocalDAO;
import com.fitmanager.model.Local;
import com.fitmanager.util.DBConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Servlet REST pour la gestion des locaux (salles).
 *
 * GET  /locaux              â†’ liste tous les locaux
 * GET  /locaux?action=view&id=X  â†’ dÃ©tail d'un local
 * POST /locaux action=create     â†’ crÃ©er un local
 * POST /locaux action=update     â†’ modifier un local
 * DELETE /locaux?id=X            â†’ supprimer un local
 */
@WebServlet(name = "LocalServlets", urlPatterns = "/locaux-api")
public class LocalServlets extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private LocalDAO localDAO;

    // -------------------------------------------------------------------------
    // Initialisation
    // -------------------------------------------------------------------------

    @Override
    public void init() throws ServletException {
        localDAO = new LocalDAO();
    }

    // -------------------------------------------------------------------------
    // Routage GET
    // -------------------------------------------------------------------------

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        if (action == null || action.isEmpty() || "list".equalsIgnoreCase(action)) {
            listLocaux(response);
            return;
        }

        switch (action.toLowerCase()) {
            case "view":
                viewLocal(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Action GET inconnue : " + action);
        }
    }

    // -------------------------------------------------------------------------
    // Routage POST
    // -------------------------------------------------------------------------

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ParamÃ¨tre 'action' requis");
            return;
        }

        switch (action.toLowerCase()) {
            case "create":
                createLocal(request, response);
                break;
            case "update":
                updateLocal(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Action POST inconnue : " + action);
        }
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        deleteLocal(request, response);
    }

    // -------------------------------------------------------------------------
    // Handlers
    // -------------------------------------------------------------------------

    private void listLocaux(HttpServletResponse response) throws IOException {
        try (Connection conn = getConnection()) {
            List<Local> locaux = localDAO.findAll(conn);
            writeJson(response, locaux);
        } catch (SQLException e) {
            sendServerError(response, e);
        }
    }

    private void viewLocal(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Integer id = parseInteger(request.getParameter("id"));
        if (id == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ParamÃ¨tre 'id' requis");
            return;
        }
        try (Connection conn = getConnection()) {
            Optional<Local> local = localDAO.findById(conn, id);
            if (local.isPresent()) {
                writeJson(response, local.get());
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "Local introuvable pour id=" + id);
            }
        } catch (SQLException e) {
            sendServerError(response, e);
        }
    }

    private void createLocal(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Local local = parseLocalFromRequest(request, false);
        if (local == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "ParamÃ¨tres invalides : nom, adresse et capaciteMax sont requis");
            return;
        }
        try (Connection conn = getConnection()) {
            int generatedId = localDAO.insert(conn, local);
            local.setId(generatedId);
            response.setStatus(HttpServletResponse.SC_CREATED);
            writeJson(response, local);
        } catch (SQLException e) {
            sendServerError(response, e);
        }
    }

    private void updateLocal(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Local local = parseLocalFromRequest(request, true);
        if (local == null || local.getId() <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "ParamÃ¨tres invalides pour la mise Ã  jour");
            return;
        }
        try (Connection conn = getConnection()) {
            boolean updated = localDAO.update(conn, local);
            if (updated) {
                writeJson(response, local);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "Local introuvable pour id=" + local.getId());
            }
        } catch (SQLException e) {
            sendServerError(response, e);
        }
    }

    private void deleteLocal(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Integer id = parseInteger(request.getParameter("id"));
        if (id == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ParamÃ¨tre 'id' requis");
            return;
        }
        try (Connection conn = getConnection()) {
            boolean deleted = localDAO.delete(conn, id);
            if (deleted) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "Local introuvable pour id=" + id);
            }
        } catch (SQLException e) {
            sendServerError(response, e);
        }
    }

    // -------------------------------------------------------------------------
    // Parsing des paramÃ¨tres
    // -------------------------------------------------------------------------

    private Local parseLocalFromRequest(HttpServletRequest request, boolean requireId) {
        Integer id          = parseInteger(request.getParameter("id"));
        String  nom         = request.getParameter("nom");
        String  adresse     = request.getParameter("adresse");
        Integer capaciteMax = parseInteger(request.getParameter("capaciteMax"));

        if (isBlank(nom) || isBlank(adresse) || capaciteMax == null || capaciteMax <= 0) {
            return null;
        }
        if (requireId && id == null) {
            return null;
        }

        Local local = new Local(nom.trim(), adresse.trim(), capaciteMax);
        if (id != null) local.setId(id);
        return local;
    }

    private static Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Connection getConnection() throws SQLException {
        return DBConnection.getConnection(getServletContext());
    }

    private void writeJson(HttpServletResponse response, Object object) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter writer = response.getWriter()) {
            if (object instanceof List) {
                StringBuilder sb = new StringBuilder("[");
                List<?> list = (List<?>) object;
                for (int i = 0; i < list.size(); i++) {
                    sb.append(toJson(list.get(i)));
                    if (i < list.size() - 1) sb.append(',');
                }
                sb.append("]");
                writer.print(sb.toString());
            } else {
                writer.print(toJson(object));
            }
        }
    }

    private String toJson(Object object) {
        if (object == null) return "null";
        if (object instanceof Local) {
            Local l = (Local) object;
            return "{"
                    + "\"id\":"          + l.getId()
                    + ",\"nom\":"        + quote(l.getNom())
                    + ",\"adresse\":"    + quote(l.getAdresse())
                    + ",\"capaciteMax\":" + l.getCapaciteMax()
                    + "}";
        }
        return quote(object.toString());
    }

    private String quote(String value) {
        if (value == null) return "null";
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private void sendServerError(HttpServletResponse response, SQLException e) throws IOException {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Erreur serveur : " + e.getMessage());
    }
}
