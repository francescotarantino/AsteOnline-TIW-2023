'use strict'; // Execute JavaScript in strict mode

document.getElementById('login-form').addEventListener('submit', function (event) {
    event.preventDefault();

    let form = event.target;
    let submitButton = form.querySelector('input[type="submit"]');

    submitButton.disabled = true;

    if(form.checkValidity()){
        callAPI('POST', 'PerformLogin', form,
            function(req) {
                switch (req.status) {
                    case 200: // Successful login
                        let response = JSON.parse(req.responseText);
                        saveUser(response);
                        window.location.href = 'app.html';
                        return;
                    default: // Error
                        submitButton.disabled = false;
                        try {
                            let response = JSON.parse(req.responseText);
                            document.getElementById('error-message').textContent = response.message;
                        } catch (e) {
                            submitButton.disabled = false;
                            document.getElementById('error-message').textContent = "An error occurred.";
                            return;
                        }
                }
            });
    } else {
        submitButton.disabled = false;
        form.reportValidity();
    }
});