let selectedUserId = null;

function fetchUsers() {
    fetch('/api/users/all')
        .then(res => res.json())
        .then(data => {
            if (data.error === "unauthorized") {
                window.location.href = "login.html";
            } else if (data.error === "forbidden") {
                window.location.href = "ads.html";
            } else {
                const tbody = document.querySelector("#usersTable tbody");
                tbody.innerHTML = '';
                data.forEach(user => {
                    const row = document.createElement("tr");
                    row.innerHTML = `
                        <td>${user.id}</td>
                        <td>${user.login}</td>
                        <td>${user.role}</td>
                        <td><button onclick="openModal(${user.id})">Изменить роль</button></td>
                    `;
                    tbody.appendChild(row);
                });
            }
        });
}

function openModal(userId) {
    selectedUserId = userId;
    document.getElementById("modal").style.display = "block";
}

function closeModal() {
    document.getElementById("modal").style.display = "none";
    selectedUserId = null;
}

function saveRole() {
    const newRole = document.getElementById("newRole").value;
    fetch('/api/users/change-role?id=' + selectedUserId + '&role=' + newRole, {
        method: 'POST'
    }).then(res => res.json()).then(data => {
        if (data.success) {
            alert("Роль обновлена!");
            closeModal();
            fetchUsers();
        } else {
            alert("Ошибка обновления роли.");
        }
    });
}

window.onload = function() {
    // Инициализируем Google Переводчик
    initializeGoogleTranslate();
    fetchUsers();
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
