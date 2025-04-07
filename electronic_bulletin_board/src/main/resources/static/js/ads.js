const apiUrl = '/api/advertisements';
const categoriesUrl = '/api/advertisements/categories';
const currentUserUrl = '/api/advertisements/current-user';

let categories = [];
let currentUser = null;

// Функция для форматирования цены с разделением разрядов
function formatPrice(price) {
    return Number(price).toLocaleString('ru-RU') + '₽';
}

// Выход из системы
function logout() {
    fetch('/api/auth/logout', {
        method: 'POST'
    })
    .then(response => {
        localStorage.removeItem('cartId');
        currentUser = null;
        updateAuthButtons();
        updateButtons(); // Обновляем кнопки после выхода
        window.location.href = 'ads.html';
    })
    .catch(error => {
        console.error('Ошибка:', error);
        alert('Ошибка при выходе из системы');
    });
}

// Валидация названия
function validateTitle(title) {
    if (title.length > 50) {
        return 'Название не должно превышать 50 символов.';
    }
    return null; // Ошибок нет
}

// Валидация цены
function validatePrice(price) {
    if (!/^\d+$/.test(price)) {
        return 'Цена должна быть целым числом.';
    }
    return null; // Ошибок нет
}

// Стандартные уведомления об ошибках как-то не очень (показываю)
function showError(message) {
    const errorContainer = document.getElementById('navErrorContainer');
    const errorMessage = document.getElementById('navErrorMessage');
    errorMessage.textContent = message;
    errorContainer.style.display = 'block';

    setTimeout(() => {
        hideError();
    }, 5000);
}

// Стандартные уведомления об ошибках как-то не очень (прячу)
function hideError() {
    const errorContainer = document.getElementById('navErrorContainer');
    errorContainer.style.display = 'none';
}

// Эта книга о том... Как стать успешным
function showSuccess(message) {
    const successContainer = document.getElementById('navSuccessContainer');
    const successMessage = document.getElementById('navSuccessMessage');
    successMessage.textContent = message;
    successContainer.style.display = 'block';

    setTimeout(() => {
        hideSuccess();
    }, 5000);
}

// Всё начитались, хватит с вас
function hideSuccess() {
    const successContainer = document.getElementById('navSuccessContainer');
    successContainer.style.display = 'none';
}

// Сброс поиска
function resetSearch() {
    document.getElementById('advertisementsByCategory').innerHTML = ''; // Очищаем результаты
    document.getElementById('resetSearchButton').style.display = 'none'; // Скрываем кнопку сброса
}

// Обновляю кнопки
function updateButtons() {
    const cards = document.querySelectorAll('.card');
    cards.forEach(card => {
        const adId = card.getAttribute('data-ad-id');
        const adUserId = card.getAttribute('data-ad-user-id');
        const editButton = card.querySelector('.edit-button');
        const deleteButton = card.querySelector('.delete-button');

        if (currentUser && (currentUser.role === 'Администратор' || currentUser.id == adUserId)) {
            if (editButton) editButton.style.display = 'inline-block';
            if (deleteButton) deleteButton.style.display = 'inline-block';
        } else {
            if (editButton) editButton.style.display = 'none';
            if (deleteButton) deleteButton.style.display = 'none';
        }
    });
}

// Загрузка информации о текущем юзере
function loadCurrentUser() {
    fetch(currentUserUrl)
        .then(response => {
            if (response.ok) {
                return response.json();
            } else {
                throw new Error('Пользователь не авторизован');
            }
        })
        .then(user => {
            currentUser = user;
            updateAuthButtons(); // Обновляем кнопки
            getAllAdvertisements();
        })
        .catch(error => {
            console.error('Ошибка загрузки информации о пользователе:', error);
            currentUser = null;
            updateAuthButtons(); // Обновляем кнопки
            getAllAdvertisements();
        });
}

