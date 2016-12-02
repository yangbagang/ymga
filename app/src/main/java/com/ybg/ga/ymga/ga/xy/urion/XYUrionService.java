/**
 * 
 */
package com.ybg.ga.ymga.ga.xy.urion;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Intent;

import com.ybg.ga.ymga.bt.AbstractBTService;
import com.ybg.ga.ymga.bt.BTAction;
import com.ybg.ga.ymga.bt.BTMessage;
import com.ybg.ga.ymga.bt.BTPrefix;

/**
 * @author 杨拔纲
 * 
 */
public class XYUrionService extends AbstractBTService {

	private ConnectThread connectThread = null;

	@Override
	protected void start() {
		if (socket == null) {
			// 未连接，先连接
			sendMessage(BTMessage.SOCKET_NULL, true);
		} else {
			connectThread = new ConnectThread();
			connectThread.start();
		}
	}

	@Override
	protected void stop() {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				sendMessage(BTMessage.SOCKET_FAIL, true);
			}
		}
		if (connectThread != null) {
			connectThread.interrupt();
		}
	}

	@Override
	protected String getPrefix() {
		return BTPrefix.XY;
	}

	private class ConnectThread extends Thread {

		private InputStream inStream;
		private OutputStream outStream;
		private Intent sendDataIntent = null;

		public ConnectThread() {
			try {
				inStream = socket.getInputStream();
				outStream = socket.getOutputStream();
			} catch (Exception e) {
				sendMessage(BTMessage.OPEN_FAIL, true);
			}
		}

		public void run() {
			// 发送启动指令
			byte[] startCMD = { (-3), (-3), -6, 5, 13, 10 };
			write(outStream, startCMD);
			// 等待返馈
			byte[] buffer = new byte[16];
			while (true) {
				try {
					if (inStream.available() > 0) {// 如果流中有数据就进行解析
						Head head = new Head();
						inStream.read(buffer);
						int[] f = CodeFormat.bytesToHexStringTwo(buffer, 6);
						head.analysis(f);
						if (head.getType() == Head.TYPE_ERROR) {
							// APP接收到血压仪的错误信息
							UrionError error = new UrionError();
							error.analysis(f);
							error.setHead(head);
							// 前台根据错误编码显示相应的提示
							sendMessage(error.getHumanErrorMsg(), true);
							// 中止线程
							break;
						}
						if (head.getType() == Head.TYPE_RESULT) {
							// APP接收到血压仪的测量结果
							Data data = new Data();
							data.analysis(f);
							data.setHead(head);
							// 前台根据测试结果来画线性图
							sendDataIntent = new Intent(
									BTAction.getSendDataAction(getPrefix()));
							sendDataIntent.putExtra(BTAction.DATA,
									data.getStringValue());
							sendBroadcast(sendDataIntent);
							// 线程停止
							break;
						}
						if (head.getType() == Head.TYPE_MESSAGE) {
							// APP接收到血压仪开始测量的通知
							Msg msg = new Msg();
							msg.analysis(f);

							msg.setHead(head);
							// sendMessage(msg.getStringMsg(), false);
						}
						if (head.getType() == Head.TYPE_PRESSURE) {
							// APP接受到血压仪测量的压力数据
							Pressure pressure = new Pressure();
							pressure.analysis(f);
							pressure.setHead(head);
							// 每接收到一条数据就发送到前台，以改变进度条的显示
							Intent sendProgressIntent = new Intent(
									BTAction.sendProgressAction(getPrefix()));
							sendProgressIntent.putExtra(BTAction.PROGRESS,
									pressure.getPressure());
							sendBroadcast(sendProgressIntent);
						}
					}
				} catch (IOException e) {
					sendMessage(BTMessage.READ_DATA_FAIL, true);
					interrupt();
					break;
				}
			}
			// 操作完成，关闭连接。
			try {
				socket.close();
			} catch (IOException e) {
				// 尝试关闭连接时出现了，算了，让它去吧。不理了。
			}
		}
	}
}
