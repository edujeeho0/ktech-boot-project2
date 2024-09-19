const jwt = localStorage.getItem("token");
if (!jwt) location.href = "/views/login";

const url = location.pathname.split("views")[1];
const shopId = url.split("/")[2];
const itemId = url.split("/")[4];

const itemImg = document.getElementById("item-image");
const itemName = document.getElementById("item-name");
const itemDesc = document.getElementById("item-desc");
const itemPrice = document.getElementById("item-price");
const itemStock = document.getElementById("item-stock");
const backLink = document.getElementById("back-link");
const orderForm = document.getElementById("order-form");
backLink.href = `/views/shops/${shopId}`;
const setData = itemData => {
  itemName.innerText = itemData.name;
  itemImg.src = itemData.img ?? "/static/img/item.png"
  itemDesc.innerText = itemData.description;
  itemPrice.querySelector("span").innerText = itemData.price;
  itemStock.querySelector("span").innerText = itemData.stock;
  orderForm.querySelector("input[type=number]").max = itemData.stock;
};

fetch(url, {
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

orderForm.addEventListener("submit", e => {
  e.preventDefault();
  const address = orderForm.querySelector("input[type=text]").value;
  const count = orderForm.querySelector("input[type=number]").value;
  fetch("/orders", {
    method: "post",
    headers: {
      "Authorization": `Bearer ${jwt}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ itemId, address, count }),
  }).then(response => {
    if (response.ok) {
      alert("order placed!!!");
      location.href = "/views/orders";
    }
    if (response.status === 403) {
      localStorage.removeItem("token");
      location.href = "/views/login";
    }
  });
});
