DELETE FROM direct_messages CASCADE;
DELETE FROM server_messages CASCADE;
DELETE FROM server_users CASCADE;
DELETE FROM servers CASCADE;
DELETE FROM users CASCADE;
DELETE FROM password_reset_questions CASCADE;
DELETE FROM statuses CASCADE;
DELETE FROM roles CASCADE;

INSERT INTO roles (id, name)
    VALUES ('ADMIN', 'admin'), ('MODERATOR', 'moderator'), ('MEMBER', 'member');

INSERT INTO statuses (id, name)
    VALUES ('OFFLINE', 'offline'), ('ONLINE', 'online'), ('BUSY', 'busy');

INSERT INTO password_reset_questions (id, question) VALUES (1, 'Example question');

INSERT INTO users (id, username, password, salt, status, password_reset_question, password_reset_answer)
    VALUES (1, 'admin', ?, ?, 'OFFLINE', 1, 'Hello');
INSERT INTO users (id, username, password, salt, status, password_reset_question, password_reset_answer)
    VALUES (2, 'moderator', ?, ?, 'OFFLINE', 1, 'Hello');
INSERT INTO users (id, username, password, salt, status, password_reset_question, password_reset_answer)
    VALUES (3, 'member', ?, ?, 'OFFLINE', 1, 'Hello');
INSERT INTO users (id, username, password, salt, status, password_reset_question, password_reset_answer)
    VALUES (4, 'extramember', ?, ?, 'OFFLINE', 1, 'Hello');

INSERT INTO servers (id, name, address) VALUES (1, 'Example Server', 'exampleserver');
INSERT INTO servers (id, name, address) VALUES (2, 'Second Server', 'server2');

INSERT INTO server_users ("user", server, role) VALUES (1, 1, 'ADMIN');
INSERT INTO server_users ("user", server, role) VALUES (2, 1, 'MODERATOR');
INSERT INTO server_users ("user", server, role) VALUES (3, 1, 'MEMBER');

INSERT INTO server_users ("user", server, role) VALUES (1, 2, 'ADMIN');
INSERT INTO server_users ("user", server, role) VALUES (3, 2, 'MEMBER');

INSERT INTO server_messages (id, "content", "server", "sender", "createdAt")
    VALUES (1, 'Hello1', 1, 1, '2020-09-27 11:28:00');
INSERT INTO server_messages (id, "content", "server", "sender", "createdAt")
    VALUES (2, 'Hello2', 1, 2, '2020-09-27 11:28:00');
INSERT INTO server_messages (id, "content", "server", "sender", "createdAt")
    VALUES (3, 'Hello3', 1, 3, '2020-09-27 11:28:00');

INSERT INTO direct_messages (id, "content", "recipient", "sender", "createdAt")
    VALUES (1, 'Hello1', 2, 1, '2020-09-27 11:28:00');
INSERT INTO direct_messages (id, "content", "recipient", "sender", "createdAt")
    VALUES (2, 'Hello2', 1, 2, '2020-09-27 11:29:00');
INSERT INTO direct_messages (id, "content", "recipient", "sender", "createdAt")
    VALUES (3, 'Hello3', 2, 1, '2020-09-27 11:31:00');