// Функция для обновления кнопок "Войти" и "Выйти"
function updateAuthButtons() {
    const loginButton = document.getElementById('loginButton');
    const logoutButton = document.getElementById('logoutButton');

    if (currentUser) {
        // Если пользователь авторизован, показываем кнопку "Выйти"
        loginButton.style.display = 'none';
        logoutButton.style.display = 'inline-block';
    } else {
        // Если пользователь не авторизован, показываем кнопку "Войти"
        loginButton.style.display = 'inline-block';
        logoutButton.style.display = 'none';
    }
}

// Загрузка категорий при загрузке страницы
function loadCategories() {
    fetch(categoriesUrl)
        .then(response => response.json())
        .then(data => {
            categories = data;
            const categorySelect = document.getElementById('categorySelect');
            const editCategorySelect = document.getElementById('editCategorySelect');
            const categoryNameSelect = document.getElementById('categoryNameSelect');

            // Очистка и заполнение выпадающих списков
            [categorySelect, editCategorySelect, categoryNameSelect].forEach(select => {
                select.innerHTML = '<option value="">Выберите категорию</option>';
                data.forEach(category => {
                    const option = document.createElement('option');
                    option.value = category.id;
                    option.textContent = category.title;
                    select.appendChild(option);
                });
            });
        })
        .catch(error => console.error('Ошибка загрузки категорий:', error));
}

// Переключаю формы
function toggleForm(formId) {
    const form = document.getElementById(formId);
    form.style.display = form.style.display === 'block' ? 'none' : 'block';
    if (form.style.display === 'block') {
        if (formId !== 'addAdvertisementForm') document.getElementById('addAdvertisementForm').style.display = 'none';
        if (formId !== 'editAdvertisementForm') document.getElementById('editAdvertisementForm').style.display = 'none';
        document.getElementById('searchCategoryContainer').style.display = 'none';
    }
}

function toggleEditForm(formId) {
    const editContainer = document.getElementById('editAdvertisementForm');
    editContainer.style.display = editContainer.style.display === 'block' ? 'none' : 'block';
}

// Переключаю поиск
function toggleSearchForm() {
    const searchContainer = document.getElementById('searchCategoryContainer');
    searchContainer.style.display = searchContainer.style.display === 'block' ? 'none' : 'block';
    if (searchContainer.style.display === 'block') {
        document.getElementById('addAdvertisementForm').style.display = 'none';
        document.getElementById('editAdvertisementForm').style.display = 'none';
    }
}

// Универсальная функция для обработки ответа от сервера
async function handleResponse(response) {
    if (!response.ok) {
        // Пытаемся прочитать ошибку как JSON, если не получается — как текст
        const errorText = await response.text();
        try {
            const errorJson = JSON.parse(errorText);
            throw new Error(errorJson.message || 'Произошла ошибка');
        } catch (e) {
            throw new Error(errorText || 'Произошла ошибка');
        }
    }
    return response.json();
}

// Добавление объявления
document.getElementById('addAdvertisementForm').addEventListener('submit', async function(event) {
    event.preventDefault();

    const title = document.getElementById('title').value;
    const price = document.getElementById('price').value;

    const titleError = validateTitle(title);
    if (titleError) {
        showError(titleError);
        return;
    }

    const priceError = validatePrice(price);
    if (priceError) {
        showError(priceError);
        return;
    }

    const formData = new FormData();
    formData.append('title', title);
    formData.append('idCategory', document.getElementById('categorySelect').value);
    formData.append('description', document.getElementById('description').value);
    formData.append('price', price);
    formData.append('photo', document.getElementById('photo').files[0]);

    try {
        const response = await fetch(apiUrl, {
            method: 'POST',
            body: formData
        });
        const data = await handleResponse(response);
        showSuccess('Объявление успешно добавлено!');
        document.getElementById('addAdvertisementForm').reset();
        document.getElementById('formContainer').style.display = 'none';
        getAllAdvertisements();
        hideError();
    } catch (error) {
        showError(error.message);
        console.error('Ошибка:', error);
    }
});

