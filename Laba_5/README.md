# Лабораторная работа №5 — RMI клиент/сервер (Вариант 2)

Распределённое приложение на Java RMI в Docker-контейнерах.

## Вариант 2

| Пункт | Значение | Что означает |
|---|---|---|
| (П.6) | **1** | создаётся `compute.jar` (интерфейсы `Compute`+`Task`) |
| (П.7) | 0 | `compute.jar` у клиента — **локально** (внутри контейнера client) |
| (П.8) | 0 | `compute.jar` у сервера — **локально** (внутри контейнера server) |
| (П.9) | 0 | `client.policy` — **локально у клиента** |
| (П.10) | **1** | `server.policy` — **в сетевой папке** (`./share/`) |
| (П.11) | **1** | `SplitTask.class` — **в сетевой папке** (`./share/classes/client/`) |

Сервер читает policy и подгружает `SplitTask.class` через RMI codebase URL из общей сетевой папки.

## Задача (из лабы №1)

Клиент передаёт серверу массив целых чисел. Сервер выполняет `SplitTask.execute()`: делит числа на две последовательности — на **нечётных позициях** (1, 3, 5, ...) и на **чётных позициях** (2, 4, 6, ...). Возвращает `int[][]`, клиент печатает.

---

## Структура проекта

```
Laba_5/
├── Dockerfile                      # один JDK-образ, собирает compute.jar, server, client
├── docker-compose.yml              # 4 сервиса: prepare, rmiregistry, server, client
├── README.md
├── interface/
│   └── compute/
│       ├── Compute.java            # интерфейс Compute extends Remote
│       └── Task.java               # интерфейс Task<T>, extends Serializable
├── server/
│   └── engine/
│       └── ComputeEngine.java      # реализация Compute, регистрируется в rmiregistry
├── client/
│   ├── client/
│   │   ├── ClientMain.java         # args: <registryHost> <num1> <num2> ...
│   │   └── SplitTask.java          # Task<int[][]> — разделяет числа по позициям
│   └── client.policy               # локально у клиента (П.9)
└── share/                          # сетевая папка (bind-mount)
    ├── server.policy               # П.10
    └── classes/
        └── client/
            └── SplitTask.class     # П.11 — появится после `docker compose run --rm prepare`
```

## Архитектура

```
┌──────────────────────────────┐                  ┌────────────────┐
│         server               │                  │    client      │
│  ┌────────────────────────┐  │                  │                │
│  │ rmiregistry :1099      │◄─┼─── lookup ───────│  ClientMain    │
│  │ (bg process)           │  │                  │  SplitTask     │
│  └────────────▲───────────┘  │                  │                │
│               │ rebind       │                  │                │
│  ┌────────────┴───────────┐  │                  │                │
│  │ ComputeEngine          │◄─┼─── executeTask ──│                │
│  └────────────────────────┘  │                  └────────────────┘
└──────────────────────────────┘                         │
                                    │                          │
                         codebase=file:/share/classes/         │
                                    │                          │
                       ┌────────────┴──────────────┐           │
                       │  ./share  (bind-mount)    │◄──────────┘
                       │  server.policy            │  (read by server)
                       │  classes/client/          │
                       │    SplitTask.class        │  (loaded via codebase)
                       └───────────────────────────┘
```

`rmiregistry` запускается внутри того же контейнера, что и `ComputeEngine` — это требование RMI: операции `bind/rebind/unbind` разрешены только с локального хоста реестра. Разные контейнеры = разные хосты с точки зрения RMI.

---

## Запуск

### 1. Собрать образ
```bash
cd /Users/barni/JavaProjects/Laba_5
docker compose build
```

Dockerfile собирает:
- `compute.jar` — из `interface/compute/`
- server classes — из `server/engine/`
- client classes — из `client/client/`

### 2. Подготовить сетевую папку
```bash
docker compose run --rm prepare
```

Копирует `SplitTask.class` из образа в `./share/classes/client/` (по п.11 — байт-код задачи в сетевой папке).

