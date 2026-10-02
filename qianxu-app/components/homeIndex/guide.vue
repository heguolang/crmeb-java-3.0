<template>
	<!-- 辅助线 -->
	<view>
		<view class="lines" :style="[boxStyle]">
			<view class="item" :style="[lineStyle]"></view>
		</view>
	</view>

</template>

<script>
	// +----------------------------------------------------------------------
	// | 黔序商城 [ 黔序科技，助力企业发展 ]
	// +----------------------------------------------------------------------
	// | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
	// +----------------------------------------------------------------------
	// | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
	// +----------------------------------------------------------------------
	// | Author: 贵州黔序科技有限公司
	// +----------------------------------------------------------------------
	export default {
		name: 'guide',
		props: {
			dataConfig: {
				type: Object,
				default: () => {}
			},
		},
		computed: {
			//最外层盒子的样式
			boxStyle() {
				return {
					background: `linear-gradient(${this.dataConfig.bgColor.color[0].item}, ${this.dataConfig.bgColor.color[1].item})`,
					margin: this.dataConfig.mbConfig.val * 2 + 'rpx' + ' ' + this.dataConfig.lrConfig.val * 2 + 'rpx' +
						' ' + 0,
				}
			},
			//线条样式
			lineStyle() {
				return {
					borderBottomWidth: 2*this.dataConfig.heightConfig.val + 'rpx',
					borderBottomColor: this.dataConfig.lineColor.color[0].item,
					borderBottomStyle: this.dataConfig.lineStyle.list[this.dataConfig.lineStyle.tabVal].style
				}
			}
		},
	}
</script>

<style lang="scss" scoped>
	.lines {
		padding: 0 20rpx;

		.item {
			width: 100%;
			box-sizing: border-box;
			border-bottom-color: #666;
			border-bottom-width: 1px;
			border-bottom-style: dotted;
		}
	}
</style>