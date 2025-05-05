const apiUrl = '/api/advertisements';
const categoriesUrl = '/api/advertisements/categories';
const currentUserUrl = '/api/advertisements/current-user';
const commentsApiUrl = '/api/comments';

let currentUser = null;

// Проверка прав администратора
function checkAdmin() {
    if (!currentUser || currentUser.role !== 'Администратор') {
        window.location.href = 'ads.html';
    }
}

// Загрузка информации о текущем пользователе
function loadCurrentUser() {
    fetch(currentUserUrl)
        .then(response => {
            if (response.ok) return response.json();
            throw new Error('Пользователь не авторизован');
        })
        .then(user => {
            currentUser = user;
            checkAdmin();
            getAllAdvertisementsForAdmin();
        })
        .catch(error => {
            console.error('Ошибка:', error);
            window.location.href = 'ads.html';
        });
}

// Создание карточки объявления для админки
function createAdminAdTableRow(ad, base64Image) {
    const row = document.createElement('tr');

    row.innerHTML = `
        <td><img src="data:image/jpeg;base64,${base64Image}" alt="${ad.title}" style="max-width: 100px; max-height: 100px;"></td>
        <td>${ad.title}</td>
        <td>${ad.description}</td>
        <td>${formatPrice(ad.price)}</td>
        <td>${ad.idCategory.title}</td> <!-- Отображаем название категории вместо даты -->
        <td>
            <button class="edit-button" onclick="editAdvertisement(${ad.id})">Редактировать</button>
            <button class="delete-button" onclick="deleteAdvertisement(${ad.id})">Удалить</button>
        </td>
    `;
    return row;
}


// Получение всех объявлений для админки в виде таблицы
function getAllAdvertisementsForAdmin() {
    fetch(apiUrl)
        .then(response => response.json())
        .then(data => {
            const container = document.getElementById('advertisementsTable');
            const tbody = container.querySelector('tbody');
            tbody.innerHTML = '';

            // Загружаем категории один раз
            fetch(categoriesUrl)
                .then(response => response.json())
                .then(categories => {
                    // Создаем карточки объявлений с категориями
                    data.forEach(ad => {
                        // Находим категорию для текущего объявления
                        const category = categories.find(c => c.id === ad.idCategory.id);
                        ad.idCategory.title = category ? category.title : 'Без категории';

                        fetch(`${apiUrl}/photo/${ad.id}`)
                            .then(response => response.text())
                            .then(base64Image => {
                                const row = createAdminAdTableRow(ad, base64Image);
                                tbody.appendChild(row);
                            })
                            .catch(error => {
                                console.error('Ошибка загрузки изображения:', error);
                                const row = createAdminAdTableRow(ad, '');
                                tbody.appendChild(row);
                            });
                    });
                })
                .catch(error => console.error('Ошибка загрузки категорий:', error));
        })
        .catch(error => console.error('Ошибка:', error));
}

// Инициализация таблицы при загрузке страницы
window.onload = function() {
    loadCurrentUser();

    // Создаем таблицу, если ее нет
    const container = document.getElementById('allAdvertisements');
    if (!container.querySelector('table')) {
        container.innerHTML = `
            <table id="advertisementsTable" class="admin-table">
                <thead>
                    <tr>
                        <th>Фото</th>
                        <th>Название</th>
                        <th>Описание</th>
                        <th>Цена</th>
                        <th>Категория</th> <!-- Измененный заголовок -->
                        <th>Действия</th>
                    </tr>
                </thead>
                <tbody></tbody>
            </table>
        `;
    }
};

// Форматирование цены
function formatPrice(price) {
    return Number(price).toLocaleString('ru-RU') + '₽';
}

