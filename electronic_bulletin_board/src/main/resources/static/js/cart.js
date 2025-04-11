const cartApiUrl = '/api/cart';
const apiUrl = '/api/advertisements';

let currentUser = null;

// Функция для форматирования цены с разделением разрядов
function formatPrice(price) {
    return Number(price).toLocaleString('ru-RU') + '₽';
}

// Загрузка информации о текущем юзере
function loadCurrentUser() {
    fetch('/api/advertisements/current-user')
        .then(response => {
            if (response.ok) {
                return response.json();
            } else {
                throw new Error('Пользователь не авторизован');
            }
        })
        .then(user => {
            currentUser = user;
            loadCartItems();
        })
        .catch(error => {
            console.error('Ошибка загрузки информации о пользователе:', error);
            currentUser = null;
            loadCartItems();
        });
}

// Загрузка товаров в корзине
function loadCartItems() {
    fetch(`${cartApiUrl}/items`)
        .then(response => response.json())
        .then(data => {
            const cartItemsContainer = document.getElementById('cartItems');
            cartItemsContainer.innerHTML = '';

            let totalPrice = 0; // Переменная для хранения общей стоимости

            data.forEach(item => {
                fetch(`${apiUrl}/photo/${item.ad.id}`)
                    .then(response => response.text())
                    .then(base64Image => {
                        const card = document.createElement('div');
                        card.className = 'card';
                        card.innerHTML = `
                            <img src="data:image/jpeg;base64,${base64Image}" alt="${item.ad.title}">
                            <h3>${item.ad.title}</h3>
                            <p>Цена: ${formatPrice(item.ad.price)}</p>
                            <button onclick="removeFromCart(${item.id})">Удалить из корзины</button>
                        `;
                        cartItemsContainer.appendChild(card);

                        totalPrice += item.ad.price;
                        updateTotalPrice(totalPrice);
                    })
                    .catch(error => {
                        console.error('Ошибка загрузки изображения:', error);
                        const card = document.createElement('div');
                        card.className = 'card';
                        card.innerHTML = `
                            <h3>${item.ad.title}</h3>
                            <p>Цена: ${formatPrice(item.ad.price)}</p>
                            <button onclick="removeFromCart(${item.id})">Удалить из корзины</button>
                        `;
                        cartItemsContainer.appendChild(card);

                        totalPrice += item.ad.price;
                        updateTotalPrice(totalPrice);
                    });
            });
        })
        .catch(error => console.error('Ошибка загрузки товаров в корзине:', error));
}

// Функция для обновления отображения общей суммы
function updateTotalPrice(totalPrice) {
    const totalPriceElement = document.getElementById('totalPrice');
    if (totalPriceElement) {
        totalPriceElement.textContent = `Общая стоимость: ${formatPrice(totalPrice)}`;
    }
}

// Функция для получения cartId
function getCartId() {
    return fetch(`${cartApiUrl}/cartId`)
        .then(response => {
            if (response.ok) {
                return response.json();
            } else {
                throw new Error('Ошибка при получении идентификатора корзины');
            }
        })
        .then(data => {
            return data;
        })
        .catch(error => {
            console.error('Ошибка:', error);
            throw error;
        });
}

// Удаление товара из корзины
function removeFromCart(itemId) {
    getCartId().then(cartId => {
        fetch(`${cartApiUrl}/${cartId}/remove/${itemId}`, {
            method: 'DELETE'
        })
        .then(response => {
            if (response.ok) {
                showSuccess('Товар успешно удален из корзины!');
                loadCartItems();
            } else {
                throw new Error('Ошибка при удалении товара из корзины');
            }
        })
        .catch(error => {
            showError(error.message);
            console.error('Ошибка:', error);
        });
    });
}

// Показ уведомлений об ошибках
function showError(message) {
    const errorContainer = document.getElementById('navErrorContainer');
    const errorMessage = document.getElementById('navErrorMessage');
    errorMessage.textContent = message;
    errorContainer.style.display = 'block';

    setTimeout(() => {
        hideError();
    }, 5000);
}

// Скрытие уведомлений об ошибках
function hideError() {
    const errorContainer = document.getElementById('navErrorContainer');
    errorContainer.style.display = 'none';
}

// Показ уведомлений об успехе
function showSuccess(message) {
    const successContainer = document.getElementById('navSuccessContainer');
    const successMessage = document.getElementById('navSuccessMessage');
    successMessage.textContent = message;
    successContainer.style.display = 'block';

    setTimeout(() => {
        hideSuccess();
    }, 5000);
}

// Скрытие уведомлений об успехе
function hideSuccess() {
    const successContainer = document.getElementById('navSuccessContainer');
    successContainer.style.display = 'none';
}

// Выход из системы
function logout() {
    fetch('/api/auth/logout', {
        method: 'POST'
    })
    .then(response => {
        if (response.ok) {
            alert('Вы успешно вышли из системы');
            currentUser = null;
            window.location.href = 'index.html';
        } else {
            alert('Ошибка при выходе из системы');
        }
    })
    .catch(error => {
        console.error('Ошибка:', error);
        alert('Ошибка при выходе из системы');
    });
}

window.onload = function() {
    loadCurrentUser();
};