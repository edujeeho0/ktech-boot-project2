const jwt = localStorage.getItem("token");
if (!jwt) location.href = "/views/login";

const requestContainer = document.getElementById("request-container");
const pageForm = document.getElementById("page-form");
const pageInput = pageForm.querySelector("input");

pageForm.addEventListener("submit", e => {
  e.preventDefault();
  const page = parseInt(pageInput.value) - 1;
  location.href = `/views/admin/upgrade-requests?page=${page}`;
});

const setData = (requestPage) => {
  const { content, page } = requestPage;
  if (content.length === 0)
    pageInput.disabled = true;
  else pageInput.max = page.totalPages;

  if (content.length !== 0) {
    const table = document.createElement("table");
    table.className = "table table-hover";
    const head = document.createElement("thead");
    const headTr = document.createElement("tr");
    const headTdId = document.createElement("td");
    headTdId.innerText = "#";
    const headTdUser = document.createElement("td");
    headTdUser.innerText = "User";
    const headTdStatus = document.createElement("td");
    headTdStatus.innerText = "Status";
    const headTdActions = document.createElement("td");
    headTdActions.innerText = "Actions";
    headTr.append(headTdId, headTdUser, headTdStatus, headTdActions);
    head.append(headTr);
    table.append(head);

    const body = document.createElement("tbody");
    content.forEach(request => {
      const row = document.createElement("tr");
      row.style.verticalAlign = "middle";
      const rowTdId = document.createElement("td");
      rowTdId.innerText = request.id;
      const rowTdUser = document.createElement("td");
      rowTdUser.innerText = request.username;
      const rowTdStatus = document.createElement("td");
      const rowTdActions = document.createElement("td");
      if (request.approved) rowTdStatus.innerText = "Approved";
      else if (request.approved === false) rowTdStatus.innerText = "Disapproved";
      else {
        rowTdStatus.innerText = "Pending";
        const approveButton = document.createElement("button");
        approveButton.className = "btn btn-primary me-2";
        approveButton.innerText = "Approve";
        approveButton.addEventListener("click", e => {
          fetch(`/admin/upgrades/${request.id}`, {
            method: "put",
            headers: {
              "Authorization": `Bearer ${jwt}`,
            },
          }).then(response => {
            if (response.ok) location.reload();
            else alert(response.status);
          });
        });
        rowTdActions.append(approveButton);

        const disapproveButton = document.createElement("button");
        disapproveButton.className = "btn btn-danger";
        disapproveButton.innerText = "Disapprove";
        disapproveButton.addEventListener("click", e => {
          fetch(`/admin/upgrades/${request.id}`, {
            method: "delete",
            headers: {
              "Authorization": `Bearer ${jwt}`,
            },
          }).then(response => {
            if (response.ok) location.reload();
            else alert(response.status);
          });
        });
        rowTdActions.append(disapproveButton);
      }
      row.append(rowTdId, rowTdUser, rowTdStatus, rowTdActions);
      body.append(row);
    });
    table.append(body);

    requestContainer.append(table);
  }
  else {
    const messageHead = document.createElement("h3");
    messageHead.innerText = "No Requests";
    requestContainer.append(messageHead);
  }
}

const pageNum = new URLSearchParams(location.search).get("page") ?? 0;

fetch(`/admin/upgrades?page=${pageNum}`, {
  headers: {
    "Authorization": `Bearer ${jwt}`,
  },
})
    .then(response => {
      if (!response.ok) {
        localStorage.removeItem("token");
        location.href = "/views/login";
      }
      return response.json();
    })
    .then(setData);

