/*
 * Copyright (c) 2026 Xiaosu
 *
 * 本程序是自由软件：你可以在 GNU Lesser General Public License v3.0
 * 的条款下重新发布和/或修改它，并附加以下限制：
 *
 * 1. 禁止商业用途：不得将本模组或其修改版本用于任何商业目的，
 *    包括但不限于付费服务器、付费整合包、周边商品销售等。
 * 2. 允许个人学习、研究及非盈利社区服务器使用。
 * 3. 修改版本必须保留本版权声明及原始许可证。
 *
 * 本程序按"原样"提供，不提供任何担保。详见 LICENSE 文件。
 */

package top.xiaosuoaa.actionpanel.client.util.gui;

import java.util.function.Consumer;

public class GUIAnimationUtil {
	/**
	 * 执行坐标过渡动画，根据时间进度计算当前坐标位置并传递给消费者函数
	 *
	 * @param consumer    用于接收计算出的过渡坐标
	 * @param startX      起始X坐标
	 * @param startY      起始Y坐标
	 * @param endX        结束X坐标
	 * @param endY        结束Y坐标
	 * @param endMillTime 过渡动画总耗时（毫秒）
	 * @param nowMillTime 帧速率查询
	 */
	public static void transitionEaseInOutQuad(Consumer<GUIPosition> consumer, int startX, int startY, int endX, int endY, int endMillTime, int nowMillTime) {
		// 如果当前时间已经超过或等于总耗时，直接使用结束坐标
		if (nowMillTime >= endMillTime) {
			consumer.accept(new GUIPosition(endX, endY));
			return;
		}

		// 根据时间进度计算当前坐标位置
		double progress = EasingFunctions.easeInOutQuad((double) nowMillTime / endMillTime);
		int currentX = (int) (startX + (endX - startX) * progress);
		int currentY = (int) (startY + (endY - startY) * progress);

		consumer.accept(new GUIPosition(currentX, currentY));
	}

	/**
	 * 动画累计时间计算（lastFrameTime以秒为单位）
	 *
	 * @param lastFrameTime   上一帧时间（秒）
	 * @param endMillTime     动画总时长（毫秒）
	 * @param accumulatedTime 累计时间（多次调用请把上一次调用的返回值传递过来）
	 * @return 返回的新累计时间
	 */
	public static AnimationTime calculateAnimationTime(double lastFrameTime, double endMillTime, double accumulatedTime) {
		// 将 lastFrameTime 从秒转换为毫秒并更新累计时间
		long currentFrameTime = System.currentTimeMillis();
		if (lastFrameTime > 0) {
			accumulatedTime += (currentFrameTime - lastFrameTime);
		}

		// 限制累计时间不超过动画时长
		if (accumulatedTime > endMillTime) {
			accumulatedTime = endMillTime;
		}

		return new AnimationTime(currentFrameTime, accumulatedTime);
	}

	public record AnimationTime(double lastFrameTime, double accumulatedTime) {
	}

	public static class EasingFunctions {
		// 线性缓动
		public static double linear(double t) {
			return t;
		}

		// 二次缓入
		public static double easeInQuad(double t) {
			return t * t;
		}

		// 二次缓出
		public static double easeOutQuad(double t) {
			return 1 - (1 - t) * (1 - t);
		}

		// 二次缓入缓出
		public static double easeInOutQuad(double t) {
			return t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2;
		}
	}

}
