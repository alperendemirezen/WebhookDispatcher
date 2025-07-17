<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    if (session == null || session.getAttribute("adminUser") == null) {
        response.sendRedirect("login.jsp");
        return;
    }
    String username = (String) session.getAttribute("adminUser");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Admin Panel</title>

    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">

    <style>
        .table-responsive {
            max-height: 500px;
            overflow-y: auto;
        }

        .table-btn {
            background-color: lightgray;
            border: none;
            padding: 10px 15px;
            margin-right: 10px;
            cursor: pointer;
            font-weight: bold;
            border-radius: 5px;
            transition: all 0.2s ease;
        }

        .table-btn.active {
            background-color: steelblue;
            color: white;
        }

        .table th {
            background-color: #f8f9fa;
            font-weight: bold;
            text-align: center;
        }

        tr.selected {
            background-color: #0d6efd !important;
            color: white;
        }
        tr.selected td {
            background-color: #0d6efd !important;
            color: white;
        }
        .table-hover tbody tr:hover {
            background-color: #dbeafe;
        }

    </style>
</head>
<body class="bg-light">
<div class="container py-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h3 class="text-primary">Welcome, <%= username %></h3>
        <form action="LogoutServlet" method="post">
            <button class="btn btn-outline-danger btn-sm">Logout</button>
        </form>
    </div>

    <div class="btn-group mb-4">
        <button class="table-btn" onclick="loadTable('subscribers', this)">Subscribers</button>
        <button class="table-btn" onclick="loadTable('private_subscribers', this)">Private Subscribers</button>
        <button class="table-btn" onclick="loadTable('failed_messages', this)">Failed Messages</button>
    </div>

    <div id="dataContainer" class="bg-white p-3 rounded shadow-sm"></div>

    <div class="d-flex justify-content-end mt-4 mb-5 gap-3" id="addBtnContainer" style="display: none;">
        <button class="btn btn-primary" onclick="openAddModal()">Add Subscriber</button>
        <button class="btn btn-danger" onclick="deleteSelectedRow()">Delete Selected</button>
    </div>

    <div class="modal fade" id="addSubscriberModal" tabindex="-1" aria-labelledby="addSubscriberModalLabel" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <form id="subscriberForm">
            <div class="modal-header">
              <h5 class="modal-title" id="addSubscriberModalLabel">Add Subscriber</h5>
              <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
              <div class="mb-3">
                <label for="url" class="form-label">URL</label>
                <input type="text" class="form-control" id="url" name="url" required
                       placeholder="http://localhost:8080/KafkaWebHook">
              </div>
              <div class="mb-3">
                <label for="offset" class="form-label">Last Offset</label>
                <input type="number" class="form-control" id="offset" name="offset" required
                       placeholder="default: 0">
              </div>
            </div>
            <div class="modal-footer">
              <button type="submit" class="btn btn-success">Add</button>
            </div>
          </form>
        </div>
      </div>
    </div>


</div>

<script src="adminPanel.js"></script>
<!-- Bootstrap JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
