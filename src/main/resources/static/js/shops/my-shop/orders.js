const jwt = localStorage.getItem("token");
if (!jwt) location.href = "/views/login";

const pageForm = document.getElementById("page-form");
const pageInput = pageForm.querySelector("input");

pageForm.addEventListener("submit", e => {
  e.preventDefault();
  const page = parseInt(pageInput.value) - 1;
  location.href = `/views/orders?page=${page}`;
});

const ordersContainer = document.getElementById("orders-container");
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
    const headTdAddress = document.createElement("td");
    headTdAddress.innerText = "Address";
    const headTdCount = document.createElement("td");
    headTdCount.innerText = "Count";
    const headTdPrice = document.createElement("td");
    headTdPrice.innerText = "Total Price";
    const headTdStatus = document.createElement("td");
    headTdStatus.innerText = "Status";
    const headTdActions = document.createElement("td");
    headTdActions.innerText = "Actions";
    headTr.append(headTdId, headTdName, headTdAddress, headTdCount, headTdPrice, headTdStatus, headTdActions);
    head.append(headTr);
    table.append(head);

    const body = document.createElement("tbody");
    content.forEach(order => {
      const row = document.createElement("tr");
      row.style.verticalAlign = "middle";
      const rowTdId = document.createElement("td");
      rowTdId.innerText = order.id;
      const rowTdName = document.createElement("td");
      rowTdName.innerText = order.itemName;
      const rowTdAddress = document.createElement("td");
      rowTdAddress.innerText = order.address;
      const rowTdCount = document.createElement("td");
      rowTdCount.innerText = order.count;
      const rowTdPrice = document.createElement("td");
      rowTdPrice.innerText = order.totalPrice;
      const rowTdStatus = document.createElement("td");
      rowTdStatus.innerText = order.status;
      const rowTdActions = document.createElement("td");
      if (order.status === "ORDERED") {
        const acceptButton = document.createElement("button");
        acceptButton.className = "btn btn-primary me-2";
        acceptButton.innerText = "Accept";
        acceptButton.addEventListener("click", e => {
          fetch(`/shops/my-shop/orders/${order.id}`, {
            method: "put",
            headers: {
              "Authorization": `Bearer ${jwt}`,
              "Content-Type": "application/json",
            },
            body: JSON.stringify({status: "ACCEPTED"})
          }).then(response => {
            if (response.ok) location.reload();
            else alert(response.status);
          });
        });
        rowTdActions.append(acceptButton);

        const declineButton = document.createElement("button");
        declineButton.className = "btn btn-danger";
        declineButton.innerText = "Decline";
        declineButton.addEventListener("click", e => {
          if (!confirm("Are you sure you want to decline?")) return;
          const reason = prompt("Why?");
          fetch(`/shops/my-shop/orders/${order.id}`, {
            method: "put",
            headers: {
              "Authorization": `Bearer ${jwt}`,
              "Content-Type": "application/json",
            },
            body: JSON.stringify({status: "DECLINED", reason}),
          }).then(response => {
            if (response.ok) location.reload();
            else alert(response.status);
          });
        });
        rowTdActions.append(declineButton);
      }
      row.append(rowTdId, rowTdName, rowTdAddress, rowTdCount, rowTdPrice, rowTdStatus, rowTdActions);
      body.append(row);
    });
    table.append(body);

    ordersContainer.append(table);
  }
  else {
    const messageHead = document.createElement("h3");
    messageHead.innerText = "No Orders";
    ordersContainer.append(messageHead);
  }
}


fetch("/shops/my-shop/orders", {
  headers: {
    "Authorization": `Bearer ${jwt}`,
  },
}).then(response => {
  if (!response.ok) {
    localStorage.removeItem("token");
    location.href = "/views/login";
  }
  return response.json();
}).then(setData);
