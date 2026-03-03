INSERT INTO role_module_access (role_name, module_key, enabled)
VALUES
  ('REGISTRADOR', 'DASHBOARD', FALSE)
ON CONFLICT (role_name, module_key) DO NOTHING;
