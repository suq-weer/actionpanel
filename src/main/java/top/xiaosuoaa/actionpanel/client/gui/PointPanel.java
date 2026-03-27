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

package top.xiaosuoaa.actionpanel.client.gui;

import com.mojang.logging.LogUtils;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import org.joml.Vector2d;
import org.slf4j.Logger;
import top.xiaosuoaa.actionpanel.ActionPanelClient;
import top.xiaosuoaa.actionpanel.client.point.PointClient;
import top.xiaosuoaa.actionpanel.client.util.PointPanelStatus;
import top.xiaosuoaa.actionpanel.client.util.gui.GUIAnimationUtil;
import top.xiaosuoaa.actionpanel.client.util.gui.GUIPosition;
import top.xiaosuoaa.actionpanel.client.util.gui.GUIStyleUtil;
import top.xiaosuoaa.actionpanel.util.ResUtil;

import java.util.List;
import java.util.Objects;

import static top.xiaosuoaa.actionpanel.ActionPanelClient.pointDataPointClientHashMap;
import static top.xiaosuoaa.actionpanel.ActionPanelClient.signalDisplayer;
import static top.xiaosuoaa.actionpanel.register.PointRegister.POINT_REGISTRY;

public class PointPanel {
	public static final int BlEND_BLACK = 0xa0000000;
	// 边距
	final static int PADDING = 5;
	// 边长
	final static int LENGTH = 50;
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Minecraft MINECRAFT = Minecraft.getInstance();
	private static final List<ResourceLocation> pointDataList = List.of(
			// TEST
			ResUtil.getRes("default_point"),
			ResUtil.getRes("enemy_point"),
			ResUtil.getRes("move_point"),
			ResUtil.getRes("attack_point"),
			ResUtil.getRes("danger_point"),
			ResUtil.getRes("item_point"),
			ResUtil.getRes("assemble_point"),
			ResUtil.getRes("machine_point")
	);
	// - 动画总时长
	private final static long END_MILL_TIME = 260;
	// 动画控制变量
	private static double accumulatedTime = 0;
	private static double lastFrameTime = 0;
	// 当前扇区
	int sector = 0;
	// 中心点（gui缩放）
	int centerX;
	int centerY;
	// 中心点
	int centerOriginalX;
	int centerOriginalY;
	double mouseX;
	double mouseY;

	public PointPanel(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		// 计算中心点
		calculateCenter();
		if (PointPanelStatus.is(ActionPanelClient.getPointPanelStatus(), PointPanelStatus.ENABLED)) {
			calculateMouse(guiGraphics);
			renderStyles(guiGraphics, deltaTracker);
		} else resetAnimation();
	}

	public static void releaseKey(int sector) {
		// sector 为 0 时不触发任何信号（取消操作）
		if (sector > 0 && sector <= pointDataList.size()) {
			signalDisplayer.sendSignal(POINT_REGISTRY.get(pointDataList.get(sector - 1)));
		}
	}

	private void calculateCenter() {
		centerX = MINECRAFT.getWindow().getGuiScaledWidth() / 2;
		centerY = MINECRAFT.getWindow().getGuiScaledHeight() / 2;
		centerOriginalX = MINECRAFT.getWindow().getWidth() / 2;
		centerOriginalY = MINECRAFT.getWindow().getHeight() / 2;
	}

