const jwt = localStorage.getItem("token");
if (!jwt) location.href = "/views/login";

const requestContainer = document.getElementById("request-container");
const pageForm = document.getElementById("page-form");
const pageInput = pageForm.querySelector("input");

pageForm.addEventListener("submit", e => {
  e.preventDefault();
  const page = parseInt(pageInput.value) - 1;
  location.href = `/views/admin/shop-open-requests?page=${page}`;
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
    const headTdName = document.createElement("td");
    headTdName.innerText = "Name";
    const headTdDescription = document.createElement("td");
    headTdDescription.innerText = "Description";
    const headTdCategory = document.createElement("td");
    headTdCategory.innerText = "Category";
    const headTdStatus = document.createElement("td");
    headTdStatus.innerText = "Status";
    const headTdActions = document.createElement("td");
    headTdActions.innerText = "Actions";
    headTr.append(headTdId, headTdName, headTdDescription, headTdCategory, headTdStatus, headTdActions);
    head.append(headTr);
    table.append(head);

    const body = document.createElement("tbody");
    content.forEach(request => {
      console.log(request);
      const row = document.createElement("tr");
      row.style.verticalAlign = "middle";
      const rowTdId = document.createElement("td");
      rowTdId.innerText = request.id;
      const rowTdName = document.createElement("td");
      rowTdName.innerText = request.name;

      const rowTdDescription = document.createElement("td");
      rowTdDescription.innerText = request.description;
      const rowTdCategory = document.createElement("td");
      rowTdCategory.innerText = request.category;
      const rowTdStatus = document.createElement("td");
      rowTdStatus.innerText = request.status;
      const rowTdActions = document.createElement("td");
      const approveButton = document.createElement("button");
      approveButton.className = "btn btn-primary me-2";
      approveButton.innerText = "Approve";
      approveButton.addEventListener("click", e => {
        fetch(`/admin/shops/${request.id}?action=approve`, {
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
        const reason = prompt("Why?");
        if (!reason) {
          alert("Please provide reason");
          return;
        }
        fetch(`/admin/shops/${request.id}?action=disapprove`, {
          method: "put",
          headers: {
            "Authorization": `Bearer ${jwt}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ reason }),
        }).then(response => {
          if (response.ok) location.reload();
          else alert(response.status);
        });
      });
      rowTdActions.append(disapproveButton);

      row.append(rowTdId, rowTdName, rowTdDescription, rowTdCategory, rowTdStatus, rowTdActions);
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

fetch(`/admin/shops?request=OPEN&page=${pageNum}`, {
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

