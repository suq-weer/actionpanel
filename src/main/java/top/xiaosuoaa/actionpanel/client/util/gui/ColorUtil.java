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

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import top.xiaosuoaa.actionpanel.util.ResUtil;

import java.io.IOException;
import java.io.InputStream;

public class ColorUtil {
	public static final Logger LOGGER = LogUtils.getLogger();

	/**
	 * 将灰度 mask 染色并返回 NativeImage（使用图片原尺寸）
	 *
	 * @param maskLoc 原灰度贴图路径，例如 "yourmod:textures/gui/icon_mask.png"
	 * @param rgb     24-bit RGB，0xRRGGBB
	 */
	public static NativeImage tintMask(ResourceLocation maskLoc, int rgb) {
		// 确保只在客户端环境中运行
		Minecraft.getInstance().getResourceManager();

		// 获取输入流以读取图片数据
		try (InputStream inputStream = Minecraft.getInstance().getResourceManager()
				.getResourceOrThrow(ResUtil.getRes(maskLoc.getPath())).open()) {
			NativeImage src = NativeImage.read(inputStream);

			int w = src.getWidth();
			int h = src.getHeight();
			NativeImage out = new NativeImage(w, h, false);   // false=不压缩

			float r = ((rgb >> 16) & 0xFF) / 255f;
			float g = ((rgb >> 8) & 0xFF) / 255f;
			float b = (rgb & 0xFF) / 255f;

			// 逐像素染色处理
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					int argb = src.getPixelRGBA(x, y);
					int a = (argb >> 24) & 0xFF;
					int srcR = (argb) & 0xFF;
					int srcG = (argb >> 8) & 0xFF;
					int srcB = (argb >> 16) & 0xFF;

					int dstR = (int) (srcR * r);
					int dstG = (int) (srcG * g);
					int dstB = (int) (srcB * b);

					out.setPixelRGBA(x, y, (a << 24) | (dstB << 16) | (dstG << 8) | dstR);
				}
			}

			src.close();   // 手动释放源图内存
			return out;    // 返回结果，由调用者负责关闭
		} catch (IOException e) {
			throw new RuntimeException("读取 mask 失败: " + maskLoc, e);
		} catch (Exception e) {
			throw new RuntimeException("处理 mask 图像时发生未知错误: " + maskLoc, e);
		}
	}

	public static ResourceLocation makeTintedRL(ResourceLocation maskPath, int rgb) {
		try {
			NativeImage colored = tintMask(maskPath, rgb);
			DynamicTexture dyn = new DynamicTexture(colored);

			String name = "tinted_" + maskPath.getPath().replace(':', '_');

			// 让 TextureManager 和 DynamicTexture 管理 NativeImage 的生命周期
			return Minecraft.getInstance().getTextureManager()
					.register(name, dyn);
		} catch (Exception e) {
			throw new RuntimeException("创建彩色纹理失败: " + maskPath, e);
		}
	}

}