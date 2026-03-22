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

package top.xiaosuoaa.actionpanel.point;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.LevelAccessor;
import top.xiaosuoaa.actionpanel.controller.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PointRunnerUtil {
	private final PointRunnerImpl pointRunnerPreImpl;

	private PointRunnerUtil(PointRunnerImpl pointRunnerPreImpl) {
		this.pointRunnerPreImpl = pointRunnerPreImpl;
	}

	/**
	 * 客户端预运行任务：显示一则消息
	 *
	 * @param color      消息颜色
	 * @param messageKey 消息
	 * @return {@link PointRunnerUtil}
	 */
	public static PointRunnerUtil displayFirstMessage(int color, MutableComponent messageKey) {
		PointRunnerImpl runnerPreImpl = new PointRunnerImpl();
		runnerPreImpl.addClientPreRunnable(
				() -> {
					LocalPlayer sourcePlayer = Minecraft.getInstance().player;
					if (sourcePlayer != null) {
						Component displayName = sourcePlayer.getDisplayName();
						MutableComponent prefix = Component.literal("<" + displayName.getString() + "> ").withStyle(style -> style.withBold(true));
						MutableComponent message = messageKey.withStyle(style -> style.withColor(color).withBold(true));
						MutableComponent translatable = prefix.append(message);
						sourcePlayer.displayClientMessage(translatable, false);
					}
				}
		);
		return new PointRunnerUtil(runnerPreImpl);
	}

	public PointRunnerImpl build() {
		return pointRunnerPreImpl;
	}


	public static class PointRunnerImpl implements PointRunner {
		private final ArrayList<Runnable> clientPreRunnable;
		private final ArrayList<Runnable> clientTickRunnable;
		private final ArrayList<Runnable> clientPostRunnable;
		private final ArrayList<Supplier<Integer>> serverPreSuppliers;
		private final ArrayList<Runnable> serverTickRunnable;
		private final ArrayList<Runnable> serverPostRunnable;

		// 构造空列表
		public PointRunnerImpl() {
			clientPreRunnable = new ArrayList<>();
			clientTickRunnable = new ArrayList<>();
			clientPostRunnable = new ArrayList<>();
			serverPreSuppliers = new ArrayList<>();
			serverTickRunnable = new ArrayList<>();
			serverPostRunnable = new ArrayList<>();
		}

		public PointRunnerImpl(ArrayList<Runnable> clientPreRunnable, ArrayList<Runnable> clientTickRunnable, ArrayList<Runnable> clientPostRunnable, ArrayList<Supplier<Integer>> serverPreSuppliers, ArrayList<Runnable> serverTickRunnable, ArrayList<Runnable> serverPostRunnable) {
			this.clientPreRunnable = clientPreRunnable;
			this.clientTickRunnable = clientTickRunnable;
			this.clientPostRunnable = clientPostRunnable;
			this.serverPreSuppliers = serverPreSuppliers;
			this.serverTickRunnable = serverTickRunnable;
			this.serverPostRunnable = serverPostRunnable;
		}

		public void addClientPreRunnable(Runnable... runnable) {
			clientPreRunnable.addAll(List.of(runnable));
		}

		public void addClientTickRunnable(Runnable... runnable) {
			clientTickRunnable.addAll(List.of(runnable));
		}

		public void addClientPostRunnable(Runnable... runnable) {
			clientPostRunnable.addAll(List.of(runnable));
		}

		@SafeVarargs
		public final void addServerPreSupplier(Supplier<Integer>... supplier) {
			serverPreSuppliers.addAll(List.of(supplier));
		}

		public void addServerTickRunnable(Runnable... runnable) {
			serverTickRunnable.addAll(List.of(runnable));
		}

		public void addServerPostRunnable(Runnable... runnable) {
			serverPostRunnable.addAll(List.of(runnable));
		}

		@Override
		public void clientPre(LevelAccessor levelAccessor, Signal signal, LocalPlayer sourcePlayer) {
			for (Runnable runnable : clientPreRunnable) {
				runnable.run();
			}
		}

		@Override
		public void clientTick(LevelAccessor levelAccessor, Signal signal, LocalPlayer sourcePlayer) {
			for (Runnable runnable : clientTickRunnable) {
				runnable.run();
			}
		}

		@Override
		public void clientPost(LevelAccessor levelAccessor, Signal signal, LocalPlayer sourcePlayer) {
			for (Runnable runnable : clientPostRunnable) {
				runnable.run();
			}
		}

		@Override
		public int serverPre(LevelAccessor levelAccessor, Signal signal, ServerPlayer sourcePlayer, Team belongTeam) {
			for (Supplier<Integer> supplier : serverPreSuppliers) {
				Integer i = supplier.get();
				// TIPS: 如果 supplier 返回非 0 不会继续执行下一个 supplier
				if (i != 0) {
					return i;
				}
			}
			return 0;
		}

		@Override
		public void serverTick(LevelAccessor levelAccessor, Signal signal, ServerPlayer sourcePlayer, Team belongTeam) {
			for (Runnable runnable : serverTickRunnable) {
				runnable.run();
			}
		}

		@Override
		public void serverPost(LevelAccessor levelAccessor, Signal signal, ServerPlayer sourcePlayer, Team belongTeam) {
			for (Runnable runnable : serverPostRunnable) {
				runnable.run();
			}
		}
	}
}
