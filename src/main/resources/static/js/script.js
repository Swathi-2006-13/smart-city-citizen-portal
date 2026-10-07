// ======================================================
// SMART CITY CITIZEN SERVICE PORTAL
// Main JavaScript
// ======================================================


// ======================================================
// PAGE LOAD
// ======================================================

window.addEventListener("load", () => {
    console.log("Smart City Portal Loaded Successfully");
});


// ======================================================
// HERO BACKGROUND SLIDER
// ======================================================

const hero = document.querySelector(".hero");

if (hero) {

    const cityImages = [
        "images/city1.jpg",
        "images/city2.jpg",
        "images/city3.jpg",
        "images/city4.jpg",
        "images/city5.jpg"
    ];

    let cityIndex = 0;

    setInterval(() => {

        cityIndex++;

        if (cityIndex >= cityImages.length) {
            cityIndex = 0;
        }

        hero.style.backgroundImage =
            "linear-gradient(rgba(0,0,0,.55),rgba(0,0,0,.55)), url('" +
            cityImages[cityIndex] +
            "')";

    }, 5000);
}


// ======================================================
// REQUEST FORM
// NOTE:
// Login is NOT handled here.
// Complaint backend integration will be connected separately.
// ======================================================

const requestForm = document.querySelector(".request-form form");

if (requestForm) {

    requestForm.addEventListener("submit", (e) => {

        e.preventDefault();

        alert(
            "Your Service Request has been submitted successfully!\n" +
            "Your Complaint ID will be generated soon."
        );

    });
}


// ======================================================
// CONTACT FORM
// ======================================================

const contactForm = document.querySelector(".message-box form");

if (contactForm) {

    contactForm.addEventListener("submit", (e) => {

        e.preventDefault();

        alert(
            "Thank you for contacting Smart City. We will reply soon."
        );

    });
}


// ======================================================
// REGISTER FORM
// IMPORTANT:
// Backend registration will be handled from register.html.
// We do NOT intercept registerForm here.
// This prevents the old fake registration alert from
// interfering with the Spring Boot backend.
// ======================================================


// ======================================================
// SCROLL ANIMATION
// ======================================================

const cards = document.querySelectorAll(
    ".service-card, .info-card, .counter div"
);

window.addEventListener("scroll", () => {

    cards.forEach(card => {

        const position = card.getBoundingClientRect().top;
        const screen = window.innerHeight;

        if (position < screen - 100) {

            card.style.opacity = "1";
            card.style.transform = "translateY(0)";

        }

    });

});


// ======================================================
// SERVICE SEARCH
// ======================================================

const search = document.querySelector(".search-service input");

if (search) {

    search.addEventListener("keyup", () => {

        const value = search.value.toLowerCase();

        const services = document.querySelectorAll(".service-card");

        services.forEach(service => {

            const text = service.innerText.toLowerCase();

            if (text.includes(value)) {

                service.style.display = "block";

            } else {

                service.style.display = "none";

            }

        });

    });

}