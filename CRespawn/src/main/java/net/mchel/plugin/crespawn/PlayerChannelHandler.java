package net.mchel.plugin.crespawn;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * @author chelcy
 */
public class PlayerChannelHandler extends ChannelDuplexHandler{

	private CRespawn fixp;
	private NettyInjector inj;
	public PlayerChannelHandler() {
		fixp = CRespawn.getInstance();
		inj = NettyInjector.getInstance();
	}

	/**
	 * クライアントからパケット受信した時
	 * @param chc
	 * @param ob
	 * @throws Exception
	 */
	@Override
	public void channelRead(ChannelHandlerContext chc, Object ob) throws Exception {
		if (ob.getClass().getSimpleName().equalsIgnoreCase("PacketPlayInSteerVehicle")) {
			if (!fixp.isFixed(inj.getPlayer(this))) {
				super.channelRead(chc, ob);
			}
		} else {
			super.channelRead(chc, ob);
		}
	}

	/**
	 * クライアントへパケット送信するとき
	 * @param chc
     * @param ob
     * @param cp
     * @throws Exception
	 */
	@Override
	public void write(ChannelHandlerContext chc, Object ob, ChannelPromise cp) throws Exception {
		super.write(chc, ob, cp);
	}

}
