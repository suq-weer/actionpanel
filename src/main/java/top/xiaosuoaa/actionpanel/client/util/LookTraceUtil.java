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

package top.xiaosuoaa.actionpanel.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 视线追踪工具类
 * 用于获取玩家视角看向的方块表面精确坐标
 */
public class LookTraceUtil {
	private static final Minecraft MINECRAFT = Minecraft.getInstance();

	/**
	 * 从摄像机位置发射射线，获取与方块表面的交点坐标
	 *
	 * @param levelAccessor 等级访问器
	 * @return 视线与方块表面的交点坐标，如果未击中则返回 null
	 */
	public static Vec3 getLookAtBlockSurface(LevelAccessor levelAccessor) {
		Entity cameraEntity = MINECRAFT.getCameraEntity();
		if (cameraEntity == null) {
			return null;
		}

		// 获取摄像机位置和视角方向
		Vec3 startVec = cameraEntity.getEyePosition();
		Vec3 lookVector = cameraEntity.getViewVector(1.0f);

		// 设置一个很大的距离作为"无限远"（足够覆盖玩家的可视距离）
		double traceDistance = 256.0; // Minecraft 的最大视距

		// 计算终点位置
		Vec3 endVec = startVec.add(
				lookVector.x * traceDistance,
				lookVector.y * traceDistance,
				lookVector.z * traceDistance
		);

		// 创建射线追踪上下文
		ClipContext clipContext = new ClipContext(
				startVec,
				endVec,
				ClipContext.Block.COLLIDER,      // 碰撞箱模式
				ClipContext.Fluid.NONE,          // 不检测流体
				cameraEntity                     // 实体引用
		);

		// 执行射线追踪
		BlockHitResult hitResult = levelAccessor.clip(clipContext);

		// 检查是否击中方块
		if (hitResult.getType() == HitResult.Type.BLOCK) {
			// 返回击中点的精确坐标（在方块表面上）
			return hitResult.getLocation();
		}

		// 未击中方块
		return null;
	}

	/**
	 * 获取或计算视线位置，如果无法获取则返回玩家眼睛位置
	 *
	 * @param levelAccessor 等级访问器
	 * @return 视线位置向量
	 */
	public static Vec3 getLookLocation(LevelAccessor levelAccessor) {
		Vec3 hitLocation = getLookAtBlockSurface(levelAccessor);
		if (hitLocation != null) {
			return hitLocation;
		}

		// 如果没有击中任何方块，返回渲染边界上的点
		Entity cameraEntity = MINECRAFT.getCameraEntity();
		if (cameraEntity != null) {
			Vec3 eyePos = cameraEntity.getEyePosition();
			Vec3 lookVector = cameraEntity.getViewVector(1.0f).normalize();

			// 获取客户端渲染距离（以区块为单位）
			int renderDistanceChunks = MINECRAFT.options.renderDistance().get();
			// 转换为方块距离（每个区块 16 方块）
			double renderDistanceBlocks = renderDistanceChunks * 16.0;

			// 计算玩家所在的区块坐标
			int playerChunkX = (int) Math.floor(eyePos.x / 16.0);
			int playerChunkZ = (int) Math.floor(eyePos.z / 16.0);

			// 计算渲染边界的绝对坐标（以玩家为中心的正方形区域）
			double minX = (playerChunkX - renderDistanceChunks) * 16.0;
			double maxX = (playerChunkX + renderDistanceChunks + 1) * 16.0;
			double minZ = (playerChunkZ - renderDistanceChunks) * 16.0;
			double maxZ = (playerChunkZ + renderDistanceChunks + 1) * 16.0;

			// 计算视线与渲染边界的交点
			// 使用射线与 AABB（轴对齐包围盒）的相交测试
			Double tMin = null;

			// 检查与每个边界的交点
			// X = minX 平面
			if (lookVector.x != 0) {
				double t = (minX - eyePos.x) / lookVector.x;
				if (t > 0) {
					double z = eyePos.z + lookVector.z * t;
					if (z >= minZ && z <= maxZ) {
						tMin = t;
					}
				}
			}

			// X = maxX 平面
			if (lookVector.x != 0) {
				double t = (maxX - eyePos.x) / lookVector.x;
				if (t > 0) {
					double z = eyePos.z + lookVector.z * t;
					if (z >= minZ && z <= maxZ) {
						if (tMin == null || t < tMin) tMin = t;
					}
				}
			}

			// Z = minZ 平面
			if (lookVector.z != 0) {
				double t = (minZ - eyePos.z) / lookVector.z;
				if (t > 0) {
					double x = eyePos.x + lookVector.x * t;
					if (x >= minX && x <= maxX) {
						if (tMin == null || t < tMin) tMin = t;
					}
				}
			}

			// Z = maxZ 平面
			if (lookVector.z != 0) {
				double t = (maxZ - eyePos.z) / lookVector.z;
				if (t > 0) {
					double x = eyePos.x + lookVector.x * t;
					if (x >= minX && x <= maxX) {
						if (tMin == null || t < tMin) tMin = t;
					}
				}
			}

			// 如果找到交点，返回边界上的位置；否则返回一个默认距离的点
			if (tMin != null) {
				return eyePos.add(
						lookVector.x * tMin,
						lookVector.y * tMin,
						lookVector.z * tMin
				);
			} else {
				// 理论上不应该到这里，除非视角完全垂直
				return eyePos.add(
						lookVector.x * renderDistanceBlocks,
						lookVector.y * renderDistanceBlocks,
						lookVector.z * renderDistanceBlocks
				);
			}
		}

		return Vec3.ZERO;
	}
}
