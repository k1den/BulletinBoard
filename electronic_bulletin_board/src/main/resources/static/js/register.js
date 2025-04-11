let verificationEmail = '';
let resendTimeout = null;

function showError(message) {
    const errorContainer = document.getElementById('errorContainer');
    const errorMessage = document.getElementById('errorMessage');
    errorMessage.textContent = message;
    errorContainer.style.display = 'block';
}

function hideError() {
    const errorContainer = document.getElementById('errorContainer');
    errorContainer.style.display = 'none';
}

async function handleResponse(response) {
    if (!response.ok) {
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            try {
                const errorJson = await response.json();
                throw new Error(errorJson.message || errorJson.error || 'Произошла ошибка');
            } catch (e) {
                // Обработка случая, когда JSON невалидный
                throw new Error('Произошла ошибка, но ответ не является корректным JSON.');
            }
        } else {
            const errorText = await response.text();
            throw new Error(errorText || 'Произошла ошибка');
        }
    }

    if (response.status === 204) {
        return {};
    }

    const text = await response.text();
    return text ? JSON.parse(text) : {};
}

// Функция для начала процесса регистрации (отправка кода подтверждения)
async function startRegistration() {
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
        const registerRequest = {
            email: email,
            phone: phone,
            login: login,
            password: password,
            confirmPassword: confirmPassword
        };

        const response = await fetch('/api/auth/validate-registration', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(registerRequest)
        });

        const data = await response.json();

        if (data.success) {
            verificationEmail = email;

            document.getElementById('registrationForm').style.display = 'none';
            document.getElementById('verificationForm').style.display = 'block';
            document.getElementById('emailDisplay').textContent = email;

            await sendVerificationCode(email);
            startResendTimer();
            hideError();
        } else {
            showError(data.message);
        }
    } catch (error) {
        showError(error.message);
        console.error('Ошибка:', error);
    }

}

// Функция для отправки кода подтверждения
async function sendVerificationCode(email) {
    try {
        const response = await fetch('/api/auth/send-verification', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ email })
        });

        await handleResponse(response);
    } catch (error) {
        showError('Не удалось отправить код подтверждения. Пожалуйста, попробуйте позже.');
        throw error;
    }
}

// Функция для проверки введенного кода
async function verifyCode() {
    const code = document.getElementById('verificationCode').value;
    
    if (!code || code.length !== 6) {
        showError('Пожалуйста, введите 6-значный код подтверждения.');
        return;
    }

    try {
        const response = await fetch('/api/auth/verify-code', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                email: verificationEmail,
                code: code
            })
        });

        const result = await handleResponse(response);
        
        if (result.success) {
            // Если код верный - конец регистрации
            await completeRegistration();
        } else {
            showError('Неверный код подтверждения. Пожалуйста, проверьте и попробуйте снова.');
        }
    } catch (error) {
        showError(error.message);
        console.error('Ошибка:', error);
    }
}

// Функция для завершения регистрации после успешной верификации
async function completeRegistration() {
    const email = verificationEmail;
    const phone = document.getElementById('phone').value;
    const login = document.getElementById('login').value;
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    try {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `email=${encodeURIComponent(email)}&phone=${encodeURIComponent(phone)}&login=${encodeURIComponent(login)}&password=${encodeURIComponent(password)}&confirmPassword=${encodeURIComponent(confirmPassword)}`
        });

        await handleResponse(response);
        
        // Перенаправлление на страницу входа после успешной регистрации
        window.location.href = 'login.html?registered=true';
    } catch (error) {
        showError(error.message);
        console.error('Ошибка:', error);
    }
}

// Функция для повторной отправки кода подтверждения
async function resendCode() {
    try {
        await sendVerificationCode(verificationEmail);
        showError('Новый код подтверждения отправлен на вашу почту.');
        startResendTimer();
    } catch (error) {
        showError(error.message);
        console.error('Ошибка:', error);
    }
}

// Таймер для повторной отправки кода
function startResendTimer() {
    const timerElement = document.getElementById('timer');
    const resendButton = document.querySelector('#verificationForm button.secondary-button');
    let timeLeft = 60;
    
    // Блокировка кнопки на время таймера
    resendButton.disabled = true;
    
    // Очистка предыдущего таймера(если он был)
    if (resendTimeout) {
        clearInterval(resendTimeout);
    }
    
    // Обновление таймера каждую секунду
    resendTimeout = setInterval(() => {
        timeLeft--;
        timerElement.textContent = timeLeft;
        
        if (timeLeft <= 0) {
            clearInterval(resendTimeout);
            resendButton.disabled = false;
            document.getElementById('timerContainer').style.display = 'none';
        }
    }, 1000);

    document.getElementById('timerContainer').style.display = 'block';
}

// Обработчик события для автоматического перехода между полями кода
document.getElementById('verificationCode')?.addEventListener('input', function(e) {
    if (this.value.length === 6) {
        verifyCode();
    }
});