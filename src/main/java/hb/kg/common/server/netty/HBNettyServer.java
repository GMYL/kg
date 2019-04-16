package hb.kg.common.server.netty;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.Getter;

@Getter
@Service
public class HBNettyServer {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Value("${netty.tcp.port}")
	private int tcpPort;
	@Value("${netty.boss.thread.count}")
	private int bossCount;
	@Value("${netty.worker.thread.count}")
	private int workerCount;
	/**
	 * 是否启用心跳保活机制。 在双方TCP套接字建立连接后（即都进入ESTABLISHED状态）
	 * 并且在两个小时左右上层没有任何数据传输的情况下，这套机制才会被激活。 在模块间进行通信时，我们暂时没有长时间保留数据的需求
	 */
	@Value("${netty.so.keepalive:false}")
	private boolean keepAlive;
	/**
	 * BACKLOG用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程全满时 用于临时存放已完成三次握手的请求的队列的最大长度
	 * 系统默认值是50，我们使用一个高值来满足更高的系统可用性
	 */
	@Value("${netty.so.backlog:1024}")
	private int backlog;
	// Netty主服务，只对外开放一个接口，内部进行连接分发
	private ServerBootstrap server;
	private NioEventLoopGroup bossGroup;
	private NioEventLoopGroup workerGroup;
	private Channel serverChannel;
	@Autowired
	private HBNettyChannelHandler hbNettyChannelHandler;
	private ReentrantReadWriteLock nettyLock = new ReentrantReadWriteLock();

	/**
	 * 注意，netty的所有配置也改为异步加载 初始化完毕后，netty服务器会阻塞到这个位置，知道系统结束或netty服务器运行失败
	 */
	@PostConstruct
	public void init() {
		logger.info("netty服务开始初始化");
		new Thread() {
			public void run() {
				if (nettyLock.writeLock().tryLock()) {
					try {
						bossGroup = new NioEventLoopGroup(bossCount);
						workerGroup = new NioEventLoopGroup(workerCount);
						server = new ServerBootstrap();
						server.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
								.handler(new LoggingHandler(LogLevel.DEBUG)) // WARN
																				// 日志输出等级是WARN，上线时可调整
								.childHandler(new ChannelInitializer<SocketChannel>() {
									@Override
									protected void initChannel(SocketChannel ch) throws Exception {
										ChannelPipeline pl = ch.pipeline();
										pl.addLast(new HttpServerCodec());
										pl.addLast(hbNettyChannelHandler);
									}
								});
						server.option(ChannelOption.SO_KEEPALIVE, keepAlive);
						server.option(ChannelOption.SO_BACKLOG, backlog);
						serverChannel = server.bind(tcpPort).sync().channel();
						logger.info("netty服务开始初始化成功，初始化线程阻塞中，直到netty服务被关闭，在此过程中netty初始化锁被长久保持");
						try {
							serverChannel.closeFuture().sync();
						} catch (InterruptedException e) {
							logger.error("netty关闭出错", e);
						} finally {
							bossGroup.shutdownGracefully();
							workerGroup.shutdownGracefully();
						}
					} catch (Exception e) {
						logger.error("netty服务器初始化失败", e);
					} finally {
						logger.info("netty服务器已关闭");
						nettyLock.writeLock().unlock();
					}
				}
			};

			public void destroy() {
				if (nettyLock.isWriteLockedByCurrentThread()) {
					nettyLock.writeLock().unlock();
				}
			};
		}.start();
	}

	@PreDestroy
	public void stop() throws Exception {
		if (serverChannel != null) {
			serverChannel.close();
			serverChannel.parent().close();
		}
	}
}
