# Лабораторная работа №9 — JavaDB (Вариант 46)

GUI-приложение на Swing с подключением к Apache Derby через JDBC. Задание: **«Учебные аудитории»**, вариант **«д»** — телефонный справочник + средняя площадь, закреплённая за ответственными.

## Вариант 46

Число варианта: **417** → двоичное: `0110100001`

| Столбец | Битов | Биты | Значение | Что означает |
|---------|-------|------|----------|--------------|
| П.5 | 1 | 0 | **0** | Интерфейс — **GUI (Swing)** |
| П.6 | 1 | 1 | **1** | Сброс полей по умолчанию — **из файла** |
| П.7 | 1 | 1 | **1** | Вывод всех значений поля **второй** таблицы (RESPONSIBLES) |
| П.8 | 1 | 0 | **0** | Поле — **первое** не-ключевое (ФИО) |
| П.10/П.13 | 3 | 100 | **«д»** | Вариант задания |
| П.12 | 1 | 0 | 0 | (не применяется, т.к. П.14=0) |
| П.14 | 1 | 0 | **0** | Задание — **«Учебные аудитории»** |
| П.15 | 1 | 1 | **1** | Выбор записи **по уникальному ключу** |

### Вариант «д» для «Учебные аудитории»
- Вывести **телефонный справочник (ФИО, телефон)** в лексикографическом порядке
- Найти **среднюю площадь, закреплённую за ответственными**

---

## Структура проекта

```
Laba_9/
├── README.md
├── Dockerfile                            # Derby Network Server
├── docker-compose.yml                    # запуск Derby
├── init.sql                              # схема + seed-данные
├── defaults.properties                   # значения по умолчанию (П.6=1)
├── derby-data/                           # persistence для Derby (создаётся docker)
├── lib/
│   ├── derbyclient.jar                   # JDBC client driver
│   ├── derbyshared.jar                   # общие классы Derby
│   └── derbytools.jar                    # утилиты
└── src/main/java/univapp/
    ├── Db.java                           # подключение JDBC
    ├── Defaults.java                     # чтение defaults.properties
    ├── ClassroomDialog.java              # модалка для аудитории (CRUD)
    ├── ResponsibleDialog.java            # модалка для ответственного (CRUD)
    └── Main.java                         # главное окно (Swing)
```

## Схема БД

Связь **1-ко-Многим**: один ответственный → много аудиторий.

```
┌─────────────────────────┐          ┌──────────────────────────────┐
│  RESPONSIBLES (табл.2)  │          │  CLASSROOMS (табл.1)         │
├─────────────────────────┤          ├──────────────────────────────┤
│  ID (PK)            ──────────┐    │  ID (PK)                     │
│  FIO                          │    │  BUILDING                    │
│  POSITION                     │    │  ROOM_NUMBER                 │
│  PHONE                        │    │  NAME                        │
│  AGE                          │    │  AREA                        │
└─────────────────────────┘     └──→ │  RESPONSIBLE_ID (FK) ────────│
                                     └──────────────────────────────┘
```

```sql
CREATE TABLE RESPONSIBLES (
    ID INTEGER NOT NULL PRIMARY KEY,
    FIO VARCHAR(100) NOT NULL,
    POSITION VARCHAR(50) NOT NULL,
    PHONE VARCHAR(20) NOT NULL,
    AGE INTEGER NOT NULL
);

CREATE TABLE CLASSROOMS (
    ID INTEGER NOT NULL PRIMARY KEY,
    BUILDING VARCHAR(50) NOT NULL,
    ROOM_NUMBER VARCHAR(20) NOT NULL,
    NAME VARCHAR(100) NOT NULL,
    AREA DOUBLE NOT NULL,
    RESPONSIBLE_ID INTEGER NOT NULL,
    CONSTRAINT FK_RESPONSIBLE FOREIGN KEY (RESPONSIBLE_ID) REFERENCES RESPONSIBLES(ID)
);
```

