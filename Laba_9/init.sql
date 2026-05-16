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

INSERT INTO RESPONSIBLES (ID, FIO, POSITION, PHONE, AGE) VALUES
    (1, 'Иванов Иван Иванович', 'Доцент', '123456', 45),
    (2, 'Петров Пётр Петрович', 'Профессор', '234567', 55),
    (3, 'Сидорова Анна Сергеевна', 'Старший преподаватель', '345678', 38),
    (4, 'Алексеев Алексей Алексеевич', 'Ассистент', '456789', 28);

INSERT INTO CLASSROOMS (ID, BUILDING, ROOM_NUMBER, NAME, AREA, RESPONSIBLE_ID) VALUES
    (1, 'Главный корпус', '101', 'Лекционная', 80.5, 1),
    (2, 'Главный корпус', '102', 'Семинарская', 35.0, 1),
    (3, 'Главный корпус', '201', 'Компьютерный класс', 60.0, 2),
    (4, 'Корпус Б', '305', 'Лаборатория', 50.0, 2),
    (5, 'Корпус Б', '306', 'Лаборатория', 50.0, 2),
    (6, 'Корпус В', '101', 'Аудитория', 40.0, 3),
    (7, 'Корпус В', '102', 'Аудитория', 40.0, 4);
