INSERT INTO mortise.mortise_system_config (id, config_group, config_key, config_value)
VALUES (1000000000000416, 'footer', 'footer.columns', null)
ON CONFLICT (config_key) DO NOTHING;
