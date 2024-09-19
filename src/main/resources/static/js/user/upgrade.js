const jwt = localStorage.getItem("token");
if (!jwt) location.href = "/views/login";
fetch("/users/get-user-info", {
  headers: {
    "Authorization": `Bearer ${jwt}`,
  },
})
    .then(response => {
      loggedIn = response.ok;
      if (!loggedIn) {
        localStorage.removeItem("token");
        location.href = "/views/login";
      }
      return response.json();
    });

document.getElementById("confirm-button").addEventListener("click", e => {
  fetch("/users/upgrade", {
    method: "put",
    headers: {
      "Authorization": `Bearer ${localStorage.getItem("token")}`,
    },
  }).then(response => {
    if (response.ok) alert("Request Submitted");
    location.href = "/views";
  });
});
