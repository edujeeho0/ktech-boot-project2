const jwt = localStorage.getItem("token");
if (!jwt) location.href = "/views/login";

const createForm = document.getElementById("create-form");
const nameInput = document.getElementById("name-input");
const descInput = document.getElementById("desc-input");
const priceInput = document.getElementById("price-input");
const stockInput = document.getElementById("stock-input");
createForm.addEventListener("submit", e => {
  e.preventDefault();
  const name = nameInput.value;
  const description = descInput.value;
  const price = priceInput.value;
  const stock = stockInput.value;
  fetch(`/shops/my-shop/items`, {
    method: "post",
    headers: {
      "Authorization": `Bearer ${jwt}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({name, description, price , stock }),
  })
      .then(response => {
        if (response.ok) location.reload();
        else alert(response.status);
      });
});

const pageForm = document.getElementById("page-form");
const pageInput = pageForm.querySelector("input");

pageForm.addEventListener("submit", e => {
  e.preventDefault();
  const page = parseInt(pageInput.value) - 1;
  location.href = `/views/shops/my-shop/items?page=${page}`;
});

const itemsContainer = document.getElementById("items-container")
const setData = (itemsPage) => {
  const { content, page } = itemsPage;
  if (content.length === 0) {
    pageInput.disabled = true;
    const messageHead = document.createElement("h3");
    messageHead.innerText = "No Items";
    itemsContainer.append(messageHead);
    return;
  }
  else pageInput.max = page.totalPages;

  const containerFluid = document.createElement("div");
  containerFluid.className = "container-fluid";
  const row = document.createElement("div");
  row.className = "row gx-5 gy-4";
  content.forEach(item => {
    const col = document.createElement("div");
    col.className = "col-12 col-sm-6 col-lg-4";
    const card = document.createElement("div");
    card.className = "card w-100";
    const img = document.createElement("img");
    img.src = item.img ?? "/static/img/item.png";
    img.className = "card-img-top p-3";
    const body = document.createElement("div");
    body.className = "card-body";
    const title = document.createElement("h5");
    title.className = "card-title";
    title.innerText = item.name;
    const info = document.createElement("p");
    info.className = "card-text";
    info.innerText = `price: ${item.price}\nstock: ${item.stock}`;

    const updateLink = document.createElement("a");
    updateLink.href = `items/${item.id}`;
    updateLink.className = "btn btn-warning me-1";
    updateLink.innerText = "Update";

    const deleteButton = document.createElement("button");
    deleteButton.className = "btn btn-danger";
    deleteButton.innerText = "Delete";
    deleteButton.addEventListener("click", e => {
      fetch(`/shops/my-shop/items/${item.id}`, {
        method: "delete",
        headers: {
          "Authorization": `Bearer ${jwt}`,
        },
      })
          .then(response => {
            if (response.ok) location.reload();
            else alert(response.status);
          });

    });

    body.append(title, info, updateLink, deleteButton);
    card.append(img, body);
    col.append(card);
    row.append(col);
  });
  containerFluid.append(row);
  itemsContainer.append(containerFluid);
}

const pageNum = new URLSearchParams(location.search).get("page") ?? 0;
fetch(`/shops/my-shop/items?page=${pageNum}`, {
  method: "get",
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
