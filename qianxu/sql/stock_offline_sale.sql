-- ============================================================
-- Stock module: offline sale (线下销售) records
-- Idempotent: safe to run repeatedly
-- ============================================================
CREATE TABLE IF NOT EXISTS eb_stock_offline_sale (
  id int(11) NOT NULL AUTO_INCREMENT,
  uid int(11) NOT NULL DEFAULT 0,
  agent_id int(11) NOT NULL DEFAULT 0,
  product_id int(11) NOT NULL DEFAULT 0,
  sku_key varchar(120) NOT NULL DEFAULT '',
  product_name varchar(255) NOT NULL DEFAULT '',
  image varchar(512) NOT NULL DEFAULT '',
  num int(11) NOT NULL DEFAULT 0,
  mark varchar(255) NOT NULL DEFAULT '' COMMENT '备注',
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_del tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_uid (uid),
  KEY idx_agent_product (agent_id, product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
