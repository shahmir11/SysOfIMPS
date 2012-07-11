package com.imps.server.handler;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.imps.server.handler.baseLogic.AddFriendReq;
import com.imps.server.handler.baseLogic.AddFriendRsp;
import com.imps.server.handler.baseLogic.FriendListRequest;
import com.imps.server.handler.baseLogic.HeartBeat;
import com.imps.server.handler.baseLogic.Login;
import com.imps.server.handler.baseLogic.Register;
import com.imps.server.handler.baseLogic.SearchFriendReq;
import com.imps.server.handler.baseLogic.SendAudio;
import com.imps.server.handler.baseLogic.SendImageReq;
import com.imps.server.handler.baseLogic.SendMessage;
import com.imps.server.main.IMPSTcpServer;
import com.imps.server.main.basetype.User;
import com.imps.server.main.basetype.userStatus;
import com.imps.server.manager.UserManager;
import com.imps.server.model.AudioMedia;
import com.imps.server.model.CommandId;
import com.imps.server.model.CommandType;
import com.imps.server.model.IMPSType;
import com.imps.server.model.ImageMedia;
import com.imps.server.model.TextMedia;

public class LogicHandler extends SimpleChannelUpstreamHandler{
    private static final Logger logger = Logger.getLogger(
            LogicHandler.class.getName());
	private int cmdType;
	private static boolean DEBUG = true;
    @Override
    public void messageReceived(
            ChannelHandlerContext ctx, MessageEvent e){
    	ChannelBuffer buffer =(ChannelBuffer)e.getMessage();
    	cmdType = buffer.readInt();
    	if(DEBUG)System.out.println("logichandler:cmdType:"+cmdType);
		IMPSType media = null;
    	switch(cmdType){
    	case com.imps.server.model.MediaType.SMS:
    		media = new TextMedia();
    		break;
    	case com.imps.server.model.MediaType.AUDIO:
    		media = new AudioMedia();
    		break;
    	case com.imps.server.model.MediaType.COMMAND:
    		media = new CommandType();
    		break;
    	case com.imps.server.model.MediaType.IMAGE:
    		media = new ImageMedia();
    		break;
		default:
			System.out.println("server:unhandled msg received:"+cmdType);
			buffer.skipBytes(buffer.readableBytes());
			e.getChannel().close();
			return;
    	}
    	media.MediaParser(buffer.array());
    	buffer.skipBytes(buffer.readableBytes());
    	if(media.getType()==IMPSType.COMMAND){
    		String command = media.getmHeader().get("Command");
    		if(command==null){
    			return;
    		}
    		if(command.equals(CommandId.C_LOGIN_REQ)){
    			if(DEBUG)System.out.println("server:login request received!");
    			new Login(e.getChannel(),media).run();
    		}else if(command.equals(CommandId.C_REGISTER)){
    			if(DEBUG)System.out.println("server:reg request received!");
    			new Register(e.getChannel(),media).run();
    		}else if(command.equals(CommandId.C_ADDFRIEND_REQ)){
    			if(DEBUG)System.out.println("server:add friend request received!");
    			new AddFriendReq(e.getChannel(),media).run();
    		}else if(command.equals(CommandId.C_FRIENDLIST_REFURBISH_REQ)){
    			if(DEBUG)System.out.println("server:get friend list request received!");
    			new FriendListRequest(e.getChannel(),media).run();    			
    		}else if(command.equals(CommandId.C_ADDFRIEND_RSP)){
    			if(DEBUG)System.out.println("server:add friend response received!");
    			new AddFriendRsp(e.getChannel(),media).run();
    		}else if(command.equals(CommandId.C_HEARTBEAT_REQ)){
    			if(DEBUG)System.out.println("server:heart beat received!");
    			new HeartBeat(e.getChannel(),media).run();
    		}else if(command.equals(CommandId.C_OFFLINE_MSG_REQ)){
    			if(DEBUG)System.out.println("server:get offline message received!");
    			new HeartBeat(e.getChannel(),media).run();
    		}else if(command.equals(CommandId.C_SEARCH_FRIEND_REQ)){
    			if(DEBUG)System.out.println("server:search friend request received!");
    			new SearchFriendReq(e.getChannel(),media).run();
    		}
    	}else if(media.getType()==IMPSType.AUDIO||media.getType()==IMPSType.IMAGE||media.getType()==IMPSType.SMS){
    		String command = media.getmHeader().get("Command");
    		if(command==null){
    			return;
    		}
    		if(command.equals(CommandId.C_AUDIO_REQ)){
    			if(DEBUG)System.out.println("server:audio request received!");
    			new SendAudio(e.getChannel(),media).run();
    		}else if(command.equals(CommandId.C_IMAGE_REQ)){
    			if(DEBUG)System.out.println("server:image request received!");
    			new SendImageReq(e.getChannel(),media).run();
    		}else if(command.equals(CommandId.C_SEND_MSG)){
    			if(DEBUG)System.out.println("server:sms request received!");
    			new SendMessage(e.getChannel(),media).run();
    		}
    	}
//    	
//		case CommandId.C_LOGIN_REQ:
//			/**客户端登录请求*/
//			System.out.println("server:login request received!");
//			new Login(e.getChannel(),buffer).run();
//			break;
//		case CommandId.C_HEARTBEAT_REQ:
//			/** 心跳检测客户端请求,同时接收地理位置信息 */
//			System.out.println("server: heartbeat received!");
//			new HeartBeat(e.getChannel(),buffer).run();
//			break;
//		case CommandId.C_FRIENDLIST_REFURBISH_REQ:
//			/**好友列表刷新请求*/
//			System.out.println("server: friend list refresh");
//			new FriendListRequest(e.getChannel(),buffer).run();
//			break;
//		case CommandId.C_ADDFRIEND_REQ:
//		/**客户端添加好友请求*/
//			System.out.println("server:add friend request receive");
//			new AddFriendReq(e.getChannel(),buffer).run();
//			break;
//		case CommandId.C_ADDFRIEND_RSP:
//			/** 客户端添加好友响应 */
//			System.out.println("server:add friend response receive");
//			new AddFriendRsp(e.getChannel(),buffer).run();
//			break;
//		case CommandId.C_SEND_MSG:
//			/**客户端发送消息*/
//			System.out.println("server: message from client to friend received");
//			new SendMessage(e.getChannel(),buffer).run();
//			break;
//		case CommandId.C_REGISTER:
//			/** 客户端用户注册*/
//			System.out.println("server: register msg received");
//			new Register(e.getChannel(),buffer).run();
//			break;
//		case CommandId.C_AUDIO_REQ:
//			/**客户端音频文件请求**/
//			System.out.println("server: audio msg received");
//			new SendAudio(e.getChannel(),buffer).run();
//			break;
//		case CommandId.C_PTP_AUDIO_REQ:
//			System.out.println("server: p2p audio req received");
//			new SendPTPAudioReq(e.getChannel(),buffer,e.getRemoteAddress()).run();
//			break;
//		case CommandId.C_PTP_AUDIO_RSP:
//			System.out.println("server: p2p audio rsp received");
//			new SendPTPAudioRsp(e.getChannel(),buffer,e.getRemoteAddress()).run();
//			break;
//		case CommandId.C_PTP_VIDEO_REQ:
//			System.out.println("server:p2p video req received");
//			new SendPTPVideoReq(e.getChannel(),buffer,e.getRemoteAddress()).run();
//			break;
//		case CommandId.C_PTP_VIDEO_RSP:
//			System.out.println("server:p2p video rsp received");
//			new SendPTPVideoRsp(e.getChannel(),buffer,e.getRemoteAddress()).run();
//			break;
//		case CommandId.C_SEARCH_FRIEND_REQ:
//			new SearchFriendReq(e.getChannel(),buffer).run();
//			break;
//		case CommandId.C_IMAGE_REQ:
//			new SendImageReq(e.getChannel(),buffer).run();
//			break;
//		case CommandId.C_OFFLINE_MSG_REQ:
//			System.out.println("== REQ: GET OFFLINE MSG ==");
//			new OfflineMsg(e.getChannel(), buffer).run();
//			break;
//		case CommandId.C_UPDATE_USER_INFO_REQ:
//			System.out.println("server: update user info request received!");
//			new UpdateUserInfo(e.getChannel(),buffer).run();
//			break;
//		case CommandId.C_UPLOAD_PORTRAIT_REQ:
//			System.out.println("server: upload user portrait!");
//			new UploadPortrait(e.getChannel(),buffer).run();
//			break;
//		case CommandId.C_STATUS_NOTIFY:
//			System.out.println("server: status notify");
//			break;

    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
    	
    	if(e.getCause() instanceof OutOfMemoryError){
    		logger.log(Level.WARNING,"OutOfMemnoryError");
       		try {
				super.exceptionCaught(ctx,e);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    	}
    	Channel session = e.getChannel();
    	if(IMPSTcpServer.getAllGroups().contains(session)){
    		//remove session
    		IMPSTcpServer.getAllGroups().remove(session);
    		try {
    			User user = UserManager.getInstance().getUserBySessionId(e.getChannel().getId());
    	    	if(user!=null){
    	    		user.setSessionId(new Integer(-1));
    	    		user.setStatus(userStatus.OFFLINE);
    	    		UserManager.getInstance().updateUserStatus(user);
    	    	}
    		} catch (SQLException e1) {
    			// TODO Auto-generated catch block
    			e1.printStackTrace();
    		} 
    	}
    	try {
			super.exceptionCaught(ctx, e);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		e.getChannel().close();
    }
    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e){
		try {
			User user = UserManager.getInstance().getUserBySessionId(e.getChannel().getId());
	    	if(user!=null){
	    		user.setSessionId(new Integer(-1));
	    		user.setStatus(userStatus.OFFLINE);
	    		UserManager.getInstance().updateUserStatus(user);
	    	}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		logger.log(Level.INFO,"client disconnected from:"+e.getChannel().getRemoteAddress().toString());
    }
    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
    	logger.log(Level.INFO,"client connected from:"+e.getChannel().getRemoteAddress().toString());
    }
}
