-- Run only after sql/01-schema.sql on an empty test database.
INSERT INTO users (id, full_name, contact_details, version) VALUES
    (1, 'Анна Иванова', 'anna@example.com', 0),
    (2, 'Иван Петров', 'ivan@example.com', 0);

INSERT INTO found_items (id, description, category, location, status, version) VALUES
    (1, 'Чёрный зонт', 'Аксессуары', 'Аудитория 101', 'AVAILABLE', 0),
    (2, 'Серый шарф', 'Аксессуары', 'Библиотека', 'AVAILABLE', 0),
    (3, 'Смартфон в синем чехле', 'Электроника', 'Столовая', 'AVAILABLE', 0);

INSERT INTO loss_reports (id, applicant_id, description, category, status, version) VALUES
    (1, 1, 'Потерян чёрный зонт', 'Аксессуары', 'OPEN', 0),
    (2, 2, 'Потерян серый шарф', 'Аксессуары', 'OPEN', 0),
    (3, 1, 'Потерян смартфон', 'Электроника', 'OPEN', 0);

INSERT INTO item_claims (id, loss_report_id, found_item_id, evidence, status, version) VALUES
    (1, 1, 1, 'На ручке есть царапина', 'PENDING', 0),
    (2, 2, 2, 'Шарф с полосатым узором', 'PENDING', 0),
    (3, 3, 3, 'Синий чехол и трещина на углу', 'PENDING', 0);

SELECT setval(pg_get_serial_sequence('users', 'id'), COALESCE((SELECT MAX(id) FROM users), 1), true);
SELECT setval(pg_get_serial_sequence('found_items', 'id'), COALESCE((SELECT MAX(id) FROM found_items), 1), true);
SELECT setval(pg_get_serial_sequence('loss_reports', 'id'), COALESCE((SELECT MAX(id) FROM loss_reports), 1), true);
SELECT setval(pg_get_serial_sequence('item_claims', 'id'), COALESCE((SELECT MAX(id) FROM item_claims), 1), true);
