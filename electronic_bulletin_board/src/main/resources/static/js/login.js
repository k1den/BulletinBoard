// Стандартные уведомления об ошибках как-то не очень (показываю)
function showError(message) {
    const errorContainer = document.getElementById('errorContainer');
    const errorMessage = document.getElementById('errorMessage');
    errorMessage.textContent = message;
    errorContainer.style.display = 'block';
}

// Стандартные уведомления об ошибках как-то не очень (прячу)
function hideError() {
    const errorContainer = document.getElementById('errorContainer');
    errorContainer.style.display = 'none';
}

// Универсальная функция для обработки ответа от сервера
async function handleResponse(response) {
    if (!response.ok) {
        // Читаю сначала JSON, если не могу, то уже как текст
        const errorText = await response.text();
        try {
            const errorJson = JSON.parse(errorText);
            throw new Error(errorJson.message || 'Неправильный email или пароль');
        } catch (e) {
            throw new Error(errorText || 'Неправильный email или пароль');
        }
    }
    return response.json();
}

async function login() {
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    if (!email || !password) {
        showError('Пожалуйста, заполните все поля.');
        return;
    }

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `email=${encodeURIComponent(email)}&password=${encodeURIComponent(password)}`
        });

        const data = await handleResponse(response);

        window.location.href = 'ads.html';
        hideError();
    } catch (error) {
        showError(error.message);
        console.error('Ошибка:', error);
    }
}