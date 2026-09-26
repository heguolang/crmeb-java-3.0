<template>
	<!-- 辅助空白-->
	<view>
		<view :style="[boxStyle]"></view>
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
		name: 'blankPage',
		props: {
			dataConfig: {
				type: Object,
				default: () => {}
			}
		},
		computed: {
			//最外层盒子的样式
			boxStyle() {
				return {
					height: this.dataConfig.heightConfig.val + 'px',
					background: `linear-gradient(${this.dataConfig.bgColor.color[0].item}, ${this.dataConfig.bgColor.color[1].item})`,
					margin: this.dataConfig.mbConfig.val * 2 + 'rpx' + ' ' + this.dataConfig.lrConfig.val * 2 + 'rpx' +
						' ' + 0,
				}
			},
		},
	}
</script>

<style lang="scss" scoped>
	.blankPage {
		.bankCon {
			width: 100%;
		}
	}
</style>