// Редактирование объявления
document.getElementById('editAdvertisementForm').addEventListener('submit', async function(event) {
    event.preventDefault();

    const title = document.getElementById('editTitle').value;
    const price = document.getElementById('editPrice').value;

    const titleError = validateTitle(title);
    if (titleError) {
        showError(titleError);
        return;
    }

    const priceError = validatePrice(price);
    if (priceError) {
        showError(priceError);
        return;
    }

    const formData = new FormData();
    formData.append('title', title);
    formData.append('idCategory', document.getElementById('editCategorySelect').value);
    formData.append('description', document.getElementById('editDescription').value);
    formData.append('price', price);
    if (document.getElementById('editPhoto').files[0]) {
        formData.append('photo', document.getElementById('editPhoto').files[0]);
    }

    const id = document.getElementById('editId').value;

    try {
        const response = await fetch(`${apiUrl}/${id}`, {
            method: 'PUT',
            body: formData
        });
        const data = await handleResponse(response);
        showSuccess('Объявление успешно обновлено!');
        document.getElementById('editAdvertisementForm').reset();
        document.getElementById('editAdvertisementForm').style.display = 'none';
        getAllAdvertisements();
        hideError();
    } catch (error) {
        showError(error.message);
        console.error('Ошибка:', error);
    }
});

// Поиск по категории
document.getElementById('getByCategoryButton').addEventListener('click', async function() {
    const categoryName = document.getElementById('categoryNameSelect').options[document.getElementById('categoryNameSelect').selectedIndex].text;
    try {
        const response = await fetch(`${apiUrl}/by-category?categoryName=${encodeURIComponent(categoryName)}`);
        const data = await handleResponse(response);
        const advertisementsByCategoryContainer = document.getElementById('advertisementsByCategory');
        advertisementsByCategoryContainer.innerHTML = '';
        data.forEach(ad => {
            fetch(`${apiUrl}/photo/${ad.id}`)
                .then(response => response.text())
                .then(base64Image => {
                    const card = document.createElement('div');
                    card.className = 'card';
                    card.setAttribute('data-ad-id', ad.id);
                    card.setAttribute('data-ad-user-id', ad.idUsers.id);
                    card.innerHTML = `
                        <img src="data:image/jpeg;base64,${base64Image}" alt="${ad.title}">
                        <h3>${ad.title}</h3>
                        <p>Цена: ${formatPrice(ad.price)}</p> <!-- Форматируем цену -->
                        ${currentUser  && (currentUser .role === 'Администратор' || currentUser .id === ad.idUsers.id) ? `
                            <button onclick="editAdvertisement(${ad.id})">Редактировать</button>
                            <button onclick="deleteAdvertisement(${ad.id})">Удалить</button>
                        ` : ''}
                        ${currentUser && currentUser.id !== ad.idUsers.id ? `
                             <button onclick="addToCart(${ad.id})">В корзину</button>
                        ` : ''}
                    `;
                    advertisementsByCategoryContainer.appendChild(card);
                    updateButtons();
                })
                .catch(error => {
                    console.error('Ошибка загрузки изображения:', error);
                    showError('Ошибка загрузки изображения');
                });
        });
        document.getElementById('resetSearchButton').style.display = 'block';
    } catch (error) {
        console.error('Ошибка:', error);
        showError(error.message);
    }
});