	private void calculateMouse(GuiGraphics guiGraphics) {
		// 抓取鼠标位置
		mouseX = MINECRAFT.mouseHandler.xpos();
		mouseY = MINECRAFT.mouseHandler.ypos();
		// 制作原始中心点为零点的鼠标向量
		Vector2d mouseVector = new Vector2d(mouseX - centerOriginalX, mouseY - centerOriginalY);

		// 如果鼠标向量为零，则不进行方向判断
		if (mouseVector.length() == 0) {
			sector = 0;
			// 同步更新全局状态
			ActionPanelClient.setCurrentSector(0);
			return;
		}
		// 如果鼠标与原始中心点距离小于 40，将 sector 设置为 0（取消区域）
		double distance = Math.sqrt(Math.pow(mouseX - centerOriginalX, 2) + Math.pow(mouseY - centerOriginalY, 2));
		if (distance < 40) {
			sector = 0;
			// 同步更新全局状态
			ActionPanelClient.setCurrentSector(0);
			return;
		}


		// 计算角度（弧度）
		double angle = Math.atan2(mouseVector.y, mouseVector.x);

		// 转换为 0-2π 范围
		if (angle < 0) {
			angle += 2 * Math.PI;
		}

		// 转换为角度并加上90度偏移（因为游戏0度是竖直向下）
		double degrees = Math.toDegrees(angle);
		if (degrees >= 360) {
			degrees -= 360;
		}
		// 扇区分布: 1(左上), 2(上), 3(右上), 4(左), 5(右), 6(左下), 7(下), 8(右下)
		// 基于角度区间划分扇区（适配游戏坐标系）
		if (degrees >= 337.5 || degrees < 25.5) {
			sector = 5; // 右 (角度: 337.5° ~ 25.5°)
		} else if (degrees >= 25.5 && degrees < 67.5) {
			sector = 8; // 右下 (角度: 25.5° ~ 67.5°)
		} else if (degrees >= 67.5 && degrees < 115.5) {
			sector = 7; // 下 (角度: 67.5° ~ 115.5°)
		} else if (degrees >= 115.5 && degrees < 157.5) {
			sector = 6; // 左下 (角度: 115.5° ~ 157.5°)
		} else if (degrees >= 157.5 && degrees < 205.5) {
			sector = 4; // 左 (角度: 157.5° ~ 205.5°)
		} else if (degrees >= 205.5 && degrees < 247.5) {
			sector = 1; // 左上 (角度: 205.5° ~ 247.5°)
		} else if (degrees >= 247.5 && degrees < 292.5) {
			sector = 2; // 上 (角度: 247.5° ~ 292.5°)
		} else if (degrees >= 292.5) {
			sector = 3; // 右上 (角度：292.5° ~ 337.5°)
		}

		// 更新全局 sector 状态
		ActionPanelClient.setCurrentSector(sector);
	}

	private void renderStyles(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		GUIAnimationUtil.AnimationTime time = GUIAnimationUtil.calculateAnimationTime(lastFrameTime, END_MILL_TIME, accumulatedTime);
		accumulatedTime = time.accumulatedTime();
		lastFrameTime = time.lastFrameTime();

		int centerBlockX = centerX - (LENGTH / 2);
		int centerBlockY = centerY - (LENGTH / 2);

		// Point1
		int block1X = centerX - (LENGTH / 2) - LENGTH - PADDING;
		int block1Y = centerY - (LENGTH / 2) - LENGTH - PADDING;
		GUIAnimationUtil.transitionEaseInOutQuad(pos -> renderPointBlock(pointDataList.getFirst(), guiGraphics, pos, 1),
				centerBlockX, centerBlockY, // start
				block1X, block1Y, // end
				(int) END_MILL_TIME, (int) accumulatedTime // time
		);

		// Point2
		int block2X = centerX - (LENGTH / 2);
		int block2Y = centerY - (LENGTH / 2) - LENGTH - PADDING;
		GUIAnimationUtil.transitionEaseInOutQuad(pos -> renderPointBlock(pointDataList.get(1), guiGraphics, pos, 2),
				centerBlockX, centerBlockY,
				block2X, block2Y,
				(int) END_MILL_TIME, (int) accumulatedTime
		);

		// Point3
		int block3X = centerX + (LENGTH / 2) + PADDING;
		int block3Y = centerY - (LENGTH / 2) - LENGTH - PADDING;
		GUIAnimationUtil.transitionEaseInOutQuad(pos -> renderPointBlock(pointDataList.get(2), guiGraphics, pos, 3),
				centerBlockX, centerBlockY,
				block3X, block3Y,
				(int) END_MILL_TIME, (int) accumulatedTime
		);

		// Point4
		int block4X = centerX - (LENGTH / 2) - LENGTH - PADDING;
		int block4Y = centerY - (LENGTH / 2);
		GUIAnimationUtil.transitionEaseInOutQuad(pos -> renderPointBlock(pointDataList.get(3), guiGraphics, pos, 4),
				centerBlockX, centerBlockY,
				block4X, block4Y,
				(int) END_MILL_TIME, (int) accumulatedTime
		);

		// Point5
		int block5X = centerX + (LENGTH / 2) + PADDING;
		int block5Y = centerY - (LENGTH / 2);
		GUIAnimationUtil.transitionEaseInOutQuad(pos -> renderPointBlock(pointDataList.get(4), guiGraphics, pos, 5),
				centerBlockX, centerBlockY,
				block5X, block5Y,
				(int) END_MILL_TIME, (int) accumulatedTime
		);

		// Point6
		int block6X = centerX - (LENGTH / 2) - LENGTH - PADDING;
		int block6Y = centerY + (LENGTH / 2) + PADDING;
		GUIAnimationUtil.transitionEaseInOutQuad(pos -> renderPointBlock(pointDataList.get(5), guiGraphics, pos, 6),
				centerBlockX, centerBlockY,
				block6X, block6Y,
				(int) END_MILL_TIME, (int) accumulatedTime
		);

		// Point7
		int block7X = centerX - (LENGTH / 2);
		int block7Y = centerY + (LENGTH / 2) + PADDING;
		GUIAnimationUtil.transitionEaseInOutQuad(pos -> renderPointBlock(pointDataList.get(6), guiGraphics, pos, 7),
				centerBlockX, centerBlockY,
				block7X, block7Y,
				(int) END_MILL_TIME, (int) accumulatedTime
		);

		// Point8
		int block8X = centerX + (LENGTH / 2) + PADDING;
		int block8Y = centerY + (LENGTH / 2) + PADDING;
		GUIAnimationUtil.transitionEaseInOutQuad(pos -> renderPointBlock(pointDataList.get(7), guiGraphics, pos, 8),
				centerBlockX, centerBlockY,
				block8X, block8Y,
				(int) END_MILL_TIME, (int) accumulatedTime
		);

		//Point0
		renderCancelBlock(guiGraphics, new GUIPosition(centerBlockX, centerBlockY));
	}

