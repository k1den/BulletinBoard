const apiUrl = '/api/advertisements';
const categoriesUrl = '/api/advertisements/categories';
const currentUserUrl = '/api/advertisements/current-user';
const commentsApiUrl = '/api/comments';

let currentAdForComment = null;
let currentUser = null;

// Инициализация при загрузке страницы
window.onload = function() {
    loadCategories();
    loadCurrentUser();
    updateAuthButtons();
};

// Загрузка текущего пользователя
function loadCurrentUser() {
    fetch(currentUserUrl)
        .then(response => {
            if (response.ok) return response.json();
            throw new Error('Пользователь не авторизован');
        })
        .then(user => {
            currentUser = user;
            updateAuthButtons();
            addAdminButtonIfNeeded();
            getAllAdvertisements();
        })
        .catch(error => {
            console.error('Ошибка:', error);
            currentUser = null;
            updateAuthButtons();
            getAllAdvertisements();
        });
}

// Добавление кнопки админ-панели если пользователь администратор
function addAdminButtonIfNeeded() {
    if (currentUser && currentUser.role === 'Администратор') {
        const adminBtn = document.createElement('button');
        adminBtn.textContent = 'Админ-панель';
        adminBtn.className = 'admin-button';
        adminBtn.onclick = () => window.location.href = 'admin.html';

        const navButtons = document.querySelector('.nav-buttons');
        if (navButtons) {
            navButtons.insertBefore(adminBtn, navButtons.firstChild);
        }
    }
}

// Обновление кнопок авторизации
function updateAuthButtons() {
    const loginButton = document.getElementById('loginButton');
    const logoutButton = document.getElementById('logoutButton');

    if (currentUser) {
        loginButton.style.display = 'none';
        logoutButton.style.display = 'inline-block';
    } else {
        loginButton.style.display = 'inline-block';
        logoutButton.style.display = 'none';
    }
}

