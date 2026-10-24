
// ========================================
// SMART CITY - COMPLAINT SUBMISSION
// ========================================

document.addEventListener("DOMContentLoaded", function () {

    const requestForm = document.getElementById("requestForm");

    if (!requestForm) {
        return;
    }

    requestForm.addEventListener("submit", async function (event) {

        event.preventDefault();

        if (!requestForm.checkValidity()) {
            requestForm.reportValidity();
            return;
        }

        const submitButton =
            document.getElementById("submitButton");

        const formData = new FormData(requestForm);

        submitButton.disabled = true;
        submitButton.textContent = "Submitting...";

        try {

            const response = await fetch("/api/complaints", {
                method: "POST",
                body: formData,
                credentials: "same-origin"
            });

            const responseText = await response.text();

            let result = {};

            try {
                result = JSON.parse(responseText);
            } catch (error) {
                result = {
                    message: responseText || "Unexpected server response"
                };
            }

            if (!response.ok) {

                if (response.status === 401) {
                    throw new Error("Please login before submitting a complaint.");
                }

                throw new Error(
                    result.message || "Complaint submission failed."
                );
            }

            if (!result.complaintId) {
                throw new Error(
                    "The server did not return a Complaint ID."
                );
            }

            alert(
                "Complaint submitted successfully!\n\n" +
                "Your Complaint ID: " + result.complaintId
            );

            requestForm.reset();

        } catch (error) {

            console.error("Complaint submission error:", error);

            alert("Error: " + error.message);

        } finally {

            submitButton.disabled = false;
            submitButton.textContent = "Submit Request";

        }

    });

 });