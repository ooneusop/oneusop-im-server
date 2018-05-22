package com.oneusop.im.server;

import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/websocket/{uuid}")
@Component
public class WebSocketServer {

    //当前在线连接数
    private static int onlineCount = 0;
    private static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<>();
    private Session session;
    private String uuid;

    /**
     * 链接建立成功调用的方法
     *
     * @param session 可选的参数，session为与某个客户端的连接回话，需要通过它来给客户端发送数据
     */
    @OnOpen
    public void onOpen(@PathParam("uuid")String uuid, Session session) {
        this.uuid=uuid;
        this.session = session;
        webSocketSet.add(this);
        System.out.println("------------------连接到服务器----------------");
    }

    @OnClose
    public void onClose() {
        webSocketSet.remove(this);
        subOnlineCount();
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("接收到消息后:"+message);
        String uuid = this.uuid;
        webSocketSet.stream().forEach(item -> {
            try {
                item.sendMessage(uuid+":"+message);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("发生未知错误，请适当处理");
            }
        });
    }

    /**
     * 通过给session用户发送消息
     *
     * @param message
     * @throws Exception
     */
    public void sendMessage(String message) throws Exception {
        this.session.getBasicRemote().sendText(message);
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocketServer.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocketServer.onlineCount--;
    }
}
