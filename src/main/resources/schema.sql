-- LM-Server MySQL Schema

CREATE TABLE IF NOT EXISTS `account_mcc_history` (
  `id` BIGINT AUTO_INCREMENT,
  `account_id` BIGINT NOT NULL,
  `old_mcc_id` BIGINT,
  `new_mcc_id` BIGINT,
  `changed_by` BIGINT,
  `change_type` VARCHAR(100) NOT NULL DEFAULT 'manual',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `account_statuses` (
  `id` BIGINT AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `owner_id` BIGINT,
  `platform` VARCHAR(100) DEFAULT 'gg',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `accounts` (
  `id` BIGINT AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `account_id` VARCHAR(100) NOT NULL,
  `mcc_id` BIGINT,
  `timezone` VARCHAR(100) DEFAULT '',
  `acquired_date` DATE DEFAULT (CURRENT_DATE),
  `death_date` DATETIME,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `owner_id` BIGINT,
  `status_changed_date` DATETIME,
  `agent_id` BIGINT,
  `status_id` BIGINT,
  `deleted_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `ad_reports` (
  `id` BIGINT AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `product_name` VARCHAR(100) NOT NULL,
  `region` VARCHAR(100) NOT NULL,
  `report_date` DATETIME NOT NULL,
  `customer_id` VARCHAR(100) NOT NULL DEFAULT '',
  `campaign` VARCHAR(100) NOT NULL DEFAULT '',
  `cost` DOUBLE DEFAULT 0,
  `impressions` BIGINT DEFAULT 0,
  `clicks` BIGINT DEFAULT 0,
  `installs` BIGINT DEFAULT 0,
  `in_app_actions` DOUBLE DEFAULT 0,
  `cost_per_in_app` DOUBLE DEFAULT 0,
  `saved_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `account` VARCHAR(100) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `agents` (
  `id` BIGINT AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `owner_id` BIGINT,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `audio_replace_history` (
  `id` BIGINT AUTO_INCREMENT,
  `video_name` VARCHAR(100) NOT NULL,
  `audio_name` VARCHAR(100) NOT NULL,
  `output_name` VARCHAR(100) NOT NULL,
  `output_path` TEXT NOT NULL,
  `size_mb` DOUBLE NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `audit_log` (
  `id` BIGINT AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `action` VARCHAR(100) NOT NULL,
  `target_type` VARCHAR(100) NOT NULL,
  `target_id` BIGINT NOT NULL,
  `target_name` VARCHAR(100) DEFAULT '',
  `detail` TEXT DEFAULT ('{}'),
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `config` (
  `key` VARCHAR(100),
  `value` VARCHAR(500),
  PRIMARY KEY (`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `copywritings` (
  `id` BIGINT AUTO_INCREMENT,
  `region` VARCHAR(100) NOT NULL DEFAULT '通用',
  `content` TEXT NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `owner_id` BIGINT,
  `effectiveness` VARCHAR(100) DEFAULT '',
  `is_public` BIGINT DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `delist_checks` (
  `id` BIGINT AUTO_INCREMENT,
  `package_id` BIGINT NOT NULL,
  `product_id` BIGINT NOT NULL,
  `is_delisted` BIGINT DEFAULT 0,
  `checked_at` DATETIME,
  `error_msg` TEXT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `delist_notifications` (
  `id` BIGINT AUTO_INCREMENT,
  `package_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `first_notified` BIGINT DEFAULT 0,
  `dismissed_at` DATETIME,
  `reminder_count` BIGINT DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `fb_account_bm` (
  `id` BIGINT AUTO_INCREMENT,
  `account_id` BIGINT NOT NULL,
  `bm_id` BIGINT NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `fb_account_bm_history` (
  `id` BIGINT AUTO_INCREMENT,
  `account_id` BIGINT NOT NULL,
  `old_bm_id` BIGINT,
  `new_bm_id` BIGINT,
  `changed_by` BIGINT,
  `change_type` VARCHAR(100) NOT NULL DEFAULT 'manual',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `fb_accounts` (
  `id` BIGINT AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `account_id` VARCHAR(100) NOT NULL,
  `timezone` VARCHAR(100) DEFAULT '',
  `status_id` BIGINT,
  `acquired_date` DATE DEFAULT (CURRENT_DATE),
  `status_changed_date` DATETIME,
  `owner_id` BIGINT,
  `deleted_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `fb_ad_reports` (
  `id` BIGINT AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `product_name` VARCHAR(100) NOT NULL,
  `line_name` VARCHAR(100) DEFAULT '',
  `report_date` DATETIME NOT NULL,
  `account_name` VARCHAR(100) DEFAULT '',
  `account_id` VARCHAR(100) DEFAULT '',
  `cost` DOUBLE DEFAULT 0,
  `impressions` BIGINT DEFAULT 0,
  `clicks` BIGINT DEFAULT 0,
  `registrations` BIGINT DEFAULT 0,
  `purchases` BIGINT DEFAULT 0,
  `cost_per_purchase` DOUBLE DEFAULT 0,
  `updated_at` DATETIME DEFAULT NULL,
  `saved_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `fb_bms` (
  `id` BIGINT AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `bm_id` VARCHAR(100) NOT NULL,
  `note` TEXT,
  `status` VARCHAR(100) DEFAULT 'normal',
  `owner_id` BIGINT,
  `deleted_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `fb_lines` (
  `id` BIGINT AUTO_INCREMENT,
  `product_id` BIGINT NOT NULL,
  `line_name` VARCHAR(100) NOT NULL,
  `link` TEXT,
  `pixel_id` BIGINT,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `fb_pixel_bms` (
  `id` BIGINT AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `bm_id` VARCHAR(100) NOT NULL,
  `note` TEXT,
  `status` VARCHAR(100) DEFAULT 'normal',
  `owner_id` BIGINT,
  `deleted_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `fb_pixels` (
  `id` BIGINT AUTO_INCREMENT,
  `pixel_bm_id` BIGINT NOT NULL,
  `pixel_name` VARCHAR(100) NOT NULL,
  `pixel_id` VARCHAR(100) NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `fb_product_bms` (
  `id` BIGINT AUTO_INCREMENT,
  `product_id` BIGINT NOT NULL,
  `bm_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `fb_product_runners` (
  `product_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  PRIMARY KEY (`product_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `fb_products` (
  `id` BIGINT AUTO_INCREMENT,
  `product_name` VARCHAR(100) NOT NULL,
  `kpi` VARCHAR(100) DEFAULT '',
  `region` VARCHAR(100) DEFAULT '',
  `status` VARCHAR(100) DEFAULT 'active',
  `sales_person_id` BIGINT,
  `agency_ratio` DOUBLE DEFAULT 0,
  `owner_id` BIGINT,
  `is_archived` BIGINT DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `import_history` (
  `id` BIGINT AUTO_INCREMENT,
  `user_id` BIGINT,
  `file_name` VARCHAR(100),
  `file_type` VARCHAR(100),
  `products_count` BIGINT DEFAULT 0,
  `packages_count` BIGINT DEFAULT 0,
  `accounts_count` BIGINT DEFAULT 0,
  `mcc_count` BIGINT DEFAULT 0,
  `videos_count` BIGINT DEFAULT 0,
  `copywritings_count` BIGINT DEFAULT 0,
  `tags_count` BIGINT DEFAULT 0,
  `skipped_count` BIGINT DEFAULT 0,
  `status` VARCHAR(100) DEFAULT 'success',
  `error_msg` TEXT,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `mcc` (
  `id` BIGINT AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `mcc_id` VARCHAR(100) NOT NULL,
  `parent_mcc_id` BIGINT,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `owner_id` BIGINT,
  `shared_user_ids` VARCHAR(100) DEFAULT ('[]'),
  `level_id` BIGINT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `mcc_levels` (
  `id` BIGINT AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `owner_id` BIGINT,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `packages` (
  `id` BIGINT AUTO_INCREMENT,
  `product_id` BIGINT,
  `series_name` VARCHAR(100),
  `package_name` VARCHAR(100),
  `url` TEXT,
  `status` VARCHAR(100) DEFAULT '',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `product_assets` (
  `id` BIGINT AUTO_INCREMENT,
  `product_id` BIGINT NOT NULL,
  `video_id` VARCHAR(100) NOT NULL,
  `video_owner_id` BIGINT NOT NULL DEFAULT 1,
  `added_by` BIGINT,
  `added_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `product_runners` (
  `product_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  PRIMARY KEY (`product_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `products` (
  `id` BIGINT AUTO_INCREMENT,
  `product_name` VARCHAR(100),
  `kpi` VARCHAR(100),
  `region` VARCHAR(100),
  `status` VARCHAR(100) DEFAULT '',
  `mcc_id` BIGINT,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `owner_id` BIGINT,
  `runner_ids` VARCHAR(100) DEFAULT ('[]'),
  `is_archived` BIGINT DEFAULT 0,
  `customer` VARCHAR(100) DEFAULT '',
  `deleted_at` DATETIME,
  `agency_ratio` DOUBLE DEFAULT NULL,
  `sales_person_id` BIGINT,
  `sales_person` VARCHAR(100) DEFAULT '',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `recharge_records` (
  `id` BIGINT AUTO_INCREMENT,
  `account_id` VARCHAR(100) NOT NULL,
  `amount` VARCHAR(100) NOT NULL,
  `operator` VARCHAR(100) DEFAULT '',
  `created_by` BIGINT,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `status` VARCHAR(100) DEFAULT '',
  `sheets_synced` BIGINT DEFAULT 0,
  `sheets_error` VARCHAR(100) DEFAULT '',
  `agent_id` BIGINT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `regions` (
  `id` BIGINT AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `timezone` VARCHAR(100) DEFAULT '',
  `platform` VARCHAR(100) DEFAULT 'gg',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `sales_persons` (
  `id` BIGINT AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `owner_id` BIGINT,
  `platform` VARCHAR(100) DEFAULT 'gg',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `scrape_cache` (
  `package_name` VARCHAR(100),
  `image_count` BIGINT DEFAULT 0,
  `saved_path` TEXT,
  `logo_path` TEXT,
  `last_scraped` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `scraped_by` BIGINT,
  PRIMARY KEY (`package_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `sheets_sync_log` (
  `id` BIGINT AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `product_name` VARCHAR(100) NOT NULL DEFAULT '',
  `spreadsheet_id` VARCHAR(100) NOT NULL DEFAULT '',
  `sheet_gid` VARCHAR(100) NOT NULL DEFAULT '',
  `status` VARCHAR(100) NOT NULL DEFAULT 'failed',
  `error_msg` TEXT,
  `rows_json` TEXT,
  `retry_count` BIGINT DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `tags` (
  `key` VARCHAR(100),
  `value` VARCHAR(108),
  PRIMARY KEY (`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `users` (
  `id` BIGINT AUTO_INCREMENT,
  `username` VARCHAR(100) NOT NULL,
  `password` VARCHAR(324) NOT NULL,
  `role` VARCHAR(100) NOT NULL DEFAULT 'user',
  `display_name` VARCHAR(100) DEFAULT '',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_login` DATETIME,
  `created_by` BIGINT,
  `config` VARCHAR(100) DEFAULT ('{}'),
  `custom_name` VARCHAR(100) DEFAULT '',
  `email` VARCHAR(100) DEFAULT '',
  `telegram_username` VARCHAR(100) DEFAULT '',
  `platform` VARCHAR(100) DEFAULT 'gg',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `video_consumption` (
  `id` BIGINT AUTO_INCREMENT,
  `video_id` VARCHAR(100) NOT NULL,
  `video_owner_id` BIGINT NOT NULL DEFAULT 1,
  `user_id` BIGINT,
  `product_id` BIGINT,
  `amount` DOUBLE NOT NULL,
  `consume_date` DATETIME,
  `created_at` DATETIME,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `video_history` (
  `id` BIGINT AUTO_INCREMENT,
  `package` VARCHAR(100) NOT NULL,
  `name` VARCHAR(100) DEFAULT '',
  `settings` TEXT NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `video_tasks` (
  `id` BIGINT AUTO_INCREMENT,
  `task_id` VARCHAR(100) NOT NULL,
  `package` VARCHAR(176) DEFAULT '',
  `status` VARCHAR(100) DEFAULT 'pending',
  `progress` DOUBLE DEFAULT 0,
  `message` TEXT,
  `output_path` TEXT,
  `settings` TEXT DEFAULT ('{}'),
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `finished_at` DATETIME,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `videos` (
  `id` VARCHAR(100) NOT NULL,
  `owner_id` BIGINT NOT NULL DEFAULT 1,
  `url` TEXT,
  `title` VARCHAR(200),
  `region` VARCHAR(100) DEFAULT '通用',
  `frame_type` VARCHAR(100) DEFAULT '非融帧',
  `effectiveness` VARCHAR(100) DEFAULT '',
  `product_name` VARCHAR(100) DEFAULT '',
  `review_status` VARCHAR(100) DEFAULT '能过审',
  `is_public` BIGINT DEFAULT 0,
  `imported_at` DATETIME,
  `channel_name` VARCHAR(100) DEFAULT '',
  PRIMARY KEY (`id`, `owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;