// Редактирование объявления (полностью соответствует второму примеру)
async function editAdvertisement(id) {
    try {
        // 1. Получаем текущие данные объявления
        const adResponse = await fetch(`${apiUrl}/${id}`);
        if (!adResponse.ok) throw new Error('Ошибка загрузки данных объявления');
        const ad = await adResponse.json();

        // 2. Создаем модальное окно для редактирования
        const modal = document.createElement('div');
        modal.style.position = 'fixed';
        modal.style.top = '50%';
        modal.style.left = '50%';
        modal.style.transform = 'translate(-50%, -50%)';
        modal.style.backgroundColor = 'white';
        modal.style.padding = '20px';
        modal.style.borderRadius = '10px';
        modal.style.boxShadow = '0 0 15px rgba(0,0,0,0.2)';
        modal.style.zIndex = '1000';
        modal.style.width = '400px';

        modal.innerHTML = `
            <h3>Редактирование объявления</h3>
            <div style="margin-bottom: 15px;">
                <label>Название:</label>
                <input type="text" id="editAdTitle" value="${ad.title}" style="width: 100%; padding: 8px;">
            </div>
            <div style="margin-bottom: 15px;">
                <label>Описание:</label>
                <textarea id="editAdDescription" style="width: 100%; padding: 8px; height: 100px;">${ad.description}</textarea>
            </div>
            <div style="margin-bottom: 15px;">
                <label>Цена:</label>
                <input type="number" id="editAdPrice" value="${ad.price}" style="width: 100%; padding: 8px;">
            </div>
            <div style="margin-bottom: 15px;">
                <label>Категория:</label>
                <select id="editAdCategory" style="width: 100%; padding: 8px;">
                    <option value="">Выберите категорию</option>
                </select>
            </div>
            <div style="margin-bottom: 15px;">
                <label>Новое изображение (оставьте пустым, чтобы не менять):</label>
                <input type="file" id="editAdPhoto" accept="image/*" style="width: 100%;">
            </div>
            <div style="display: flex; justify-content: space-between;">
                <button onclick="document.body.removeChild(this.parentNode.parentNode)">Отмена</button>
                <button onclick="submitAdEdit(${id})">Сохранить</button>
            </div>
        `;

        document.body.appendChild(modal);

        // 3. Заполняем категории
        const categoriesResponse = await fetch(categoriesUrl);
        if (!categoriesResponse.ok) throw new Error('Ошибка загрузки категорий');
        const categories = await categoriesResponse.json();

        const select = document.getElementById('editAdCategory');
        categories.forEach(category => {
            const option = document.createElement('option');
            option.value = category.id;
            option.textContent = category.title;
            option.selected = (category.id === ad.idCategory.id);
            select.appendChild(option);
        });

    } catch (error) {
        showError(error.message);
    }
}

// Отправка отредактированного объявления (полностью соответствует второму примеру)
async function submitAdEdit(id) {
    try {
        const title = document.getElementById('editAdTitle').value.trim();
        const description = document.getElementById('editAdDescription').value.trim();
        const price = document.getElementById('editAdPrice').value;
        const categoryId = document.getElementById('editAdCategory').value;
        const photoFile = document.getElementById('editAdPhoto').files[0];

        if (!title || !description || !price || !categoryId) {
            throw new Error('Заполните все обязательные поля');
        }

        const formData = new FormData();
        formData.append('title', title);
        formData.append('description', description);
        formData.append('price', price);
        formData.append('idCategory', categoryId);

        if (photoFile) {
            formData.append('photo', photoFile);
        }

        const response = await fetch(`${apiUrl}/${id}`, {
            method: 'PUT',
            body: formData
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Ошибка при обновлении');
        }

        // Закрываем форму и обновляем список
        document.querySelector('div[style*="position: fixed"]').remove();
        showSuccess('Объявление успешно обновлено!');
        getAllAdvertisementsForAdmin();

    } catch (error) {
        showError(error.message);
    }
}

// Удаление объявления
function deleteAdvertisement(id) {
    if (!confirm('Вы уверены, что хотите удалить это объявление?')) return;

    fetch(`${apiUrl}/${id}`, {
        method: 'DELETE'
    })
        .then(response => {
            if (response.ok) {
                showSuccess('Объявление успешно удалено!');
                getAllAdvertisementsForAdmin();
            } else {
                throw new Error('Ошибка при удалении');
            }
        })
        .catch(error => showError(error.message));
}

// Уведомления
function showError(message) {
    const errorContainer = document.getElementById('navErrorContainer');
    const errorMessage = document.getElementById('navErrorMessage');
    errorMessage.textContent = message;
    errorContainer.style.display = 'block';
    setTimeout(() => errorContainer.style.display = 'none', 5000);
}

function showSuccess(message) {
    const successContainer = document.getElementById('navSuccessContainer');
    const successMessage = document.getElementById('navSuccessMessage');
    successMessage.textContent = message;
    successContainer.style.display = 'block';
    setTimeout(() => successContainer.style.display = 'none', 5000);
}

// Выход из системы
function logout() {
    fetch('/api/auth/logout', {
        method: 'POST'
    })
        .then(() => {
            window.location.href = 'ads.html';
        })
        .catch(error => {
            console.error('Ошибка:', error);
        });
}