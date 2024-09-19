const jwt = localStorage.getItem("token");
if (!jwt) location.href = "/views/login";

const pathArray = location.pathname.split("/");
const itemId = pathArray[pathArray.length - 1];

const updateForm = document.getElementById("update-form");
const nameInput = document.getElementById("name-input");
const descInput = document.getElementById("desc-input");
const priceInput = document.getElementById("price-input");
const stockInput = document.getElementById("stock-input");
const imgContainer = document.getElementById("img-container");

const setData = itemData => {
  nameInput.value = itemData.name;
  descInput.value = itemData.description;
  priceInput.value = itemData.price;
  stockInput.value = itemData.stock;
  if (itemData.img) {
    const imageElem = document.createElement("img");
    imageElem.className = "img-thumbnail rounded";
    imageElem.src = itemData.img;
    imgContainer.innerHTML = "";
    imgContainer.appendChild(imageElem)
  }
}

updateForm.addEventListener("submit", e => {
  e.preventDefault();
  const name = nameInput.value;
  const description = descInput.value;
  const price = priceInput.value;
  const stock = stockInput.value;
  fetch(`/shops/my-shop/items/${itemId}`, {
    method: "put",
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

const imageForm = document.getElementById("item-img-form");
imageForm.addEventListener("submit", e => {
  e.preventDefault();
  const formData  = new FormData();
  const imageInput = imageForm.querySelector("input");
  formData.append("file", imageInput.files[0]);

  fetch(`/shops/my-shop/items/${itemId}/image`, {
    method: "put",
    headers: {
      "Authorization": `Bearer ${localStorage.getItem("token")}`,
    },
    body: formData,
  }).then(response => {
    if (response.ok) location.reload();
    else if (response.status === 403)
      location.href = "/views/login";
    else alert(response.status);
  });
});


fetch(`/shops/my-shop/items/${itemId}`, {
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