// Загрузка категорий
function loadCategories() {
    fetch(categoriesUrl)
        .then(response => response.json())
        .then(data => {
            const categorySelect = document.getElementById('categorySelect');
            const categoryNameSelect = document.getElementById('categoryNameSelect');

            [categorySelect, categoryNameSelect].forEach(select => {
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

// Создание карточки объявления
function createAdCard(ad, base64Image) {
    const card = document.createElement('div');
    card.className = 'card admin-card';
    card.setAttribute('data-ad-id', ad.id);
    card.setAttribute('data-ad-user-id', ad.idUsers.id);

    card.innerHTML = `
        <img src="data:image/jpeg;base64,${base64Image}" alt="${ad.title}">
        <h3>${ad.title}</h3>
        <p>Цена: ${formatPrice(ad.price)}</p>
        <div class="admin-buttons">
            ${currentUser && currentUser.id === ad.idUsers.id ? `
                <button class="edit-button" onclick="editUserAdvertisement(${ad.id})">Редактировать</button>
                <button class="delete-button" onclick="deleteUserAdvertisement(${ad.id})">Удалить</button>
            ` : ''}
            ${currentUser && currentUser.id !== ad.idUsers.id ? `
                <button onclick="addToCart(${ad.id})">В корзину</button>
                <button class="comment-button" onclick="showCommentForm(${ad.id})">Добавить комментарий</button>
            ` : ''}
        </div>
        <div class="comment-preview" style="display: none;"></div>
        <div class="comment-container" style="display: none;"></div>
    `;

    card.addEventListener('mouseenter', () => {
        loadCommentsForAd(ad.id);
        card.querySelector('.comment-preview').style.display = 'block';
    });

    card.addEventListener('mouseleave', () => {
        card.querySelector('.comment-preview').style.display = 'none';
    });

    return card;
}

// Форматирование цены
function formatPrice(price) {
    return Number(price).toLocaleString('ru-RU') + '₽';
}

// Получение всех объявлений
function getAllAdvertisements() {
    fetch(apiUrl)
        .then(response => response.json())
        .then(data => {
            const container = document.getElementById('allAdvertisements');
            container.innerHTML = '';
            data.forEach(ad => {
                fetch(`${apiUrl}/photo/${ad.id}`)
                    .then(response => response.text())
                    .then(base64Image => {
                        const card = createAdCard(ad, base64Image);
                        container.appendChild(card);
                    })
                    .catch(error => console.error('Ошибка загрузки изображения:', error));
            });
        })
        .catch(error => console.error('Ошибка:', error));
}

// Редактирование собственного объявления
function editUserAdvertisement(id) {
    fetch(`${apiUrl}/${id}`)
        .then(response => response.json())
        .then(ad => {
            if (currentUser && currentUser.id !== ad.idUsers.id) {
                showError('Вы можете редактировать только свои объявления');
                return;
            }

            // Создаем модальное окно для редактирования
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

            // Заполняем категории
            fetch(categoriesUrl)
                .then(response => response.json())
                .then(categories => {
                    const select = document.getElementById('editAdCategory');
                    categories.forEach(category => {
                        const option = document.createElement('option');
                        option.value = category.id;
                        option.textContent = category.title;
                        option.selected = (category.id === ad.idCategory.id);
                        select.appendChild(option);
                    });
                });
        })
        .catch(error => showError(error.message));
}

// Отправка отредактированного объявления
function submitAdEdit(id) {
    const title = document.getElementById('editAdTitle').value.trim();
    const description = document.getElementById('editAdDescription').value.trim();
    const price = document.getElementById('editAdPrice').value;
    const categoryId = document.getElementById('editAdCategory').value;
    const photoFile = document.getElementById('editAdPhoto').files[0];

    if (!title || !description || !price || !categoryId) {
        showError('Заполните все обязательные поля');
        return;
    }

    const formData = new FormData();
    formData.append('title', title);
    formData.append('description', description);
    formData.append('price', price);
    formData.append('idCategory', categoryId);
    if (photoFile) {
        formData.append('photo', photoFile);
    }

    fetch(`${apiUrl}/${id}`, {
        method: 'PUT',
        body: formData
    })
        .then(response => {
            if (response.ok) {
                return response.json();
            }
            throw new Error('Ошибка при обновлении');
        })
        .then(() => {
            showSuccess('Объявление успешно обновлено!');
            document.querySelector('div[style*="position: fixed"]').remove();
            getAllAdvertisements();
        })
        .catch(error => showError(error.message));
}

// Удаление собственного объявления
function deleteUserAdvertisement(id) {
    if (!confirm('Вы уверены, что хотите удалить это объявление?')) return;

    fetch(`${apiUrl}/${id}`, {
        method: 'DELETE'
    })
        .then(response => {
            if (response.ok) {
                showSuccess('Объявление успешно удалено!');
                getAllAdvertisements();
            } else {
                throw new Error('Ошибка при удалении');
            }
        })
        .catch(error => showError(error.message));
}

// Работа с комментариями
function showCommentForm(adId) {
    document.getElementById('commentAdId').value = adId;
    document.getElementById('commentFormContainer').style.display = 'block';
}

function closeCommentForm() {
    document.getElementById('commentFormContainer').style.display = 'none';
}

async function submitComment() {
    const adId = document.getElementById('commentAdId').value;
    const commentText = document.getElementById('commentText').value.trim();

    if (!commentText) {
        showError('Комментарий не может быть пустым');
        return;
    }

    try {
        const response = await fetch(commentsApiUrl, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                adsId: adId,
                comment: commentText
            })
        });

        if (!response.ok) {
            throw new Error('Ошибка при добавлении комментария');
        }

        showSuccess('Комментарий успешно добавлен');
        document.getElementById('commentText').value = '';
        closeCommentForm();
        loadCommentsForAd(adId);
    } catch (error) {
        showError(error.message);
    }
}

async function loadCommentsForAd(adId) {
    try {
        const response = await fetch(`${commentsApiUrl}/ad/${adId}`);
        const comments = await response.json();
        updateCommentUI(adId, comments);
    } catch (error) {
        console.error('Ошибка загрузки комментариев:', error);
    }
}

function updateCommentUI(adId, comments) {
    const card = document.querySelector(`.card[data-ad-id="${adId}"]`);
    if (!card) return;

    const commentButton = card.querySelector('.comment-button');
    const commentContainer = card.querySelector('.comment-container');
    const commentPreview = card.querySelector('.comment-preview');

    if (commentContainer) commentContainer.innerHTML = '';

    const userComment = currentUser ? comments.find(comment => comment.idUsers.id === currentUser.id) : null;

    if (userComment) {
        if (commentButton) commentButton.style.display = 'none';

        const commentControls = document.createElement('div');
        commentControls.className = 'comment-controls';
        commentControls.innerHTML = `
            <button onclick="viewComment('${escapeHtml(userComment.text)}')">Посмотреть</button>
            <button onclick="deleteUserComment(${userComment.id}, ${adId})">Удалить</button>
        `;

        if (commentContainer) {
            commentContainer.appendChild(commentControls);
        }
    } else {
        if (commentButton) commentButton.style.display = 'inline-block';
        const existingControls = card.querySelector('.comment-controls');
        if (existingControls) existingControls.remove();
    }

    if (commentPreview) {
        commentPreview.innerHTML = comments.length > 0
            ? `<p>${comments.length} ${comments.length === 1 ? 'комментарий' : 'комментариев'}</p>`
            : '<p>Нет комментариев</p>';
    }
}

function viewComment(text) {
    alert(`Ваш комментарий: ${text}`);
}

async function deleteUserComment(commentId, adId) {
    if (!confirm('Вы уверены, что хотите удалить этот комментарий?')) return;

    try {
        const response = await fetch(`${commentsApiUrl}/${commentId}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            throw new Error('Ошибка при удалении комментария');
        }

        showSuccess('Комментарий успешно удален');
        loadCommentsForAd(adId);
    } catch (error) {
        showError(error.message);
    }
}

// Работа с корзиной
async function addToCart(adId) {
    try {
        let cartId = await getCartId();

        const response = await fetch(`/api/cart/${cartId}/add/${adId}`, {
            method: 'POST'
        });

        if (!response.ok) {
            throw new Error('Ошибка при добавлении в корзину');
        }

        showSuccess('Товар добавлен в корзину!');
    } catch (error) {
        if (error.message.includes('Корзина не найдена')) {
            await createCartAndAddItem(adId);
        } else {
            showError(error.message);
        }
    }
}

async function getCartId() {
    const response = await fetch('/api/cart/cartId');
    if (!response.ok) throw new Error('Корзина не найдена');
    const data = await response.json();
    return data.id;
}

async function createCartAndAddItem(adId) {
    const createResponse = await fetch('/api/cart/create', {
        method: 'POST'
    });

    if (!createResponse.ok) {
        throw new Error('Ошибка при создании корзины');
    }

    const cart = await createResponse.json();
    const addResponse = await fetch(`/api/cart/${cart.id}/add/${adId}`, {
        method: 'POST'
    });

    if (!addResponse.ok) {
        throw new Error('Ошибка при добавлении в корзину');
    }

    showSuccess('Товар добавлен в корзину!');
}

// Вспомогательные функции
function escapeHtml(unsafe) {
    return unsafe
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

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

// Поиск объявлений
function searchAdvertisementsByTitle() {
    const searchText = document.getElementById('searchTitleInput').value.trim();

    if (!searchText) {
        showError('Введите текст для поиска');
        return;
    }

    fetch(`${apiUrl}/search?title=${encodeURIComponent(searchText)}`)
        .then(response => response.json())
        .then(data => {
            const container = document.getElementById('advertisementsByCategory');
            container.innerHTML = '';

            if (data.length === 0) {
                container.innerHTML = '<p>Ничего не найдено</p>';
                return;
            }

            data.forEach(ad => {
                fetch(`${apiUrl}/photo/${ad.id}`)
                    .then(response => response.text())
                    .then(base64Image => {
                        const card = createAdCard(ad, base64Image);
                        container.appendChild(card);
                    })
                    .catch(error => {
                        console.error('Ошибка загрузки изображения:', error);
                        showError('Ошибка загрузки изображения');
                    });
            });

            document.getElementById('resetSearchButton').style.display = 'block';
        })
        .catch(error => {
            console.error('Ошибка:', error);
            showError(error.message);
        });
}

// Поиск объявлений по категории
function getAdvertisementsByCategory() {
    const categorySelect = document.getElementById('categoryNameSelect');
    const categoryName = categorySelect.options[categorySelect.selectedIndex].text;

    if (!categorySelect.value) {
        showError('Выберите категорию для поиска');
        return;
    }

    // Показываем индикатор загрузки
    const searchBtn = document.getElementById('getByCategoryButton');
    searchBtn.disabled = true;
    searchBtn.textContent = 'Поиск...';

    fetch(`${apiUrl}/by-category?categoryName=${encodeURIComponent(categoryName)}`)
        .then(response => {
            searchBtn.disabled = false;
            searchBtn.textContent = 'Найти объявления';
            if (!response.ok) throw new Error('Ошибка сервера');
            return response.json();
        })
        .then(data => {
            const container = document.getElementById('advertisementsByCategory');
            container.innerHTML = '';

            if (data.length === 0) {
                container.innerHTML = '<p>Нет объявлений в выбранной категории</p>';
                return;
            }

            data.forEach(ad => {
                fetch(`${apiUrl}/photo/${ad.id}`)
                    .then(response => response.text())
                    .then(base64Image => {
                        const card = createAdCard(ad, base64Image);
                        container.appendChild(card);
                    })
                    .catch(() => {
                        const card = createAdCard(ad, '');
                        container.appendChild(card);
                    });
            });

            document.getElementById('resetSearchButton').style.display = 'block';
        })
        .catch(error => {
            console.error('Ошибка:', error);
            showError(error.message);
        });
}

function resetSearch() {
    document.getElementById('advertisementsByCategory').innerHTML = '';
    document.getElementById('searchTitleInput').value = '';
    document.getElementById('categoryNameSelect').selectedIndex = 0;
    document.getElementById('resetSearchButton').style.display = 'none';
    getAllAdvertisements();
}

// Управление формами
function toggleForm(formId) {
    const form = document.getElementById(formId);
    form.style.display = form.style.display === 'block' ? 'none' : 'block';
}

function toggleSearchForm() {
    const searchContainer = document.getElementById('searchContainer');
    searchContainer.style.display = searchContainer.style.display === 'block' ? 'none' : 'block';
}

async function submitAdForm() {
    const title = document.getElementById('title').value.trim();
    const categoryId = document.getElementById('categorySelect').value;
    const description = document.getElementById('description').value.trim();
    const price = document.getElementById('price').value;
    const photoFile = document.getElementById('photo').files[0];

    if (!photoFile) {
        showError('Выберите изображение для объявления');
        return;
    }

    try {
        // Создаем FormData и добавляем все поля
        const formData = new FormData();
        formData.append('title', title);
        formData.append('idCategory', categoryId);
        formData.append('description', description);
        formData.append('price', price);
        formData.append('photo', photoFile);  // Обратите внимание на имя 'photo' - должно совпадать с @RequestParam на сервере

        console.log('Отправляемые данные:');
        for (let [key, value] of formData.entries()) {
            console.log(key, value);
        }

        const response = await fetch(apiUrl, {
            method: 'POST',
            body: formData  // Не устанавливаем Content-Type - браузер сделает это автоматически
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Ошибка сервера');
        }

        const responseData = await response.json();
        console.log('Ответ сервера:', responseData);

        showSuccess('Объявление успешно добавлено!');
        document.getElementById('addAdvertisementForm').reset();
        toggleForm('formContainer');
        getAllAdvertisements();
    } catch (error) {
        console.error('Ошибка:', error);
        showError(error.message || 'Произошла ошибка при отправке формы');
    }
}

function initializeGoogleTranslate() {
    // Создаем div для виджета
    const translateDiv = document.createElement('div');
    translateDiv.id = 'google_translate_element';
    translateDiv.style.position = 'fixed';
    translateDiv.style.bottom = '20px';
    translateDiv.style.right = '20px';
    translateDiv.style.zIndex = '1000';
    document.body.appendChild(translateDiv);

    // Функция для загрузки скрипта Google Translate
    function loadGoogleTranslateScript() {
        const script = document.createElement('script');
        script.type = 'text/javascript';
        script.src = 'https://translate.google.com/translate_a/element.js?cb=googleTranslateElementInit';
        document.head.appendChild(script);
    }

    // Функция инициализации виджета
    window.googleTranslateElementInit = function() {
        new google.translate.TranslateElement({
            pageLanguage: 'ru',
            includedLanguages: 'en,es,fr,de,zh-CN,ja,ar,ru',
            layout: google.translate.TranslateElement.InlineLayout.SIMPLE,
            autoDisplay: false
        }, 'google_translate_element');

        // Оставляем только стилизацию для комбобокса (по желанию)
        const style = document.createElement('style');
        style.textContent = `
            .goog-te-gadget .goog-te-combo {
                padding: 5px;
                border-radius: 4px;
                border: 1px solid #ccc;
                background-color: white;
                color: #000 !important;
            }
        `;
        document.head.appendChild(style);
    };

    // Загружаем скрипт Google Translate
    loadGoogleTranslateScript();
}

window.onload = function() {
    loadCategories();
    loadCurrentUser();
    updateAuthButtons();
    initializeGoogleTranslate();

    // Добавьте этот код
    const addForm = document.getElementById('addAdvertisementForm');
    if (addForm) {
        addForm.addEventListener('submit', function(e) {
            e.preventDefault();
            submitAdForm();
        });
    }
};