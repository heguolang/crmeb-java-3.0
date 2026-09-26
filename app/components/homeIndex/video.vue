<template>
	<view>
		<view class="diy_video acea-row row-center-wrapper" :style="[boxStyle]">
			<video :style="[contantRadius]" :src="link" :show-mute-btn="pageGesture" :poster="cover" controls :autoplay="false" loop
				objectFit="cover"></video>
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
		name: 'pictureCube',
		props: {
			dataConfig: {
				type: Object, 
				default: () => {}
			},
		},
		data() {
			return {
				pageGesture: true,
				onloadCode: ''
			};
		},
		created() {
			// #ifdef APP
			this.onloadCode =
				`this.contentWindow.document.body.innerHTML = '<video style="width: 100%;height: 100%" objectFit="cover" controls="controls"  loop show-mute-btn="${this.pageGesture}" poster="${this.cover}" src="${this.link}"></video>';`
			// #endif
		},
		computed: {
			//视频封面
			cover() {
				return this.dataConfig.cover.url
			},
			//视频地址
			link() {
				if (this.dataConfig.tabConfig.tabVal === 0) {
					return this.dataConfig.uploadVideo.url
				} else {
					return this.dataConfig.link.val
				}

			},
			//最外层盒子的样式
			boxStyle() {
				return {
					borderRadius: this.dataConfig.bgStyle.val * 2 + 'rpx',
					background: `linear-gradient(${this.dataConfig.bgColor.color[0].item}, ${this.dataConfig.bgColor.color[1].item})`,
					margin: this.dataConfig.mbConfig.val * 2 + 'rpx' + ' ' + this.dataConfig.lrConfig.val * 2 + 'rpx' +
						' ' + 0,
					padding: this.dataConfig.upConfig.val * 2 + 'rpx' + ' ' + 0 + ' ' + this.dataConfig.downConfig.val *
						2 + 'rpx'
				}
			},
			contantRadius() {
			      return { 'border-radius': this.dataConfig.contantStyle.val ? this.dataConfig.contantStyle.val + 'px' : '0' };
			    },
		}
	}
</script>
<style lang="scss" scoped>
	.diy_video {
		iframe {
			border: none;

			body {
				margin: 0;
			}
		}

		video {
			width: 100%;
			height: 340rpx;
			border-radius: 14rpx;
		}
	}
</style>