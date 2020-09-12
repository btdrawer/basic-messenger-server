DELETE FROM messages CASCADE;
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

INSERT INTO users (id, username, password, status, password_reset_question, password_reset_answer)
    VALUES (1, 'ben', 'password', 'OFFLINE', 1, 'Hello');
