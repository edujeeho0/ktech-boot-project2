const jwt = localStorage.getItem("token");
if (!jwt) location.href = "/views/login";

const pageForm = document.getElementById("page-form");
const pageInput = pageForm.querySelector("input");
const searchParams = new URLSearchParams(location.search);

pageForm.addEventListener("submit", e => {
  e.preventDefault();
  const page = parseInt(pageInput.value) - 1;
  searchParams.set("page", page.toString());
  location.href = `${location.pathname}?${searchParams}`;
});

const setShopData = shopData => {
  document.getElementById("shop-name").innerText = shopData.name;
  document.getElementById("shop-desc").innerText = shopData.description;
  const category = document.createElement("span");
  category.className = "badge text-bg-info";
  category.innerText = shopData.category;
  document.getElementById("shop-category").appendChild(category);
};
const shopUrl = location.pathname.split("views")[1];
fetch(shopUrl, {
  headers: {
    "Authorization": `Bearer ${jwt}`,
  },
}).then(response => {
  if (!response.ok) {
    localStorage.removeItem("token");
    location.href = "/views/login";
  }
  return response.json();
}).then(setShopData);

const shopId = shopUrl.split("/")[2].split("?")[0];
const itemsContainer = document.getElementById("items-container");
const setItemData = itemPage => {
  const { content, page } = itemPage;
  if (content.length === 0)
    pageInput.disabled = true;
  else pageInput.max = page.totalPages;

  if (content.length === 0) {
    const messageHead = document.createElement("h3");
    messageHead.innerText = "No Items";
    shopsContainer.append(messageHead);
    return;
  }

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

    const link = document.createElement("a");
    link.className = "card-link";
    link.href = `/views/shops/${shopId}/items/${item.id}`;
    link.innerText = "Go See";

    body.append(title, info, link);
    card.append(img, body);
    col.append(card);
    row.append(col);
  });
  containerFluid.append(row);
  itemsContainer.append(containerFluid);
}
const itemsUrl = shopUrl + `/items?${searchParams}`;
fetch(itemsUrl, {
  headers: {
    "Authorization": `Bearer ${jwt}`,
  },
}).then(response => {
  if (!response.ok) {
    localStorage.removeItem("token");
    location.href = "/views/login";
  }
  return response.json();
}).then(setItemData);

