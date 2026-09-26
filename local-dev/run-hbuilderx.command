#!/bin/bash
# 一键：在 HBuilderX 中运行会员端（uni-app H5，内置浏览器）
# 用法：双击本文件，或在终端执行  ./run-hbuilderx.command
# 说明：macOS 首次运行会弹「辅助功能」授权，允许即可（系统设置 → 隐私与安全性 → 辅助功能）

PROJECT_DIR="/Users/qianxu/WorkBuddy/java/qianxu-java-3.0/app"

echo "==> 激活 HBuilderX 并打开项目"
open -a HBuilderX "$PROJECT_DIR"
sleep 3

echo "==> 点击菜单：运行 → 运行到浏览器 → 运行到内置浏览器"
osascript <<'EOF' 2>&1
tell application "HBuilderX" to activate
delay 2

tell application "System Events"
	tell process "HBuilderX"
		-- 1) 展开「运行」菜单
		click menu bar item "运行" of menu bar 1
		delay 0.8

		-- 2) 展开「运行到浏览器」子菜单（兼容带快捷键标记的标题）
		set runMenu to menu 1 of menu bar item "运行" of menu bar 1
		set browserItem to missing value
		repeat with mi in (every menu item of runMenu)
			if name of mi starts with "运行到浏览器" then
				set browserItem to mi
				exit repeat
			end if
		end repeat
		if browserItem is missing value then
			set names to {}
			repeat with mi in (every menu item of runMenu)
				set end of names to name of mi
			end repeat
			error "未找到『运行到浏览器』子菜单，实际菜单项：" & (names as string)
		end if
		click browserItem
		delay 1

		-- 3) 点击「运行到内置浏览器」
		set subMenu to menu 1 of browserItem
		set target to missing value
		repeat with mi in (every menu item of subMenu)
			if name of mi starts with "运行到内置浏览器" then
				set target to mi
				exit repeat
			end if
		end repeat
		if target is missing value then
			set names to {}
			repeat with mi in (every menu item of subMenu)
				set end of names to name of mi
			end repeat
			error "未找到『运行到内置浏览器』，实际菜单项：" & (names as string)
		end if
		click target
	end tell
end tell

return "已触发运行，HBuilderX 正在编译（首次约 1 分钟），完成后会弹出内置浏览器窗口"
EOF

echo ""
echo "如果上面报『权限违例 -10004』，请：系统设置 → 隐私与安全性 → 辅助功能 → 把 终端/Terminal 打开，然后重新双击本文件。"
