insert into url
    (name, created_at)
values
    ('https://test.net', '2023-01-01 00:00:00');
insert into url_check
    (status_code, title, h1, description, url_id, created_at)
values
    (200, 'title', 'h1', 'description', 1, '2022-01-01 12:00:00');