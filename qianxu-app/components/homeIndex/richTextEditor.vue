<template>
	<!-- 富文本 -->
	<view v-if="description" :style="[boxStyle]">
		<view class="rich_text">
			<!-- #ifdef MP || APP-PLUS -->
			<mp-html :content="description" :tag-style="tagStyle" selectable="true"  show-img-menu="true"/>  
			<!-- #endif -->
			<!-- #ifdef H5 -->
			<view v-html="description"></view>
			<!-- #endif -->
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
	import mpHtml from "@/uni_modules/mp-html/components/mp-html/mp-html.vue";
	export default {
		name: 'richText',
		props: {
			dataConfig: {
				type: Object,
				default: () => {}
			},
		},
		data(){
			return{
				tagStyle: {
					img: 'width:100%;display:block;',
					table: 'width:100%',
					video: 'width:100%'
				},
			}
		},
		components:{
			mpHtml
		},
		computed: {
			//最外层盒子的样式
			boxStyle() {
				return {
					borderRadius: this.dataConfig.bgStyle.val * 2 + 'rpx',
					background: `linear-gradient(${this.dataConfig.bgColor.color[0].item}, ${this.dataConfig.bgColor.color[1].item})`,
					margin: this.dataConfig.mbConfig.val * 2 + 'rpx' + ' ' + this.dataConfig.lrConfig.val * 2 + 'rpx' +
						' ' + 0,
				}
			},
			//富文本内容
			description() {
				return this.dataConfig.richText.val.replace(/<video/g, "<video style='width:100%'").replace(/\<img/gi, '<img style="max-width:100%;height:auto" ')
					.replace(/style="text-wrap: wrap;"/gi, '');
			}
		},
		methods: {

		}
	}
</script>

<style lang="scss" scoped>
	.rich_text {
		padding: 10px;
		width: 100%;
		overflow: hidden;
	}
</style>