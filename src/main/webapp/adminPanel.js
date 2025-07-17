

let selectedRowData = null;


function loadTable(tableName, clickedButton) {

    document.querySelectorAll(".table-btn").forEach(btn => {
        btn.classList.remove("active");
    });
    clickedButton.classList.add("active");

    const container = document.getElementById("dataContainer");
    container.innerHTML = "";

    const addBtnContainer = document.getElementById("addBtnContainer");

        if (tableName === "subscribers") {
            addBtnContainer.classList.remove("d-none");
            addBtnContainer.classList.add("d-flex");
        } else {
            addBtnContainer.classList.remove("d-flex");
            addBtnContainer.classList.add("d-none");
        }


    fetch("DataServlet?table=" + tableName)
        .then(response => response.json())
        .then(data => {
            if (data.length === 0) {
                container.innerHTML = "<p class='text-muted'>No data found.</p>";
                return;
            }

            const table = document.createElement("table");
            table.className = "table table-bordered table-hover table-striped";

            const thead = document.createElement("thead");
            const headerRow = document.createElement("tr");
            Object.keys(data[0]).forEach(key => {
                const th = document.createElement("th");
                th.innerText = key;
                headerRow.appendChild(th);
            });
            thead.appendChild(headerRow);
            table.appendChild(thead);

            const tbody = document.createElement("tbody");
            data.forEach(row => {
                const tr = document.createElement("tr");
                Object.values(row).forEach(value => {
                    const td = document.createElement("td");
                    td.innerText = value;
                    tr.appendChild(td);
                });
                tbody.appendChild(tr);
            });
            table.appendChild(tbody);

            const wrapper = document.createElement("div");
            wrapper.className = "table-responsive";
            wrapper.appendChild(table);
            container.appendChild(wrapper);
            makeRowsSelectable(table);
        })
        .catch(error => {
            console.error("Error loading data:", error);
            container.innerHTML = "<p class='text-danger'>Error loading data.</p>";
        });
}

function makeRowsSelectable(table) {
    const rows = table.getElementsByTagName("tr");

    for (let i = 1; i < rows.length; i++) {
        rows[i].onclick = function () {
            for (let j = 1; j < rows.length; j++) {
                rows[j].classList.remove("selected");
            }
            this.classList.add("selected");

            const cells = this.getElementsByTagName("td");
            let rowData = {};
            const headers = table.getElementsByTagName("th");
            for (let k = 0; k < cells.length; k++) {
                rowData[headers[k].innerText] = cells[k].innerText;
            }
            console.log("Selected row data:", rowData);
        }
    }
}

document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("subscriberForm");
    if (form) {
        form.addEventListener("submit", function (e) {
            e.preventDefault();

            const url = document.getElementById("url").value;
            const offset = document.getElementById("offset").value;

            fetch("AddSubscriberServlet", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body: "url=" + encodeURIComponent(url) + "&offset=" + encodeURIComponent(offset)
            })
            .then(response => {
                if (response.ok) {
                    alert("Subscriber added successfully.");
                    form.reset();

                    const modalElement = document.getElementById("addSubscriberModal");
                    const modalInstance = bootstrap.Modal.getInstance(modalElement);
                    modalInstance.hide();

                    loadTable("subscribers", document.querySelector(".table-btn.active"));
                } else {
                    alert("Failed to add subscriber.");
                }
            })
            .catch(error => {
                console.error("Error:", error);
                alert("Error occurred.");
            });
        });
    }

    const firstBtn = document.querySelector(".table-btn");
    if (firstBtn) loadTable("subscribers", firstBtn);
});

function openAddModal() {
    const modal = new bootstrap.Modal(document.getElementById("addSubscriberModal"));
    modal.show();
}

function makeRowsSelectable(table) {
    const rows = table.getElementsByTagName("tr");

    for (let i = 1; i < rows.length; i++) {
        rows[i].onclick = function () {
            const isAlreadySelected = this.classList.contains("selected");

            // Seçimi kaldır
            for (let j = 1; j < rows.length; j++) {
                rows[j].classList.remove("selected");
            }

            if (!isAlreadySelected) {
                this.classList.add("selected");

                const cells = this.getElementsByTagName("td");
                const headers = table.getElementsByTagName("th");
                selectedRowData = {};
                for (let k = 0; k < cells.length; k++) {
                    const key = headers[k].innerText.trim();  // boşlukları temizle
                    selectedRowData[key] = cells[k].innerText;
                }
                console.log("Selected row data:", selectedRowData);
            } else {
                selectedRowData = null;
                console.log("Selection cleared");
            }
        };
    }
}

function deleteSelectedRow() {
    if (!selectedRowData || !selectedRowData["id"]) {
        alert("Please select a row to delete.");
        return;
    }

    if (!confirm("Are you sure you want to delete this subscriber?")) return;

    const id = selectedRowData["id"];

    fetch("DeleteSubscriberServlet", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: "id=" + encodeURIComponent(id)
    })
    .then(response => {
        if (response.ok) {
            alert("Subscriber deleted.");
            selectedRowData = null;
            loadTable("subscribers", document.querySelector(".table-btn.active"));
        } else {
            alert("Failed to delete subscriber.");
        }
    })
    .catch(error => {
        console.error("Error deleting subscriber:", error);
        alert("Error occurred.");
    });
}