## Архитектура

```
┌──────────────────────────────────┐
│  GUI клиент (Swing)              │
│                                   │
│  ┌────────────────────────────┐  │
│  │ [Аудитории] [Ответственные]│  │  ← JTabbedPane
│  ├────────────────────────────┤  │
│  │ Просмотр Создать Изменить  │  │
│  │ Удалить                    │  │  ← кнопки CRUD
│  ├────────────────────────────┤  │
│  │  JTable (список записей)   │  │
│  └────────────────────────────┘  │
│  ┌────────────────────────────┐  │
│  │ Ключ: [___]                │  │  ← П.15=1
│  ├────────────────────────────┤  │
│  │ [Список ФИО] [Тел. справ.] │  │  ← запросы по варианту
│  │ [Средняя площадь]          │  │
│  ├────────────────────────────┤  │
│  │ JTextArea (результат)      │  │
│  └────────────────────────────┘  │
└────────────┬─────────────────────┘
             │ JDBC (jdbc:derby://localhost:1527)
             ▼
┌──────────────────────────────────┐
│  Derby Network Server            │
│  (Docker контейнер laba9-derby)  │
│  Порт: 1527                       │
│  БД: UnivDB                      │
└──────────────────────────────────┘
```

### Файлы и их роль

| Файл | Роль |
|------|------|
| `Db.java` | Синглтон подключения к БД. Регистрирует `ClientAutoloadedDriver` |
| `Defaults.java` | Чтение `defaults.properties` (П.6=1: сброс из файла) |
| `Main.java` | Главное окно. Две вкладки + панель запросов и сервиса |
| `ClassroomDialog.java` | Модалка для аудитории: создание / редактирование / просмотр. Кнопка «Сброс к умолчанию» подставляет значения из файла |
| `ResponsibleDialog.java` | То же для ответственного |

---

## Запуск

### 1. Поднять Derby в Docker

```bash
cd Laba_9
docker compose build
docker compose up -d
```

Проверка:
```bash
docker compose logs derby
# должно быть: "Apache Derby Network Server - started and ready to accept connections on port 1527"
```

### 2. Создать БД и наполнить данными (один раз)

```bash
docker cp init.sql laba9-derby:/tmp/init.sql

docker exec laba9-derby sh -c "echo \"CONNECT 'jdbc:derby://localhost:1527/UnivDB;user=db_user;password=db_user;create=true';\" > /tmp/setup.sql && cat /tmp/init.sql >> /tmp/setup.sql && echo 'DISCONNECT;' >> /tmp/setup.sql && echo 'EXIT;' >> /tmp/setup.sql && java -jar /derby/lib/derbyrun.jar ij /tmp/setup.sql"
```

### 3. Скомпилировать клиент

```bash
cd src/main/java
javac -encoding UTF-8 -cp ../../../lib/derbyclient.jar univapp/*.java
cd ../../..
```

### 4. Запустить GUI

```bash
java -cp "lib/*:src/main/java" univapp.Main
```

### 5. Остановить Derby

```bash
docker compose down
```

Данные останутся в `./derby-data/` благодаря bind-mount.

---

## Использование

### Вкладка «Аудитории»

- **Просмотр** — открывает диалог только для чтения (выбранная строка или ключ из поля)
- **Создать** — новая запись; ID подставляется автоматически (MAX+1), остальные поля — из `defaults.properties`
- **Редактировать** — изменение по уникальному ключу (П.15=1)
- **Удалить** — удаление по ключу (с подтверждением, в транзакции)
- **Сброс к умолчанию** (внутри диалога) — заполняет поля из файла `defaults.properties` (П.6=1)

### Вкладка «Ответственные»
То же самое для таблицы RESPONSIBLES.

