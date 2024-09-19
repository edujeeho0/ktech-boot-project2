const jwt = localStorage.getItem("token");
if (!jwt) location.href = "/views/login";

const status = document.getElementById("status");
const nameInput = document.getElementById("name-input");
const descInput = document.getElementById("desc-input");
const categorySelect = document.getElementById("category-select");
const setBaseData = (shopInfo) => {
  nameInput.value = shopInfo.name;
  if (shopInfo.status !== "OPEN") {
    status.querySelector("button").classList.remove("d-none");
  }
  else status.querySelector("form").classList.remove("d-none");
  const statusH3 = status.querySelector("h3");
  statusH3.innerText = `Status: ${shopInfo.status}`;
  if (shopInfo.status === "REJECTED") {
    const statusP = status.querySelector("p");
    fetch("/shops/my-shop/reject-reason",{
      headers: {
        "Authorization": `Bearer ${jwt}`,
      },
    }).then(response => {
      if (!response.ok) {
        localStorage.removeItem("token");
        location.href = "/views/login";
      }
      return response.json();
    }).then(json => {
      statusP.innerText = `Reason: ${json.reason}`;
    });
  }
  descInput.value = shopInfo.description;
  const categoryOption = categorySelect.querySelector(`option[value="${shopInfo.category}"]`)
  if (categoryOption) categoryOption.selected = true;
  else document.querySelector("option").selected = true;
  status.querySelector("form").querySelector("input").value = shopInfo.closeReason ?? "";
}

fetch("/shops/my-shop", {
  headers: {
    "Authorization": `Bearer ${jwt}`,
  },
}).then(response => {
  if (!response.ok) {
    localStorage.removeItem("token");
    location.href = "/views/login";
  }
  return response.json();
}).then(setBaseData);

const updateForm = document.getElementById("update-form");
updateForm.addEventListener("submit", e => {
  e.preventDefault();
  const name = nameInput.value;
  const description = descInput.value;
  const category = categorySelect.value;
  fetch("/shops/my-shop", {
    method: "put",
    headers: {
      "Authorization": `Bearer ${jwt}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({name, description, category}),
  }).then(response => {
    if (response.ok) location.reload();
    else alert(response.status);
  });
})

const openButton = status.querySelector("button");
openButton.addEventListener("click", e => {
  fetch("/shops/my-shop/open", {
    method: "put",
    headers: {
      "Authorization": `Bearer ${jwt}`,
    },
  }).then(response => {
    if (response.ok) {
      alert("Request Submitted");
      location.reload();
    }
    else alert(response.status);
  });
});

const closeForm = status.querySelector("form");
closeForm.addEventListener("submit", e => {
  e.preventDefault();
  const reasonInput = closeForm.querySelector("input");
  const closeReason = reasonInput.value;
  fetch("/shops/my-shop/close", {
    method: "put",
    headers: {
      "Authorization": `Bearer ${jwt}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({closeReason}),
  }).then(response => {
    if (response.ok) location.reload();
    else alert(response.status);
  });
})