`./share/server.policy` уже в репозитории.

### 3. Запустить сервер (rmiregistry + ComputeEngine) в фоне
```bash
docker compose up -d server
```

### 4. Запустить клиент с аргументами
```bash
docker compose run --rm client server 10 20 30 40 50
```

Первый аргумент (`server`) — имя хоста реестра (совпадает с именем сервиса в compose). Внутри контейнера `server` крутится `rmiregistry` на :1099 и `ComputeEngine`, зарегистрированный там.

Ожидаемый вывод:
```
Исходный массив: [10, 20, 30, 40, 50]
Последовательность с нечётными номерами: [10, 30, 50]
Последовательность с чётными номерами:  [20, 40]
```

На сервере (`docker compose logs server`):
```
[server] ComputeEngine зарегистрирован как 'Compute' в localhost:1099
[server] получена задача: client.SplitTask
[server] результат: int[][]{[10, 30, 50], [20, 40]}
```

### 5. Остановить
```bash
docker compose down
```

---

## Логи

**Сервер (rmiregistry + ComputeEngine в одном контейнере, видны получение задач и результаты):**
```bash
docker compose logs -f server
```

**Клиент** — выводит в stdout окна, в котором его запустили (`docker compose run`).

---

## Ключевые JVM-флаги

### Сервер
| Флаг | Зачем |
|---|---|
| `-Djava.security.manager=allow` | Java 17: разрешить `System.setSecurityManager()` |
| `-Djava.security.policy=/share/server.policy` | П.10 — policy из сетевой папки |
| `-Djava.rmi.server.useCodebaseOnly=false` | разрешить загрузку классов по codebase URL из сериализованного потока (иначе сервер не подгрузит `SplitTask` от клиента) |
| `-Djava.rmi.server.hostname=server` | в экспортируемых stub'ах подставляется это имя, чтобы клиент мог достучаться по docker-DNS |

### Клиент
| Флаг | Зачем |
|---|---|
| `-Djava.security.manager=allow` | то же |
| `-Djava.security.policy=/app/client.policy` | П.9 — policy локально |
| `-Djava.rmi.server.codebase=file:/share/classes/` | URL, откуда сервер загрузит `client.SplitTask` — указывает на сетевую папку (П.11) |

---

## Соответствие ТЗ (Вариант 2)

| Пункт ТЗ | Как выполнено |
|---|---|
| 1. Запуск примера | см. ниже **Альтернативно** |
| 2. Задача из лабы №1 как `Task<T>` | `client/SplitTask.java` реализует `Task<int[][]>` |
| 3. ≥2 разных хоста | 4 отдельных контейнера (rmiregistry, server, client, prepare) в сети `rmi-net` |
| 4. Параметры клиента через command-line | `ClientMain <registryHost> <num...>` |
| 5. Пакеты в разных каталогах | `interface/`, `server/`, `client/` |
| 6. Создание `compute.jar` | `Dockerfile` стадия 1: `jar cvf compute.jar compute/*.class` |
| 7. `compute.jar` у клиента локально | Копируется в `/app/compute.jar` образа `client` |
| 8. `compute.jar` у сервера локально | То же, в образ `server` |
| 9. `client.policy` локально | Копируется в `/app/client.policy` образа `client` |
| 10. `server.policy` в сетевой папке | `./share/server.policy`, монтируется в `server` |
| 11. `SplitTask.class` в сетевой папке | `./share/classes/client/SplitTask.class`, клиент указывает на него `java.rmi.server.codebase` |

---

## Полезные команды

```bash
# пересобрать после правок
docker compose build

# очистить сетевую папку и пересоздать
rm -rf share/classes && docker compose run --rm prepare

# запустить клиент с другим набором чисел
docker compose run --rm client server 1 2 3 4 5 6 7 8 9 10

# зайти внутрь сервера
docker compose exec server sh

# проверить что лежит в сетевой папке
ls -la share/ share/classes/client/

# полный сброс
docker compose down -v && rm -rf share/classes
```
