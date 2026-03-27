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

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import top.xiaosuoaa.actionpanel.ActionPanelClient;
import top.xiaosuoaa.actionpanel.client.point.PointClient;
import top.xiaosuoaa.actionpanel.client.util.gui.GUIPosition;
import top.xiaosuoaa.actionpanel.config.MainConfig;
import top.xiaosuoaa.actionpanel.point.PointData;
import top.xiaosuoaa.actionpanel.point.Signal;

import java.util.ArrayList;

import static top.xiaosuoaa.actionpanel.ActionPanelClient.signalDisplayer;

public class PointSeek {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Minecraft MINECRAFT = Minecraft.getInstance();

	public PointSeek(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		commonRender(guiGraphics);
	}

	/**
	 * 渲染信号指示器
	 * 获取每个signal的世界坐标并检测维度，如果与玩家维度相同就从世界坐标变换到玩家摄像机的屏幕坐标
	 */
	private void commonRender(GuiGraphics guiGraphics) {
		if (MINECRAFT.player == null || MINECRAFT.level == null) {
			return;
		}

		ArrayList<Signal> signals = signalDisplayer.getSignals();
		if (signals == null || signals.isEmpty()) {
			return;
		}

		ResourceKey<Level> playerDimension = MINECRAFT.level.dimension();

		for (Signal signal : signals) {
			// 检测维度是否相同
			if (!signal.signalLevel().equals(playerDimension)) {
				continue;
			}

			// 将世界坐标转换为屏幕坐标
			GUIPosition screenPos = worldToScreen(signal.pos());
			if (screenPos == null) {
				continue;
			}

			// 渲染信号图标
			renderSignalIcon(guiGraphics, signal.pointData(), screenPos);
		}
	}

	/**
	 * 将世界坐标转换为屏幕坐标
	 *
	 * @param worldPos 世界坐标
	 * @return 屏幕坐标（如果在屏幕外或背后则返回 null）
	 */
	private GUIPosition worldToScreen(Vec3 worldPos) {
		if (MINECRAFT.player == null) {
			return null;
		}

		var camera = MINECRAFT.gameRenderer.getMainCamera();

		// 获取相机位置
		Vec3 cameraPos = camera.getPosition();

		// 计算从相机到目标点的向量
		Vec3 targetVec = worldPos.subtract(cameraPos);

		// 如果点在相机背后，不渲染
		// 使用玩家的视角向量来判断前后
		Vec3 viewVector = MINECRAFT.player.getViewVector(1.0f);
		if (targetVec.dot(viewVector) <= 0) {
			return null;
		}

		// 使用游戏渲染器的投影方法
		int screenWidth = MINECRAFT.getWindow().getGuiScaledWidth();
		int screenHeight = MINECRAFT.getWindow().getGuiScaledHeight();

		// 获取目标FOV
		double targetFov = MINECRAFT.options.fov().get() * ActionPanelClient.fov;


		// 计算投影
		float aspectRatio = (float) screenWidth / (float) screenHeight;
		float halfFovRad = (float) Math.toRadians(targetFov / 2.0);
		float tanHalfFov = (float) Math.tan(halfFovRad);

		// 使用相机的四元数旋转来构建正确的坐标系基向量
		// Minecraft 的相机使用 Quaternionf，相机看向 -Z 方向（OpenGL 惯例）
		var rotation = camera.rotation();

		// 通过复制并旋转标准基向量来获取实际的基向量
		// 右向量 (1,0,0) 经过旋转
		org.joml.Vector3f rightVec = new org.joml.Vector3f(1, 0, 0);
		rightVec.rotate(rotation);
		Vec3 right = new Vec3(rightVec.x, rightVec.y, rightVec.z);

		// 上向量 (0,1,0) 经过旋转
		org.joml.Vector3f upVec = new org.joml.Vector3f(0, 1, 0);
		upVec.rotate(rotation);
		Vec3 up = new Vec3(upVec.x, upVec.y, upVec.z);

		// 前向量 - Minecraft 相机看向 -Z 方向，所以使用 (0,0,-1)
		org.joml.Vector3f forwardVec = new org.joml.Vector3f(0, 0, -1);
		forwardVec.rotate(rotation);
		Vec3 forward = new Vec3(forwardVec.x, forwardVec.y, forwardVec.z).normalize();

		// 计算在相机坐标系中的坐标
		double x = targetVec.dot(right);
		double y = targetVec.dot(up);
		double z = targetVec.dot(forward);

		// 计算归一化设备坐标
		float ndcX = (float) (x / (z * aspectRatio * tanHalfFov));
		float ndcY = (float) (y / (z * tanHalfFov));

		// 检查是否在视锥体内
		if (ndcX < -1 || ndcX > 1 || ndcY < -1 || ndcY > 1) {
			return null;
		}

		// 转换到屏幕空间
		int screenX = (int) ((ndcX + 1) * 0.5 * screenWidth);
		int screenY = (int) ((1 - ndcY) * 0.5 * screenHeight); // Y 轴需要翻转

		return new GUIPosition(screenX, screenY);
	}

	/**
	 * 渲染信号图标
	 */
	private void renderSignalIcon(GuiGraphics guiGraphics, PointData pointData, GUIPosition pos) {
		PointClient pointClient = ActionPanelClient.pointDataPointClientHashMap.get(pointData.getResourceLocation());

		ResourceLocation icon = pointClient.getIcon();
		ResourceLocation background = pointClient.getTintedIcon();

		int point_scale = MainConfig.point_scale;

		// 计算绘制区域（以图标为中心）
		int x = pos.x() - point_scale / 2;
		int y = pos.y() - point_scale / 2;

		int centerX = MINECRAFT.getWindow().getWidth() / 2;
		int centerY = MINECRAFT.getWindow().getScreenHeight() / 2;

		// 计算图标到中心点的距离
		int distance = (int) Math.sqrt(Math.pow(pos.x() - centerX, 2) + Math.pow(pos.y() - centerY, 2));
		float alpha;
		if (distance < 40) {
			alpha = (float) (40 - distance) / 40;
		} else {
			alpha = 0.3F;
		}

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);

		// 渲染背景框
		guiGraphics.blit(background, x, y, point_scale, point_scale, 0, 0, 64, 64, 64, 64);

		// 渲染图标
		if (icon != null) {
			guiGraphics.blit(
					icon,
					x, y,
					point_scale, point_scale,
					0, 0,
					64, 64,
					64, 64
			);
		}

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableBlend();
	}
}
