SET @column_exists := (
    SELECT COUNT(*)
      FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME = 'members'
       AND COLUMN_NAME = 'password_hash'
);

SET @ddl := IF(
    @column_exists = 0,
    'ALTER TABLE members ADD COLUMN password_hash VARCHAR(255) NULL AFTER provider_user_id',
    'SELECT ''members.password_hash already exists'' AS message'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
