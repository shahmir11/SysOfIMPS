package com.imps.server.handler;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.imps.server.handler.doodleLogic.DoodleData;
import com.imps.server.handler.doodleLogic.DoodleLogin;
import com.imps.server.handler.doodleLogic.DoodleReq;
import com.imps.server.handler.doodleLogic.DoodleRsp;
import com.imps.server.main.DoodleTcpServer;
import com.imps.server.main.basetype.CommandId;
import com.imps.server.manager.DoodleManager;

public class DoodleLogicHandler extends SimpleChannelUpstreamHandler{

	/**
	 * DD+length+type+userName+data
	 */
	private byte cmdType;
	@Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e){
		ChannelBuffer buffer =(ChannelBuffer)e.getMessage();
    	cmdType = buffer.readByte();
    	switch(cmdType){
    	case CommandId.C_DOODLE_REQ:
    		System.out.println("Doodle Req...");
    		//new DoodleReq(e.getChannel(),buffer).run();
    		break;
    	case CommandId.C_DOODLE_RSP:
    		System.out.println("Doodle Rsp...");
    		//new DoodleRsp(e.getChannel(),buffer).run();
    		break;
    	case CommandId.DOODLE_DATA:
    		System.out.println("Doodle Data...");
    		//new DoodleData(e.getChannel(),buffer).run();
    		break;
    	case CommandId.DOODLE_LOGIN:
    		System.out.println("Doodle Login...");
    		//new DoodleLogin(e.getChannel(),buffer).run();
    		break;
    	default:
    		System.out.println("Unknown msg type of doodle:"+cmdType);
    		break;
    	}
	}
    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
    	System.out.println("client connected from:"+e.getChannel().getRemoteAddress().toString());
    }
    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e){
    	System.out.println("client disconnected from:"+e.getChannel().getRemoteAddress().toString());
    	Integer id = e.getChannel().getId();
    	Channel channel = DoodleTcpServer.allGroups.find(id);
    	if(channel!=null){
    		DoodleTcpServer.allGroups.remove(channel);
    		for(int i=0;i<DoodleTcpServer.doodleUsers.size();i++){
    			if(DoodleTcpServer.doodleUsers.get(i).getSessionId().intValue()==channel.getId().intValue()){
    				DoodleManager.getInstance().notifyStatus(DoodleTcpServer.doodleUsers.get(i).getUsername(),false);
    				DoodleTcpServer.doodleUsers.remove(i);
    				break;
    			}
    		}
    		System.out.println("Remove session from doodle group");
    	}
    }
    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e){
    	System.out.println("tcp closed to:"+e.getChannel().getRemoteAddress().toString());
    }
}
