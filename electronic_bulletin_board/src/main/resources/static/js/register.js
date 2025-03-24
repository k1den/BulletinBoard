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
        // Получаем Content-Type из заголовков
        const contentType = response.headers.get('content-type');

        // Если ответ в формате JSON, парсим его
        if (contentType && contentType.includes('application/json')) {
            const errorJson = await response.json();
            throw new Error(errorJson.message || 'Произошла ошибка');
        } else {
            // Если ответ не в формате JSON, читаем как текст
            const errorText = await response.text();
            throw new Error(errorText || 'Произошла ошибка');
        }
    }
    return response.json();
}

// Функция для регистрации
async function register() {
    const email = document.getElementById('email').value;
    const phone = document.getElementById('phone').value;
    const login = document.getElementById('login').value;
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    if (!email || !login || !password || !confirmPassword) {
        showError('Пожалуйста, заполните все обязательные поля.');
        return;
    }

    if (password !== confirmPassword) {
        showError('Пароли не совпадают.');
        return;
    }

    try {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `email=${encodeURIComponent(email)}&phone=${encodeURIComponent(phone)}&login=${encodeURIComponent(login)}&password=${encodeURIComponent(password)}&confirmPassword=${encodeURIComponent(confirmPassword)}`
        });

        const data = await handleResponse(response);

        window.location.href = 'login.html';
        hideError();
    } catch (error) {
        showError(error.message);
        console.error('Ошибка:', error);
    }
}