	private void renderRenderedPoint(ResourceLocation pointData, GuiGraphics guiGraphics, GUIPosition pos, int number) {
		PointClient point = pointDataPointClientHashMap.get(Objects.requireNonNull(POINT_REGISTRY.get(pointData)).getResourceLocation());
		guiGraphics.blit(
				point.getTintedIcon(),
				pos.x() + 2 * PADDING, pos.y() + PADDING,
				LENGTH - 4 * PADDING, LENGTH - 4 * PADDING,
				0, 0,
				64, 64,
				64, 64
		);
		if (point.getIcon() != null) {
			guiGraphics.blit(
					point.getIcon(),
					pos.x() + 2 * PADDING, pos.y() + PADDING,
					LENGTH - 4 * PADDING, LENGTH - 4 * PADDING,
					0, 0,
					64, 64,
					64, 64
			);
		}
		renderText(point.getPointData().displayName(), guiGraphics, pos, number);
	}

	private void renderPointBlock(ResourceLocation pointData, GuiGraphics guiGraphics, GUIPosition pos, int number) {
		backGroundBlock(guiGraphics, pos, number);
		renderRenderedPoint(pointData, guiGraphics, pos, number);
	}

	private void renderText(Component name, GuiGraphics guiGraphics, GUIPosition pos, int number) {
		var font = MINECRAFT.font;
		int width = font.width(name);
		boolean isSector = sector == number;
		guiGraphics.drawString(
				font,
				name,
				pos.x() + LENGTH / 2 - width / 2,
				pos.y() + PADDING + LENGTH / 2 + font.lineHeight / 2 + 2,
				isSector ? 0x000000 : 0xffffff,
				!isSector
		);
	}

	private void renderCancelBlock(GuiGraphics guiGraphics, GUIPosition pos) {
		backGroundBlock(guiGraphics, pos, 0);
		renderText(Component.translatable("actionpanel.cancel"), guiGraphics, pos, 0);
	}

	private void backGroundBlock(GuiGraphics guiGraphics, GUIPosition pos, int number) {
		int color = sector == number ? CommonColors.WHITE : BlEND_BLACK;
		GUIStyleUtil.displayBlock(guiGraphics, color, pos.x(), pos.y(), LENGTH);
	}

	private void resetAnimation() {
		accumulatedTime = 0;
		lastFrameTime = 0;
		sector = 0;
		// 同步更新全局状态
		ActionPanelClient.setCurrentSector(0);
	}
}