// Получение всех объявлений
function getAllAdvertisements() {
    fetch(apiUrl)
        .then(response => response.json())
        .then(data => {
            const allAdvertisementsContainer = document.getElementById('allAdvertisements');
            allAdvertisementsContainer.innerHTML = '';
            data.forEach(ad => {
                fetch(`${apiUrl}/photo/${ad.id}`)
                    .then(response => response.text())
                    .then(base64Image => {
                        const card = document.createElement('div');
                        card.className = 'card';
                        card.setAttribute('data-ad-id', ad.id);
                        card.setAttribute('data-ad-user-id', ad.idUsers.id);
                        card.innerHTML = `
                            <img src="data:image/jpeg;base64,${base64Image}" alt="${ad.title}">
                            <h3>${ad.title}</h3>
                            <p>Цена: ${formatPrice(ad.price)}</p>
                            ${currentUser && (currentUser.role === 'Администратор' || currentUser.id === ad.idUsers.id) ? `
                                <button onclick="editAdvertisement(${ad.id})">Редактировать</button>
                                <button onclick="deleteAdvertisement(${ad.id})">Удалить</button>
                            ` : ''}
                            ${currentUser && currentUser.id !== ad.idUsers.id ? `
                                <button onclick="addToCart(${ad.id})">В корзину</button>
                            ` : ''}
                        `;
                        allAdvertisementsContainer.appendChild(card);
                    })
                    .catch(error => console.error('Ошибка загрузки изображения:', error));
            });
        })
        .catch(error => console.error('Ошибка:', error));
}
function getCartId() {
    return fetch('/api/cart/cartId')
        .then(response => {
            if (response.ok) {
                return response.json();
            } else {
                throw new Error('Ошибка при получении идентификатора корзины');
            }
        })
        .then(data => {
            return data; // data будет содержать cartId
        })
        .catch(error => {
            console.error('Ошибка:', error);
            throw error;
        });
}

function addToCart(adId) {
    // Сначала проверяем, есть ли у пользователя корзина
    getCartId()
        .then(cartId => {
            // Если корзина есть, добавляем товар
            console.log('Корзина найдена, cartId:', cartId);
            return fetch(`/api/cart/${cartId}/add/${adId}`, {
                method: 'POST'
            });
        })
        .catch(error => {
            // Если корзины нет, создаем её
            console.log('Корзина не найдена, создаем новую...');
            return fetch('/api/cart/create', {
                method: 'POST'
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Ошибка при создании корзины');
                }
                return response.json();
            })
            .then(cart => {
                console.log('Корзина создана, cartId:', cart.id);
                // После создания корзины добавляем товар
                return fetch(`/api/cart/${cart.id}/add/${adId}`, {
                    method: 'POST'
                });
            });
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Ошибка при добавлении товара в корзину');
            }
            return response.json();
        })
        .then(data => {
            console.log('Товар успешно добавлен в корзину:', data);
            showSuccess('Товар успешно добавлен в корзину!');
        })
        .catch(error => {
            console.error('Ошибка:', error);
            showError(error.message);
        });
}

// Редактирование объявления (заполнение формы)
function editAdvertisement(id) {
    fetch(`${apiUrl}/${id}`)
        .then(response => response.json())
        .then(ad => {
            document.getElementById('editId').value = ad.id;
            document.getElementById('editTitle').value = ad.title;
            document.getElementById('editCategorySelect').value = ad.idCategory.id;
            document.getElementById('editDescription').value = ad.description;
            document.getElementById('editPrice').value = ad.price;
            toggleEditForm();
        })
        .catch(error => {
            alert('Ошибка при загрузке данных объявления');
            console.error('Ошибка:', error);
        });
}

// Удаление объявления
async function deleteAdvertisement(id) {
    try {
        const response = await fetch(`${apiUrl}/${id}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Ошибка при удалении объявления');
        }

        showSuccess('Объявление успешно удалено!');

        getAllAdvertisements();

        hideError();
    } catch (error) {
        showError(error.message);
        console.error('Ошибка:', error);
    }
}

function toggleForm() {
    var formContainer = document.getElementById("formContainer");
    if (formContainer.style.display === "none" || formContainer.style.display === "") {
        formContainer.style.display = "block";
    } else {
        formContainer.style.display = "none";
    }
}

function toggleSearchForm() {
    var searchContainer = document.getElementById("searchContainer");
    if (searchContainer.style.display === "none" || searchContainer.style.display === "") {
        searchContainer.style.display = "block";
    } else {
        searchContainer.style.display = "none";
    }
}

window.onload = function() {
    loadCategories();
    loadCurrentUser();
    updateAuthButtons(); // Обновляем кнопки при загрузке страницы
};