-- 仅显式启用 DEMO_DATA_ENABLED 时由应用执行；相同标题存在时跳过，不覆盖编辑后的内容。
INSERT INTO charity_project (title, description, target_amount, status)
SELECT '乡村阅读角', '演示项目：为乡村儿童建设阅读角。本系统仅进行模拟捐款。', 10000.00, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM charity_project WHERE title = '乡村阅读角');

INSERT INTO charity_project (title, description, target_amount, status)
SELECT '社区暖心关怀', '演示草稿：为社区困难家庭提供生活物资，待管理员发布。', 5000.00, 'DRAFT'
WHERE NOT EXISTS (SELECT 1 FROM charity_project WHERE title = '社区暖心关怀');
