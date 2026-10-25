
// ========================================
// SMART CITY - MAIN JAVASCRIPT
// ========================================

document.addEventListener("DOMContentLoaded", function () {

    // ========================================
    // 1. COMPLAINT SUBMISSION
    // ========================================

    const requestForm = document.getElementById("requestForm");

    if (requestForm) {

        requestForm.addEventListener("submit", async function (event) {

            event.preventDefault();

            if (!requestForm.checkValidity()) {
                requestForm.reportValidity();
                return;
            }

            const submitButton = document.getElementById("submitButton");

            if (!submitButton) {
                console.error("Submit button not found.");
                return;
            }

            const formData = new FormData(requestForm);

            submitButton.disabled = true;
            submitButton.textContent = "Submitting...";

            try {

                const response = await fetch("/api/complaints", {
                    method: "POST",
                    body: formData,
                    credentials: "same-origin",
                    headers: {
                        "Accept": "application/json"
                    }
                });

                const responseText = await response.text();

                let result = {};

                try {
                    result = responseText ? JSON.parse(responseText) : {};
                } catch {
                    throw new Error("Unexpected server response.");
                }

                if (!response.ok) {

                    if (response.status === 401) {
                        throw new Error(
                            "Please login before submitting a complaint."
                        );
                    }

                    throw new Error(
                        result.message || "Complaint submission failed."
                    );
                }

                if (!result.complaintId) {
                    throw new Error(
                        "Complaint ID was not returned by the server."
                    );
                }

                alert(
                    "Complaint submitted successfully!\n\n" +
                    "Your Complaint ID: " + result.complaintId +
                    "\n\nPlease save this ID to track your complaint."
                );

                requestForm.reset();

            } catch (error) {

                console.error("Complaint submission error:", error);
                alert("Submission Error:\n" + error.message);

            } finally {

                submitButton.disabled = false;
                submitButton.textContent = "Submit Request";

            }

        });

    }


    // ========================================
    // 2. ORIGINAL COMPLAINT TRACKING
    // ========================================

    const trackForm = document.getElementById("trackForm");

    if (trackForm) {

        const complaintInput = document.getElementById("complaintId");
        const trackButton = document.getElementById("trackButton");
        const trackMessage = document.getElementById("trackMessage");

        const detailsSection =
            document.getElementById("complaintDetails");

        const progressSection =
            document.getElementById("progressSection");


        // Display text safely
        function setText(id, value) {

            const element = document.getElementById(id);

            if (element) {
                element.textContent =
                    value === null || value === undefined || value === ""
                        ? "Not available"
                        : value;
            }

        }


        // Format dates
        function formatDate(value) {

            if (!value) {
                return "Not available";
            }

            const date = new Date(value);

            if (Number.isNaN(date.getTime())) {
                return value;
            }

            return date.toLocaleString("en-IN");

        }


        // Update complaint progress
        function updateProgress(status) {

            const normalizedStatus =
                String(status || "")
                    .toUpperCase()
                    .replace(/[\s-]+/g, "_");

            const received =
                document.getElementById("stepReceived");

            const progress =
                document.getElementById("stepProgress");

            const completed =
                document.getElementById("stepCompleted");

            const fill =
                document.getElementById("progressFill");


            [received, progress, completed].forEach(function (step) {

                if (step) {
                    step.classList.remove("active", "completed");
                }

            });


            let percentage = 10;

            if (normalizedStatus === "RECEIVED") {

                percentage = 25;

                if (received) {
                    received.classList.add("active");
                }

            } else if (
                normalizedStatus === "IN_PROGRESS" ||
                normalizedStatus === "PROCESSING"
            ) {

                percentage = 55;

                if (received) received.classList.add("completed");
                if (progress) progress.classList.add("active");

            } else if (
                normalizedStatus === "COMPLETED" ||
                normalizedStatus === "RESOLVED"
            ) {

                percentage = 100;

                [received, progress, completed].forEach(function (step) {

                    if (step) {
                        step.classList.add("completed");
                    }

                });

            } else if (
                normalizedStatus === "REJECTED" ||
                normalizedStatus === "CANCELLED"
            ) {

                percentage = 10;

                if (received) {
                    received.classList.add("active");
                }

            } else {

                if (received) {
                    received.classList.add("active");
                }

            }

            if (fill) {
                fill.style.width = percentage + "%";
            }

        }


        // Track complaint
        trackForm.addEventListener("submit", async function (event) {

            event.preventDefault();

            const complaintId = complaintInput.value.trim();

            if (!complaintId) {
                trackMessage.textContent =
                    "Please enter your Complaint ID.";
                return;
            }

            trackButton.disabled = true;
            trackButton.textContent = "Searching...";

            trackMessage.textContent = "";

            detailsSection.hidden = true;
            progressSection.hidden = true;


            try {

                const response = await fetch(
                    "/api/complaints/track/" +
                    encodeURIComponent(complaintId),
                    {
                        method: "GET",
                        credentials: "same-origin",
                        headers: {
                            "Accept": "application/json"
                        }
                    }
                );


                const responseText = await response.text();

                let result = {};

                try {
                    result = responseText
                        ? JSON.parse(responseText)
                        : {};
                } catch {
                    throw new Error("Invalid server response.");
                }


                if (!response.ok) {
                    throw new Error(
                        result.message || "Complaint not found."
                    );
                }


                // Display original complaint details
                setText("resultComplaintId", result.complaintId);
                setText("resultCitizenName", result.citizenName);
                setText("resultPhone", result.phone);
                setText("resultCategory", result.category);
                setText("resultService", result.serviceRequired);

                // Support either backend location field
                setText(
                    "resultLocation",
                    result.location || result.place
                );

                setText("resultAddress", result.address);
                setText("resultArea", result.area);
                setText("resultWard", result.wardNumber);

                setText(
                    "resultDate",
                    formatDate(result.dateSubmitted)
                );

                setText("resultPriority", result.priority);
                setText("resultStatus", result.status);
                setText("resultOfficer", result.assignedOfficer);

                setText(
                    "resultCompletion",
                    result.expectedCompletion
                        ? formatDate(result.expectedCompletion)
                        : "Not scheduled"
                );

                setText("resultDescription", result.description);


                // Display uploaded complaint image
                const imageContainer =
                    document.getElementById("resultImage");

                if (imageContainer) {

                    imageContainer.textContent = "";

                    if (result.imagePath) {

                        const image = document.createElement("img");

                        image.src = result.imagePath;
                        image.alt = "Complaint image";
                        image.style.maxWidth = "220px";
                        image.style.maxHeight = "180px";

                        image.onerror = function () {
                            imageContainer.textContent =
                                "Uploaded image could not be loaded.";
                        };

                        imageContainer.appendChild(image);

                    } else {

                        imageContainer.textContent =
                            "No image uploaded";

                    }

                }


                // Update original progress bar
                updateProgress(result.status);


                // Show original tracking details
                detailsSection.hidden = false;
                progressSection.hidden = false;

                trackMessage.textContent =
                    "Complaint details loaded successfully.";

            } catch (error) {

                console.error("Tracking error:", error);

                trackMessage.textContent = error.message;

            } finally {

                trackButton.disabled = false;
                trackButton.textContent = "Track Status";

            }

        });

    }


    // ========================================
    // 3. CONTACT FORM
    // ========================================

    const contactForm =
        document.querySelector(".contact-form form");

    if (contactForm) {

        contactForm.addEventListener("submit", function (event) {

            event.preventDefault();

            alert(
                "Thank you for contacting Smart City!\n" +
                "Your message has been received."
            );

            contactForm.reset();

        });

    }


    // ========================================
    // 4. SERVICE SEARCH
    // ========================================

    const serviceSearch = document.querySelector(
        "#serviceSearch, .service-search input"
    );

    if (serviceSearch) {

        serviceSearch.addEventListener("input", function () {

            const searchValue =
                serviceSearch.value.toLowerCase().trim();

            const serviceCards = document.querySelectorAll(
                ".service-card, .service-box"
            );

            serviceCards.forEach(function (card) {

                const cardText =
                    card.textContent.toLowerCase();

                card.style.display =
                    cardText.includes(searchValue) ? "" : "none";

            });

        });

    }


    // ========================================
    // 5. SCROLL ANIMATIONS
    // ========================================

    const animatedElements = document.querySelectorAll(
        ".animate, .fade-in, .service-card"
    );

    if ("IntersectionObserver" in window) {

        const observer = new IntersectionObserver(
            function (entries, observer) {

                entries.forEach(function (entry) {

                    if (entry.isIntersecting) {

                        entry.target.classList.add("visible");
                        observer.unobserve(entry.target);

                    }

                });

            },
            {
                threshold: 0.15
            }
        );

        animatedElements.forEach(function (element) {
            observer.observe(element);
        });

    } else {

        animatedElements.forEach(function (element) {
            element.classList.add("visible");
        });

    }


    // ========================================
    // 6. HERO IMAGE SLIDER
    // ========================================

    const heroImages = document.querySelectorAll(".hero-slide");

    const nextButton = document.querySelector(".slider-next");
    const previousButton = document.querySelector(".slider-prev");

    if (heroImages.length > 0) {

        let currentSlide = 0;

        function showSlide(index) {

            heroImages.forEach(function (slide) {
                slide.classList.remove("active");
            });

            currentSlide =
                (index + heroImages.length) % heroImages.length;

            heroImages[currentSlide].classList.add("active");

        }

        if (nextButton) {

            nextButton.addEventListener("click", function () {
                showSlide(currentSlide + 1);
            });

        }

        if (previousButton) {

            previousButton.addEventListener("click", function () {
                showSlide(currentSlide - 1);
            });

        }

        showSlide(0);

    }

});
