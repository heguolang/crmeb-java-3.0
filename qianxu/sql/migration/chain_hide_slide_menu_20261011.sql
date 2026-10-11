-- 滑落日志菜单先隐藏（滑落逻辑仍启用，需要时可直连 /chain/slide 或改回 is_show=1）
SET NAMES utf8mb4;

UPDATE `eb_system_menu`
   SET `is_show` = 0, `update_time` = NOW()
 WHERE `component` = '/chain/slide' AND `is_delte` = 0;

UPDATE `eb_system_menu`
   SET `is_show` = 0, `update_time` = NOW()
 WHERE `component` LIKE '/distribution/chainSlide%' AND `is_delte` = 0;