### Поле «Уникальный ключ записи»
Если ввести число в это поле, операции Просмотр/Изменить/Удалить будут работать по этому ключу, минуя выбор в таблице. Это реализация **П.15=1: выбор по уникальному ключу**.

### Запросы (внизу окна)

1. **«Список ФИО (П.7=1, П.8=0)»** — выводит все ФИО из второй таблицы (RESPONSIBLES) в лексикографическом порядке.

2. **«Телефонный справочник (вариант д)»** — выводит пары (ФИО, телефон) в лекс. порядке. Это часть задания варианта «д».

3. **«Средняя площадь по ответственным (вариант д)»** — для каждого ответственного считает суммарную площадь его аудиторий, выводит таблицу и среднее значение. Вторая часть задания варианта «д».

```
=== Вариант д: средняя площадь, закреплённая за ответственными ===
ФИО                                      Σ площадей
------------------------------------------------------------
Алексеев Алексей Алексеевич              40.00
Иванов Иван Иванович                     115.50
Петров Пётр Петрович                     160.00
Сидорова Анна Сергеевна                  40.00
------------------------------------------------------------
Средняя площадь на ответственного: 88.88 кв.м (всего ответственных: 4)
```

### Транзакции (п.17)

Удаление ответственного — это **транзакция**: сначала удаляются все его аудитории, потом сама запись. Если что-то идёт не так — `rollback()`:

```java
c.setAutoCommit(false);
try {
    DELETE FROM CLASSROOMS WHERE RESPONSIBLE_ID = ?;
    DELETE FROM RESPONSIBLES WHERE ID = ?;
    c.commit();
} catch (SQLException e) {
    c.rollback();
}
```

---

## Соответствие ТЗ (Вариант 46)

| Пункт ТЗ | Как выполнено |
|----------|---------------|
| 1. БД + подключение | Apache Derby в Docker, JDBC через `derbyclient.jar` |
| 3. БД derby | `Dockerfile` тянет `db-derby-10.16.1.1-bin` |
| 4. ≥2 связанных по ключу таблиц | RESPONSIBLES и CLASSROOMS, FK `RESPONSIBLE_ID` |
| 5. GUI клиент (П.5=0) | Swing-приложение `Main.java` |
| 6. Сброс из файла (П.6=1) | `defaults.properties` + кнопка «Сброс к умолчанию» в диалогах |
| 7. Вывод ФИО (П.7=1, П.8=0) | Кнопка «Список ФИО» → запрос `SELECT FIO FROM RESPONSIBLES ORDER BY FIO` |
| 10. «Учебные аудитории» вариант «д» | Кнопки «Телефонный справочник» и «Средняя площадь» |
| 14. Учебные аудитории (П.14=0) | Реализовано |
| 15. Выбор по уникальному ключу (П.15=1) | Поле «Уникальный ключ записи» в нижней панели |
| 16. Просмотр/Создание/Изменение/Удаление | Кнопки на каждой вкладке + диалоги |
| 17. Транзакции | `setAutoCommit(false)` + `commit()`/`rollback()` при удалении ответственного и в диалогах |

---

## Полезные команды

```bash
# Проверить что Derby слушает порт
lsof -i :1527

# Подключиться к Derby через ij (SQL CLI)
docker exec -it laba9-derby java -jar /derby/lib/derbyrun.jar ij
# Внутри ij:
# CONNECT 'jdbc:derby://localhost:1527/UnivDB;user=db_user;password=db_user';
# SELECT * FROM CLASSROOMS;
# DISCONNECT;
# EXIT;

# Пересоздать БД с нуля
docker compose down
rm -rf derby-data
docker compose up -d
# затем заново шаг 2 из "Запуск"

# Логи Derby
docker compose logs -f derby

# Перекомпилировать клиент
cd src/main/java && javac -encoding UTF-8 -cp ../../../lib/derbyclient.jar univapp/*.java && cd ../../..

# Запустить GUI
java -cp "lib/*:src/main/java" univapp.Main
```
